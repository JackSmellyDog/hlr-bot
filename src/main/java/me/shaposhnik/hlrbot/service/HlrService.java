package me.shaposhnik.hlrbot.service;

import me.shaposhnik.hlrbot.model.Hlr;
import me.shaposhnik.hlrbot.model.HlrId;
import me.shaposhnik.hlrbot.model.Phone;
import me.shaposhnik.hlrbot.persistence.entity.HlrEntity;

import java.util.Collection;
import java.util.List;

public interface HlrService {

    HlrId sendHlr(Phone phone, String token);

    <T extends Collection<Phone>> List<HlrId> sendHlrs(T phones, String token);

    Hlr getHlrInfo(HlrId id, String token);

    void save(HlrEntity hlr);

    HlrEntity retrieveHlrByProviderId(String providerId);

}
