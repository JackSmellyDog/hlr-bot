package me.shaposhnik.hlrbot.bot;

import java.util.Set;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ToString(exclude = "token")
@ConfigurationProperties(prefix = "bot")
public class HlrBotProperties {

  private String name;
  private String token;
  private Set<String> languages;
  private int limitOfNumbers;
  private int expectedExecutionTimePerRequestInMilliseconds;
  private int longExecutionMessageValueInMinutes;

  @Getter(value = AccessLevel.PRIVATE)
  private File file;

  public int getMaxFileSizeInMegabytes() {
    return file.maxSize;
  }

  public int getMaxFileSizeInBytes() {
    return file.maxSize * 1024 * 1024;
  }

  public int getLimitOfNumbersInFile() {
    return file.limitOfNumbers;
  }

  @Getter
  @Setter
  @ToString
  static class File {
    private int maxSize;
    private int limitOfNumbers;
  }
}
