package me.shaposhnik.hlrbot.files.writers;

import java.util.List;
import java.util.Set;
import me.shaposhnik.hlrbot.files.persistence.FileEntity;
import me.shaposhnik.hlrbot.model.Hlr;

public interface HlrResultsFileWriter {

  FileEntity write(FileEntity fileEntity, List<Hlr> hlrRowList);

  Set<String> getSupportedFileExtensions();

}
