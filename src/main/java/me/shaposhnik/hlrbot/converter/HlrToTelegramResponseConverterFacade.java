package me.shaposhnik.hlrbot.converter;

import lombok.RequiredArgsConstructor;
import me.shaposhnik.hlrbot.model.Hlr;

import java.util.Locale;
import java.util.Map;
import java.util.Optional;

@RequiredArgsConstructor
public class HlrToTelegramResponseConverterFacade {
    private static final Locale DEFAULT_LOCALE = Locale.US;

    private final Map<Locale, HlrToTelegramResponseConverter> localeHlrToTelegramResponseConverterMap;

    public String convert(Hlr hlr, Locale locale) {
        var converter = Optional.ofNullable(localeHlrToTelegramResponseConverterMap.get(locale))
            .orElseGet(() -> localeHlrToTelegramResponseConverterMap.get(DEFAULT_LOCALE));

        return converter.convert(hlr);
    }

}
