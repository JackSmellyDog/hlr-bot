package me.shaposhnik.hlrbot.files.storage;

import me.shaposhnik.hlrbot.files.persistence.FileEntity;
import org.telegram.telegrambots.meta.api.objects.Document;

import java.io.InputStream;

public interface FileStorage {

    FileEntity get(String id);

    FileEntity save(Document document, InputStream is);

    void delete(String id);

}
