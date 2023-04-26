package me.shaposhnik.hlrbot.files.readers;

import static me.shaposhnik.hlrbot.files.util.ExcelUtils.findFirstMatchingColumnIndex;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import me.shaposhnik.hlrbot.files.ExcelHeaderRegexProperties;
import me.shaposhnik.hlrbot.files.exception.ReadFileException;
import me.shaposhnik.hlrbot.files.persistence.FileEntity;
import me.shaposhnik.hlrbot.files.util.ExcelUtils;
import me.shaposhnik.hlrbot.model.Phone;
import me.shaposhnik.hlrbot.service.PhoneService;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.beans.factory.annotation.Autowired;


@Slf4j
public abstract class AbstractExcelPhoneFileReader implements PhonesFileReader {

  private static final int FIRST_DOCUMENT_SHEET = 0;
  private static final int HEADER_ROW_INDEX = 0;

  private PhoneService phoneService;
  private ExcelHeaderRegexProperties headerRegexProperties;

  abstract Workbook createWorkbook(File file) throws IOException;

  @Autowired
  public void setPhoneService(PhoneService phoneService) {
    this.phoneService = phoneService;
  }

  @Autowired
  public void setHeaderRegexProperties(ExcelHeaderRegexProperties headerRegexProperties) {
    this.headerRegexProperties = headerRegexProperties;
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
      int indexOfPhoneColumn = findFirstMatchingColumnIndex(sheet.getRow(HEADER_ROW_INDEX),
          headerRegexProperties.getRegexList());

      for (int i = 1; i <= sheet.getLastRowNum(); i++) {
        Optional.ofNullable(sheet.getRow(i))
            .map(row -> row.getCell(indexOfPhoneColumn))
            .map(ExcelUtils::mapCellValueToString)
            .ifPresent(result::add);
      }

      return result;
    } catch (Exception e) {
      log.error("Error while reading excel file: ", e);
      throw new ReadFileException(e);
    }
  }

}
