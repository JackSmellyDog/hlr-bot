package me.shaposhnik.hlrbot.integration.bsg;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.shaposhnik.hlrbot.integration.bsg.converter.HlrInfoToHlrConverter;
import me.shaposhnik.hlrbot.integration.bsg.dto.*;
import me.shaposhnik.hlrbot.integration.bsg.properties.HlrInfoSettings;
import me.shaposhnik.hlrbot.integration.bsg.properties.HlrStatuses;
import me.shaposhnik.hlrbot.model.Hlr;
import me.shaposhnik.hlrbot.model.Phone;
import me.shaposhnik.hlrbot.model.SentHlr;
import me.shaposhnik.hlrbot.service.HlrAsyncService;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;

import static me.shaposhnik.hlrbot.integration.bsg.BsgApiErrorCode.NO_ERRORS;
import static me.shaposhnik.hlrbot.integration.bsg.BsgApiErrorCode.fromErrorCode;

@Slf4j
@Service
@RequiredArgsConstructor
public class BsgHlrService implements HlrAsyncService {

    private final BsgApiClient api;
    private final ReferenceGenerator referenceGenerator;
    private final HlrInfoToHlrConverter hlrInfoToHlrConverter;
    private final HlrStatuses hlrStatuses;
    private final HlrInfoSettings hlrInfoSettings;

    @Override
    public SentHlr sendHlr(Phone phone, String token) {
        final String reference = referenceGenerator.generateReference();
        final HrlRequest request = createHrlRequest(reference, phone);

        final var hlrResponse = api.sendHlr(request, ApiKey.of(token));

        final var apiErrorCode = fromErrorCode(hlrResponse.getError());

        return apiErrorCode == NO_ERRORS
            ? SentHlr.of(hlrResponse.getId(), phone)
            : SentHlr.asError(apiErrorCode.getDescription(), phone);
    }

    @Override
    public <T extends Collection<Phone>> List<SentHlr> sendHlrs(T phones, String token) {
        final Map<String, Phone> referenceToPhoneMap = phones.stream()
            .collect(Collectors.toMap(phone -> referenceGenerator.generateReference(), Function.identity()));

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

    private SentHlr mapHlrResponseToSentHlr(HlrResponse hlrResponse, Map<String, Phone> referenceToPhoneMap) {
        // TODO: 9/16/21 add reference to SentHlr
        final var apiErrorCode = fromErrorCode(hlrResponse.getError());
        final var phone = referenceToPhoneMap.get(hlrResponse.getReference());

        return apiErrorCode == NO_ERRORS
            ? SentHlr.of(hlrResponse.getId(), phone)
            : SentHlr.asError(apiErrorCode.getDescription(), phone);
    }

    @Override
    public Hlr getHlrInfo(SentHlr sentHlr, String token) {
        if (!sentHlr.isSuccessful()) {
            return buildFailedHlr(sentHlr.getPhone(), sentHlr.getErrorDescription());
        }

        final var hlrInfo = api.getHlrInfo(sentHlr.getId(), ApiKey.of(token));

        final var bsgApiErrorCode = fromErrorCode(hlrInfo.getError());
        if (bsgApiErrorCode != NO_ERRORS) {
            return buildFailedHlrWithProviderId(sentHlr.getId(), sentHlr.getPhone(), bsgApiErrorCode.getDescription());
        }

        final Hlr hlr = hlrInfoToHlrConverter.convert(hlrInfo);
        hlr.setPhone(sentHlr.getPhone());

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
            final Hlr hlr = buildFailedHlr(sentHlr.getPhone(), sentHlr.getErrorDescription());
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

        Optional.ofNullable(groupedBySuccess.get(false)).ifPresent(failedSentHlrList -> failedSentHlrList.stream()
            .map(sentHlr -> buildFailedHlr(sentHlr.getPhone(), sentHlr.getErrorDescription()))
            .forEach(resultList::add)
        );

        if (groupedBySuccess.get(true) == null) {
            return CompletableFuture.completedFuture(resultList);
        }

        List<SentHlr> uncheckedSentHlrList = new ArrayList<>(groupedBySuccess.get(true));

        int triesCounter = 0;
        do {
            sleep(hlrInfoSettings.getPause());
            List<Hlr> hlrInfoList = getHlrInfoList(uncheckedSentHlrList, token);

            Map<Boolean, List<Hlr>> groupedByFinalStatus = hlrInfoList.stream()
                .collect(Collectors.groupingBy(hlr -> isFinalizedStatus(hlr.getStatus())));

            Optional.ofNullable(groupedByFinalStatus.get(true)).ifPresent(resultList::addAll);

            uncheckedSentHlrList = Optional.ofNullable(groupedByFinalStatus.get(false))
                .orElseGet(Collections::emptyList)
                .stream()
                .map(hlr -> SentHlr.of(hlr.getProviderId(), hlr.getPhone()))
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

    private Hlr buildFailedHlr(Phone phone, String errorDescription) {
        return Hlr.builder()
            .phone(phone)
            .errorDescription(errorDescription)
            .build();
    }

    private Hlr buildFailedHlrWithProviderId(String providerId, Phone phone, String errorDescription) {
        return Hlr.builder()
            .providerId(providerId)
            .phone(phone)
            .errorDescription(errorDescription)
            .build();
    }

    private Hlr buildFailedHlrWithProviderId(String providerId, String errorDescription) {
        return buildFailedHlrWithProviderId(providerId, null, errorDescription);
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
            log.error("I can't sleep!!!", e);
            Thread.currentThread().interrupt();
        }
    }
}
