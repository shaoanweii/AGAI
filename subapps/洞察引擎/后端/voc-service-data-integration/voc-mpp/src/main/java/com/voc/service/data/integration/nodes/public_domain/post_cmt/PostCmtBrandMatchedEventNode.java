package com.voc.service.data.integration.nodes.public_domain.post_cmt;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.voc.service.data.integration.api.model.ChannelMetaDataModel;
import com.voc.service.data.integration.api.model.DataIntegrationRecordModel;
import com.voc.service.data.integration.config.PublicDomainConfig;
import com.voc.service.data.integration.enums.ErrorDataMsgEnums;
import com.voc.service.data.integration.nodes.public_domain.context.PublicDomainDatasetContext;
import com.yomahub.liteflow.annotation.LiteflowComponent;
import com.yomahub.liteflow.core.NodeComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @Title: LoadChannelDatasetNode
 * @Package: com.voc.service.data.integration.nodes
 * @Description:
 * @Author: cuick
 * @Date: 2024/9/24 17:14
 * @Version:1.0
 */
@LiteflowComponent(id = "postCmtBrandMatchedEventNode", name = "处理品牌车系数据-帖子类")
public class PostCmtBrandMatchedEventNode extends NodeComponent {
    public static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss", Locale.CHINA);
    private static final Logger log = LoggerFactory.getLogger(PostCmtBrandMatchedEventNode.class);

    @Autowired
    PublicDomainConfig config;


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
            log.error("【{}】推送数据错误信息：", context.getWorkId(), e);
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


        final Map<String, String> weiboBrandMapping = config.getBrandMappingPostCmtWeiboBrandMapping().stream()
                .collect(Collectors.toMap(PublicDomainConfig.WeiboBrandMap::getUserId, PublicDomainConfig.WeiboBrandMap::getBrand));
        // 1. 初始化引擎（使用默认选项）
        list.stream()
                .filter(Objects::nonNull)
                .filter(model -> Objects.nonNull(model.getData()))
                .forEach(model -> {
                    try {
                        ChannelMetaDataModel data = JSONUtil.toBean(String.valueOf(model.getData()), ChannelMetaDataModel.class);
                        if (!"post_cmt".equalsIgnoreCase(data.getContentType())) {
//                            log.error("【{}】数据分类处理错误：contentType is null -> {}", model.getId(), model.getData());
                            return ;
                        }
                        if(Objects.equals(data.getIsDeleted(), 1)){
                            log.debug("【{}】{}数据已删除：{}", context.getWorkId(), model.getId(), model.getData());
                            return ;
                        }

                        log.debug("【{}】数据分类处理：{}", model.getId(), model.getData());

                        final JSONObject attrs = JSONUtil.isTypeJSONObject(String.valueOf(data.getAttrs()))
                                ? JSONUtil.parseObj(data.getAttrs2()) : new JSONObject();

                        // 车系
                        final String rawChannel = attrs.getStr("channel");
                        final String series_cleaned = this.cleanChannelSeries(rawChannel);
                        data.setSeries(series_cleaned);
                        log.debug("【{}】{}微博渠道车系：{}", context.getWorkId(), model.getId(), series_cleaned);

                        // 品牌
                        if (data.getChannelCode().equalsIgnoreCase("pd_post_wb")
                                && weiboBrandMapping.containsKey(data.getUserId())) {
                            final String brand = weiboBrandMapping.get(data.getUserId());
                            log.debug("【{}】{}微博渠道品牌：{}", context.getWorkId(), model.getId(), brand);
                            data.setBrand(brand);
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


    /**
     * 清洗渠道名称- 车系
     *
     * @param rawChannel 原始渠道名称
     * @return 清洗后的系列名称
     */
    public String cleanChannelSeries(String rawChannel) {
        if (StrUtil.isBlank(rawChannel)) {
            return null;
        }

        // 特殊排除：车友圈广场
        if ("车友圈广场".equals(rawChannel)) {
            return null;
        }

        // 清洗渠道名称：去除车友圈/论坛/社区
        String cleanedChannelCleaned = rawChannel;

        if (rawChannel.contains("车友圈") ||
                rawChannel.contains("论坛") ||
                rawChannel.contains("社区")) {
            cleanedChannelCleaned = rawChannel.replaceAll("(车友圈|论坛|社区)", "").trim();
        }

        // 城市排除
        for (PublicDomainConfig.CityItem cityItme : config.getBrandMappingPostCmtCityList()) {
            if (cityItme.getName().contains(cleanedChannelCleaned)) {
                return null;
            }
        }

        // 不包含车友圈/论坛/社区的直接返回 null
        if (!rawChannel.contains("车友圈") &&
                !rawChannel.contains("论坛") &&
                !rawChannel.contains("社区")) {
            return null;
        }

        return null; // 其他情况返回 null
    }


    @Override
    public boolean isAccess() {
        PublicDomainDatasetContext context = this.getRequestData();
        Assert.isTrue(StrUtil.isNotBlank(context.getWorkId()), "getWorkId cannot be empty");

        return true;
    }
}
