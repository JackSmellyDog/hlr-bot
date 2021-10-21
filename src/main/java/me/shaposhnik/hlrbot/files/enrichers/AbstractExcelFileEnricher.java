package me.shaposhnik.hlrbot.files.enrichers;

import lombok.extern.slf4j.Slf4j;
import me.shaposhnik.hlrbot.files.persistence.FileEntity;
import me.shaposhnik.hlrbot.model.Hlr;
import me.shaposhnik.hlrbot.model.enums.Ported;
import me.shaposhnik.hlrbot.model.enums.Roaming;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

@Slf4j
public abstract class AbstractExcelFileEnricher implements HlrResultFileEnricher {

    // TODO: 10/10/21 to yml config
    private static final Set<String> PHONES_COLUMN_HEADERS_REGEX = Set.of(
        ".*phone.*", ".*number.*", ".*телефон.*", ".*номер.*"
    );

    private static final int FIRST_DOCUMENT_SHEET = 0;
    private static final String DASH = "-";

    abstract Workbook createWorkbook(File file) throws IOException;

    @Override
    public FileEntity enrich(FileEntity fileEntity, List<Hlr> hlrRowList) {
        try(Workbook workbook = createWorkbook(fileEntity.toFile())) {
            Sheet sheet = workbook.getSheetAt(FIRST_DOCUMENT_SHEET);
            Map<String, Hlr> rawNumberToHlrMap = buildRawNumberToHlrMap(hlrRowList);

            int lastCellColumn = findLastCellColumn(sheet);

            addData(sheet, rawNumberToHlrMap, lastCellColumn);

            workbook.write(new FileOutputStream(fileEntity.toFile()));

        } catch (Exception e) {
            log.error("Failed to enrich excel file!", e);
        }

        return fileEntity;
    }

    private void addData(Sheet sheet, Map<String, Hlr> rawNumberToHlrMap, int lastCellColumn) {
        int statusColumn = lastCellColumn + 1;
        int portedColumn = lastCellColumn + 2;
        int roamingColumn = lastCellColumn + 3;

        Row header = sheet.getRow(0);
        int phoneColumn = findIndexOfPhoneColumn(header);

        createCell(header, statusColumn, "Status");
        createCell(header, portedColumn, "Ported");
        createCell(header, roamingColumn, "Roaming");

        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);

            if (row == null)
                return;

            String rawPhoneNumber = mapCellValueToString(row.getCell(phoneColumn));

            Hlr hlr = rawNumberToHlrMap.get(rawPhoneNumber);

            final String status = Optional.ofNullable(hlr).map(Hlr::getStatus).map(StringUtils::capitalize).orElse(DASH);
            createCell(row, statusColumn, status);

            final String ported = Optional.ofNullable(hlr).map(Hlr::getPorted).map(Ported::toString).orElse(DASH);
            createCell(row, portedColumn, ported);

            final String roaming = Optional.ofNullable(hlr).map(Hlr::getRoaming).map(Roaming::toString).orElse(DASH);
            createCell(row, roamingColumn, roaming);
        }
    }

    private void createCell(Row row, int index, String value) {
        var cell = row.createCell(index, CellType.STRING);
        cell.setCellValue(value);
    }

    private Map<String, Hlr> buildRawNumberToHlrMap(List<Hlr> hlrRowList) {
        Map<String, Hlr> map = new HashMap<>();

        for (Hlr hlr : hlrRowList) {
            map.put(hlr.getPhone().getRawNumberValue(), hlr);
        }

        return map;
    }

    private int findLastCellColumn(Sheet sheet) {
        int lastCellColumn = 0;

        for (Row row : sheet) {
            if (lastCellColumn < row.getLastCellNum()) {
                lastCellColumn = row.getLastCellNum();
            }
        }

        return lastCellColumn;
    }

    private int findIndexOfPhoneColumn(Row headerRow) {
        for (int i = 0; i < headerRow.getLastCellNum(); i++) {
            for (String phonesColumnHeadersRegex : PHONES_COLUMN_HEADERS_REGEX) {

                final String header = mapCellValueToString(headerRow.getCell(i)).toLowerCase();
                if (header.matches(phonesColumnHeadersRegex)) {
                    return i;
                }
            }
        }

        return -1;
    }

    private String mapCellValueToString(Cell cell) {
        if (cell == null) return "";

        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();

            case NUMERIC:
                return BigDecimal.valueOf(cell.getNumericCellValue()).toPlainString();

            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());

            case ERROR:
                return "ERROR";

            case _NONE:
            case BLANK:
                return "";

            default:
                log.warn("Unknown cell type: ({}). Set to empty string", cell.getCellType());
                return "";
        }
    }

}
