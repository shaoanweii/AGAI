package com.voc.service.data.integration.nodes.public_domain.opinion;

import cn.hutool.core.bean.BeanUtil;
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
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @Title: LoadChannelDatasetNode
 * @Package: com.voc.service.data.integration.nodes
 * @Description:
 * @Author: cuick
 * @Date: 2024/9/24 17:14
 * @Version:1.0
 */
@LiteflowComponent(id = "opinionWOMChannelMatchedEventNode", name = "处理渠道数据-投诉类")
public class OpinionWOMChannelMatchedEventNode extends NodeComponent {
    public static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss", Locale.CHINA);
    private static final Logger log = LoggerFactory.getLogger(OpinionWOMChannelMatchedEventNode.class);

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
            log.info("【{}】推送数据错误信息：", context.getWorkId(), e);
            //出现服务异常时，将所有数据归为异常处理数据集
            final List<DataIntegrationRecordModel> errorList = context.getSuccessfulDataset().stream().map(data -> {
                data.setErrorCode(ErrorDataMsgEnums.PushServiceHasFailed.getCode());
                data.setErrorMsg(ErrorDataMsgEnums.PushServiceHasFailed.getText());
                return data;
            }).toList();
            context.getFailedDataset().addAll(errorList);
            context.setSuccessfulDataset(null);
        }
    }

    /**
     * 数据分类处理流程
     */
    public List<DataIntegrationRecordModel> cleanAndNormalizeRawData(PublicDomainDatasetContext context, List<DataIntegrationRecordModel> list) {
        // 创建数据集用户存储扩展出集合 ，例如依据需要将options字段值拆分出多条数据
        AtomicReference<List<DataIntegrationRecordModel>> extendedDataList
                = new AtomicReference<>(CollUtil.newCopyOnWriteArrayList(null));

        // 1. 初始化引擎（使用默认选项）
        list.stream()
                .filter(Objects::nonNull)
                .filter(model -> Objects.nonNull(model.getData()))
                .forEach(model -> {
                    try {
                        ChannelMetaDataModel data = JSONUtil.toBean(String.valueOf(model.getData()), ChannelMetaDataModel.class);
                        if (!"quest".equalsIgnoreCase(data.getContentType())) {
//                            log.error("【{}】数据分类处理错误：contentType is null -> {}", model.getId(), model.getData());
                            return ;
                        }
                        log.debug("【{}】{}数据分类处理：{}", context.getWorkId(), model.getId(), model.getData());
                        if (Objects.equals(data.getIsDeleted(), 1)) {
                            log.debug("【{}】{}数据已删除：{}", context.getWorkId(), model.getId(), model.getData());
                            return;
                        }

                        final JSONObject attrs = JSONUtil.isTypeJSONObject(String.valueOf(data.getAttrs()))
                                ? JSONUtil.parseObj(data.getAttrs()) : new JSONObject();

                        // 规则1：匹配 site_domain
                        AtomicReference<String> channelCode = new AtomicReference<>(data.getChannelCode());
                        // 规则2：匹配 path
                        config.getChannelMappingOpinionRule3().stream()
                                .filter(c -> StrUtil.isNotBlank(c.getSourceChannelCode()))
                                .filter(c -> c.getSourceChannelCode().equals(channelCode.get()))
                                .forEach(c -> {
                                    log.debug("【{}】数据分类处理： 渠道配置值-rule2 {}", context.getWorkId(), c);

                                    if(CollUtil.isNotEmpty(c.getRequiredFields())){
                                        final boolean hasMatch =  c.getRequiredFields().stream().allMatch(attrs::containsKey);
                                        if(hasMatch){
                                            channelCode.set(c.getTargetChannelCode());
                                            log.info("【{}】 {} 数据分类处理： 渠道配置值1-replace {}", context.getWorkId(), model.getId(), channelCode.get());
                                        }else{
                                            log.error("【{}】{} 数据分类处理： 渠道配置值1-requiredFields {}", context.getWorkId(), model.getId(), c.getRequiredFields());
                                        }
                                    }
                                });
                        log.debug("【{}】{}数据分类处理：{}", context.getWorkId(), model.getId(), channelCode.get());

                        // 设置渠道值（匹配值优先，否则使用 site_domain）
                        data.setChannelCode(channelCode.get());
                        log.debug("【{}】{}数据分类处理结果：{}", context.getWorkId(), model.getId(), data);

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

    @Override
    public boolean isAccess() {
        PublicDomainDatasetContext context = this.getRequestData();
        Assert.isTrue(StrUtil.isNotBlank(context.getWorkId()), "getWorkId cannot be empty");

        return true;
    }
}
