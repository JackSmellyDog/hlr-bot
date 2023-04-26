package me.shaposhnik.hlrbot.config;

import static org.apache.commons.lang3.StringUtils.substringBefore;

import lombok.extern.slf4j.Slf4j;
import me.shaposhnik.hlrbot.HlrBotApplication;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class PropertiesInitializationLogging implements ApplicationListener<ContextRefreshedEvent> {
  @Override
  public void onApplicationEvent(ContextRefreshedEvent event) {
    ApplicationContext applicationContext = event.getApplicationContext();
    String appPackageName = HlrBotApplication.class.getPackageName();

    applicationContext.getBeansWithAnnotation(ConfigurationProperties.class).values().stream()
        .filter(property -> property.getClass().getPackageName().contains(appPackageName))
        .forEach(property -> {
          String classSimpleName = substringBefore(property.getClass().getSimpleName(), "$$");
          log.info("PROPERTY: {}: {}", classSimpleName, property);
        });
  }
}
