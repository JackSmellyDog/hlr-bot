package me.shaposhnik.hlrbot.files.enrichers;

import lombok.RequiredArgsConstructor;
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
public class HlrResultFileEnricherFacade {
    private final List<HlrResultFileEnricher> enrichers;
    private Map<String, HlrResultFileEnricher> extensionToFileEnricherMap;

    @PostConstruct
    private void initExtensionToFileWriterMap() {
        extensionToFileEnricherMap = new HashMap<>();
        enrichers.forEach(this::mapFileExtensionsToEnricher);
    }

    private void mapFileExtensionsToEnricher(HlrResultFileEnricher enricher) {
        enricher.getSupportedFileExtensions()
            .forEach(extension -> extensionToFileEnricherMap.put(extension.toLowerCase(), enricher));
    }

    public Optional<FileEntity> enrich(FileEntity fileEntity, List<Hlr> hlrList) {
        return Optional.ofNullable(extensionToFileEnricherMap.get(fileEntity.getExtension().toLowerCase()))
            .map(enricher -> enricher.enrich(fileEntity, hlrList));
    }

}
