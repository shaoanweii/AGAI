# 重庆长安汽车 VOC Report 报表统计服务

## 服务介绍
重庆长安汽车报表统计服务是VOC Cloud系统的数据分析层，负责客户反馈数据的统计、分析和可视化展示。


## 下钻页接口需求说明

### 下钻接口-趋势分析

#### 数据趋势变化接口

- 数据范围：数据产生时间(data_create_time)范围内业务宽表VocSoundsEntity（voc_sentiment_annotations_results_v）
    - 入参：
        - 公共查询条件（ExtendComQueryModel）
    - 回参：
        - 负面率均值
        - 趋势数据
            - 时间轴
            - 负面率
            - 正面提及量
            - 负面提及量
            - 中性提及量
            - 总提及量
            - 表情类型
- 说明：
    - 根据入参中的开始/结束时间查询data_create_time范围内的adb_tag_first为产品的数据，负面率均值计算逻辑为：负面率均值=当前时间范围内（负面数/总提及量）/
      天数 * 100%
    - 如果输入的开始/结束时间超过30天，则时间轴以月为单位进行展示（近一 12个 月的数据），否则以天为单位进行展示
    - 表情类型按照xxx中的规则配置计算得出（计算规则待定义），如：xxx中的规则配置为：1:正面，2:中性，3:负面，则计算结果为：1:
      正面，2:中性，3:负面

### 下钻接口-车系分析

#### 品牌简报

- 数据范围：数据产生时间(data_create_time)范围内业务宽表VocSoundsEntity（voc_sentiment_annotations_results_v）
    - 入参：
        - 公共查询条件（ExtendComQueryModel）
    - 回参：（数组集合）
        - 品牌名称
        - 品牌Code
        - 提及量
        - 负面率
        - 环比
        - 同比
- 说明：
    - 根据入参中的开始/结束时间查询data_create_time范围内的产品的数据，当品牌为空时，则取所有品牌数据，品牌名称为：长安汽车集团
    - 按照负面率进行排序

#### 车系排行
- 数据范围：数据产生时间(data_create_time)范围内业务宽表VocSoundsEntity（voc_sentiment_annotations_results_v）的数据

  - 入参：
    - 公共查询条件（ExtendComQueryModel）
  - 回参：
      - 车系名称
      - 车系Code
      - 负面率
      - 提及量
      - 环比
      - 同比

- 说明：
    - 显示当前筛选条件下结果数据对应各车系TOP10的数据表现，支持点击右上角切换查看负面率和提及量的排行，默认显示负面率排行。
    - 悬浮任一车系均可显示当前车系对应的标题（车系）、名称、负面率、提及量、环比、同比。

#### 车系列表
- 数据范围：数据产生时间(data_create_time)范围内业务宽表VocSoundsEntity（voc_sentiment_annotations_results_v）的数据
  - 入参：
    - 公共查询条件（ExtendComQueryModel）
  - 回参：
      - 品牌名称
      - 品牌Code
      - 车系名称
      - 车系Code
      - 负面率
      - 提及
          - 提及量
          - 占比
          - 趋势
      - 负面
          - 负面率
          - 趋势
          - 环比
      - 正面
          - 正面率
          - 趋势
          - 环比
      - 中性
          - 中性率
          - 趋势
          - 环比
- 说明：
    - 显示当前筛选条件下结果数据对应所有车系的数据表现，包括提及量、正面率和负面率的数据明细，支持点击表头切换按提及量、提及量环比、负面率、负面率环比、正面率、正面率环比进行列表排序。

### 下钻接口-指标分析

#### 指标排行
- 数据范围：数据产生时间(data_create_time)范围内业务宽表VocSoundsEntity（voc_sentiment_annotations_results_v）的数据
  - 入参：
    - 公共查询条件（ExtendComQueryModel）
  - 回参：
      - 标签名称
      - 标签code
      - 数值（负面率、提及量）
      - 环比
      - 同比

- 说明：
    - 显示当前筛选条件下子级指标的TOP10数据表现，默认按照负面率由高到低依次显示，可通过右上角切换查看提及量TOP10。

#### 指标列表
- 数据范围：数据产生时间(data_create_time)范围内业务宽表VocSoundsEntity（voc_sentiment_annotations_results_v）的数据
  - 入参：
    - 公共查询条件（ExtendComQueryModel）
  - 回参：
      - 标签名称
      - 标签code
      - 提及
          - 提及量
          - 占比
          - 趋势
      - 负面
          - 负面率
          - 趋势
          - 环比
      - 正面
          - 正面率
          - 趋势
          - 环比
      - 中性
          - 中性率
          - 趋势
          - 环比

- 说明：
    - 当前筛选条件下结果数据对应所有子级指标的数据表现，包括提及量、正面率和负面率的数据明细，支持点击表头切换按提及量、提及量环比、负面率、负面率环比、正面率、正面率环比进行列表排序。

### 下钻接口-观点分析

#### 观点列表
- 数据范围：数据产生时间(data_create_time)范围内业务宽表VocSoundsEntity（voc_sentiment_annotations_results_v）的数据
  - 入参：
    - 公共查询条件（ExtendComQueryModel）
  - 回参：
      - 观点
      - 意图
    - 提及量
      - 占比
      - 趋势
      - 环比

- 说明：
    - 提及量、占比、环比可以进行选中排序，默认提及量排序

### 下钻接口-场景分析

#### 场景列表
- 数据范围：数据产生时间(data_create_time)范围内业务宽表VocSoundsEntity（voc_sentiment_annotations_results_v）的数据
  - 入参：
    - 公共查询条件（ExtendComQueryModel）
  - 回参：
      - 场景
    - 提及量
      - 占比
      - 趋势
      - 环比

- 说明：
    - 提及量、占比、环比可以进行选中排序，默认提及量排序

### 下钻接口-数据源分析

#### 渠道发声top

- 数据范围：数据产生时间(data_create_time)范围内业务宽表VocSoundsEntity（voc_sentiment_annotations_results_v）的数据
    - 入参：
        - 公共查询条件（ExtendComQueryModel）
    - 回参：
        - 渠道名称
        - 渠道编码
        - 数值（负面率、提及量）

- 说明：
    - 应数据渠道TOP5的数据表现

#### 观点评价top
- 数据范围：数据产生时间(data_create_time)范围内业务宽表VocSoundsEntity（voc_sentiment_annotations_results_v）的数据
    - 入参：
        - 公共查询条件（ExtendComQueryModel）
    - 回参：
        - 观点
        - 正面
        - 负面
        - 中性
        - 提及量

- 说明：
    - 应数据渠道TOP5的数据表现

#### 数据源列表
- 数据范围：数据产生时间(data_create_time)范围内业务宽表VocSoundsEntity（voc_sentiment_annotations_results_v）的数据
    - 入参
    - 公共查询条件（ExtendComQueryModel）
  - 回参：
      - 渠道名称
      - 渠道编码
      - 提及
          - 提及量
          - 占比
          - 趋势
          - 环比
      - 负面
          - 负面率
          - 趋势
          - 环比
      - 正面
          - 正面率
          - 趋势
          - 环比

- 说明：
    - 提及量、负面率、环比、负面率、环比、正面率、环比可以进行排序，默认提及量排序
