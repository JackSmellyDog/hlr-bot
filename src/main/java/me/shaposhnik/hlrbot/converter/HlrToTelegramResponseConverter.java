package me.shaposhnik.hlrbot.converter;

import me.shaposhnik.hlrbot.model.Hlr;
import org.springframework.core.convert.converter.Converter;

public interface HlrToTelegramResponseConverter extends Converter<Hlr, String> {
}
