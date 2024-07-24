package com.github.istin.dmtools.presale;

import com.github.istin.dmtools.presale.command.HyperlinkCommand;
import com.github.istin.dmtools.presale.model.Estimation;
import com.github.istin.dmtools.presale.model.StoryEstimation;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.CellValue;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jxls.builder.xls.XlsCommentAreaBuilder;
import org.jxls.common.Context;
import org.jxls.util.JxlsHelper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class PresaleResultExcelExporter {

    static public void exportToExcelMobileTemplate(List<StoryEstimation> stories, String folderPath) throws Exception {
        Map<String, List<Estimation>> groupedEstimations = new HashMap<>();

        for (StoryEstimation story : stories) {
            story.estimations.forEach((key, value) -> {
                // Check if the map already contains this platform
                if (!groupedEstimations.containsKey(key)) {
                    // If not, add the platform with a new list
                    groupedEstimations.put(key, new ArrayList<>());
                }

                // Add the estimation to the list for this platform
                groupedEstimations.get(key).add(value);
            });
        }

        try (InputStream is = PreSaleSupport.class.getResourceAsStream("/ftl/template/mcc_template.xlsx")) {
            try (OutputStream os = Files.newOutputStream(Paths.get(folderPath + "/output.xlsx"))) {

                Context context = new Context();
                context.putVar("stories", stories);
                context.putVar("backendEstimations", groupedEstimations.get("Backend"));
                context.putVar("flutterEstimations", groupedEstimations.get("Flutter"));
                context.putVar("reactEstimations", groupedEstimations.get("React Native"));
                context.putVar("androidEstimations", groupedEstimations.get("Android Native"));
                context.putVar("iosEstimations", groupedEstimations.get("iOS Native"));

                XlsCommentAreaBuilder.addCommandMapping("hyperlink", HyperlinkCommand.class);
                JxlsHelper instance = JxlsHelper.getInstance();
                instance.processTemplate(is, os, context);
            }
        }
    }

    static public void exportToExcel(List<StoryEstimation> stories, String folderPath) throws Exception {
        Map<String, List<Estimation>> groupedEstimations = new HashMap<>();

        for (StoryEstimation story : stories) {
            story.estimations.forEach((key, value) -> {
                // Check if the map already contains this platform
                if (!groupedEstimations.containsKey(key)) {
                    // If not, add the platform with a new list
                    groupedEstimations.put(key, new ArrayList<>());
                }

                // Add the estimation to the list for this platform
                groupedEstimations.get(key).add(value);
            });
        }

        groupedEstimations.forEach((k, v) -> writeEstimation(k, v, folderPath));
        writeInitialStoryData(stories, folderPath);
        writeEstimationToStory(groupedEstimations.keySet(), folderPath);
        groupedEstimations.forEach((key, value) -> {
            new File(folderPath + "/output" + key + ".xlsx").delete();
        });
    }

    private static void writeInitialStoryData(List<StoryEstimation> stories, String path) throws Exception {
        try (InputStream is = PreSaleSupport.class.getResourceAsStream("/ftl/template/presale_story_template.xlsx")) {
            try (OutputStream os = Files.newOutputStream(Paths.get(path + "/output.xlsx"))) {
                Context context = new Context();
                context.putVar("stories", stories);
                JxlsHelper.getInstance().processTemplate(is, os, context);
            }
        }
    }


    private static void writeEstimation(String platform, List<Estimation> estimations, String path) {
        try (InputStream is = PreSaleSupport.class.getResourceAsStream("/ftl/template/estimation_template.xlsx")) {
            try (OutputStream os = Files.newOutputStream(Paths.get(path + "/output" + platform + ".xlsx"))) {
                Context context = new Context();
                context.putVar("estimations", estimations);
                context.putVar("platform", platform);
                JxlsHelper.getInstance().processTemplate(is, os, context);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void writeEstimationToStory(Set<String> platforms, String path) throws Exception {
        int columnOffset = 1;

        try (FileInputStream fis2 = new FileInputStream(path + "/output.xlsx")) {
            XSSFWorkbook destinationWorkbook = new XSSFWorkbook(fis2);
            XSSFSheet destinationSheet = destinationWorkbook.getSheetAt(0); // destination workbook sheet


            int shift = 0;

            for (String platform : platforms) {
                shift = 0;

                try (FileInputStream fis = new FileInputStream(path + "/output" + platform + ".xlsx")) {
                    XSSFWorkbook sourceWorkbook = new XSSFWorkbook(fis);
                    XSSFSheet sourceSheet = sourceWorkbook.getSheetAt(0);  // source workbook sheet

                    FormulaEvaluator evaluator = sourceWorkbook.getCreationHelper().createFormulaEvaluator();

                    // Open the destination workbook and get the sheet
                    shift = sourceSheet.getPhysicalNumberOfRows();

                    for (int i = 0; i < sourceSheet.getPhysicalNumberOfRows(); i++) {
                        XSSFRow sourceRow = sourceSheet.getRow(i);

                        XSSFRow destinationRow;

                        if (destinationSheet.getRow(i) != null) {
                            destinationRow = destinationSheet.getRow(i);
                        } else {
                            destinationRow = destinationSheet.createRow(i);
                        }

                        if (sourceRow != null) {
                            for (int j = 0; j < sourceRow.getPhysicalNumberOfCells(); j++) {
                                XSSFCell sourceCell = sourceRow.getCell(j);
                                XSSFCell destinationCell = destinationRow.createCell(columnOffset + j);

                                if (sourceCell != null) {
                                    switch (sourceCell.getCellType()) {
                                        case STRING:
                                            destinationCell.setCellValue(sourceCell.getRichStringCellValue());
                                            break;
                                        case NUMERIC:
                                            destinationCell.setCellValue(sourceCell.getNumericCellValue());
                                            break;
                                        case FORMULA:
                                            CellValue cellValue = evaluator.evaluate(sourceCell);
                                            destinationCell.setCellValue(cellValue.getNumberValue());
                                            break;
                                        case BOOLEAN:
                                            destinationCell.setCellValue(sourceCell.getBooleanCellValue());
                                            break;
                                        default:
                                            break;
                                    }
                                    CellStyle style = destinationWorkbook.createCellStyle();
                                    style.cloneStyleFrom(sourceCell.getCellStyle());
                                    destinationCell.setCellStyle(style);
                                }
                            }
                        }
                    }

                    if (sourceSheet.getPhysicalNumberOfRows() > 1) {
                        XSSFRow sourceRow = sourceSheet.getRow(1);

                        if (sourceRow != null) {
                            shift = sourceRow.getPhysicalNumberOfCells();
                        }
                    }
                    // Save the destination workbook

                }
                columnOffset += shift;
            }

            Row firstRow = destinationSheet.getRow(0);

            int index = 0;

            for (Cell ignored : firstRow) {
                destinationSheet.autoSizeColumn(index);
                index++;
            }

            for (Cell cell : firstRow) {
                if (cell.getCellType() == CellType.STRING && !cell.getStringCellValue().trim().equals("")) {
                    CellRangeAddress region = new CellRangeAddress(0, 0, cell.getColumnIndex(), cell.getColumnIndex() + shift - 1);
                    destinationSheet.addMergedRegion(region);
                }
            }

            try (FileOutputStream fos = new FileOutputStream(path + "/output.xlsx")) {
                destinationWorkbook.write(fos);
            }
        }
    }
}
