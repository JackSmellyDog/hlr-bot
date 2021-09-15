package me.shaposhnik.hlrbot.files;

import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class FileExtensionResolver {

    // TODO: 9/16/21 to configuration
    private final Map<String, String> mimeTypeToFileExtension = Map.of(
        "text/plain", "txt",
        "text/csv", "csv"
    );

    public String resolveExtensionOrDefault(String mimeType, String defaultExtension) {
        return mimeTypeToFileExtension.getOrDefault(mimeType, defaultExtension);
    }
}
