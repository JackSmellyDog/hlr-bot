package me.shaposhnik.hlrbot.files.writers;

import lombok.extern.slf4j.Slf4j;
import me.shaposhnik.hlrbot.model.Hlr;
import me.shaposhnik.hlrbot.model.enums.Ported;
import me.shaposhnik.hlrbot.model.enums.Roaming;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;


@Slf4j
@Component
public class CsvHlrResultsFileWriter implements HlrResultsFileWriter {

    private static final List<String> HEADER_ROW = List.of("Raw number", "Msisdn", "Status", "Ported", "Roaming", "Network");

    @Value("${files.csv-file-writer-separator}")
    private String separator;

    @Override
    public Path write(Path path, List<Hlr> hlrRowList) {
        try (PrintWriter pw = new PrintWriter(path.toFile())) {
            final String header = HEADER_ROW.stream().collect(Collectors.joining(separator));
            pw.println(header);

            hlrRowList.stream()
                .map(this::mapHlrToRow)
                .forEach(pw::println);

        } catch (IOException e) {
            log.error("Failed to write to the file", e);
        }

        return path;
    }

    private String mapHlrToRow(Hlr hlr) {
        final String rawNumberValue = hlr.getPhone().getRawNumberValue();
        final String msisdn = hlr.getMsisdn();
        final String status = StringUtils.capitalize(hlr.getStatus());
        final String ported = Optional.ofNullable(hlr.getPorted()).map(Ported::toString).orElse("-");
        final String roaming = Optional.ofNullable(hlr.getRoaming()).map(Roaming::toString).orElse("-");
        final String network = hlr.getNetwork();

        return List.of(rawNumberValue, msisdn, status, ported, roaming, network)
            .stream().collect(Collectors.joining(separator));
    }

    @Override
    public Set<String> getSupportedFileExtensions() {
        return Set.of("txt", "csv");
    }
}
