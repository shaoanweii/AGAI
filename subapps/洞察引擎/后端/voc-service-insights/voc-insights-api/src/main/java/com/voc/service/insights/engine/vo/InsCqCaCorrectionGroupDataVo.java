package com.voc.service.insights.engine.vo;

import com.alibaba.excel.annotation.write.style.ColumnWidth;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InsCqCaCorrectionGroupDataVo implements Serializable {

    private static final long serialVersionUID = 1L;

    private String fieldName;

    private String groupValue;

    private String countNum;


}
