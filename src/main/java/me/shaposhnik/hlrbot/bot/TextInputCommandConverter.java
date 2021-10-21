package me.shaposhnik.hlrbot.bot;

import lombok.RequiredArgsConstructor;
import me.shaposhnik.hlrbot.bot.enums.Command;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class TextInputCommandConverter {

    private final HlrBotProperties hlrBotProperties;
    private final HlrBotMessageSource messageSource;
    private Map<Command, Set<String>> commandToAliases;


    @PostConstruct
    private void init() {
        commandToAliases = EnumSet.allOf(Command.class).stream()
            .collect(Collectors.toMap(Function.identity(), this::createAliases));
    }

    public Optional<Command> convertTextToCommand(@NonNull String text) {
        return Command.fromString(text)
            .or(() -> commandToAliases.entrySet().stream()
                .filter(entry -> entry.getValue().contains(text))
                .map(Map.Entry::getKey)
                .findFirst());
    }

    public String convertCommandToButton(Command command, Locale locale) {
        return String.format("%s %s",
            messageSource.getMessage(command.getEmoji(), locale),
            messageSource.getMessage(command.getButtonAlias(), locale)
        );
    }

    private Set<String> createAliases(Command command) {
        return hlrBotProperties.getLanguages().stream()
            .map(Locale::forLanguageTag)
            .map(locale -> convertCommandToButton(command, locale))
            .collect(Collectors.toSet());
    }

}
