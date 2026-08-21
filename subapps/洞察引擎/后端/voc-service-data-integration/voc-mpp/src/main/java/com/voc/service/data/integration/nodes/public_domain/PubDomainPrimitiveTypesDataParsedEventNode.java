package com.voc.service.data.integration.nodes.public_domain;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.StrBuilder;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.DigestUtil;
import cn.hutool.crypto.digest.MD5;
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

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * @Title: LoadChannelDatasetNode
 * @Package: com.voc.service.data.integration.nodes
 * @Description:
 * @Author: cuick
 * @Date: 2024/9/24 17:14
 * @Version:1.0
 */
@LiteflowComponent(id = "pubDomainPrimitiveTypesDataParsedEventNode", name = "推送数据到数据清洗服务")
public class PubDomainPrimitiveTypesDataParsedEventNode extends NodeComponent {
    public static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss", Locale.CHINA);
    public static final DateTimeFormatter formatter2 = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.CHINA);
    private static final Logger log = LoggerFactory.getLogger(PubDomainPrimitiveTypesDataParsedEventNode.class);

    @Autowired
    TextProcessorService textProcessor;

    @Autowired
    PublicDomainConfig config;

    @Override
    public void process() throws Exception {
        PublicDomainDatasetContext context = this.getRequestData();
        try {
            if (CollUtil.isEmpty(context.getSuccessfulDataset())) {
                throw new Exception("【".concat(context.getChannelType()).concat("】数据验证后无成功数据："));
            } else {
                log.info("【{}】数据验证成功，成功数据：{}", context.getWorkId(), context.getSuccessfulDataset().size());
                //保存记录成功数据集
                List<DataIntegrationRecordModel> list = context.getSuccessfulDataset().stream().map(item -> {
                    JSONObject jsonObj = JSONUtil.parseObj(item.getData());
                    return JSONUtil.toBean(jsonObj, DataIntegrationRecordModel.class);
                }).collect(Collectors.toList());

                list = this.cleanAndNormalizeRawData(context, list);
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

        // 1. 初始化引擎（使用默认选项）
        list.stream()
                .filter(Objects::nonNull)
                .forEach(model -> {
                    try {
                        if (model.getData() == null) {
                            log.error("【{}】数据格式错误，未获取到 data 属性：{}", context.getWorkId(), model);
                            return;
                        }
                        // 数据转换
                        final String data = String.valueOf(model.getData());
                        final JSONObject dataObj = JSONUtil.isTypeJSONObject(String.valueOf(data))
                                ? JSONUtil.parseObj(data) : new JSONObject();
                        // 内容类型
                        final String contentType;
                        // 检查 opinions 字段是否存在且不为 null
                        if (dataObj.get("opinions") != null) {
                            contentType = "opinion"; // 口碑
                        }
                        // 检查 content 字段是否包含 "投诉编号"
                        else if (dataObj.get("opinions") == null &&
                                dataObj.getStr("content") != null &&
                                dataObj.getStr("content").contains("投诉编号")) {
                            contentType = "complaint"; // 投诉
                        }
                        // 默认情况
                        else {
                            contentType = "post_cmt"; // 评论
                        }
                        // 获取站点名称（中文)
                        final String siteName = String.valueOf(dataObj.getByPath("gather.site_name"));
                        // 获取数据创建时间
                        final LocalDateTime dataCreateTime = LocalDateTime.ofInstant(
                                Instant.ofEpochSecond(dataObj.getLong("ctime")),
                                ZoneId.systemDefault()
                        );

                        // id
                        final String id = MD5.create().digestHex(model.getId());
                        // 站点域
                        final String site_domain =
                                ObjUtil.isNotNull(dataObj.getByPath("gather.site_domain"))
                                        ? dataObj.getByPath("gather.site_domain", String.class) : null;
                        if (StrUtil.isBlank(site_domain)) {
                            log.error("【{}】站点域名为空：{}", context.getWorkId(), dataObj);
                            return;
                        }

                        // 1、排除列表
                        final int isDeleted = this.exclueList(context.getWorkId(), dataObj, site_domain, id);
                        log.debug("【{}】站点名称：【{}】", context.getWorkId(), siteName);

                        AtomicReference<String> channelCode = new AtomicReference<>(siteName);


                        AtomicReference<List<String>> sub_domain = new AtomicReference<>(CollUtil.newArrayList(siteName));
                        // 2、匹配 site_domain
                        if (ObjUtil.isNotNull(dataObj.getByPath("gather.site_domain"))) {
                            sub_domain.set(
                                    JSONUtil.isTypeJSONArray(String.valueOf(dataObj.getByPath("gather.sub_domain")))
                                            ? JSONUtil.toList(JSONUtil.parseArray(dataObj.getByPath("gather.sub_domain")), String.class) : new ArrayList<>()
                            );
                        }

                        final String channelCode1 = this.mathChannelCode(context.getWorkId(), contentType, siteName, site_domain, sub_domain.get(), id);
                        channelCode.set(channelCode1);

                        //  3、匹配path中配置
                        final String channelCode2 = this.mathPath(context.getWorkId(), channelCode.get(), dataObj, id);
                        channelCode.set(channelCode2);

                        // 获取用户id
                        final String user_id = this.generateUserId(context.getWorkId(), dataObj, site_domain);

                        // 获取用户名
                        final String userName = StrUtil.blankToDefault(
                                StrUtil.blankToDefault(String.valueOf(dataObj.getByPath("user.name")), "")
                                        .replaceAll("(?i)^null$|^null$", ""),  // 移除 'null' 或 'NULL' 字符串
                                StrUtil.blankToDefault(
                                        StrUtil.blankToDefault(String.valueOf(dataObj.getByPath("user.nickname"))
                                                .replaceAll("(?i)^null$|^null$", ""), ""),  // 移除 'null' 或 'NULL' 字符串
                                        null // 移除 'null' 或 'NULL' 字符串
                                ) // 移除 'null' 或 'NULL' 字符串
                        );
                        log.debug("【{}】用户名：【{}】 【{}】", context.getWorkId(), dataObj.getByPath("user.name"), dataObj.getByPath("user.nickname"));
                        // 是否是转发
                        final String isMainPost = (StrUtil.isBlank(dataObj.getStr("wtype")) || !dataObj.getStr("wtype").equals("1"))
                                ? "N" : "Y";
                        //TITLE
                        final String maskTitleVal = this.processTitleAndMaskPhone(context.getWorkId(), dataObj, isMainPost);
                        final String title = StrUtil.isBlank(maskTitleVal) ? null : maskTitleVal.replaceAll("(?i)^null$|^null$", "");
                        //CONTENT
                        final String maskContentVal = this.processContentAndMaskPhone(dataObj);
                        final String content = StrUtil.isBlank(maskContentVal) ? null : maskContentVal.replaceAll("(?i)^null$|^null$", "");

                        // 是否水军
                        /*final String isWsaterArmy = Optional.ofNullable(dataObj.getByPath("analysis.noise"))
                                .filter(ObjUtil::isNotNull).map(String::valueOf)
                                .filter(StrUtil::isNotBlank).filter(v -> v.equals("1") || v.equalsIgnoreCase("Y"))
                                .map(v -> "Y") .orElse("N");*/
                        final String isWsaterArmy = "N";

                        // URL
                        final String url = dataObj.getStr("url");

                        // data_id
                        final String data_id = MD5.create().digestHex(String.join("|"
                                , url
                                , user_id
                                , content
                                , dataCreateTime.format(formatter)));


                        // ATTRS
                        JSONObject attrs2 = new JSONObject();
                        attrs2.set("url", url);
                        attrs2.set("view_count", dataObj.getStr("visit_count"));
                        attrs2.set("comment_count", dataObj.getStr("comment_count"));
                        attrs2.set("like_count", dataObj.getStr("like_count"));
                        attrs2.set("share_count", dataObj.getStr("repost_count"));
                        attrs2.set("favorite_count", dataObj.getStr("collection_count"));
                        attrs2.set("main_post_id", MD5.create().digestHex(String.join("|", data_id)));
                        attrs2.set("is_main_post", isMainPost);
                        attrs2.set("site_domain", site_domain);
                        attrs2.set("sub_domain", sub_domain.get());
                        attrs2.set("voc_raw_id", model.getId());
                        attrs2.set("wtype", dataObj.getStr("wtype"));
                        attrs2.set("ctime", dataObj.getStr("ctime"));   // 原贴发布时间
                        attrs2.set("user_name", userName);   // 原贴作者昵称
                        attrs2.set("user_uid", user_id); // 原贴作者ID
                        attrs2.set("uuid", dataObj.getStr("uuid"));
                        attrs2.set("video_urls", dataObj.getStr("video_urls"));
                        attrs2.set("pic_urls", dataObj.getStr("pic_urls"));
                        attrs2.set("istar_asr", dataObj.getStr("istar_asr"));
                        attrs2.set("followers_count", dataObj.getStr("followers_count"));
                        attrs2.set("verified_type", dataObj.getStr("verified_type"));
                        attrs2.set("verified_reason", dataObj.getStr("verified_reason"));
                        attrs2.set("user_description", dataObj.getStr("description"));
                        attrs2.set("user_city", dataObj.getStr("city"));   // 作者所属地
                        attrs2.set("reply_count", dataObj.getStr("reply_count"));
                        attrs2.set("like_count", dataObj.getStr("like_count"));
                        attrs2.set("price", dataObj.getByPath("opinions.price", String.class));
                        attrs2.set("location", dataObj.getByPath("opinions.location", String.class));
                        attrs2.set("date", dataObj.getByPath("opinions.date", String.class));   //购车日期
                        attrs2.set("seller", dataObj.getByPath("opinions.seller", String.class));   //购车经销商
                        attrs2.set("mileage", dataObj.getByPath("opinions.mileage", String.class));

                        ChannelMetaDataModel new_data = ChannelMetaDataModel.builder()
                                .id(id)
                                .dataId(data_id)
                                .channelCode(channelCode.get())
                                .userId(user_id)
                                .userName(userName)
                                .content(content)
                                .title(title)
                                .isWsaterArmy(isWsaterArmy)
                                .isOuter("Y")
                                .dlrType("未知")
                                .done(0)
                                .modelType(0)
                                .dataCreateTime(dataCreateTime)
                                .contentType(contentType)
                                .workId(context.getWorkId())
                                .weight(0)
                                .attrs(JSONUtil.toJsonStr(dataObj))
                                .attrs2(JSONUtil.toJsonStr(attrs2))
                                .isDeleted(isDeleted)
                                .build();
                        log.debug("【{}】数据处理结果：{}", context.getWorkId(), new_data);

                        model.setChannelType(contentType);
                        model.setId(new_data.getId());
                        model.setDataId(new_data.getDataId());
                        model.setData(JSONUtil.toJsonStr(new_data));
                        model.setWorkId(context.getWorkId());
                    } catch (Exception e) {
                        log.error(e.getMessage(), e);
                        log.error("【{}】数据处理失败：{}", context.getWorkId(), model.getId());
                        model.setErrorCode(ErrorDataMsgEnums.FailedToParseOriginalData.getCode());
                        model.setErrorMsg(ErrorDataMsgEnums.FailedToParseOriginalData.getText());
                    }
                });
        log.info("【{}】数据分类处理完成，共有 {} 条数据", context.getWorkId(), list.size());
        return list;
    }

    private String generateUserId(String workId, JSONObject dataObj, String site_domain) {
        final String pathVal = config.geUserIdPathValue(site_domain);
        log.debug("【{}】{} site_domain：【{}】", workId, pathVal, site_domain);

        //  user.uid
        final String userId = StrUtil.blankToDefault(
                StrUtil.blankToDefault(String.valueOf(dataObj.getByPath(pathVal)), "")
                        .replaceAll("(?i)^null$|^null$", "")
                , null);

        log.debug("【{}】{} 用户id：【{}】", workId, site_domain, userId);
        log.info("【{}】{} 用户id：【{}】", workId, pathVal, StrUtil.isNotBlank(config.getEncryptionTypeMap(pathVal)));
        if (StrUtil.isNotBlank(userId) && StrUtil.isNotBlank(config.getEncryptionTypeMap(pathVal))) {
            if (config.getEncryptionTypeMap(pathVal).equalsIgnoreCase("md5")) {
                return MD5.create().digestHex(userId);
            } else {
                return userId;
            }
        }
        return userId;
    }

    private String mathChannelCode(final String workId, final String contentType, final String defaultChannelCode
            , final String siteDomain, List<String> subDomain, final String dataId) {
        AtomicReference<String> channelCode = new AtomicReference<>(defaultChannelCode);  //  默认站点提供中文名
        config.getChannelMappingSubDomain()
                .stream()
                .filter(c -> c.getSiteDomain().equals(siteDomain))     // 主键匹配：必须匹配 site_domain
                .forEach(c -> {
                    final String type = StrUtil.blankToDefault(c.getType(), "post_cmt");
                    log.debug("【{}】{} - contentType:{} 数据分类处理： 站点配置值 {}", workId, contentType, dataId, c);
                    if (CollUtil.isEmpty(c.getSubDomain()) && contentType.equalsIgnoreCase(type)) {
                        log.debug("【{}】{} 数据分类处理： 站点配置值1-sub_domain {}", workId, dataId, c);
                        channelCode.set(c.getChannelCode());
                        return;
                    }
                    boolean anyMatch = subDomain.stream().anyMatch(item -> {
                        String itemStr = StrBuilder.create(item).toString();
                        String[] searchArray = Arrays.stream(c.getSubDomain().toArray())
                                .map(Object::toString)
                                .toArray(String[]::new);
                        return StrUtil.containsAny(itemStr, searchArray);
                    });
                    if (anyMatch && contentType.equalsIgnoreCase(type)) {
                        channelCode.set(c.getChannelCode());
                        log.debug("【{}】{} 数据分类处理： 站点配置值2-sub_domain {}", workId, dataId, c);
                    }
                });
        return channelCode.get();
    }


    private String mathPath(final String workId, final String channelCode, JSONObject dataObj, final String dataId) {
        AtomicReference<String> newChannelCode = new AtomicReference<>(channelCode);
        config.getChannelMappingPathRule().stream()
                .filter(c -> StrUtil.isNotBlank(c.getTargetChannelCode()))
                .filter(c -> StrUtil.isNotBlank(c.getChannelCode()))
                .filter(c -> StrUtil.isNotBlank(c.getPath()))
                .filter(c -> CollUtil.isNotEmpty(c.getVal()))
                .filter(c -> c.getTargetChannelCode().equalsIgnoreCase(channelCode))
                .forEach(c -> {
                    try {
                        Object val = ObjUtil.defaultIfNull(dataObj.getByPath(c.getPath()), "");
                        if (StrUtil.isNotBlank(String.valueOf(val))) {
                            final List<String> values = JSONUtil.isTypeJSONArray(String.valueOf(val)) ?
                                    JSONUtil.toList(JSONUtil.parseArray(String.valueOf(val)), String.class) : Collections.singletonList(String.valueOf(val));

                            boolean anyMatch = values.stream().anyMatch(item -> {
                                String itemStr = StrBuilder.create(item).toString();
                                String[] searchArray = Arrays.stream(c.getVal().toArray())
                                        .map(Object::toString)
                                        .toArray(String[]::new);
                                return StrUtil.containsAny(itemStr, searchArray);
                            });

                            if (anyMatch) {
                                newChannelCode.set(c.getChannelCode());
                                log.info("【{}】{} 数据分类处理： 站点配置值-exclude1 {} 匹配成功1", workId, dataId, c);
                            }
                        } else {
                            log.debug("【{}】{} 数据分类处理： 站点配置值-exclude2 {} 匹配失败1", workId, dataId, c);
                        }
                    } catch (Exception e) {
                        log.error("【{}】{} error_msg:{}", workId, dataId, e.getMessage());
                        log.error("【{}】{} 数据分类处理： 站点配置值-exclude3 {} 匹配失败", workId, dataId, c);
                    }
                });
        return newChannelCode.get();
    }

    private int exclueList(String workId, JSONObject dataObj, String site_domain, String dataId) {
        // 规则3：匹配 exclude
        AtomicReference<Integer> delFlag = new AtomicReference<>(0);
        config.getChannelMappingExcludeList().stream()
                .filter(c -> StrUtil.isNotBlank(c.getSiteDomain()) && StrUtil.isNotBlank(c.getPath()) && CollUtil.isNotEmpty(c.getVal()))
                .filter(c -> StrUtil.isNotBlank(site_domain))
                .filter(c -> site_domain.equals(c.getSiteDomain()))
                .forEach(c -> {
                    log.debug("【{}】{} 数据分类处理： 站点配置值-exclude {}", workId, dataId, c);

                    try {
                        Object val = ObjUtil.defaultIfNull(dataObj.getByPath(c.getPath()), "");
                        if (StrUtil.isNotBlank(String.valueOf(val))) {
                            final List<String> values = JSONUtil.isTypeJSONArray(String.valueOf(val)) ?
                                    JSONUtil.toList(JSONUtil.parseArray(String.valueOf(val)), String.class) : Collections.singletonList(String.valueOf(val));

                            boolean anyMatch = values.stream().anyMatch(item -> {
                                String itemStr = StrBuilder.create(item).toString();
                                String[] searchArray = Arrays.stream(c.getVal().toArray())
                                        .map(Object::toString)
                                        .toArray(String[]::new);
                                return StrUtil.containsAny(itemStr, searchArray);
                            });
                            if (anyMatch) {
                                delFlag.set(1);
                                log.info("【{}】{} 数据分类处理： 站点配置值-exclude {} 匹配成功1", workId, dataId, c);
                            }
                        } else {
                            log.error("【{}】{} 数据分类处理： 站点配置值-exclude {} 匹配失败1", workId, dataId, c);
                        }
                    } catch (Exception e) {
                        log.error("【{}】{} 数据分类处理： 站点配置值-exclude {} 匹配异常", workId, dataId, c);
                    }
                });
        return delFlag.get();

    }

    /**
     * 处理文本并脱敏手机号
     *
     * @param isMainPost 是否主贴
     * @return 处理后的文本
     */
    public String processTitleAndMaskPhone(String workId, JSONObject dataObj, String isMainPost) {
        String title = dataObj.getStr("title");
        String content = dataObj.getStr("content");
        String selectedText;
        log.debug("【{}】处理文本：{}", workId, title);
        if ("Y".equals(isMainPost) && StrUtil.isNotBlank(title)) {
            selectedText = title;
        } else if ("Y".equals(isMainPost) && StrUtil.isBlank(content)) {
            selectedText = "-";
        } else if (!"Y".equals(isMainPost) && StrUtil.isNotEmpty(dataObj.getByPath("retweeted.title", String.class))) {
            selectedText = dataObj.getByPath("retweeted.title", String.class);
        } else {
            selectedText = "-";
        }
        if (!selectedText.equals("-")) {
            selectedText = textProcessor.maskPhoneNumbers(selectedText);
            selectedText = textProcessor.maskIdCard(selectedText);
            selectedText = textProcessor.maskVIN(selectedText);
            selectedText = textProcessor.maskLicensePlate(selectedText);
        }
        log.debug("【{}】处理后的文本：{}", workId, selectedText);
        // 使用正则表达式替换手机号
        return selectedText;
    }


    public String processContentAndMaskPhone(JSONObject dataObj) {
//        selectedText = "aa15901209193aaaaaa--京A88888--13552002885--230828198511020011--LSCBBZ2T6LG741172--";
        if (dataObj.isNull("content")) {
            return null;
        }
        String selectedText = dataObj.getStr("content");
        log.debug("处理前的文本：{}", selectedText);
        selectedText = textProcessor.maskPhoneNumbers(selectedText);
        selectedText = textProcessor.maskIdCard(selectedText);
        selectedText = textProcessor.maskVIN(selectedText);
        selectedText = textProcessor.maskLicensePlate(selectedText);
        log.debug("处理后的文本：{}", selectedText);

        // 使用正则表达式替换手机号
        return selectedText;
    }


    @Override
    public boolean isAccess() {
        PublicDomainDatasetContext context = this.getRequestData();
        Assert.isTrue(StrUtil.isNotBlank(context.getWorkId()), "getWorkId cannot be empty");

        return true;
    }
}
