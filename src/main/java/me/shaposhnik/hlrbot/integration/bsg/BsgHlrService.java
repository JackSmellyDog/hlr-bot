package me.shaposhnik.hlrbot.integration.bsg;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.shaposhnik.hlrbot.integration.bsg.dto.*;
import me.shaposhnik.hlrbot.model.*;
import me.shaposhnik.hlrbot.model.enums.Ported;
import me.shaposhnik.hlrbot.model.enums.Roaming;
import me.shaposhnik.hlrbot.persistence.entity.HlrEntity;
import me.shaposhnik.hlrbot.persistence.repository.HlrEntityRepository;
import me.shaposhnik.hlrbot.service.HlrService;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BsgHlrService implements HlrService {
    private final BsgApiClient api;
    private final HlrEntityRepository repository;

    @Override
    public HlrId sendHlr(Phone phone, String token) {
        final HrlRequest request = mapPhoneToHrlRequest(phone);
        final HlrResponse hlrResponse = api.sendHlr(request, ApiKey.of(token));

        return HlrId.of(hlrResponse.getId());
    }

    @Override
    public <T extends Collection<Phone>> List<HlrId> sendHlrs(T phones, String token) {
        final List<HrlRequest> hrlRequests = phones.stream()
            .map(this::mapPhoneToHrlRequest)
            .collect(Collectors.toList());

        final MultipleHlrResponse multipleHlrResponse = api.sendHlrs(hrlRequests, ApiKey.of(token));

        return multipleHlrResponse.getResult().stream()
            .map(HlrResponse::getId)
            .map(HlrId::of)
            .collect(Collectors.toList());
    }

    @Override
    public Hlr getHlrInfo(HlrId hlrId, String token) {
        final HlrInfo hlrInfo = api.getHlrInfo(hlrId.getId(), ApiKey.of(token));

        return mapHlrInfoToHlr(hlrInfo);
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

        return Hlr.builder()
            .providerId(hlrInfo.getId())
            .number(hlrInfo.getMsisdn())
            .network(hlrInfo.getNetwork())
            .status(hlrInfo.getStatus())
            .ported(ported)
            .roaming(roaming)
            .createdAt(hlrInfo.getCreatedDatetime().toLocalDateTime())
            .statusReceivedAt(hlrInfo.getStatusDatetime().toLocalDateTime())
            .build();
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
}
