package me.shaposhnik.hlrbot.files;

import lombok.experimental.UtilityClass;

import java.util.Map;

@UtilityClass
public class FileExtensionResolver {
    private static final Map<String, String> MIME_TYPE_TO_FILE_EXTENSION = Map.of(
        "text/plain", "txt",
        "text/csv", "csv"
    );

    public static String resolveExtensionOrDefault(String mimeType, String defaultExtension) {
        return MIME_TYPE_TO_FILE_EXTENSION.getOrDefault(mimeType, defaultExtension);
    }

    public static String resolveExtensionOrDefaultWithDot(String mimeType, String defaultExtension) {
        return String.format(".%s", resolveExtensionOrDefault(mimeType, defaultExtension));
    }
}
