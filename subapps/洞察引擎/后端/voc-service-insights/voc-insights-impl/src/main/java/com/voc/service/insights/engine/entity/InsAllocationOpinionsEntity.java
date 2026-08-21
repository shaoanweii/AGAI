package com.voc.service.insights.engine.entity;

import com.baomidou.mybatisplus.annotation.TableName;
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
@TableName("cli_allocation_opinions_record")
public class InsAllocationOpinionsEntity implements Serializable {
    /**
     * 主键     primary key
     */
    private String id;

    private String tagId;

    private String tagType;

    private String tagCategory;

    private String opinionsId;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
