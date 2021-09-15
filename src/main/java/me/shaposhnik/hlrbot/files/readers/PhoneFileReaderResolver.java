package me.shaposhnik.hlrbot.files.readers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class PhoneFileReaderResolver {

    private final List<PhonesFileReader> readers;
    private Map<String, PhonesFileReader> extensionToFileReaderMap;

    @PostConstruct
    private void initExtensionToFileReaderMap() {
        extensionToFileReaderMap = new HashMap<>();
        // TODO: 9/16/21 refactor, add other method
        readers.forEach(reader -> reader.getSupportedFileExtensions().stream()
            .map(String::toLowerCase)
            .forEach(extension -> extensionToFileReaderMap.put(extension, reader)));
    }

    // TODO: 8/1/21 default case
    public PhonesFileReader resolve(String fileExtension) {
        return extensionToFileReaderMap.get(fileExtension.toLowerCase());
    }
}
