package me.shaposhnik.hlrbot.integration.bsg.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class HlrResponse {
  private String id;
  private String reference;
  private int error;
  private String price;
  private String currency;
}
