package me.shaposhnik.hlrbot.integration.bsg.properties;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ToString
@AllArgsConstructor
@ConfigurationProperties(prefix = "integration.bsg.hlr-statuses")
public class HlrStatuses {

  private List<String> finalized;

  private List<String> pending;

}
