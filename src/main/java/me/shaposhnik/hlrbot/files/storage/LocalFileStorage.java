package me.shaposhnik.hlrbot.files.storage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.shaposhnik.hlrbot.files.persistence.FileEntity;
import me.shaposhnik.hlrbot.files.persistence.FileRepository;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Document;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class LocalFileStorage implements FileStorage {

    private final FileRepository fileRepository;

    // TODO: 10/7/21 make path for windows and unix-like
    @Value("${bot.files.directory-to-download}")
    private String fileDownloadDirectory;

    @Override
    public FileEntity get(String id) {
        return fileRepository.findById(id).orElseThrow();
    }

    @Override
    public FileEntity save(Document document, InputStream is) {

        Path tempFile = null;
        try(is) {
            final String extension = FilenameUtils.getExtension(document.getFileName());
            final String realFileName = createRealFileName(extension);
            final String fullPath = createFullPath(realFileName);

            tempFile = Files.createFile(Path.of(fullPath));
            FileUtils.copyInputStreamToFile(is, tempFile.toFile());

            FileEntity entity = FileEntity.builder()
                .id(document.getFileUniqueId())
                .mimeType(document.getMimeType())
                .extension(extension)
                .fullPath(fullPath)
                .realFileName(realFileName)
                .receivedFileName(document.getFileName())
                .build();

            return fileRepository.save(entity);

        } catch (Exception e) {
            log.error("Failed to save a file to the file storage!", e);
            delete(tempFile);
            throw new RuntimeException(e);
        }
    }

    private String createRealFileName(String extension) {
        return UUID.randomUUID().toString().replace("-", "") + "." + extension;
    }

    private String createFullPath(String realFileName) {
        return fileDownloadDirectory + File.separator + realFileName;
    }

    @Override
    public void delete(String id) {
        fileRepository.findByIdAndDeletedFalse(id)
            .map(FileEntity::toPath)
            .ifPresent(file -> {
                delete(file);
                fileRepository.markDeleted(id);
            });
    }

    private void delete(Path path) {
        if (path == null) return;

        try {
            Files.deleteIfExists(path);
            log.info("File has been deleted: {}", path.getFileName());
        } catch (IOException e) {
            log.error("Failed to delete a file!", e);
        }
    }
}
