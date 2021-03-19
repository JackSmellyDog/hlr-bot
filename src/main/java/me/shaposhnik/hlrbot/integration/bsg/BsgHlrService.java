package me.shaposhnik.hlrbot.integration.bsg;

import me.shaposhnik.hlrbot.model.Hlr;
import me.shaposhnik.hlrbot.model.HlrId;
import me.shaposhnik.hlrbot.model.Phone;
import me.shaposhnik.hlrbot.service.HlrService;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Collection;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class BsgHlrService implements HlrService {
    private final BsgApiClient api;


    @Override
    public HlrId sendHlr(Phone phone) {
        return null;
    }

    @Override
    public <T extends Collection<Phone>> List<HlrId> sendHlrs(T phones) {
        return null;
    }

    @Override
    public Hlr getHlrInfo(HlrId id) {
        return null;
    }
}
