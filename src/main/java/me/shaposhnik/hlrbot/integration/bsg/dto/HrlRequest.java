package me.shaposhnik.hlrbot.integration.bsg.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class HrlRequest {
  private final String msisdn;
  private final String reference;
}
