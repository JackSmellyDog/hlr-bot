package me.shaposhnik.hlrbot.integration.bsg;

import static me.shaposhnik.hlrbot.integration.bsg.BsgApiErrorCode.NO_ERRORS;
import static me.shaposhnik.hlrbot.integration.bsg.BsgApiErrorCode.fromErrorCode;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.shaposhnik.hlrbot.integration.bsg.converter.HlrInfoToHlrConverter;
import me.shaposhnik.hlrbot.integration.bsg.dto.ApiKey;
import me.shaposhnik.hlrbot.integration.bsg.dto.HlrResponse;
import me.shaposhnik.hlrbot.integration.bsg.dto.HrlRequest;
import me.shaposhnik.hlrbot.integration.bsg.properties.HlrInfoSettings;
import me.shaposhnik.hlrbot.integration.bsg.properties.HlrStatuses;
import me.shaposhnik.hlrbot.model.Hlr;
import me.shaposhnik.hlrbot.model.Phone;
import me.shaposhnik.hlrbot.model.SentHlr;
import me.shaposhnik.hlrbot.service.HlrAsyncService;
import org.apache.commons.collections4.ListUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class BsgHlrService implements HlrAsyncService {

  private final BsgApiClient api;
  private final ReferenceGenerator referenceGenerator;
  private final HlrInfoToHlrConverter hlrInfoToHlrConverter;
  private final HlrStatuses hlrStatuses;
  private final HlrInfoSettings hlrInfoSettings;

  @Value("${bsg.max-hlrs-per-request:1000}")
  private int maxNumberOfHlrsPerRequest;

  @Override
  public SentHlr sendHlr(Phone phone, String token) {
    final String reference = referenceGenerator.generateReference();
    final HrlRequest request = createHrlRequest(reference, phone);

    final var hlrResponse = api.sendHlr(request, ApiKey.of(token));

    final var apiErrorCode = fromErrorCode(hlrResponse.getError());

    return apiErrorCode == NO_ERRORS
        ? SentHlr.of(hlrResponse.getId(), phone, reference)
        : SentHlr.asError(apiErrorCode.getDescription(), phone, reference);
  }

  @Override
  public List<SentHlr> sendHlrs(List<Phone> phones, String token) {
    return ListUtils.partition(phones, maxNumberOfHlrsPerRequest).stream()
        .map(pack -> sendOnePackOfHlrs(pack, token))
        .flatMap(Collection::stream)
        .collect(Collectors.toList());
  }

  private List<SentHlr> sendOnePackOfHlrs(List<Phone> phones, String token) {
    final Map<String, Phone> referenceToPhoneMap = phones.stream()
        .collect(
            Collectors.toMap(phone -> referenceGenerator.generateReference(), Function.identity()));

    final List<HrlRequest> hrlRequests = referenceToPhoneMap.entrySet().stream()
        .map(entry -> createHrlRequest(entry.getKey(), entry.getValue()))
        .collect(Collectors.toList());

    final var multipleHlrResponse = api.sendHlrs(hrlRequests, ApiKey.of(token));

    final var apiErrorCode = fromErrorCode(multipleHlrResponse.getError());
    if (apiErrorCode != NO_ERRORS) {
      return phones.stream()
          .map(phone -> SentHlr.asError(apiErrorCode.getDescription(), phone))
          .collect(Collectors.toList());
    }

    return multipleHlrResponse.getResult().stream()
        .map(hlrResponse -> mapHlrResponseToSentHlr(hlrResponse, referenceToPhoneMap))
        .collect(Collectors.toList());
  }

  private SentHlr mapHlrResponseToSentHlr(HlrResponse hlrResponse,
                                          Map<String, Phone> referenceToPhoneMap) {
    final String reference = hlrResponse.getReference();
    final var apiErrorCode = fromErrorCode(hlrResponse.getError());
    final var phone = referenceToPhoneMap.get(reference);

    return apiErrorCode == NO_ERRORS
        ? SentHlr.of(hlrResponse.getId(), phone, reference)
        : SentHlr.asError(apiErrorCode.getDescription(), phone, reference);
  }

  @Override
  public Hlr getHlrInfo(SentHlr sentHlr, String token) {
    if (!sentHlr.isSuccessful()) {
      return buildFailedHlr(sentHlr.getErrorDescription(), sentHlr.getPhone(),
          sentHlr.getReference());
    }

    final var hlrInfo = api.getHlrInfo(sentHlr.getId(), ApiKey.of(token));

    final var bsgApiErrorCode = fromErrorCode(hlrInfo.getError());
    if (bsgApiErrorCode != NO_ERRORS) {
      final String description = bsgApiErrorCode.getDescription();
      return buildFailedHlrWithProviderId(description, sentHlr.getPhone(), sentHlr.getId(),
          sentHlr.getReference());
    }

    final Hlr hlr = hlrInfoToHlrConverter.convert(hlrInfo);
    hlr.setPhone(sentHlr.getPhone());
    hlr.setReference(sentHlr.getReference());

    return hlr;
  }

  @Override
  public Hlr getHlrInfoByProviderId(String providerId, String token) {
    final var hlrInfo = api.getHlrInfo(providerId, ApiKey.of(token));

    final var bsgApiErrorCode = fromErrorCode(hlrInfo.getError());
    if (bsgApiErrorCode != NO_ERRORS) {
      return buildFailedHlrWithProviderId(providerId, bsgApiErrorCode.getDescription());
    }

    return hlrInfoToHlrConverter.convert(hlrInfo);
  }

  @Override
  public CompletableFuture<Hlr> getHlrInfoAsync(SentHlr sentHlr, String token) {
    if (!sentHlr.isSuccessful()) {
      final Hlr hlr = buildFailedHlr(sentHlr.getErrorDescription(), sentHlr.getPhone(),
          sentHlr.getReference());
      return CompletableFuture.completedFuture(hlr);
    }

    final var apiKey = ApiKey.of(token);
    var hlrInfo = api.getHlrInfo(sentHlr.getId(), apiKey);

    int triesCounter = 0;
    while (triesCounter < hlrInfoSettings.getLimit() && !isFinalizedStatus(hlrInfo.getStatus())) {
      sleep(hlrInfoSettings.getPause());
      hlrInfo = api.getHlrInfo(sentHlr.getId(), apiKey);

      triesCounter++;
    }

    final Hlr hlr = hlrInfoToHlrConverter.convert(hlrInfo);
    return CompletableFuture.completedFuture(hlr);
  }

  @Override
  public CompletableFuture<List<Hlr>> getHlrInfoListAsync(List<SentHlr> sentHlrList, String token) {
    List<Hlr> resultList = new ArrayList<>();

    Map<Boolean, List<SentHlr>> groupedBySuccess = sentHlrList.stream()
        .collect(Collectors.groupingBy(SentHlr::isSuccessful));

    Optional.ofNullable(groupedBySuccess.get(false))
        .ifPresent(failedSentHlrList -> failedSentHlrList.stream()
            .map(sentHlr -> buildFailedHlr(sentHlr.getErrorDescription(), sentHlr.getPhone(),
                sentHlr.getReference()))
            .forEach(resultList::add)
        );

    if (groupedBySuccess.get(true) == null) {
      return CompletableFuture.completedFuture(resultList);
    }

    List<SentHlr> uncheckedSentHlrList = new ArrayList<>(groupedBySuccess.get(true));

    int triesCounter = 0;
    do {
      sleep(hlrInfoSettings.getPause());
      Map<String, String> referenceToProviderIdMap = uncheckedSentHlrList.stream()
          .collect(Collectors.toMap(SentHlr::getReference, SentHlr::getId));

      List<Hlr> hlrInfoList = getHlrInfoList(uncheckedSentHlrList, token);

      Map<Boolean, List<Hlr>> groupedByFinalStatus = hlrInfoList.stream()
          .collect(Collectors.groupingBy(hlr -> isFinalizedStatus(hlr.getStatus())));

      Optional.ofNullable(groupedByFinalStatus.get(true)).ifPresent(resultList::addAll);

      uncheckedSentHlrList = Optional.ofNullable(groupedByFinalStatus.get(false))
          .orElseGet(Collections::emptyList)
          .stream()
          .map(hlr -> SentHlr.of(referenceToProviderIdMap.get(hlr.getReference()), hlr.getPhone(),
              hlr.getReference()))
          .collect(Collectors.toList());

      triesCounter++;

    } while (!uncheckedSentHlrList.isEmpty() && triesCounter < hlrInfoSettings.getLimit());

    if (!uncheckedSentHlrList.isEmpty()) {
      log.info("There are still some sent HLRs left");
      List<Hlr> notFinalizedHlrList = getHlrInfoList(uncheckedSentHlrList, token);
      resultList.addAll(notFinalizedHlrList);
    }

    return CompletableFuture.completedFuture(resultList);
  }

  private List<Hlr> getHlrInfoList(List<SentHlr> sentHlrList, String token) {
    return sentHlrList.stream()
        .map(sentHlr -> getHlrInfo(sentHlr, token))
        .collect(Collectors.toList());
  }

  private boolean isFinalizedStatus(String responseStatus) {
    if (responseStatus == null) {
      return false;
    }
    return hlrStatuses.getFinalized().stream()
        .anyMatch(status -> status.equalsIgnoreCase(responseStatus));
  }

  private Hlr buildFailedHlr(String errorDescription, Phone phone, String reference) {
    return Hlr.builder()
        .phone(phone)
        .reference(reference)
        .errorDescription(errorDescription)
        .build();
  }

  private Hlr buildFailedHlrWithProviderId(String errorDescription, Phone phone, String reference,
                                           String providerId) {
    return Hlr.builder()
        .providerId(providerId)
        .phone(phone)
        .reference(reference)
        .errorDescription(errorDescription)
        .build();
  }

  private Hlr buildFailedHlrWithProviderId(String providerId, String errorDescription) {
    return Hlr.builder()
        .providerId(providerId)
        .errorDescription(errorDescription)
        .build();
  }

  private HrlRequest createHrlRequest(String reference, Phone phone) {
    return HrlRequest.builder()
        .reference(reference)
        .msisdn(phone.getFilteredNumber())
        .build();
  }

  private void sleep(long millis) {
    try {
      Thread.sleep(millis);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
  }
}
