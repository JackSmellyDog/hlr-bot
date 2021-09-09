package me.shaposhnik.hlrbot.converter;

import me.shaposhnik.hlrbot.model.Hlr;
import me.shaposhnik.hlrbot.model.Phone;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.util.Optional;

import static org.apache.commons.lang3.StringUtils.capitalize;


@Component
public class HlrToTelegramResponseConverter implements Converter<Hlr, String> {

    private static final String REQUESTED_PHONE_NUMBER = "Requested number";
    private static final String PROVIDER_ID = "ProviderId";
    private static final String MSISDN = "Msisdn";
    private static final String NETWORK = "Network";
    private static final String STATUS = "Status";
    private static final String PORTED = "Ported";
    private static final String ROAMING = "Roaming";
    private static final String CREATED_AT = "Created at";
    private static final String STATUS_RECEIVED_AT = "Status received at";
    private static final String DETAILS = "Details";
    private static final String OTHER_PROPERTIES = "Other properties";
    private static final String COLON_SPACE = ": ";
    private static final String TAB = "    ";
    private static final char EOL = '\n';

    @Override
    public String convert(@NonNull Hlr hlr) {

        final String requestedPhoneNumber = Optional.ofNullable(hlr.getPhone())
            .map(Phone::getRawNumberValue)
            .orElse("-");

        var mainBuilder = new StringBuilder()
            .append(bold(REQUESTED_PHONE_NUMBER)).append(COLON_SPACE).append(requestedPhoneNumber).append(EOL)
            .append(bold(PROVIDER_ID)).append(COLON_SPACE).append(hlr.getProviderId()).append(EOL)
            .append(bold(MSISDN)).append(COLON_SPACE).append(hlr.getMsisdn()).append(EOL)
            .append(bold(NETWORK)).append(COLON_SPACE).append(hlr.getNetwork()).append(EOL)
            .append(bold(STATUS)).append(COLON_SPACE).append(capitalize(hlr.getStatus())).append(EOL)
            .append(bold(PORTED)).append(COLON_SPACE).append(hlr.getPorted()).append(EOL)
            .append(bold(ROAMING)).append(COLON_SPACE).append(hlr.getRoaming()).append(EOL)
            .append(bold(CREATED_AT)).append(COLON_SPACE).append(hlr.getCreatedAt()).append(EOL)
            .append(bold(STATUS_RECEIVED_AT)).append(COLON_SPACE).append(hlr.getStatusReceivedAt()).append(EOL);

        final var details = hlr.getDetails();
        if (details != null && !details.isEmpty()) {
            StringBuilder detailsBuilder = new StringBuilder();

            details.forEach((k, v) -> 
                detailsBuilder.append(TAB).append(bold(k)).append(COLON_SPACE).append(v).append(EOL));

            mainBuilder.append(bold(DETAILS)).append(COLON_SPACE).append(EOL).append(detailsBuilder);
        }

        final var otherProperties = hlr.getOtherProperties();

        if (otherProperties != null && !otherProperties.isEmpty()) {
            var otherPropertiesBuilder = new StringBuilder();

            otherProperties.forEach((k, v) ->
                otherPropertiesBuilder.append(TAB).append(bold(k)).append(COLON_SPACE).append(v).append(EOL));

            mainBuilder.append(bold(OTHER_PROPERTIES)).append(COLON_SPACE).append(EOL).append(otherPropertiesBuilder);
        }

        return mainBuilder.toString();
    }
    
    private String bold(String text) {
        return String.format("***%s***", text);
    }
}
