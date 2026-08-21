package com.voc.service.insights.engine.vo;

import com.alibaba.excel.annotation.write.style.ColumnWidth;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ColumnWidth(25)
public class InsCqCaCorrectionInfoVo implements Serializable {

    private static final long serialVersionUID = 1L;


    @Schema(description = "纠错明细")
    private  List<CorrectionInfo> correctionInfoList;

    @Schema(description = "错误类型")
    private String  errorType;

    @Schema(description = "提交人姓名")
    private String submitUserName;

    @Schema(description = "提交人工号")
    private String submitUserEmployeeID;

    @Schema(description = "提交时间")
    private String submitTime;

    @Schema(description = "审核人姓名")
    private String auditUserName;

    @Schema(description = "审核人工号")
    private String auditUserEmployeeID;

    @Schema(description = "审核时间")
    private String auditTime;




}
