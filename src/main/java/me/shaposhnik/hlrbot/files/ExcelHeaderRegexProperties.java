package me.shaposhnik.hlrbot.files;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Set;

@Getter
@Setter
@ConfigurationProperties(prefix = "files.headers")
public class ExcelHeaderRegexProperties {
    private Set<String> regexList;
}
