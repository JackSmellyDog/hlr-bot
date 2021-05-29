package me.shaposhnik.hlrbot.integration.bsg;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.shaposhnik.hlrbot.integration.bsg.converter.HlrInfoToHlrConverter;
import me.shaposhnik.hlrbot.integration.bsg.dto.*;
import me.shaposhnik.hlrbot.integration.bsg.properties.HlrInfoSettings;
import me.shaposhnik.hlrbot.integration.bsg.properties.HlrStatuses;
import me.shaposhnik.hlrbot.model.Hlr;
import me.shaposhnik.hlrbot.model.HlrId;
import me.shaposhnik.hlrbot.model.Phone;
import me.shaposhnik.hlrbot.persistence.entity.HlrEntity;
import me.shaposhnik.hlrbot.persistence.repository.HlrEntityRepository;
import me.shaposhnik.hlrbot.service.HlrAsyncService;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static me.shaposhnik.hlrbot.integration.bsg.BsgApiErrorCode.NO_ERRORS;
import static me.shaposhnik.hlrbot.integration.bsg.BsgApiErrorCode.fromErrorCode;

@Slf4j
@Service
@RequiredArgsConstructor
public class BsgHlrService implements HlrAsyncService {

    private final BsgApiClient api;
    private final HlrEntityRepository repository;
    private final BsgApiErrorHandler bsgApiErrorHandler;
    private final HlrInfoToHlrConverter hlrInfoToHlrConverter;
    private final HlrStatuses hlrStatuses;
    private final HlrInfoSettings hlrInfoSettings;

    @Override
    public HlrId sendHlr(Phone phone, String token) {
        final HrlRequest request = mapPhoneToHrlRequest(phone);
        final HlrResponse hlrResponse = api.sendHlr(request, ApiKey.of(token));
        bsgApiErrorHandler.handle(fromErrorCode(hlrResponse.getError()));

        return HlrId.of(hlrResponse.getId());
    }

    @Override
    public <T extends Collection<Phone>> List<HlrId> sendHlrs(T phones, String token) {
        final List<HrlRequest> hrlRequests = phones.stream()
            .map(this::mapPhoneToHrlRequest)
            .collect(Collectors.toList());

        final MultipleHlrResponse multipleHlrResponse = api.sendHlrs(hrlRequests, ApiKey.of(token));
        bsgApiErrorHandler.handle(fromErrorCode(multipleHlrResponse.getError()));

        multipleHlrResponse.getResult().stream()
            .filter(hlrResponse -> fromErrorCode(hlrResponse.getError()) != NO_ERRORS)
            .findAny()
            .ifPresent(hlrResponse -> bsgApiErrorHandler.handle(fromErrorCode(hlrResponse.getError())));

        return multipleHlrResponse.getResult().stream()
            .map(HlrResponse::getId)
            .map(HlrId::of)
            .collect(Collectors.toList());
    }

    @Override
    public Hlr getHlrInfo(HlrId hlrId, String token) {
        final HlrInfo hlrInfo = api.getHlrInfo(hlrId.getId(), ApiKey.of(token));
        bsgApiErrorHandler.handle(fromErrorCode(hlrInfo.getError()));

        return hlrInfoToHlrConverter.convert(hlrInfo);
    }

    @Override
    public CompletableFuture<Hlr> getHlrInfoAsync(HlrId hlrId, String token) {
        final ApiKey apiKey = ApiKey.of(token);

        HlrInfo hlrInfo = api.getHlrInfo(hlrId.getId(), apiKey);

        int triesCounter = 0;
        while (triesCounter < hlrInfoSettings.getLimit() && !isFinalizedStatus(hlrInfo.getStatus())) {
            sleep(hlrInfoSettings.getPause());
            hlrInfo = api.getHlrInfo(hlrId.getId(), apiKey);

            triesCounter++;
        }

        final Hlr hlr = hlrInfoToHlrConverter.convert(hlrInfo);
        return CompletableFuture.completedFuture(hlr);
    }

    @Override
    public CompletableFuture<List<Hlr>> getHlrInfoListAsync(List<HlrId> hlrIds, String token) {
        List<Hlr> resultList = new ArrayList<>();
        List<HlrId> uncheckedHlrIds = new ArrayList<>(hlrIds);

        int triesCounter = 0;
        do {
            sleep(hlrInfoSettings.getPause());
            List<Hlr> hlrInfoList = getHlrInfoList(uncheckedHlrIds, token);

            Map<Boolean, List<Hlr>> groupedByFinalStatus = hlrInfoList.stream()
                .collect(Collectors.groupingBy(hlr -> isFinalizedStatus(hlr.getStatus())));

            Optional.ofNullable(groupedByFinalStatus.get(true)).ifPresent(resultList::addAll);

            uncheckedHlrIds = Optional.ofNullable(groupedByFinalStatus.get(false))
                .orElseGet(Collections::emptyList)
                .stream()
                .map(Hlr::getProviderId)
                .map(HlrId::of)
                .collect(Collectors.toList());

            triesCounter++;

        } while (!uncheckedHlrIds.isEmpty() && triesCounter < hlrInfoSettings.getLimit());

        if (!uncheckedHlrIds.isEmpty()) {
            log.info("There are still some sent HLRs left");
            List<Hlr> notFinalizedHlrList = getHlrInfoList(uncheckedHlrIds, token);
            resultList.addAll(notFinalizedHlrList);
        }

        return CompletableFuture.completedFuture(resultList);
    }

    @Override
    public void save(HlrEntity hlr) {
        repository.save(hlr);
    }

    @Override
    public HlrEntity retrieveHlrByProviderId(String providerId) {
        return repository.findByProviderId(providerId).orElseThrow(RuntimeException::new);
    }

    private List<Hlr> getHlrInfoList(List<HlrId> hlrIds, String token) {
        return hlrIds.stream()
            .map(hlrId -> getHlrInfo(hlrId, token))
            .collect(Collectors.toList());
    }

    private boolean isFinalizedStatus(String responseStatus) {
        if (responseStatus == null) {
            return false;
        }
        return hlrStatuses.getFinalized().stream()
            .anyMatch(status -> status.equalsIgnoreCase(responseStatus));
    }

    private HrlRequest mapPhoneToHrlRequest(Phone phone) {
        return HrlRequest.builder()
            .msisdn(phone.getFilteredNumber())
            .reference(generateReference())
            .build();
    }

    private String generateReference() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 13);
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
