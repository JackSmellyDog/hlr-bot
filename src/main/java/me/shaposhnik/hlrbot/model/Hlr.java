package me.shaposhnik.hlrbot.model;

import java.time.LocalDateTime;
import java.util.Map;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import me.shaposhnik.hlrbot.model.enums.Ported;
import me.shaposhnik.hlrbot.model.enums.Roaming;

@Getter
@Setter
@Builder
public class Hlr {
  private Phone phone;
  private String reference;
  private String errorDescription;

  private String providerId;
  private String msisdn;
  private String network;
  private String status;

  private Ported ported;
  private Roaming roaming;
  private Map<String, String> details;

  private LocalDateTime createdAt;
  private LocalDateTime statusReceivedAt;

  private Map<String, String> otherProperties;
}
