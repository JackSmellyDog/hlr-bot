package me.shaposhnik.hlrbot.integration.bsg.properties;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ToString
@ConfigurationProperties(prefix = "integration.bsg.async-hlr-info")
public class HlrInfoSettings {

    private Integer limit;

    private Integer pause;

}
