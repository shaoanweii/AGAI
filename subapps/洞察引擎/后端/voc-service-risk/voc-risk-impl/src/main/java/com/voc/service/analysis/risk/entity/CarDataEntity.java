package com.voc.service.analysis.risk.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 车辆相关舆情/数据实体类
 * 对应业务字段：hash_code,app_name_final,brand,car_brand,content,feel_tag,news_author_final,news_posttime,news_title,news_url,report_date,series,create_time,job_nm,batch_dt,process_attention,is_deleted,ds,insert_dt
 */
@Data // 自动生成getter/setter/toString/equals/hashCode
@NoArgsConstructor // 无参构造器
@AllArgsConstructor // 全参构造器
@TableName(value = "voc_imp_hudi_ca_es_bdu_bdu_netopinion_complaint_miaozhen")
public class CarDataEntity implements Serializable {
    private static final long serialVersionUID = 1L; // 序列化版本号，避免反序列化异常

    /**
     * 哈希编码（唯一标识）
     */
    private String hashCode;

    /**
     * 最终应用名称
     */
    private String appNameFinal;

    /**
     * 品牌（通用）
     */
    private String brand;

    /**
     * 汽车品牌
     */
    private String carBrand;

    /**
     * 内容正文
     */
    private String content;

    /**
     * 情感标签（如正面/负面/中性）
     */
    private String feelTag;

    /**
     * 最终新闻作者
     */
    private String newsAuthorFinal;

    /**
     * 新闻发布时间
     */
    private LocalDateTime newsPosttime;

    /**
     * 新闻标题
     */
    private String newsTitle;

    /**
     * 新闻链接
     */
    private String newsUrl;

    /**
     * 报告日期
     */
    private LocalDate reportDate;

    /**
     * 车系（如长安CS75PLUS、逸动等）
     */
    private String series;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 任务名称
     */
    private String jobNm;

    /**
     * 批次日期
     */
    private LocalDate batchDt;

    /**
     * 处理关注度（数值型，如1-5级）
     */
    private Integer processAttention;

    /**
     * 是否删除（0=未删，1=已删）
     */
    private Boolean isDeleted;

    /**
     * 日期分区（格式：yyyyMMdd，用于数仓分区）
     */
    private String ds;

    /**
     * 插入时间
     */
    private LocalDateTime insertDt;

    @TableField(exist = false)
    private String channelCode;

    @TableField(exist = false)
    private String channelName;

    @TableField(exist = false)
    private String secondChannelCode;
}