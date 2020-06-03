package me.shaposhnikandrii.hlrbot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.telegram.telegrambots.ApiContextInitializer;

@SpringBootApplication
public class HlrBotApplication {

  public static void main(String[] args) {
    ApiContextInitializer.init();
    SpringApplication.run(HlrBotApplication.class, args);
  }

}
