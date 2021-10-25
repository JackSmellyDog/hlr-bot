package me.shaposhnik.hlrbot.files.util;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

import java.math.BigDecimal;
import java.util.Collection;

@Slf4j
@UtilityClass
public class ExcelUtils {

    public static String mapCellValueToString(Cell cell) {
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

    public static int findLastCellColumn(Sheet sheet) {
        int lastCellColumn = 0;

        for (Row row : sheet) {
            if (lastCellColumn < row.getLastCellNum()) {
                lastCellColumn = row.getLastCellNum();
            }
        }

        return lastCellColumn;
    }

    public int findFirstMatchingColumnIndex(Row row, Collection<String> values) {
        for (int i = 0; i < row.getLastCellNum(); i++) {
            for (String phonesColumnHeadersRegex : values) {

                final String header = mapCellValueToString(row.getCell(i)).toLowerCase();
                if (header.matches(phonesColumnHeadersRegex)) {
                    return i;
                }
            }
        }

        return -1;
    }

    public static void createStringCell(Row row, int index, String value) {
        var cell = row.createCell(index, CellType.STRING);
        cell.setCellValue(value);
    }

}
