package me.shaposhnik.hlrbot.integration.bsg.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "integration.bsg.async-hlr-info")
public class HlrInfoSettings {

    private Integer limit;

    private Integer pause;

}
