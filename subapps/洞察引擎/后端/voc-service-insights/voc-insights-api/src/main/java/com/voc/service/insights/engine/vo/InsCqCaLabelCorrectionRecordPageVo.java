package com.voc.service.insights.engine.vo;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ColumnWidth(25)
public class InsCqCaLabelCorrectionRecordPageVo implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "主键ID")
    private String id;

    @Schema(description = "纠错信息")
    private String correctionInfo;

    @Schema(description = "纠错数量")
    private String correctionCount;

    @Schema(description = "发起人")
    private String createUser;

    @Schema(description = "发起时间")
    private String createTime;

    @Schema(description = "审核时间")
    private String auditTime;

    @Schema(description = "审核人")
    private String auditUser;

    @Schema(description = "审核状态")
    private String auditStatus;

    private String auditStatusCode;

}
