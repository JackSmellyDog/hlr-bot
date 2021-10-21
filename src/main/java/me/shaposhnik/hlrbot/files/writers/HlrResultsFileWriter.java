package me.shaposhnik.hlrbot.files.writers;

import me.shaposhnik.hlrbot.files.persistence.FileEntity;
import me.shaposhnik.hlrbot.model.Hlr;

import java.util.List;
import java.util.Set;

public interface HlrResultsFileWriter {

    FileEntity write(FileEntity fileEntity, List<Hlr> hlrRowList);

    Set<String> getSupportedFileExtensions();

}
