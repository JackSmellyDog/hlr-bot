package me.shaposhnik.hlrbot.files.enrichers;

import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Set;

@Component
public class XlsxFileEnricher extends AbstractExcelFileEnricher {

    @Override
    public Set<String> getSupportedFileExtensions() {
        return Set.of("xlsx");
    }

    @Override
    protected Workbook createWorkbook(File file) throws IOException {
        return new XSSFWorkbook(new FileInputStream(file));
    }
}
