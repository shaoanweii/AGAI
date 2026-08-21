package com.voc.service.analysis.core.v2.utils;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.support.ExcelTypeEnum;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.ss.SpreadsheetVersion;

import java.lang.reflect.Field;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class ExcelUtils {

    public static void excelExportXls(List records, String filename, HttpServletResponse response, Class head) {
        response.setCharacterEncoding("utf-8");
        String fileName;
        // 导出数据前调用
        initCellMaxTextLength();
        try {
            fileName = URLEncoder.encode(filename, StandardCharsets.UTF_8).replaceAll("\\+", "%20");
            response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");
            EasyExcel.write(response.getOutputStream(), head).excelType(ExcelTypeEnum.XLSX).sheet("原始数据结果").doWrite(records);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 初始化 cell 内容长度
     * cell 原本内容长度限制 32767  现修改为Integer.MAX_VALUE
     */
    public static void initCellMaxTextLength() {
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
