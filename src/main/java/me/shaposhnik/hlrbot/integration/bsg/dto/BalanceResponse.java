package me.shaposhnik.hlrbot.integration.bsg.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class BalanceResponse {
  private int error;
  private String errorDescription;

  private String amount;
  private String currency;
  private int limit;
}
