package com.voc.service.analysis.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.io.Serializable;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"data"})
public class AiBatchPushModel implements Serializable {

    @NonNull
    private String requestId;

    @NonNull
    @Schema(description = "客户标识")
    private String clientId;
    /**
     * ModelTypeEnum.java
     * 大模型离线/大模型实时/小模型
     */
    @NonNull
    @Schema(description = "模型类型")
    private String modelType;


    @Schema(description = "1本地上传 2系统集成")
    private Integer showType;

    @Schema(description = "数据源标识")
    private String dataSource;
    /**
     * 批次数量总和
     */
    @Schema(description = "批次数量总和")
    private Integer total;

    /**
     * 本次提交批次数据总数
     */
    @Schema(description = "本次提交批次数据总数")
    private Integer currentBatchTotal;
    /**
     * 批次次数
     */
    @Schema(description = "批次次数")
    private Integer batchPageTotal;

    /**
     * 本次提交批次号
     */
    @Schema(description = "本次提交批次号")
    private Integer currentBatchPage;


    /**
     * 必填项: channelId、type[RuleContentType.java]、content，publish_time
     *
     * 样例数据：
     *
     * [
     *         {
     *             "batch_id": "343c6676999f91c044d879058176e655",
     *             "user_name": "",
     *             "title": "坦克300的劲敌，不到16万起，标配2.0T，能越野能家用，北京BJ40",
     *             "content": "哈佛H2想去4S店升级这次的油电双降，但是蓄电池一直电量低，没办法升级，该怎么解决？难道非的用油去给蓄电池充电",
     *             "data_name": "测试数据源详情",
     *             "publish_time": "2024-01-06 00:00:00",
     *             "redirection_count": "",
     *             "total_num": "",
     *             "fail_num": "",
     *             "id": "CSSJ2024134441",
     *             "data_source_id": "1802596889330225154",
     *             "focus_count": "",
     *             "new_id": "cb2c883405b3189ba5a2396a269dd1fc",
     *             "work_id": "",
     *             "reading_count": "",
     *             "favor_count": "",
     *             "create_time": "2024-06-18T17:44:20",
     *             "success_num": "",
     *             "uRL": "",
     *             "sum_total": "",
     *             "collections_count": "",
     *             "user_id": "",
     *             "comments_count": "",
     *             "channelId": "1548fcc6983755e682d7addcc1641cfe",
     *             "type": "text",
     *             "biz_ext_fileds": {"ctiy":"北京","biz_channel":"APP"},
     *             "status": "0"
     *         }
     *     ]
     */
    @Schema(description = "请求数据")
    private List<Object> data;




}
