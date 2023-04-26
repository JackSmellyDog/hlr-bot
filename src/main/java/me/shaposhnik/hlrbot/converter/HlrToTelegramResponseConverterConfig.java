package me.shaposhnik.hlrbot.converter;

import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import me.shaposhnik.hlrbot.bot.HlrBotMessageSource;
import me.shaposhnik.hlrbot.bot.HlrBotProperties;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

@Configuration
@RequiredArgsConstructor
public class HlrToTelegramResponseConverterConfig {

  private final HlrBotMessageSource messageSource;
  private final HlrBotProperties hlrBotProperties;

  @Bean
  @Scope(value = BeanDefinition.SCOPE_PROTOTYPE)
  @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
  public HlrToTelegramResponseConverter createConverter(Locale locale) {
    return new HlrToTelegramResponseConverterImpl(locale, messageSource);
  }

  @Bean
  public HlrToTelegramResponseConverterFacade hlrToTelegramResponseConverterProvider() {
    Map<Locale, HlrToTelegramResponseConverter> localToConverter = hlrBotProperties.getLanguages()
        .stream()
        .map(Locale::forLanguageTag)
        .collect(Collectors.toMap(Function.identity(), this::createConverter));

    return new HlrToTelegramResponseConverterFacade(localToConverter);
  }

}
