package me.shaposhnik.hlrbot.files.readers;

import lombok.extern.slf4j.Slf4j;
import me.shaposhnik.hlrbot.files.exception.ReadFileException;
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
import java.util.stream.Collectors;

@Slf4j
public abstract class AbstractExcelPhoneFileReader implements PhonesFileReader {

    protected static final Set<String> PHONES_COLUMN_HEADERS_REGEX = Set.of(
        ".*phone.*", ".*number.*", ".*телефон.*", ".*номер.*"
    );
    protected static final int FIRST_DOCUMENT_SHEET = 0;
    protected static final int HEADER_ROW_INDEX = 0;

    private PhoneService phoneService;

    abstract Workbook createWorkbook(File file) throws IOException;

    @Autowired
    public void setPhoneService(PhoneService phoneService) {
        this.phoneService = phoneService;
    }

    @Override
    public List<Phone> readPhones(File file) {
        final Map<Integer, List<String>> content = readExcelFile(file);
        final List<String> headers = content.getOrDefault(HEADER_ROW_INDEX, Collections.emptyList());
        final int phoneColumnIndex = findIndexOfPhoneColumnByHeader(headers);
        final List<String> phoneColumnData = readColumnByIndexIgnoreHeader(content, phoneColumnIndex);

        return phoneService.toPhones(phoneColumnData);
    }

    protected Map<Integer, List<String>> readExcelFile(File file) {
        try (Workbook workbook = createWorkbook(file)) {
            Sheet sheet = workbook.getSheetAt(FIRST_DOCUMENT_SHEET);
            Map<Integer, List<String>> result = new HashMap<>();

            int i = 0;
            for (Row row : sheet) {
                result.put(i, new ArrayList<>());
                for (Cell cell : row) {
                    result.get(i).add(mapCellValueToString(cell));
                }
                i++;
            }

            return result;
        } catch (Exception e) {
            log.error("Error while reading excel file: ", e);
            throw new ReadFileException(e);
        }
    }

    protected int findIndexOfPhoneColumnByHeader(List<String> headers) {
        for (int i = 0; i < headers.size(); i++) {
            for (String phonesColumnHeadersRegex : PHONES_COLUMN_HEADERS_REGEX) {
                if (headers.get(i).toLowerCase().matches(phonesColumnHeadersRegex)) {
                    return i;
                }
            }
        }

        return -1;
    }

    protected List<String> readColumnByIndexIgnoreHeader(Map<Integer, List<String>> content, int index) {
        return content.values().stream()
            .skip(1)
            .map(row -> row.get(index))
            .collect(Collectors.toList());
    }

    protected String mapCellValueToString(Cell cell) {
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
