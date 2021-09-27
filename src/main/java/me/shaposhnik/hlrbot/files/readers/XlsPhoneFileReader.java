package me.shaposhnik.hlrbot.files.readers;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Set;

@Component
public class XlsPhoneFileReader extends AbstractExcelPhoneFileReader {

    @Override
    public Set<String> getSupportedFileExtensions() {
        return Set.of("xls");
    }

    @Override
    protected Workbook createWorkbook(File file) throws IOException {
        return new HSSFWorkbook(new FileInputStream(file));
    }
}
