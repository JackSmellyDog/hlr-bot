package me.shaposhnik.hlrbot.files.readers;

import me.shaposhnik.hlrbot.model.Phone;

import java.io.File;
import java.util.List;
import java.util.Set;

public interface PhonesFileReader {

    List<Phone> readPhones(File file);

    Set<String> getSupportedFileExtensions();

}
