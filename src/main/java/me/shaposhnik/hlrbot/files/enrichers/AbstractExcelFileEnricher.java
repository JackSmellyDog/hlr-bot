package me.shaposhnik.hlrbot.files.enrichers;

import static me.shaposhnik.hlrbot.files.util.ExcelUtils.createStringCell;
import static me.shaposhnik.hlrbot.files.util.ExcelUtils.findFirstMatchingColumnIndex;
import static me.shaposhnik.hlrbot.files.util.ExcelUtils.findLastCellColumn;
import static me.shaposhnik.hlrbot.files.util.ExcelUtils.mapCellValueToString;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import me.shaposhnik.hlrbot.files.ExcelHeaderRegexProperties;
import me.shaposhnik.hlrbot.files.persistence.FileEntity;
import me.shaposhnik.hlrbot.model.Hlr;
import me.shaposhnik.hlrbot.model.enums.Ported;
import me.shaposhnik.hlrbot.model.enums.Roaming;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
public abstract class AbstractExcelFileEnricher implements HlrResultFileEnricher {

  private static final int FIRST_DOCUMENT_SHEET = 0;
  private static final String DASH = "-";

  private ExcelHeaderRegexProperties headerRegexProperties;

  abstract Workbook createWorkbook(File file) throws IOException;

  @Autowired
  public void setHeaderRegexProperties(ExcelHeaderRegexProperties headerRegexProperties) {
    this.headerRegexProperties = headerRegexProperties;
  }

  @Override
  public FileEntity enrich(FileEntity fileEntity, List<Hlr> hlrRowList) {
    try (Workbook workbook = createWorkbook(fileEntity.toFile())) {
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
    int phoneColumn = findFirstMatchingColumnIndex(header, headerRegexProperties.getRegexList());

    createStringCell(header, statusColumn, "Status");
    createStringCell(header, portedColumn, "Ported");
    createStringCell(header, roamingColumn, "Roaming");

    for (int i = 1; i <= sheet.getLastRowNum(); i++) {
      Row row = sheet.getRow(i);

      if (row == null) {
        return;
      }

      String rawPhoneNumber = mapCellValueToString(row.getCell(phoneColumn));

      Hlr hlr = rawNumberToHlrMap.get(rawPhoneNumber);

      final String status = Optional.ofNullable(hlr).map(Hlr::getStatus)
          .map(StringUtils::capitalize).orElse(DASH);
      createStringCell(row, statusColumn, status);

      final String ported = Optional.ofNullable(hlr).map(Hlr::getPorted).map(Ported::toString)
          .orElse(DASH);
      createStringCell(row, portedColumn, ported);

      final String roaming = Optional.ofNullable(hlr).map(Hlr::getRoaming).map(Roaming::toString)
          .orElse(DASH);
      createStringCell(row, roamingColumn, roaming);
    }
  }

  private Map<String, Hlr> buildRawNumberToHlrMap(List<Hlr> hlrRowList) {
    Map<String, Hlr> map = new HashMap<>();

    for (Hlr hlr : hlrRowList) {
      map.put(hlr.getPhone().getRawNumberValue(), hlr);
    }

    return map;
  }

}
