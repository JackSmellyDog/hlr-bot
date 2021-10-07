package me.shaposhnik.hlrbot.files.readers;

import me.shaposhnik.hlrbot.files.persistence.FileEntity;
import me.shaposhnik.hlrbot.model.Phone;

import java.util.List;
import java.util.Set;

public interface PhonesFileReader {

    List<Phone> readPhones(FileEntity file);

    Set<String> getSupportedFileExtensions();

}
