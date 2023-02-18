package me.shaposhnik.hlrbot.files.readers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.shaposhnik.hlrbot.files.exception.ReadFileException;
import me.shaposhnik.hlrbot.files.persistence.FileEntity;
import me.shaposhnik.hlrbot.model.Phone;
import me.shaposhnik.hlrbot.service.PhoneService;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.nio.file.Files;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class PlainTextPhonesFileReader implements PhonesFileReader {

    private final PhoneService phoneService;

    @Override
    public List<Phone> readPhones(FileEntity fileEntity) {

        try (BufferedReader br = Files.newBufferedReader(fileEntity.toPath())) {

            return br.lines()
                    .map(phoneService::parseFromString)
                    .flatMap(Collection::stream)
                    .distinct()
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("Can't read a file {}.", fileEntity.getReceivedFileName(), e);
            throw new ReadFileException(e);
        }
    }

    @Override
    public Set<String> getSupportedFileExtensions() {
        return Set.of("txt", "csv");
    }
}
