package me.shaposhnik.hlrbot.converter;

import java.util.Locale;
import lombok.RequiredArgsConstructor;
import me.shaposhnik.hlrbot.bot.HlrBotMessageSource;
import me.shaposhnik.hlrbot.model.Balance;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BalanceToTelegramResponseConverter {

  private final HlrBotMessageSource messageSource;

  public String convert(Balance balance, Locale locale) {
    return balance.getAmount() != null
        ? successfulToString(balance, locale)
        : failedToString(balance, locale);
  }

  private String failedToString(Balance balance, Locale locale) {
    return String.format("***%s: *** %s",
        messageSource.getMessage("response.error", locale),
        balance.getErrorDescription());
  }

  private String successfulToString(Balance balance, Locale locale) {
    return String.format("***%s:*** %s%n***%s:*** %s%n***%s:*** %s",
        messageSource.getMessage("response.balance.amount", locale), balance.getAmount(),
        messageSource.getMessage("response.balance.currency", locale), balance.getCurrency(),
        messageSource.getMessage("response.balance.limit", locale), balance.getLimit());
  }
}
