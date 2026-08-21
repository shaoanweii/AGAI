package com.voc.service.insights.engine.common.util;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.support.ExcelTypeEnum;
import com.alibaba.excel.write.handler.SheetWriteHandler;
import com.alibaba.excel.write.metadata.style.WriteCellStyle;
import com.alibaba.excel.write.style.HorizontalCellStyleStrategy;
import com.alibaba.excel.write.style.column.LongestMatchColumnWidthStyleStrategy;
import com.voc.service.insights.engine.vo.TagLibCategoryVo;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.ss.SpreadsheetVersion;
import org.apache.poi.ss.usermodel.HorizontalAlignment;

import java.io.OutputStream;
import java.lang.reflect.Field;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;

/**
 * Excel工具类:
 * @author Thinkpad
 */
public class ExcelUtil {


    public static void dynamicExcel(HttpServletResponse response, List<List<Object>> data, String fileName, String sheetName,List<List<String>> headList) throws Exception {
        //表头样式
        WriteCellStyle headWriteCellStyle = new WriteCellStyle();
        //设置表头居中对齐
        headWriteCellStyle.setHorizontalAlignment(HorizontalAlignment.CENTER);
        //内容样式
        WriteCellStyle contentWriteCellStyle = new WriteCellStyle();
        //设置内容靠左对齐
        contentWriteCellStyle.setHorizontalAlignment(HorizontalAlignment.LEFT);
        // 导出数据前调用
        initCellMaxTextLength();
        HorizontalCellStyleStrategy horizontalCellStyleStrategy = new HorizontalCellStyleStrategy(headWriteCellStyle, contentWriteCellStyle);
        EasyExcel.write(getOutputStream(fileName, response))
                .excelType(ExcelTypeEnum.XLSX)
                .head(headList)
                .sheet(sheetName)
                .registerWriteHandler(horizontalCellStyleStrategy)
                .needHead(true)
                .doWrite(data);

    }

    /**
     * 导出
     *
     * @param response
     * @param data
     * @param fileName
     * @param sheetName
     * @param clazz
     * @throws Exception
     */
    public static void writeExcel(HttpServletResponse response, List<? extends Object> data, String fileName, String sheetName, Class clazz, Map<Integer, List<String>> map) throws Exception {
        //表头样式
        WriteCellStyle headWriteCellStyle = new WriteCellStyle();
        //设置表头居中对齐
        headWriteCellStyle.setHorizontalAlignment(HorizontalAlignment.CENTER);
        //内容样式
        WriteCellStyle contentWriteCellStyle = new WriteCellStyle();
        //设置内容靠左对齐
        contentWriteCellStyle.setHorizontalAlignment(HorizontalAlignment.LEFT);
        // 导出数据前调用
        initCellMaxTextLength();
        HorizontalCellStyleStrategy horizontalCellStyleStrategy = new HorizontalCellStyleStrategy(headWriteCellStyle, contentWriteCellStyle);
        EasyExcel.write(getOutputStream(fileName, response), clazz)
//                .excelType(ExcelTypeEnum.CSV)
                .excelType(ExcelTypeEnum.XLSX)
                .sheet(sheetName)
                .registerWriteHandler(horizontalCellStyleStrategy)
                .registerWriteHandler(new DropdownListWriteHandler(map))
                .needHead(true)
                .doWrite(data);
    }


    public static void writeExcelRuleTestExcel(HttpServletResponse response, List<? extends Object> data, String fileName, String sheetName, Class clazz, Map<Integer, List<String>> normalDropdownMap, List<TagLibCategoryVo> tagTree, int[] cascadeColumnIndices) throws Exception {
        //表头样式
        WriteCellStyle headWriteCellStyle = new WriteCellStyle();
        //设置表头居中对齐
        headWriteCellStyle.setHorizontalAlignment(HorizontalAlignment.CENTER);
        //内容样式
        WriteCellStyle contentWriteCellStyle = new WriteCellStyle();
        //设置内容靠左对齐
        contentWriteCellStyle.setHorizontalAlignment(HorizontalAlignment.LEFT);
        // 导出数据前调用
        initCellMaxTextLength();
        HorizontalCellStyleStrategy horizontalCellStyleStrategy = new HorizontalCellStyleStrategy(headWriteCellStyle, contentWriteCellStyle);
        EasyExcel.write(getOutputStream(fileName, response), clazz)
//                .excelType(ExcelTypeEnum.CSV)
                .excelType(ExcelTypeEnum.XLSX)
                .sheet(sheetName)
                .registerWriteHandler(horizontalCellStyleStrategy)
                .registerWriteHandler(new CascadingDropdownWriteHandler(normalDropdownMap, tagTree, cascadeColumnIndices))
                .needHead(true)
                .doWrite(data);
    }


    private static OutputStream getOutputStream(String fileName, HttpServletResponse response) throws Exception {
        fileName = URLEncoder.encode(fileName, "UTF-8");
//        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setContentType("application/octet-stream");
//        response.setContentType("application/csv");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Content-Disposition", "attachment;filename=" + fileName);
        return response.getOutputStream();
    }

    private static void initCellMaxTextLength() {
        SpreadsheetVersion excel2007 = SpreadsheetVersion.EXCEL2007;
        if (Integer.MAX_VALUE != excel2007.getMaxTextLength()) {
            Field field;
            try {
                field = excel2007.getClass().getDeclaredField("_maxTextLength");
                field.setAccessible(true);
                field.set(excel2007, Integer.MAX_VALUE);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
