package me.shaposhnik.hlrbot.converter;

import lombok.RequiredArgsConstructor;
import me.shaposhnik.hlrbot.bot.HlrBotMessageSource;
import me.shaposhnik.hlrbot.model.Hlr;
import me.shaposhnik.hlrbot.model.Phone;
import org.springframework.lang.NonNull;

import javax.annotation.PostConstruct;
import java.util.Locale;
import java.util.Optional;

import static org.apache.commons.lang3.StringUtils.capitalize;

@RequiredArgsConstructor
public class HlrToTelegramResponseConverterImpl implements HlrToTelegramResponseConverter {

    private static final String COLON_SPACE = ": ";
    private static final String TAB = "    ";
    private static final char EOL = '\n';

    private final Locale locale;
    private final HlrBotMessageSource messageSource;

    private String requestedPhoneNumberKey;
    private String providerIdKey;
    private String msisdnKey;
    private String networkKey;
    private String statusKey;
    private String portedKey;
    private String roamingKey;
    private String createdAtKey;
    private String statusReceivedAtKey;
    private String detailsKey;
    private String otherPropertiesKey;
    private String errorKey;

    @PostConstruct
    private void initMessages() {
        requestedPhoneNumberKey = bold(messageSource.getMessage("response.requested-phone-number", locale));
        providerIdKey = bold(messageSource.getMessage("response.provider-id", locale));
        msisdnKey = bold(messageSource.getMessage("response.msisdn", locale));
        networkKey = bold(messageSource.getMessage("response.network", locale));
        statusKey = bold(messageSource.getMessage("response.status", locale));
        portedKey = bold(messageSource.getMessage("response.ported", locale));
        roamingKey = bold(messageSource.getMessage("response.roaming", locale));
        createdAtKey = bold(messageSource.getMessage("response.created-at", locale));
        statusReceivedAtKey = bold(messageSource.getMessage("response.status-received-at", locale));
        detailsKey = bold(messageSource.getMessage("response.details", locale));
        otherPropertiesKey = bold(messageSource.getMessage("response.other-properties", locale));
        errorKey = bold(messageSource.getMessage("response.error", locale));
    }

    @Override
    public String convert(@NonNull Hlr hlr) {

        final String requestedPhoneNumber = Optional.ofNullable(hlr.getPhone())
            .map(Phone::getRawNumberValue)
            .orElse("-");

        if (hlr.getErrorDescription() != null) {
            return buildErrorMessage(hlr.getErrorDescription(), requestedPhoneNumber);
        }

        var mainBuilder = new StringBuilder()
            .append(requestedPhoneNumberKey).append(COLON_SPACE).append(requestedPhoneNumber).append(EOL)
            .append(providerIdKey).append(COLON_SPACE).append(hlr.getProviderId()).append(EOL)
            .append(msisdnKey).append(COLON_SPACE).append(hlr.getMsisdn()).append(EOL)
            .append(networkKey).append(COLON_SPACE).append(hlr.getNetwork()).append(EOL)
            .append(statusKey).append(COLON_SPACE).append(capitalize(hlr.getStatus())).append(EOL)
            .append(portedKey).append(COLON_SPACE).append(hlr.getPorted()).append(EOL)
            .append(roamingKey).append(COLON_SPACE).append(hlr.getRoaming()).append(EOL)
            .append(createdAtKey).append(COLON_SPACE).append(hlr.getCreatedAt()).append(EOL)
            .append(statusReceivedAtKey).append(COLON_SPACE).append(hlr.getStatusReceivedAt()).append(EOL);

        final var details = hlr.getDetails();
        if (details != null && !details.isEmpty()) {
            StringBuilder detailsBuilder = new StringBuilder();

            details.forEach((k, v) -> 
                detailsBuilder.append(TAB).append(bold(k)).append(COLON_SPACE).append(v).append(EOL));

            mainBuilder.append(detailsKey).append(COLON_SPACE).append(EOL).append(detailsBuilder);
        }

        final var otherProperties = hlr.getOtherProperties();

        if (otherProperties != null && !otherProperties.isEmpty()) {
            var otherPropertiesBuilder = new StringBuilder();

            otherProperties.forEach((k, v) ->
                otherPropertiesBuilder.append(TAB).append(bold(k)).append(COLON_SPACE).append(v).append(EOL));

            mainBuilder.append(otherPropertiesKey).append(COLON_SPACE).append(EOL).append(otherPropertiesBuilder);
        }

        return mainBuilder.toString();
    }

    private String buildErrorMessage(String errorMessage, String requestedPhoneNumber) {
        return requestedPhoneNumberKey + COLON_SPACE + requestedPhoneNumber + EOL + errorKey + COLON_SPACE + errorMessage + EOL;
    }
    
    private String bold(String text) {
        return String.format("***%s***", text);
    }
}
