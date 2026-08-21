package com.voc.service.insights.engine.data.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 数据集详情(InsDataDesc)表实体类
 *
 * @author leiww
 * @since 2024-02-27 16:48:54
 */
@Data
@TableName("ins_data_source_desc")
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class InsDataSourceDescEntity  implements Serializable {

    /**
     * 主键
     */
    private String newId;
    /**
     * id
     */
    private String id;
    /**
     * 渠道
     */
    private String channelId;
    /**
     * 标题
     */
    private String title;
    /**
     * 内容
     */
    private String content;
    /**
     * 发布时间
     */
    private String publishTime;
    /**
     * 用户id
     */
    private String userId;
    /**
     * 昵称
     */
    private String userName;
    /**
     * 链接
     */
    @TableField("URL")
    private String URL;
    /**
     * 阅读数
     */
    private String readingCount;
    /**
     * 关注数
     */
    private String focusCount;
    /**
     * 评论数
     */
    private String commentsCount;
    /**
     * 点赞数
     */
    private String favorCount;
    /**
     * 收藏数
     */
    private String collectionsCount;
    /**
     * 转发数
     */
    private String redirectionCount;
    /**
     * 总数
     */
    private Long totalNum;
    /**
     * 成功数
     */
    private Long successNum;
    /**
     * 失败数
     */
    private Long failNum;
    /**
     * 数据有效性(0:无效 1：有效)
     */
    private String dataValidity;

    /**
     * 数据名称
     */
    private String dataName;
    /**
     * 批次id
     */
    private String batchId;
    /**
     * 数据源id
     */
    private String dataSourceId;
    /**
     * 状态 未处理：0 处理中：1 已处理：2 处理失败：-1
     */
    @Builder.Default
    private String status = "0";
    /**
     * 文件上传并解析入库时间
     */
    private LocalDateTime createTime;
    @TableField(exist = false)
    private String sumTotal;
    /**
     * 数据链路id
     */
    private String workId;
    /**
     * 省份
     */
    private String province;
    /**
     * 城市
     */
    private String city;
}

