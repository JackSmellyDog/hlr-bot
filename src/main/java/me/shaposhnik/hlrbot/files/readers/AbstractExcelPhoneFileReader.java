package me.shaposhnik.hlrbot.files.readers;

import lombok.extern.slf4j.Slf4j;
import me.shaposhnik.hlrbot.files.exception.ReadFileException;
import me.shaposhnik.hlrbot.files.persistence.FileEntity;
import me.shaposhnik.hlrbot.model.Phone;
import me.shaposhnik.hlrbot.service.PhoneService;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;


@Slf4j
public abstract class AbstractExcelPhoneFileReader implements PhonesFileReader {

    private static final Set<String> PHONES_COLUMN_HEADERS_REGEX = Set.of(
        ".*phone.*", ".*number.*", ".*телефон.*", ".*номер.*"
    );
    private static final int FIRST_DOCUMENT_SHEET = 0;
    private static final int HEADER_ROW_INDEX = 0;

    private PhoneService phoneService;

    abstract Workbook createWorkbook(File file) throws IOException;

    @Autowired
    public void setPhoneService(PhoneService phoneService) {
        this.phoneService = phoneService;
    }

    @Override
    public List<Phone> readPhones(FileEntity fileEntity) {
        final List<String> phoneColumnData = readPhoneColumn(fileEntity.toFile());

        return phoneService.toPhones(phoneColumnData);
    }

    private List<String> readPhoneColumn(File file) {
        try (Workbook workbook = createWorkbook(file)) {
            Sheet sheet = workbook.getSheetAt(FIRST_DOCUMENT_SHEET);

            List<String> result = new ArrayList<>();
            int indexOfPhoneColumn = findIndexOfPhoneColumn(sheet.getRow(HEADER_ROW_INDEX));

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Optional.ofNullable(sheet.getRow(i))
                    .map(row -> row.getCell(indexOfPhoneColumn))
                    .map(this::mapCellValueToString)
                    .ifPresent(result::add);
            }

            return result;
        } catch (Exception e) {
            log.error("Error while reading excel file: ", e);
            throw new ReadFileException(e);
        }
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
