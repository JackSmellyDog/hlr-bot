package me.shaposhnik.hlrbot.integration.bsg;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.shaposhnik.hlrbot.integration.bsg.dto.*;
import me.shaposhnik.hlrbot.integration.bsg.properties.HlrInfoSettings;
import me.shaposhnik.hlrbot.integration.bsg.properties.HlrStatuses;
import me.shaposhnik.hlrbot.model.Hlr;
import me.shaposhnik.hlrbot.model.HlrId;
import me.shaposhnik.hlrbot.model.Phone;
import me.shaposhnik.hlrbot.model.enums.Ported;
import me.shaposhnik.hlrbot.model.enums.Roaming;
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

        return mapHlrInfoToHlr(hlrInfo);
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

        return CompletableFuture.completedFuture(mapHlrInfoToHlr(hlrInfo));
    }

    @Override
    public void save(HlrEntity hlr) {
        repository.save(hlr);
    }

    @Override
    public HlrEntity retrieveHlrByProviderId(String providerId) {
        return repository.findByProviderId(providerId).orElseThrow(RuntimeException::new);
    }

    private Hlr mapHlrInfoToHlr(HlrInfo hlrInfo) {
        final Ported ported = Optional.ofNullable(hlrInfo.getDetails())
            .map(HlrInfo.Details::getPorted)
            .map(status -> status.equals("1") ? Ported.YES : Ported.NO)
            .orElse(Ported.UNKNOWN);

        final Roaming roaming = Optional.ofNullable(hlrInfo.getDetails())
            .map(HlrInfo.Details::getRoaming)
            .map(status -> status.equals("1") ? Roaming.YES : Roaming.NO)
            .orElse(Roaming.UNKNOWN);

        final Map<String, String> details = Optional.ofNullable(hlrInfo.getDetails())
            .map(HlrInfo.Details::getOtherDetails)
            .orElseGet(Map::of);

        return Hlr.builder()
            .providerId(hlrInfo.getId())
            .number(hlrInfo.getMsisdn())
            .network(hlrInfo.getNetwork())
            .status(hlrInfo.getStatus())
            .ported(ported)
            .roaming(roaming)
            .details(details)
            .createdAt(hlrInfo.getCreatedDatetime())
            .statusReceivedAt(hlrInfo.getStatusDatetime())
            .otherProperties(hlrInfo.getOtherProperties())
            .build();
    }

    private boolean isFinalizedStatus(String responseStatus)  {
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
