package me.shaposhnik.hlrbot.bot;

import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class HlrBotMessageSource {

  private final MessageSource messageSource;

  public String getMessage(String key, Locale locale, Object... args) {
    return messageSource.getMessage(key, args, locale);
  }

  public String getDefaultErrorMessage(Locale locale) {
    return getMessage("error.default", locale);
  }
}
