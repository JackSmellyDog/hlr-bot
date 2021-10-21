package me.shaposhnik.hlrbot.files.writers;

import lombok.RequiredArgsConstructor;
import me.shaposhnik.hlrbot.files.exception.WriteFileException;
import me.shaposhnik.hlrbot.files.persistence.FileEntity;
import me.shaposhnik.hlrbot.model.Hlr;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class HlrResultFileWriterFacade {

    private final List<HlrResultsFileWriter> writers;
    private Map<String, HlrResultsFileWriter> extensionToFileWriterMap;

    @PostConstruct
    private void initExtensionToFileWriterMap() {
        extensionToFileWriterMap = new HashMap<>();
        writers.forEach(this::mapFileExtensionsToWriter);
    }

    private void mapFileExtensionsToWriter(HlrResultsFileWriter writer) {
        writer.getSupportedFileExtensions()
            .forEach(extension -> extensionToFileWriterMap.put(extension.toLowerCase(), writer));
    }

    public FileEntity write(FileEntity fileEntity, List<Hlr> hlrList) {
        return Optional.ofNullable(extensionToFileWriterMap.get(fileEntity.getExtension().toLowerCase()))
            .map(writer -> writer.write(fileEntity, hlrList))
            .orElseThrow(WriteFileException::new);
    }

}
