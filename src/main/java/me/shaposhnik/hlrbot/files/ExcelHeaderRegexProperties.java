package me.shaposhnik.hlrbot.files;

import java.util.Set;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ToString
@ConfigurationProperties(prefix = "files.headers")
public class ExcelHeaderRegexProperties {
  private Set<String> regexList;
}
