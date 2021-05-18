package me.shaposhnik.hlrbot.converter;

import me.shaposhnik.hlrbot.model.Hlr;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.util.Map;

import static org.apache.commons.lang3.StringUtils.capitalize;


@Component
public class HlrToTelegramResponseConverter implements Converter<Hlr, String> {

    @Override
    public String convert(@NonNull Hlr hlr) {
        StringBuilder mainBuilder = new StringBuilder()
            .append("***ProviderId:*** ").append(hlr.getProviderId()).append('\n')
            .append("***Number:*** ").append(hlr.getNumber()).append('\n')
            .append("***Network:*** ").append(hlr.getNetwork()).append('\n')
            .append("***Status:*** ").append(capitalize(hlr.getStatus())).append('\n')
            .append("***Ported:*** ").append(hlr.getPorted()).append('\n')
            .append("***Roaming:*** ").append(hlr.getRoaming()).append('\n')
            .append("***Created at:*** ").append(hlr.getCreatedAt()).append('\n')
            .append("***Status received at:*** ").append(hlr.getStatusReceivedAt()).append('\n');

        final Map<String, String> details = hlr.getDetails();
        if (details != null && !details.isEmpty()) {
            StringBuilder detailsBuilder = new StringBuilder();

            details.forEach((k, v) ->
                detailsBuilder.append("    ***").append(k).append("***").append(": ").append(v).append('\n'));

            mainBuilder.append("***Details:*** ").append('\n')
                .append(detailsBuilder);
        }

        final Map<String, String> otherProperties = hlr.getOtherProperties();

        if (otherProperties != null && !otherProperties.isEmpty()) {
            StringBuilder otherPropertiesBuilder = new StringBuilder();

            otherProperties.forEach((k, v) ->
                otherPropertiesBuilder.append("    ***").append(k).append("***").append(": ").append(v).append('\n'));

            mainBuilder.append("***Other properties:*** ").append('\n')
                .append(otherPropertiesBuilder);
        }

        return mainBuilder.toString();
    }
}
