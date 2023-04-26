package me.shaposhnik.hlrbot.integration.bsg.properties;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@ConfigurationProperties(prefix = "integration.urls")
public class IntegrationUrlsProperties {

  private String baseUrl;

  private String requestHlrInfoUrl;

  private String createHlrUrl;

  private String balanceUrl;

}
