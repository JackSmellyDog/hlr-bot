package me.shaposhnik.hlrbot.files;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.shaposhnik.hlrbot.files.properties.FilesProperties;
import me.shaposhnik.hlrbot.files.readers.PhoneFileReaderFacade;
import me.shaposhnik.hlrbot.model.Phone;
import org.apache.commons.io.FilenameUtils;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileService {

    private final FilesProperties filesProperties;
    private final PhoneFileReaderFacade phoneFileReaderFacade;

    public List<Phone> readPhones(File file) {
        final String extension = FilenameUtils.getExtension(file.getName());
        return phoneFileReaderFacade.readPhones(file, extension);
    }

    public String getFileExtensionByMimeType(String mimeType) {
        return filesProperties.resolveExtension(mimeType);
    }

}
