# 汽车 VOC Report 报表统计服务

## 服务介绍

汽车报表统计服务是VOC Cloud系统的数据分析层，负责客户反馈数据的统计、分析和可视化展示。

## 主要功能

- 数据统计：对客户反馈数据进行统计和聚合
- 报表生成：生成各类分析报表和图表
- 数据分析：对客户反馈进行深度分析
- 趋势分析：分析客户反馈的变化趋势

## 技术实现

- 基于Spring Boot框架
- 使用MyBatis-Plus进行数据库交互
- 集成ECharts等图表库进行数据可视化
- 定时任务框架实现数据定时分析

## 接口需求说明

### VOC总览领导层

#### 市场横评-集团简报

- 数据范围：数据产生时间(data_create_time)范围内业务宽表VocSoundsEntity（voc_sentiment_annotations_results_v）的数据
    - 入参：
        - 公共查询条件（ExtendComQueryModel）
    - 回参：
        - 各品牌/车系分析数据集
            - 品牌名称
            - 品牌code
            - 品牌图片
            - 品牌/车系top数据集
                - 排名
                - 排名标识（与时间筛选范围内上一期时间对比：上升/下降）
                - 上一期排名
                - 品牌名称
                - 品牌code
                - 提及量
                - 负面率
                - 环比
                - 同比
- 说明：
    - 各品牌分析数据集中要包含一个是所有品牌的数据，其中所有品牌的名为：长安汽车集团（包含所有品牌数据的集的子菜单为品牌集）
    - 各品牌分析数据集中要包含一个是市场均值数据
    - 只查看公域渠道数据

#### 市场横评-品牌排行

- 数据范围：数据产生时间(data_create_time)范围内业务宽表VocSoundsEntity（voc_sentiment_annotations_results_v）的数据
    - 入参：
        - 公共查询条件（ExtendComQueryModel）
    - 回参：
        - 品牌/车系排行数据集
            - 排行
            - 排名标识（与时间筛选范围内上一期时间对比：上升/下降）
            - 上一期排名
            - 名称
            - code
            - 图片
            - 类型
            - 提及量
            - 提及量环比
            - 提及量占比
            - 提及量同比
            - 负面率
            - 负面率环比
            - 负面率占比
            - 负面率同比
            - 负面率/提及量走势
            - 关注场景top3
                - 场景/标签
                - 负面率/提及量
                - 环比
                - 同比
- 说明：
    - 各品牌分析数据集中要包含一个是所有品牌的数据，其中所有品牌的名为：长安汽车集团
    - 各品牌分析数据集中要包含一个是市场均值数据
    - 使用dataType切换展示品牌/车系

#### 品牌洞察-品牌简报

- 数据范围：数据产生时间(data_create_time)范围内业务宽表VocSoundsEntity（voc_sentiment_annotations_results_v）的数据
    - 入参：
        - 公共查询条件（ExtendComQueryModel）
    - 回参：
        - 各品牌分析数据集
            - 品牌名称
            - 品牌code
            - 提及量
            - 负面率
            - 负面率环比
            - 趋势
- 说明：
    - 各品牌分析数据集中要包含一个是所有品牌的数据，其中所有品牌的名为：长安汽车集团

#### 品牌洞察-产品场景分析

- 数据范围：数据产生时间(data_create_time)范围内业务宽表VocSoundsEntity（voc_sentiment_annotations_results_v）的数据
    - 入参：
        - 公共查询条件（ExtendComQueryModel）
    - 回参：
        - 标签名称
        - 标签code
        - 提及量
        - 负面率
        - 环比
        - 正面提及量
        - 中性提及量
        - 负面提及量
- 说明：产品类二级体验代码
-

#### 品牌洞察-服务场景分析

- 数据范围：数据产生时间(data_create_time)范围内业务宽表VocSoundsEntity（voc_sentiment_annotations_results_v）的数据
    - 入参：
        - 公共查询条件（ExtendComQueryModel）
    - 回参：
        - 标签名称
        - 标签code
        - 提及量
        - 负面率
        - 环比
        - 正面提及量
        - 中性提及量
        - 负面提及量
- 说明：服务类二级体验代码

#### 品牌洞察=-用户意图观点 TOP接口

- 数据范围：数据产生时间(data_create_time)范围内业务宽表VocSoundsEntity（voc_sentiment_annotations_results_v） 的adb_tag_first为产品的数据
    - 入参：
        - 公共查询条件（ExtendComQueryModel）
    - 回参：
        - 抱怨观点（数组集合top5）
            - 观点名（opinion）
            - 提及量
            - 提及量环比
            - 提及量同比
        - 咨询观点（数组集合top5）
            - 观点名
            - 提及量
            - 提及量环比
            - 提及量同比
        - 建议观点（数组集合top5）
            - 观点名
            - 提及量
            - 提及量环比
            - 提及量同比
        - 表扬观点（数组集合top5）
            - 观点名
            - 提及量
            - 提及量环比
            - 提及量同比
- 说明：
    - 根据入参中的开始/结束时间查询data_create_time范围内的adb_tag_first为产品的数据，按照以下几种意图
        - 抱怨
        - 咨询
        - 建议
        - 表扬
    - 按照提及量进行排序，取top5的数据

