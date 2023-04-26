package me.shaposhnik.hlrbot.files.readers;

import java.util.List;
import java.util.Set;
import me.shaposhnik.hlrbot.files.persistence.FileEntity;
import me.shaposhnik.hlrbot.model.Phone;

public interface PhonesFileReader {

  List<Phone> readPhones(FileEntity file);

  Set<String> getSupportedFileExtensions();

}
