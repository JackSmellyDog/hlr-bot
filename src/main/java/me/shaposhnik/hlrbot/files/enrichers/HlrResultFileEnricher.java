package me.shaposhnik.hlrbot.files.enrichers;

import me.shaposhnik.hlrbot.files.persistence.FileEntity;
import me.shaposhnik.hlrbot.model.Hlr;

import java.util.List;
import java.util.Set;

public interface HlrResultFileEnricher {

    FileEntity enrich(FileEntity fileEntity, List<Hlr> hlrRowList);

    Set<String> getSupportedFileExtensions();
}
