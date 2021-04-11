package me.shaposhnik.hlrbot.integration.bsg.properties;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@ConfigurationProperties(prefix = "integration.bsg.hlr-statuses")
public class HlrStatuses {

    private List<String> finalized;

    private List<String> pending;

}
