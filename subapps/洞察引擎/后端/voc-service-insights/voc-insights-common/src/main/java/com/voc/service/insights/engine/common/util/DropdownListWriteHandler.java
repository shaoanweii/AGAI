package com.voc.service.insights.engine.common.util;

import com.alibaba.excel.write.handler.SheetWriteHandler;
import com.alibaba.excel.write.metadata.holder.WriteSheetHolder;
import com.alibaba.excel.write.metadata.holder.WriteWorkbookHolder;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.xssf.usermodel.XSSFDataValidation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @创建者: fanrong
 * @创建时间: 2023/6/8 13:39
 * @描述:
 **/
public class DropdownListWriteHandler implements SheetWriteHandler {

    private Map<Integer, List<String>> map;

    private int index;


    public DropdownListWriteHandler(Map<Integer,List<String>> map) {
        this.map = map;
        this.index = 0;
    }

    @Override
    public void afterSheetCreate(WriteWorkbookHolder writeWorkbookHolder, WriteSheetHolder writeSheetHolder) {
//        SheetWriteHandler.super.afterSheetCreate(writeWorkbookHolder, writeSheetHolder);
        if(ObjectUtils.isEmpty(map)){
            return;
        }

        // 获取一个workbook
        Workbook workbook = writeWorkbookHolder.getWorkbook();
        Sheet sheetAt = workbook.getSheetAt(0);

        // k 为存在下拉数据集的单元格下表 v为下拉数据集
        List<Integer> setSheetHidden = new ArrayList<>();
        map.forEach((k,v)->{
            // 1.创建一个隐藏的sheet
            Sheet hiddenSheet = workbook.createSheet();
            // 从第二个工作簿开始隐藏
            this.index++;
            setSheetHidden.add(this.index);
            for (int i = 0; i < v.size(); i++) {
                hiddenSheet.createRow(i).createCell(0).setCellValue(v.get(i));
            }
            Name category1Name = workbook.createName();
            String uniqueName = "hidden_options_for_col_" + k;
            category1Name.setNameName(uniqueName);
            // 4 $A$1:$A$N代表 以A列1行开始获取N行下拉数据
            category1Name.setRefersToFormula(hiddenSheet.getSheetName() + "!$A$1:$A$"+v.size());
            DataValidationHelper dataValidationHelper = sheetAt.getDataValidationHelper();
            String refersToFormula = category1Name.getRefersToFormula();
            DataValidationConstraint formulaListConstraint = dataValidationHelper.createFormulaListConstraint(refersToFormula);
            // 5 将刚才设置的sheet引用到你的下拉列表中
            CellRangeAddressList addressList = new CellRangeAddressList(0, 65535, k, k);
            DataValidation validation = dataValidationHelper.createValidation(formulaListConstraint, addressList);

            // 处理Excel兼容性问题
            if (validation instanceof XSSFDataValidation) {
                validation.setSuppressDropDownArrow(true);
            } else {
                validation.setSuppressDropDownArrow(false);
            }
            validation.setShowErrorBox(true);
            validation.createErrorBox("提示","请选择下拉框存在的选项");
            validation.setShowErrorBox(true);
            sheetAt.addValidationData(validation);
        });
//         设置隐藏sheet
        setSheetHidden.forEach(index-> workbook.setSheetHidden(index, true));
    }
}
