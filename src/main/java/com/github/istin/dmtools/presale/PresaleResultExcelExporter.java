package com.github.istin.dmtools.presale;

import com.github.istin.dmtools.presale.model.Estimation;
import com.github.istin.dmtools.presale.model.StoryEstimation;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class PresaleResultExcelExporter {

    static public void exportToExcel(List<StoryEstimation> estimations, String folderPath) {
             Workbook workbook = new XSSFWorkbook();
             Sheet sheet = workbook.createSheet("Estimations");

             int cellIndex = 1;
             String[] platforms = {"Android", "iOS", "Flutter", "React Native", "Backend"};

             CellStyle centeredStyle = workbook.createCellStyle();
             centeredStyle.setAlignment(HorizontalAlignment.CENTER);
             // platform headers - each platform will have 4 columns merged
             Row platformRow = sheet.createRow(0);

             for (int i = 0; i < platforms.length; i++) {
                 Cell cell = platformRow.createCell(cellIndex + 4 * i);
                 cell.setCellStyle(centeredStyle);
                 cell.setCellValue(platforms[i]);
                 sheet.addMergedRegion(new CellRangeAddress(0, 0, cellIndex + 4 * i, cellIndex + 4 * i + 3));
             }

             // Create headers for each estimation type after platform title
             Row headerRow = sheet.createRow(1);
             headerRow.createCell(0).setCellValue("Story Title");
             String[] estimates = {"Optimistic", "Pessimistic", "Most Likely", "Man hours"};

             for (int i = 0; i < platforms.length; i++) {
                 for (String estimate : estimates) {
                     headerRow.createCell(cellIndex++).setCellValue(estimate);
                 }
             }

             int rowIndex = 2;
             for (StoryEstimation story : estimations) {
                 Row row = sheet.createRow(rowIndex++);
                 cellIndex = 0;

                 row.createCell(cellIndex++).setCellValue(story.title);

                 writeEstimationToRow(row, cellIndex, story.androidEstimation);
                 cellIndex += 4;

                 writeEstimationToRow(row, cellIndex, story.iosEstimation);
                 cellIndex += 4;

                 writeEstimationToRow(row, cellIndex, story.flutterEstimation);
                 cellIndex += 4;

                 writeEstimationToRow(row, cellIndex, story.reactEstimation);
                 cellIndex += 4;

                 writeEstimationToRow(row, cellIndex, story.backendEstimation);
             }

             for (int i = 0; i < cellIndex; i++) {
                 sheet.autoSizeColumn(i);
             }

             // Write the output
             try (FileOutputStream outputStream = new FileOutputStream(folderPath + "/result.xlsx")) {
                 workbook.write(outputStream);
             } catch (IOException e) {
                 e.printStackTrace();
             }



    }

    private static void writeEstimationToRow(Row row, int startIndex, Estimation estimation) {
        row.createCell(startIndex).setCellValue(estimation.optimistic);
        row.createCell(startIndex + 1).setCellValue(estimation.pessimistic);
        row.createCell(startIndex + 2).setCellValue(estimation.mostLikely);

        Cell cell = row.createCell(startIndex + 3);
        cell.setCellFormula(
                String.format("(%s%d + %s%d + 4 * %s%d) / 6",
                        CellReference.convertNumToColString(startIndex), row.getRowNum() + 1,
                        CellReference.convertNumToColString(startIndex + 1), row.getRowNum() + 1,
                        CellReference.convertNumToColString(startIndex + 2), row.getRowNum() + 1)
        );
    }
}
