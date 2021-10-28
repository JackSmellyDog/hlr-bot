package me.shaposhnik.hlrbot.files.writers;

import lombok.extern.slf4j.Slf4j;
import me.shaposhnik.hlrbot.files.persistence.FileEntity;
import me.shaposhnik.hlrbot.model.Hlr;
import me.shaposhnik.hlrbot.model.enums.Ported;
import me.shaposhnik.hlrbot.model.enums.Roaming;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static me.shaposhnik.hlrbot.files.util.ExcelUtils.createStringCell;


@Slf4j
@Component
public class XlsxHlrResultsFileWriter implements HlrResultsFileWriter {

    private static final String SHEET_NAME = "Results";

    @Override
    public FileEntity write(FileEntity fileEntity, List<Hlr> hlrRowList) {
        try(XSSFWorkbook workbook = new XSSFWorkbook()) {
            var sheet = workbook.createSheet(SHEET_NAME);
            createHeader(sheet);

            int rowId = 1;
            for (Hlr hlr : hlrRowList) {
                final Row row = sheet.createRow(rowId);

                createStringCell(row, 0, hlr.getPhone().getRawNumberValue());
                createStringCell(row, 1, hlr.getMsisdn());
                createStringCell(row, 2, StringUtils.capitalize(hlr.getStatus()));
                createStringCell(row, 3, Optional.ofNullable(hlr.getPorted()).map(Ported::toString).orElse("-"));
                createStringCell(row, 4, Optional.ofNullable(hlr.getRoaming()).map(Roaming::toString).orElse("-"));
                createStringCell(row, 5, hlr.getNetwork());

                rowId++;
            }

            workbook.write(new FileOutputStream(fileEntity.toFile()));

        } catch (IOException e) {
            log.error("Failed to write xlsx file!", e);
        }

        return fileEntity;
    }

    private void createHeader(XSSFSheet sheet) {
        var header = sheet.createRow(0);
        createStringCell(header, 0, "Raw number");
        createStringCell(header, 1, "Msisdn");
        createStringCell(header, 2, "Status");
        createStringCell(header, 3, "Ported");
        createStringCell(header, 4, "Roaming");
        createStringCell(header, 5, "Network");
    }

    @Override
    public Set<String> getSupportedFileExtensions() {
        return Set.of("xlsx", "xls");
    }
}
