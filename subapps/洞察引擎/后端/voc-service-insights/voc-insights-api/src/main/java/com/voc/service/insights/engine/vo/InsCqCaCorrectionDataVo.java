package com.voc.service.insights.engine.vo;

import com.alibaba.excel.annotation.write.style.ColumnWidth;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ColumnWidth(25)
public class InsCqCaCorrectionDataVo implements Serializable {

    private static final long serialVersionUID = 1L;

    private String id;

    private String brandName;

    private String carSeriesName;

    private String originalTextScene;

    private String tagType;

    private String category;

    private String sentiment;

}
