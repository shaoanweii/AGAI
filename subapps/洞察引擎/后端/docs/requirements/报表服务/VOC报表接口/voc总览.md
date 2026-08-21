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

## 数据模型
### 业务宽表实体类
  - 声音ID	id
  - 数据唯一标识	data_id
  - 渠道编码	channel_code
  - 渠道名称	channel
  - 品牌编码	brand_code
  - 品牌名称	brand
  - 车系编码	vehicle_series_code
  - 车系名称	vehicle_series
  - 车型编码	vehicle_model_code
  - 车型名称	vehicle_model
  - 数据类型	label_type
  - 情感	sentiment
  - 意图	intention
  - 热词	hot_word
  - 用户旅程	user_journey
  - 关键词	keywords
  - 数据产生时间	data_create_time
  - 数据抓取时间	create_time
  - 经销商ID	dealer_id
  - 经销商编码	dealer_code
  - 经销商全称	dealer_name
  - 经销商所属大区编码	dealer_province_code
  - 经销商所属大区	dealer_province
  - 经销商所在省编码	dealer_province_code
  - 经销商所在省	dealer_province_code
  - 经销商所在市编码	dealer_city_code
  - 经销商所在市	dealer_city
  - 车辆购买日期	vehicle_purchase_date
  - 车辆生产日期	vehicle_production _date
  - 车辆出厂日期	vehicle_factory_felease_date
  - 车辆车架号	vehicle_vin
  - oneId	one_id
  - 客户姓名	cust_name
  - 客户手机号	cust_mobile
  - 客户年龄	cust_age
  - 客户性别	cust_gender
  - 客户常驻省份编码	cust_province_code
  - 客户常驻省份	cust_province
  - 客户常驻市编码	cust_city_code
  - 客户常驻市	cust_city
  - 客户常驻区编码	cust_district_code
  - 客户常驻区	cust_district
  - 客户最高学历	cust_highest_edu
  - 客户月收入	cust_monthly_income
  - 客户最近一次购车时间	cust_last_purchase_date
  - 客户类型	cust_ype
  - 是否车主	is_vehicle_user
  - 原文内容类型	content_type
  - 工单ID	work_order_id
  - 是否主贴	is_main_post
  - 帖子标题	post_title
  - 帖子原文链接	post_original_link
  - 帖子原文详情	post_original_content
  - 问卷类型（问卷类型拥有）	quest_type
  - 问卷题目/内容（问卷类型拥有）	quest_question_content
  - 问卷答案分数（问卷类型拥有）	quest_answer_score
  - 问卷业务类型（问卷类型拥有）	quest_business_type
  - 问卷业务场景（问卷类型拥有）	quest_business_scenario
  - 观点	topic
  - VRT标签编码1级	vtr_tag_first_code
  - VRT标签编码2级	vtr_tag_second_code
  - VRT标签编码3级	vtr_tag_three_code
  - VRT标签1级	vtr_tag_first
  - VRT标签2级	vtr_tag_second
  - VRT标签3级	vtr_tag_three
  - 商品化属性标签编码1级	com_tag_first_code
  - 商品化属性标签编码2级	com_tag_second_code
  - 商品化属性标签编码3级	com_tag_three_code
  - 商品化属性标签1级	com_tag_first
  - 商品化属性标签2级	com_tag_second
  - 商品化属性标签3级	com_tag_three
  - 全领域业务标签编码1级	adb_tag_first_code
  - 全领域业务标签编码2级	adb_tag_second_code
  - 全领域业务标签编码3级	adb_tag_three_code
  - 全领域业务标签1级	adb_tag_first
  - 全领域业务标签2级	adb_tag_second
  - 全领域业务标签3级	adb_tag_three
  - 口碑评价指标编码1级	wom_tag_first_code
  - 口碑评价指标编码2级	wom_tag_second_code
  - 口碑评价指标编码3级	wom_tag_three_code
  - 口碑评价指标1级	wom_tag_first
  - 口碑评价指标2级	wom_tag_second
  - 口碑评价指标3级	wom_tag_three
  - 客户体验指标编码1级	cx_tag_first_code
  - 客户体验指标编码2级	cx_tag_second_code
  - 客户体验指标编码3级	cx_tag_three_code
  - 客户体验指标1级	cx_tag_first
  - 客户体验指标2级	cx_tag_second
  - 客户体验指标3级	cx_tag_three
  - 全旅程客户签编码1级	cj_tag_first_code
  - 全旅程客户签编码2级	cj_tag_second_code
  - 全旅程客户签编码3级	cj_tag_three_code
  - 全旅程客户签1级	cj_tag_first
  - 全旅程客户签2级	cj_tag_second
  - 全旅程客户签3级	cj_tag_three
  - 销售线索编码1级	sl_tag_first_code
  - 销售线索编码2级	sl_tag_second_code
  - 销售线索编码3级	sl_tag_three_code
  - 销售线索1级	sl_tag_first
  - 销售线索2级	sl_tag_second
  - 销售线索3级	sl_tag_three
  - 全媒体指标编码1级	om_tag_first_code
  - 全媒体指标编码2级	om_tag_second_code
  - 全媒体指标编码3级	om_tag_three_code
  - 全媒体指标1级	om_tag_first
  - 全媒体指标2级	om_tag_second
  - 全媒体指标3级	om_tag_three
  - 数据产生周期-周	data_create_week
  - 数据产生周期-月	data_create_month
  - 数据产生周期-季	data_create_quarter
  - 数据产生周期-年	data_create_year

### 查询条件model
- 基础查询Model
  - 时间维度 -1日，0周，1月，2季，3年
  - 开始时间
  - 结束时间
  - 品牌编码	brand_code
  - 情感	sentiment
  - 意图	intention
  - 排序字段	orderBy
  - 排序方式	orderType
  - 页码	pageNum
  - 每页大小	pageSize

- 查询条件Model extend 基础查询Model
  - 指标类型  ????? 数据处理(全旅程客户。。。)
  - 度量值 ????? 需求
  
  - 车系编码	vehicle_series_code
  - 车型编码	vehicle_model_code
  - 渠道编码	channel_code
  - oneId	one_id
  - 声音ID	id
  - 数据唯一标识(原文 id)	data_id
  - 关键词	keywords

  - 全旅程客户标签编码1级	cj_tag_first_code
  - 全旅程客户标签编码2级	cj_tag_second_code
  - 全旅程客户标签编码3级	cj_tag_three_code  

  - 全领域业务标签编码1级	adb_tag_first_code
  - 全领域业务标签编码2级	adb_tag_second_code
  - 全领域业务标签编码3级	adb_tag_three_code

  - 商品化属性标签编码1级	com_tag_first_code
  - 商品化属性标签编码2级	com_tag_second_code
  - 商品化属性标签编码3级	com_tag_three_code

  - VRT标签编码1级	vtr_tag_first_code
  - VRT标签编码2级	vtr_tag_second_code
  - VRT标签编码3级	vtr_tag_three_code


## 接口需求说明

### VOC总览
#### 各品牌数据分析接口
- 数据范围：数据产生时间(data_create_time)范围内业务宽表VocSoundsEntity（voc_sentiment_annotations_results_v）的数据
  - 入参：
    - 公共查询条件（OverviewParamsModel）
  - 回参：
    - 各品牌分析数据集
      - 品牌名称
      - 品牌code
      - 提及量
      - 体验值
      - 负面提及率
- 说明：
  - 各品牌分析数据集中要包含一个是所有品牌的数据，其中所有品牌的名为：长安汽车集团
  
#### 全旅程客户体验值接口
- 数据范围：数据产生时间(data_create_time)范围内业务宽表VocSoundsEntity（voc_sentiment_annotations_results_v）的数据
  - 入参：
    - 公共查询条件（OverviewParamsModel）
  - 回参：
    - 全旅程客户签1级体验值
      - 全旅程客户答1级名称
      - 全旅程客户答1级体验值
- 说明：
  - 各品牌分析数据集中要包含一个是所有品牌的数据，其中所有品牌的名为：长安汽车集团


#### 全旅程top问题接口
- 数据范围：数据产生时间(data_create_time)范围内业务宽表VocSoundsEntity（voc_sentiment_annotations_results_v）的数据
  - 入参：
    - 公共查询条件（OverviewParamsModel）
  - 回参：
    - 标准关键词
    - 情感
    - 提及量
    - 提及量变化
    - 提及量环比
    - 提及率
- 说明：
  - 各品牌分析数据集中要包含一个是所有品牌的数据，其中所有品牌的名为：长安汽车集团
  
#### 客户体验趋势接口
- 数据范围：数据产生时间(data_create_time)范围内业务宽表VocSoundsEntity（voc_sentiment_annotations_results_v）的数据
  - 入参：
    - 公共查询条件（OverviewParamsModel）
  - 回参：
    - 提及量
    - 体验值
    - 日期
    - 正面提及量
    - 中性提及量
    - 负面提及量
- 说明：
  - 各品牌分析数据集中要包含一个是所有品牌的数据，其中所有品牌的名为：长安汽车集团

#### 领域颁接口
- 数据范围：数据产生时间(data_create_time)范围内业务宽表VocSoundsEntity（voc_sentiment_annotations_results_v）的数据
  - 入参：
    - 公共查询条件（OverviewParamsModel）
  - 回参：
    - 正面提及量
    - 正面提及量占比
    - 中性提及量
    - 中性提及量占比
    - 负面提及量
    - 负面提及量占比
    - 产品提及量
    - 产品提及量占比
    - 服务提及量
    - 服务提及量占比
- 说明：
  - 各品牌分析数据集中要包含一个是所有品牌的数据，其中所有品牌的名为：长安汽车集团


#### 专项分析报告查询记录接口
- 数据范围：报告查看记录表 @TableName("report_view_log")
      - 入参：
        - 报告 ID
    - 回参：
        - 报告id
- 说明：
    - 当用户点击专项分析报告时，记录该报告查看记录


在