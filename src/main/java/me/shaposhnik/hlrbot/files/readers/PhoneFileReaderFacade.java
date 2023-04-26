package me.shaposhnik.hlrbot.files.readers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import me.shaposhnik.hlrbot.files.exception.ReadFileException;
import me.shaposhnik.hlrbot.files.persistence.FileEntity;
import me.shaposhnik.hlrbot.model.Phone;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PhoneFileReaderFacade {

  private final List<PhonesFileReader> readers;
  private Map<String, PhonesFileReader> extensionToFileReaderMap;

  @PostConstruct
  void initExtensionToFileReaderMap() {
    extensionToFileReaderMap = new HashMap<>();
    readers.forEach(this::mapFileExtensionsToReader);
  }

  private void mapFileExtensionsToReader(PhonesFileReader reader) {
    reader.getSupportedFileExtensions()
        .forEach(extension -> extensionToFileReaderMap.put(extension.toLowerCase(), reader));
  }

  public List<Phone> readPhones(FileEntity fileEntity) {
    return Optional.ofNullable(
            extensionToFileReaderMap.get(fileEntity.getExtension().toLowerCase()))
        .map(reader -> reader.readPhones(fileEntity))
        .orElseThrow(ReadFileException::new);
  }
}
