package com.voc.service.data.integration.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @Title: OrderTypeModel
 * @Package: com.voc.service.data.integration.api
 * @Description:
 * @Author: cuick
 * @Date: 2024/9/24 15:47
 * @Version:1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "voc_ext_ins_channel_mv")
public class ChannelInfoDataEntity implements Serializable {
    String code;
    String name;
}
