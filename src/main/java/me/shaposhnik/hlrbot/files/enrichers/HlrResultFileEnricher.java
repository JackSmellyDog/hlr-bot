package me.shaposhnik.hlrbot.files.enrichers;

import java.util.List;
import java.util.Set;
import me.shaposhnik.hlrbot.files.persistence.FileEntity;
import me.shaposhnik.hlrbot.model.Hlr;

public interface HlrResultFileEnricher {

  FileEntity enrich(FileEntity fileEntity, List<Hlr> hlrRowList);

  Set<String> getSupportedFileExtensions();
}
