package me.shaposhnik.hlrbot.service;

import me.shaposhnik.hlrbot.model.Hlr;
import me.shaposhnik.hlrbot.model.HlrId;
import me.shaposhnik.hlrbot.model.Phone;

import java.util.Collection;
import java.util.List;

public interface HlrService {

    HlrId sendHlr(Phone phone);

    <T extends Collection<Phone>> List<HlrId> sendHlrs(T phones);

    Hlr getHlrInfo(HlrId id);

}
