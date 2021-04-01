package me.shaposhnik.hlrbot.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JsonToYamlConverter implements Converter<String, String> {

    private final ObjectMapper objectMapper;
    private final YAMLMapper yamlMapper;

    @Override
    public String convert(@NonNull String json) {

        try {
            return yamlMapper.writeValueAsString(objectMapper.readTree(json));
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException(e);
        }
    }
}
