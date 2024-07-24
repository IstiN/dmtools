package com.github.istin.dmtools.presale.command;

import org.apache.poi.common.usermodel.HyperlinkType;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Hyperlink;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.jxls.command.AbstractCommand;
import org.jxls.common.CellRef;
import org.jxls.common.Context;
import org.jxls.common.Size;
import org.jxls.transform.Transformer;
import org.jxls.transform.poi.PoiTransformer;


public class HyperlinkCommand extends AbstractCommand {

    private String objectName;

    public HyperlinkCommand() {
    }

    @Override
    public String getName() {
        return "hyperlink";
    }

    @Override
    public Size applyAt(CellRef cellRef, Context context) {
        Transformer transformer = this.getTransformer();

        if (!(transformer instanceof PoiTransformer)) {
            throw new IllegalArgumentException("HyperlinkCommand can only be used with PoiTransformer");
        }

        Sheet sheet = ((PoiTransformer) transformer).getWorkbook().getSheet(cellRef.getSheetName());
        Row row = sheet.getRow(cellRef.getRow());

        if (row == null) {
            row = sheet.createRow(cellRef.getRow());
        }

        Cell cell = row.getCell(cellRef.getCol());


        if (cell == null) {
            cell = row.createCell(cellRef.getCol());
        }

        int nextRow = cellRef.getRow() + 1;

        sheet.shiftRows(nextRow, sheet.getLastRowNum(), 1);

        Workbook workbook = cell.getSheet().getWorkbook();
        CreationHelper createHelper = workbook.getCreationHelper();
        Hyperlink hyperlink = createHelper.createHyperlink(HyperlinkType.URL);

        CellStyle linkStyle = workbook.createCellStyle();
        Font hlinkFont = workbook.createFont();
        hlinkFont.setUnderline(Font.U_SINGLE);
        hlinkFont.setColor(IndexedColors.BLUE.getIndex());
        linkStyle.setFont(hlinkFont);

        Object object = context.getVar(objectName);
        String link = "";
        String text = "";
        try {
            link = (String) object.getClass().getDeclaredField("link").get(object);
            text = (String) object.getClass().getDeclaredField("title").get(object);
        } catch (Exception e) {
            e.printStackTrace();
        }

        hyperlink.setAddress(link);
        cell.setCellValue(text);
        cell.setHyperlink(hyperlink);


        return new Size(1, 1);
    }

    public String getObjectName() {
        return objectName;
    }

    public void setObjectName(String objectName) {
        this.objectName = objectName;
    }
}