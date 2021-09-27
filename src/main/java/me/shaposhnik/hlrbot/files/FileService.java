package me.shaposhnik.hlrbot.files;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.shaposhnik.hlrbot.files.properties.FilesProperties;
import org.apache.commons.io.FilenameUtils;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;


@Slf4j
@Service
@RequiredArgsConstructor
public class FileService {

    private final FilesProperties filesProperties;

    public String getFileExtensionByMimeType(String mimeType) {
        return filesProperties.resolveExtension(mimeType);
    }

    public String getExtension(String filename) {
        return FilenameUtils.getExtension(filename);
    }

    public void deleteFile(Path path) {
        try {
            Files.deleteIfExists(path);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
