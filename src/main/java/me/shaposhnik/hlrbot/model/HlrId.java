package me.shaposhnik.hlrbot.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(staticName = "of")
public class HlrId {
    private final String id;
}
