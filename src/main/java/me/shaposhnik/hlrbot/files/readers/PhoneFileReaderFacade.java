package me.shaposhnik.hlrbot.files.readers;

import lombok.RequiredArgsConstructor;
import me.shaposhnik.hlrbot.files.exception.ReadFileException;
import me.shaposhnik.hlrbot.model.Phone;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PhoneFileReaderFacade {

    private final List<PhonesFileReader> readers;
    private Map<String, PhonesFileReader> extensionToFileReaderMap;

    @PostConstruct
    private void initExtensionToFileReaderMap() {
        extensionToFileReaderMap = new HashMap<>();
        readers.forEach(this::mapFileExtensionsToReader);
    }

    private void mapFileExtensionsToReader(PhonesFileReader reader) {
        reader.getSupportedFileExtensions()
            .forEach(extension -> extensionToFileReaderMap.put(extension.toLowerCase(), reader));
    }

    public List<Phone> readPhones(File file, String fileExtension) {
        return Optional.ofNullable(extensionToFileReaderMap.get(fileExtension.toLowerCase()))
            .map(reader -> reader.readPhones(file))
            .orElseThrow(ReadFileException::new);
    }
}
