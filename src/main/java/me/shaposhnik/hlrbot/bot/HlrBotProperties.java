package me.shaposhnik.hlrbot.bot;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Set;

@Getter
@Setter
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
    static class File {
        private int maxSize;
        private int limitOfNumbers;
    }
}
