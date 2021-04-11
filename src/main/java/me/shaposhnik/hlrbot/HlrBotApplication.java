package me.shaposhnik.hlrbot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@ConfigurationPropertiesScan
@SpringBootApplication
public class HlrBotApplication {

    public static void main(String[] args) {
        SpringApplication.run(HlrBotApplication.class, args);
    }

}
