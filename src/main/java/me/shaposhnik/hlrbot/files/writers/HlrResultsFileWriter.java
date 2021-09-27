package me.shaposhnik.hlrbot.files.writers;

import me.shaposhnik.hlrbot.model.Hlr;

import java.nio.file.Path;
import java.util.List;
import java.util.Set;

public interface HlrResultsFileWriter {

    Path write(Path file, List<Hlr> hlrRowList);

    Set<String> getSupportedFileExtensions();

}
