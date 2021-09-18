package me.shaposhnik.hlrbot.files.properties;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import me.shaposhnik.hlrbot.files.exception.UnsupportedMimeTypeException;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;
import java.util.Optional;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ConfigurationProperties(prefix = "files")
public class FilesProperties {

    private Map<String, String> mimeTypeToFileExtension;

    public String resolveExtension(String mimeType) {
        return Optional.ofNullable(mimeTypeToFileExtension.get(mimeType)).orElseThrow(UnsupportedMimeTypeException::new);
    }
}
