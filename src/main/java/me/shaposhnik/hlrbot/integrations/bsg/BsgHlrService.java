package me.shaposhnik.hlrbot.integrations.bsg;

import me.shaposhnik.hlrbot.service.HlrService;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class BsgHlrService implements HlrService {
    private final BsgApiClient api;


}
