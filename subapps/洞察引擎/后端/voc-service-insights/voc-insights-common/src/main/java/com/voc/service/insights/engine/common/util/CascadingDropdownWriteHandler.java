package com.voc.service.insights.engine.common.util;

import com.alibaba.excel.write.handler.SheetWriteHandler;
import com.alibaba.excel.write.metadata.holder.WriteSheetHolder;
import com.alibaba.excel.write.metadata.holder.WriteWorkbookHolder;
import com.voc.service.insights.engine.vo.TagLibCategoryVo;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFDataValidation;
import org.springframework.util.CollectionUtils;

import java.util.*;

public class CascadingDropdownWriteHandler implements SheetWriteHandler {

    // 普通下拉框配置: key=列索引, value=下拉选项列表
    private final Map<Integer, List<String>> normalDropdownMap;

    private int index;
    // 级联下拉框配置
    private final List<TagLibCategoryVo> cascadeDataList; // 层级映射
    private final int[] cascadeColumnIndices; // 级联列索引数组

    // 隐藏工作表的名称
    private static final String HIDDEN_SHEET_NAME = "hidden_options";

    public CascadingDropdownWriteHandler(Map<Integer, List<String>> normalDropdownMap,
                                         List<TagLibCategoryVo> tagTree,
                                         int[] cascadeColumnIndices) {
        this.normalDropdownMap = normalDropdownMap;
        this.cascadeDataList = tagTree;
        this.cascadeColumnIndices = cascadeColumnIndices;
    }

    @Override
    public void afterSheetCreate(WriteWorkbookHolder writeWorkbookHolder, WriteSheetHolder writeSheetHolder) {
        Workbook workbook = writeWorkbookHolder.getWorkbook();
        Sheet mainSheet = writeSheetHolder.getSheet();

        // 1. 创建一个隐藏工作表，用于存储所有下拉选项
        Sheet hiddenSheet = workbook.createSheet(HIDDEN_SHEET_NAME);
        workbook.setSheetHidden(workbook.getSheetIndex(hiddenSheet), true);

        // 2. 处理普通下拉框 (将选项写入隐藏表，并设置引用)
        handleNormalDropdowns(writeWorkbookHolder);
    }

    /**
     * 处理普通下拉框（修复：确保选项纵向写入隐藏表，避免横向长度超限+索引更清晰）
     */
    public void handleNormalDropdowns(WriteWorkbookHolder writeWorkbookHolder) {
        if(ObjectUtils.isEmpty(normalDropdownMap)){
            return;
        }

        // 获取一个workbook
        Workbook workbook = writeWorkbookHolder.getWorkbook();
        Sheet sheetAt = workbook.getSheetAt(0);

        // k 为存在下拉数据集的单元格下表 v为下拉数据集
        List<Integer> setSheetHidden = new ArrayList<>();
        normalDropdownMap.forEach((k,v)->{
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
        setSheetHidden.add(this.index+1);
        setSheetHidden.forEach(index-> workbook.setSheetHidden(index, true));
    }

    /**
     * 配置数据验证的通用样式
     */
    private void configureValidation(DataValidation validation) {
        validation.setShowErrorBox(true);
        validation.createErrorBox("输入错误", "请从下拉列表中选择一个选项。");
        // 对于 .xlsx 格式，显示下拉箭头
        if (validation instanceof XSSFDataValidation) {
            validation.setSuppressDropDownArrow(false);
        } else {
            // 对于 .xls 格式，隐藏箭头以避免兼容性问题
            validation.setSuppressDropDownArrow(true);
        }
    }
}