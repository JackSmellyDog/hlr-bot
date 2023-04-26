package me.shaposhnik.hlrbot.files.storage;

import java.io.InputStream;
import me.shaposhnik.hlrbot.files.persistence.FileEntity;
import org.telegram.telegrambots.meta.api.objects.Document;

public interface FileStorage {

  FileEntity get(String id);

  FileEntity save(Document document, InputStream is);

  FileEntity create(String filename);

  void delete(String id);
}
