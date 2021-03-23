package me.shaposhnik.hlrbot.integration.bsg.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(staticName = "of")
public class ApiKey {
    private final String key;
}
