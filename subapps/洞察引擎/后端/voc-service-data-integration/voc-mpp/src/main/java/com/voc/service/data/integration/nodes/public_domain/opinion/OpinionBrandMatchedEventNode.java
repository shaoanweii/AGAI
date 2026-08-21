package com.voc.service.data.integration.nodes.public_domain.opinion;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.voc.service.data.integration.api.model.ChannelMetaDataModel;
import com.voc.service.data.integration.api.model.DataIntegrationRecordModel;
import com.voc.service.data.integration.config.PublicDomainConfig;
import com.voc.service.data.integration.enums.ErrorDataMsgEnums;
import com.voc.service.data.integration.nodes.public_domain.context.PublicDomainDatasetContext;
import com.voc.service.data.integration.services.TextProcessorService;
import com.yomahub.liteflow.annotation.LiteflowComponent;
import com.yomahub.liteflow.core.NodeComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * @Title: LoadChannelDatasetNode
 * @Package: com.voc.service.data.integration.nodes
 * @Description:
 * @Author: cuick
 * @Date: 2024/9/24 17:14
 * @Version:1.0
 */
@LiteflowComponent(id = "opinionBrandMatchedEventNode", name = "处理品牌车系数据-投诉类")
public class OpinionBrandMatchedEventNode extends NodeComponent {
    public static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss", Locale.CHINA);
    private static final Logger log = LoggerFactory.getLogger(OpinionBrandMatchedEventNode.class);

    @Autowired
    TextProcessorService textProcessor;
    @Autowired
    PublicDomainConfig config;

    // 预编译正则表达式模式
    private static final Pattern CZW_TS_PATTERN = Pattern.compile("投诉品牌[:：\\s]*([^\\n\\r]+)");
    private static final Pattern HMTS_TS_PATTERN = Pattern.compile("汽车品牌[:：\\s]*([^\\n\\r]+)");
    private static final Pattern QCTSW_TS_PATTERN = Pattern.compile("投诉品牌[:：\\s]*([^\\n\\r]+)");
    private static final Pattern QCZHW_TS_PATTERN = Pattern.compile("品牌[:：\\s]*([^\\n\\r]+)");
    private static final Pattern ZGQCZLW_TS_PATTERN = Pattern.compile("生产厂商[:：\\s]*([^\\n\\r]+)");
    private static final Pattern ZGQCW_PATTERN = Pattern.compile("投诉品牌[:：\\s]*([^\\n\\r]+)");


    // 预编译正则表达式模式
    private static final Pattern CZW_TS_SERIES_PATTERN = Pattern.compile("投诉车系[:：\\s]*([^\\n\\r]+)");
    private static final Pattern HMTS_SERIES_PATTERN = Pattern.compile("车系[:：\\s]*([^\\n\\r]+)");
    private static final Pattern QCTSW_MODEL_PATTERN = Pattern.compile("投诉车型[:：\\s]*([^\\n\\r]+)");
    private static final Pattern QCTSW_SERIES_PATTERN = Pattern.compile("^([^0-9]*(?:20[0-9]{2}[^0-9]*)*)");
    private static final Pattern QCZHW_SERIES_PATTERN = Pattern.compile("投诉车系[:：\\s]*([^\\n\\r]+)");
    private static final Pattern ZGQCZLW_SERIES_PATTERN = Pattern.compile("投诉车系[:：\\s]*([^\\n\\r]+)");
    private static final Pattern ZGQCW_SERIES_PATTERN = Pattern.compile("投诉车系[:：\\s]*([^\\n\\r]+)");

    // 预编译正则表达式模式
    private static final Pattern CZW_TS_MODEL_PATTERN = Pattern.compile("投诉车型[:：\\s]*([^\\n\\r]+)");
    private static final Pattern HMTS_MODEL_PATTERN = Pattern.compile("车型[:：\\s]*([^\\n\\r]+)");
    //    private static final Pattern QCTSW_MODEL_PATTERN = Pattern.compile("投诉车型[:：\\s]*([^\\n\\r]+)");
    private static final Pattern QCTSW_YEAR_MODEL_PATTERN = Pattern.compile("(20[0-9]{2}.*)");
    private static final Pattern QCZHW_MODEL_PATTERN = Pattern.compile("车型[:：\\s]*([^\\n\\r]+)");
    private static final Pattern ZGQCZLW_MODEL_PATTERN = Pattern.compile("车型[:：\\s]*([^\\n\\r]+)");
    private static final Pattern ZGQCW_MODEL_PATTERN = Pattern.compile("车型[:：\\s]*([^\\n\\r]+)");


    @Override
    public void process() throws Exception {
        PublicDomainDatasetContext context = this.getRequestData();
        try {
            if (CollUtil.isEmpty(context.getSuccessfulDataset())) {
                throw new Exception("【".concat(context.getChannelType()).concat("】数据验证后无成功数据："));
            } else {
                //保存记录成功数据集
                List<DataIntegrationRecordModel> list = context.getSuccessfulDataset();

                list = this.cleanAndNormalizeRawData(context, list);
//                this.pushSuccessfulData(model.getId(), context.getClientId(), context.getChannelType(), list);
                context.setSuccessfulDataset(list);
            }
        } catch (Exception e) {
            log.error("【{}】推送数据错误信息：{}", context.getWorkId(), e.getMessage());
            //出现服务异常时，将所有数据归为异常处理数据集
            final List<DataIntegrationRecordModel> errorList = context.getSuccessfulDataset().stream().map(data -> {
                data.setErrorCode(ErrorDataMsgEnums.PushServiceHasFailed.getCode());
                data.setErrorMsg(ErrorDataMsgEnums.PushServiceHasFailed.getText());
                return data;
            }).toList();
            context.getFailedDataset().addAll(errorList);
            context.setSuccessfulDataset(null);
//            throw new Exception(e.getMessage(), e);
        }
    }

    /**
     * 数据分类处理流程
     */
    public List<DataIntegrationRecordModel> cleanAndNormalizeRawData(PublicDomainDatasetContext context, List<DataIntegrationRecordModel> list) {

        // 1. 初始化引擎（使用默认选项）
        list.stream()
                .filter(Objects::nonNull)
                .filter(model -> Objects.nonNull(model.getData()))
                .forEach(model -> {
                    try {
                        ChannelMetaDataModel data = JSONUtil.toBean(String.valueOf(model.getData()), ChannelMetaDataModel.class);
                        if (Stream.of("opinion", "quest").noneMatch(s -> s.equalsIgnoreCase(data.getContentType()))) {
//                        if (!"opinion".equalsIgnoreCase(data.getContentType())) {
//                            log.error("【{}】数据分类处理错误：contentType is null -> {}", model.getId(), model.getData());
                            return;
                        }
                        if (Objects.equals(data.getIsDeleted(), 1)) {
                            log.debug("【{}】{}数据已删除：{}", context.getWorkId(), model.getId(), model.getData());
                            return;
                        }
                        log.debug("【{}】{}数据分类处理：{}", context.getWorkId(), model.getId(), model.getData());

                        // 数据转换
                        final JSONObject attrs = JSONUtil.isTypeJSONObject(String.valueOf(data.getAttrs()))
                                ? JSONUtil.parseObj(data.getAttrs()) : new JSONObject();

                        // 车系
                        final String series_cleaned = this.extractSeriesByChannel(context.getWorkId(), data.getChannelCode(), attrs);
                        data.setSeries(series_cleaned);
                        log.debug("【{}】{}渠道车系：{}", context.getWorkId(), model.getId(), series_cleaned);

                        //车型
                        final String model_cleaned = this.extractModelByChannel(context.getWorkId(), data.getChannelCode(), attrs);
                        data.setModel(model_cleaned);
                        log.debug("【{}】{}渠道车型：{}", context.getWorkId(), model.getId(), model_cleaned);

                        //内容， 针对特定渠道特殊 处理
                        if (StrUtil.isBlank(data.getContent())) {
                            final String maskContentVal = this.processContentAndMaskPhone(context.getWorkId(), attrs);
                            final String content = StrUtil.isBlank(maskContentVal) ? null : maskContentVal.replaceAll("(?i)^null$|^null$", "");

                            final String content_cleaned = this.extractContentByChannel(data.getChannelCode(), attrs, content);
                            data.setContent(content_cleaned);
                        }

                        model.setId(data.getId());
                        model.setDataId(data.getDataId());
                        model.setData(JSONUtil.toJsonStr(data));

                    } catch (Exception e) {
                        log.error("【{}】数据分类处理错误：{}", context.getWorkId(), model);
                        log.error(e.getMessage(), e);
                        model.setErrorCode(ErrorDataMsgEnums.FailedToParseOriginalData.getCode());
                    }
                });

        log.info("【{}】数据分类处理结果：{}", context.getWorkId(), list.size());
        return list;
    }

    public String processContentAndMaskPhone(String workId, JSONObject dataObj) {
        Object contentValObj = dataObj.getByPath("status");
        if (ObjectUtil.isEmpty(contentValObj)) {
            log.error("【{}】数据格式错误，未获取到 status 值：{}", workId, contentValObj);
            return null;
        }

        final List<JSONObject> status = JSONUtil.toList(JSONUtil.parseArray(contentValObj), JSONObject.class);

        Object contentVal = status.stream()
                .filter(Objects::nonNull)
                .map(item -> item.getByPath("content"))
                .findFirst().get();
        if (ObjectUtil.isEmpty(contentVal)) {
            log.error("【{}】数据格式错误，未获取到 content 值：{}", workId, contentValObj);
            return null;
        }

        String selectedText = String.valueOf(contentVal).replaceAll("(?i)^null$|^null$", "");
        log.debug("处理前的文本：{}", selectedText);
        selectedText = textProcessor.maskPhoneNumbers(selectedText);
        selectedText = textProcessor.maskIdCard(selectedText);
        selectedText = textProcessor.maskVIN(selectedText);
        selectedText = textProcessor.maskLicensePlate(selectedText);
        log.debug("处理后的文本：{}", selectedText);

        // 使用正则表达式替换手机号
        return selectedText;
    }

    /**
     * 根据渠道代码提取品牌信息
     *
     * @return 提取的品牌信息，未匹配到则返回 null
     */
    /*public String extractBrandByChannel(JSONObject data) {
        data
                opinions.model
    }*/


    /**
     * //pd_post_dcd_kbms\pd_post_dcd_kb 懂车帝口碑: "opinions"分段下 取"model"字段
     * //pd_post_qczj_kbms\pd_post_qczj_kb 汽车之家 口碑: "opinions"分段下 取"model"字段
     * //pd_post_yc_kbms\pd_post_yc_kb 易车 口碑 "opinions"分段下//取"series"字段
     * //pd_post_czw_kb 车质网 口碑:  "opinions"分段下取"series"字段
     */
    public String extractSeriesByChannel(String workId, String channelCode, JSONObject attrs) {

        /*final Object val = switch (channelCode) {
            case "pd_post_dcd_kbms", "pd_post_dcd_kb" -> attrs.getByPath("opinions.model");
            case "pd_post_qczj_kbms", "pd_post_qczj_kb" -> attrs.getByPath("opinions.model");
            case "pd_post_yc_kbms", "pd_post_yc_kb" -> attrs.getByPath("opinions.series");
            case "pd_post_czw_kb" -> attrs.getByPath("opinions.series");
            default -> null;
        };

        return ObjectUtil.isEmpty(val) ? null : String.valueOf(val);*/
        return config.getOpinionSeriesMappingList().stream()
                .filter(item -> Objects.equals(item.getChannelCode(), channelCode))
                .findFirst()
                .map(item -> {
                    if ("#list_last_value".equalsIgnoreCase(item.getVal())) {
                        if (JSONUtil.isTypeJSONArray(attrs.getStr(item.getPath()))) {
                            final List<String> valList = JSONUtil.toList(JSONUtil.parseArray(attrs.get(item.getPath())), String.class);
                            //返回valList集合最后一条数据
                            return valList.get(valList.size() - 1);
                        }
                    }

                    return attrs.getByPath(item.getPath());
                })
                .map(String::valueOf)
                .orElse(null);
    }


    /**
     * //pd_post_dcd_kbms\pd_post_dcd_kb 懂车帝口碑:    暂无
     * //pd_post_qczj_kbms\pd_post_qczj_kb 汽车之家 口碑: "breadcrump"字段下 最后一个文本值
     * //pd_post_yc_kbms\pd_post_yc_kb 易车 口碑 :  "opinions"分段下取"model"字段
     * //pd_post_czw_kb 车质网 口碑:  "opinions"分段下 取"model"字段
     */
    public String extractModelByChannel(String workId, String channelCode, JSONObject attrs) {
        return config.getOpinionModelMappingList().stream()
                .filter(item -> Objects.equals(item.getChannelCode(), channelCode))
                .findFirst()
                .map(item -> {
                    if ("#list_last_value".equalsIgnoreCase(item.getVal())) {
                        if (JSONUtil.isTypeJSONArray(attrs.getStr(item.getPath()))) {
                            final List<String> valList = JSONUtil.toList(JSONUtil.parseArray(attrs.get(item.getPath())), String.class);
                            //返回valList集合最后一条数据
                            return valList.get(valList.size() - 1);
                        }
                    }

                    return attrs.getByPath(item.getPath());
                })
                .map(String::valueOf)
                .orElse(null);

        /*final Object val = switch (channelCode) {
//            case "pd_post_dcd_kbms" -> null;
//            case "pd_post_dcd_kb" ->  null;
            case "pd_post_qczj_kbms", "pd_post_qczj_kb" -> {
                if (JSONUtil.isTypeJSONArray("breadcrump")) {
                    final List<String> valList = JSONUtil.toList(JSONUtil.parseArray(attrs.get("breadcrump")), String.class);
                    //返回valList集合最后一条数据
                    yield valList.get(valList.size() - 1);
                }
                yield null;
            }
            case "pd_post_yc_kbms", "pd_post_yc_kb" -> attrs.getByPath("opinions.model");
            case "pd_post_czw_kb" -> attrs.getByPath("opinions.model");
            default -> null;
        };

        return ObjectUtil.isEmpty(val) ? null : String.valueOf(val);*/
    }


    /**
     * 在content前位置新增 ‘总评分：5’ 内容， 取值如下
     * "opinions": {
     * "series":"马自达3 昂克赛拉",
     * "model":"2021款 2.0L 自动 质雅版",
     * "score_finally":5
     * },
     *
     * @param channelCode
     * @param attrs
     * @return
     */
    public String extractContentByChannel(String channelCode, JSONObject attrs, String defaultContent) {
        if (!CollUtil.newHashSet("pd_post_czw_kbms").contains(channelCode)) {
            return defaultContent;
        }

        final String score_finally = attrs.getByPath("opinions.score_finally", String.class);
        if (StrUtil.isBlank(score_finally)) {
            return defaultContent;
        }

        return StrUtil.format("总评分：{} ", score_finally).concat(defaultContent);
    }


    @Override
    public boolean isAccess() {
        PublicDomainDatasetContext context = this.getRequestData();
        Assert.isTrue(StrUtil.isNotBlank(context.getWorkId()), "getWorkId cannot be empty");

        return true;
    }
}
