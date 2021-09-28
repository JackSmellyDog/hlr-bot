package me.shaposhnik.hlrbot.files.writers;

import lombok.extern.slf4j.Slf4j;
import me.shaposhnik.hlrbot.model.Hlr;
import me.shaposhnik.hlrbot.model.enums.Ported;
import me.shaposhnik.hlrbot.model.enums.Roaming;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Component
public class XlsxHlrResultsFileWriter implements HlrResultsFileWriter {

    private static final String SHEET_NAME = "Results";

    @Override
    public Path write(Path path, List<Hlr> hlrRowList) {
        try(XSSFWorkbook workbook = new XSSFWorkbook()) {
            var sheet = workbook.createSheet(SHEET_NAME);
            createHeader(sheet);

            int rowId = 1;
            for (Hlr hlr : hlrRowList) {
                final Row row = sheet.createRow(rowId);

                createCell(row, 0, hlr.getPhone().getRawNumberValue());
                createCell(row, 1, hlr.getMsisdn());
                createCell(row, 2, StringUtils.capitalize(hlr.getStatus()));
                createCell(row, 3, Optional.ofNullable(hlr.getPorted()).map(Ported::toString).orElse("-"));
                createCell(row, 4, Optional.ofNullable(hlr.getRoaming()).map(Roaming::toString).orElse("-"));
                createCell(row, 5, hlr.getNetwork());

                rowId++;
            }

            workbook.write(new FileOutputStream(path.toFile()));

        } catch (IOException e) {
            log.error("Failed to write xlsx file!", e);
        }

        return path;
    }

    private void createHeader(XSSFSheet sheet) {
        var header = sheet.createRow(0);
        createCell(header, 0, "Raw number");
        createCell(header, 1, "Msisdn");
        createCell(header, 2, "Status");
        createCell(header, 3, "Ported");
        createCell(header, 4, "Roaming");
        createCell(header, 5, "Network");
    }

    private void createCell(Row row, int index, String value) {
        var cell = row.createCell(index, CellType.STRING);
        cell.setCellValue(value);
    }


    @Override
    public Set<String> getSupportedFileExtensions() {
        return Set.of("xlsx", "xls");
    }
}
