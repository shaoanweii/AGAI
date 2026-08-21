package com.voc.service.data.integration.nodes.public_domain.complaint;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.voc.service.data.integration.api.model.ChannelMetaDataModel;
import com.voc.service.data.integration.api.model.DataIntegrationRecordModel;
import com.voc.service.data.integration.enums.ErrorDataMsgEnums;
import com.voc.service.data.integration.nodes.public_domain.context.PublicDomainDatasetContext;
import com.yomahub.liteflow.annotation.LiteflowComponent;
import com.yomahub.liteflow.core.NodeComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @Title: LoadChannelDatasetNode
 * @Package: com.voc.service.data.integration.nodes
 * @Description:
 * @Author: cuick
 * @Date: 2024/9/24 17:14
 * @Version:1.0
 */
@LiteflowComponent(id = "complaintBrandMatchedEventNode", name = "处理品牌车系数据-投诉类")
public class ComplaintBrandMatchedEventNode extends NodeComponent {
    public static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss", Locale.CHINA);
    private static final Logger log = LoggerFactory.getLogger(ComplaintBrandMatchedEventNode.class);


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
                        if (!"complaint".equalsIgnoreCase(data.getContentType())) {
//                            log.error("【{}】数据分类处理错误：contentType is null -> {}", model.getId(), model.getData());
                            return;
                        }
                        if (Objects.equals(data.getIsDeleted(), 1)) {
                            log.debug("【{}】{}数据已删除：{}", context.getWorkId(), model.getId(), model.getData());
                            return;
                        }
                        log.debug("【{}】{}数据分类处理：{}", context.getWorkId(), model.getId(), model.getData());

                        // 品牌
                        final String brand = this.extractBrandByChannel(data);
                        data.setBrand(brand);

                        // 车系
                        final String series_cleaned = this.extractSeriesByChannel(data);
                        data.setSeries(series_cleaned);
                        log.debug("【{}】{}渠道车系：{}", context.getWorkId(), model.getId(), series_cleaned);

                        //车型
                        final String model_cleaned = this.extractModelByChannel(data);
                        data.setModel(model_cleaned);
                        log.debug("【{}】{}渠道车型：{}", context.getWorkId(), model.getId(), model_cleaned);


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

    /**
     * 根据渠道代码提取品牌信息
     *
     * @return 提取的品牌信息，未匹配到则返回 null
     */
    public String extractBrandByChannel(ChannelMetaDataModel data) {
        final String channelCode = data.getChannelCode();
        final String content = data.getContent();
        if (StrUtil.hasBlank(channelCode, content)) {
            return null;
        }

        return switch (channelCode) {
            case "pd_post_czw_ts" -> extractWithPattern(content, CZW_TS_PATTERN);
            case "pd_post_hmts_ts" -> extractWithPattern(content, HMTS_TS_PATTERN);
            case "pd_post_qctsw_ts" -> extractWithPattern(content, QCTSW_TS_PATTERN);
            case "pd_post_qczhw_ts" -> extractWithPattern(content, QCZHW_TS_PATTERN);
            case "d_post_zgqczlw_ts" -> extractWithPattern(content, ZGQCZLW_TS_PATTERN);
            case "pd_post_zgqcw" -> extractWithPattern(content, ZGQCW_PATTERN);
            default -> null;
        };
    }


    /**
     * 根据渠道代码提取车型信息
     *
     * @param data 数据模型
     * @return 提取的车型信息，未匹配到则返回 null
     */
    public String extractSeriesByChannel(ChannelMetaDataModel data) {
        final String channelCode = data.getChannelCode();
        final String content = data.getContent();
        if (StrUtil.hasBlank(channelCode, content)) {
            return null;
        }

        return switch (channelCode) {
            case "pd_post_czw_ts" -> extractWithPattern(content, CZW_TS_SERIES_PATTERN);
            case "pd_post_hmts_ts" -> extractWithPattern(content, HMTS_SERIES_PATTERN);
            case "pd_post_qctsw_ts" -> {
                String model = extractWithPattern(content, QCTSW_MODEL_PATTERN);
                yield model != null ? extractWithPattern(model, QCTSW_SERIES_PATTERN) : null;
                // 先提取车型，再从车型中提取车系
            }
            case "pd_post_qczhw_ts" -> extractWithPattern(content, QCZHW_SERIES_PATTERN);
            case "d_post_zgqczlw_ts" -> extractWithPattern(content, ZGQCZLW_SERIES_PATTERN);
            case "pd_post_zgqcw" -> extractWithPattern(content, ZGQCW_SERIES_PATTERN);
            default -> null;
        };
    }

    public String extractModelByChannel(ChannelMetaDataModel data) {
        final String channelCode = data.getChannelCode();
        final String content = data.getContent();
        if (StrUtil.hasBlank(channelCode, content)) {
            return null;
        }

        switch (channelCode) {
            case "pd_post_czw_ts":
                return extractWithPattern(content, CZW_TS_MODEL_PATTERN);
            case "pd_post_hmts_ts":
                return extractWithPattern(content, HMTS_MODEL_PATTERN);
            case "pd_post_qctsw_ts":
                // 先提取完整车型，然后尝试提取20开头的部分，如果失败则返回完整车型
                String fullModel = extractWithPattern(content, QCTSW_MODEL_PATTERN);
                fullModel = extractWithPattern(fullModel, QCTSW_YEAR_MODEL_PATTERN);
                if (fullModel != null) {
                    String yearModel = extractWithPattern(fullModel, QCTSW_YEAR_MODEL_PATTERN);
                    // 使用COALESCE逻辑：返回第一个非null的值
                    return yearModel != null ? yearModel : fullModel;
                }
                return null;
            case "pd_post_qczhw_ts":
                return extractWithPattern(content, QCZHW_MODEL_PATTERN);
            case "d_post_zgqczlw_ts":
                return extractWithPattern(content, ZGQCZLW_MODEL_PATTERN);
            case "pd_post_zgqcw":
                return extractWithPattern(content, ZGQCW_MODEL_PATTERN);
            default:
                return null;
        }
    }

    /**
     * 使用正则表达式提取匹配组
     *
     * @param content 内容文本
     * @param pattern 正则表达式模式
     * @return 匹配的组内容，未匹配到则返回 null
     */
    private String extractWithPattern(String content, Pattern pattern) {
        if (StrUtil.hasBlank(content)) {
            return null;
        }

        Matcher matcher = pattern.matcher(content);
        if (matcher.find()) {
            return matcher.group(1); // 返回第一个捕获组
        }
        return null;
    }


    @Override
    public boolean isAccess() {
        PublicDomainDatasetContext context = this.getRequestData();
        Assert.isTrue(StrUtil.isNotBlank(context.getWorkId()), "getWorkId cannot be empty");

        return true;
    }
}
