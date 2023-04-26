package me.shaposhnik.hlrbot.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import okhttp3.OkHttpClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@Configuration
public class AppConfig {

  @Bean
  public ObjectMapper objectMapper() {
    var objectMapper = new ObjectMapper();

    objectMapper.registerModule(new JavaTimeModule());
    return objectMapper;
  }

  @Bean
  public OkHttpClient okHttpClient() {
    return new OkHttpClient();
  }

}
