package me.shaposhnik.hlrbot.files;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.shaposhnik.hlrbot.files.readers.PhoneFileReaderResolver;
import me.shaposhnik.hlrbot.model.Phone;
import org.apache.commons.io.FilenameUtils;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileService {
    private final PhoneFileReaderResolver phoneFileReaderResolver;

    public List<Phone> readPhones(File file) {
        final String extension = FilenameUtils.getExtension(file.getName());
        final var reader = phoneFileReaderResolver.resolve(extension);

        return reader.readPhones(file);
    }

}
