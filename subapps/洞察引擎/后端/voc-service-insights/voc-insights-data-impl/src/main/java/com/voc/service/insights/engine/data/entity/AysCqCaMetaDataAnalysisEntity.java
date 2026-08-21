package com.voc.service.insights.engine.data.entity;

import com.baomidou.mybatisplus.annotation.TableField;
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
@TableName(value = "voc_anal_flow_mate_data_full_v")
public class AysCqCaMetaDataAnalysisEntity implements Serializable {
    private String id; // 主键ID
    private LocalDateTime createTime; // 创建时间（create_time）
    private String contentType; // 内容类型（content_type）
    private LocalDateTime dataCreateTime; // 数据创建时间（data_create_time）
   // private LocalDateTime dataUpdateTime; // 数据更新时间（data_update_time）
    private String dataId; // 数据ID（data_id）
    private String channelCode; // 渠道编码（channel_code）
    private String brand; // 品牌（brand）
    private String series; // 车系（series）
    private String model; // 车型（model）
    private String isOuter; // 是否外部（is_outer）
    private String oneId; // 唯一标识（one_id）
    private String idCarNo; // 车辆识别号/车牌号（id_car_no）
    private String mobile; // 手机号（mobile）
    private String email; // 邮箱（email）
    private String globalId; // 全局ID（global_id）
    private String userId; // 用户ID（user_id）
    private String userName; // 用户名（user_name）
    private String vhlId; // 车辆ID（vhl_id）
    private String vhlVin; // 车辆识别号（vhl_vin）
    private String dlrId; // 经销商ID（dlr_id）
    private String dlrCode; // 经销商编码（dlr_code）
    private String dlrType; // 经销商类型（dlr_type）
    private String marketId; // 市场ID（market_id）
    private String title; // 标题（title）
    private String content; // 内容（content）
    private String isWsaterArmy; // 是否水军（疑似拼写错误，可能为is_water_army）
    private Integer weight; // 权重（weight）
    private Object attrs; // 属性1（attrs，可存储JSON或键值对字符串）
    private Object attrs2; // 属性2（attrs2）
    private Object attrs3; // 属性3（attrs3）
    private String workId; // 工作ID（work_id）
    private Boolean done; // 是否完成（done）
    private String modelType; // 模型类型（model_type）
    private String dataStatus;

    @TableField(exist = false)
    private String isManagerFocused;
    @TableField(exist = false)
    private String isBigV;
    @TableField(exist = false)
    private String authorId;
    @TableField(exist = false)
    private String authorNick;
    @TableField(exist = false)
    private String isMainPost;
    @TableField(exist = false)
    private String url;
    @TableField(exist = false)
    private String viewCount;
    @TableField(exist = false)
    private String commentCount;
    @TableField(exist = false)
    private String likeCount;
    @TableField(exist = false)
    private String shareCount;
    @TableField(exist = false)
    private String favoriteCount;
    @TableField(exist = false)
    private String orderId;
    @TableField(exist = false)
    private String questId;
    @TableField(exist = false)
    private String questType;
    @TableField(exist = false)
    private String questAnswerScore;
    @TableField(exist = false)
    private String questBusinessType;
    @TableField(exist = false)
    private String questBusinessScenario;

}
