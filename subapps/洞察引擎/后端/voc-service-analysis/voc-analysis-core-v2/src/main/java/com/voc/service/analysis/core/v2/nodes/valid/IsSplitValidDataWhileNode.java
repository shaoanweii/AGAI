package com.voc.service.analysis.core.v2.nodes.valid;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.DigestUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.github.pagehelper.PageInfo;
import com.voc.service.analysis.api.IAysModelResltAnalysisValidService;
import com.voc.service.analysis.core.v2.config.AnalysisConfig;
import com.voc.service.analysis.core.v2.impl.cenvert.AysConvertMapperService;
import com.voc.service.analysis.core.v2.nodes.abstracts.AbstractNodeIf;
import com.voc.service.analysis.core.v2.nodes.context.AnlysisDefaultContext;
import com.voc.service.analysis.model.AysModelResltDataAnalysisValidModel;
import com.voc.service.analysis.model.AysProcessDataModel;
import com.voc.service.analysis.model.AysValidDataModel;
import com.yomahub.liteflow.annotation.LiteflowComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author ckcui
 * @version 1.0.0
 * @ClassName StoreSourceDataNode
 * @createTime 2024年03月07日 10:49
 * @Copyright cuick
 */
@LiteflowComponent(id = "isSplitValidDataWhileNode", name = "读取本次验证测试数据节点")
public class IsSplitValidDataWhileNode extends AbstractNodeIf {
    private static final Logger log = LoggerFactory.getLogger(IsSplitValidDataWhileNode.class);
    @Autowired
    IAysModelResltAnalysisValidService resltDataService;
    @Autowired
    AnalysisConfig config;
    @Autowired
    AysConvertMapperService convertMapperService;

    @Override
    public boolean processIf() throws Exception {

        AnlysisDefaultContext context = this.getRequestData();
        final AysValidDataModel param = context.getValidDataParam();
        final PageInfo<AysModelResltDataAnalysisValidModel> pageInfo = resltDataService.find(context.getClientId(),param, 1000);
        log.info("读取副本数据 {}", pageInfo.getList().size());
        if (ObjectUtil.isNull(pageInfo.getList())){
            return false;
        }
        List<AysProcessDataModel> processData = this.convert(pageInfo.getList());
        context.setProcessData(processData);
//        int size = pageInfo.getSize();
//        log.info("分页查询总数:{}", size);
//        if (pageInfo.getSize() > 0) {
//            param.setPageNum(pageInfo.getNextPage());
//            log.info("继续读取数据 {}", param.getPageNum());
//            return true;
//        }
        log.info("isSplitValidDataWhileNode读取所有数据完成:{}",processData.size());
        return true;
    }


    private List<AysProcessDataModel> convert(List<AysModelResltDataAnalysisValidModel> parmas) {

        List<AysProcessDataModel> list = parmas.stream().map(model -> {
            Map<String, String> contentMD5 = new HashMap<>();
            if (StrUtil.isNotBlank(model.getOriginalTextScene())) {
                contentMD5.put("content", DigestUtil.md5Hex(StrUtil.trim(model.getOriginalTextScene())));
            }

            AysProcessDataModel processDataModel = convertMapperService.convertToAysModelResltDataAnalysisValidModel(model);
            processDataModel.setData(JSON.toJSONString(model, SerializerFeature.WriteNullStringAsEmpty));
            processDataModel.setDataMd5(JSONUtil.toJsonStr(contentMD5));
            processDataModel.setPublishTime(model.getPublishTime());
            if(ObjectUtil.isNotNull(model.getExtFields())) {
                processDataModel.setExtFields(JSONUtil.parseObj(model.getExtFields()));
            }

            return processDataModel;
        }).collect(Collectors.toList());
        return list;
    }

    @Override
    public boolean isAccess() {
        super.isAccess();
        AnlysisDefaultContext context = this.getRequestData();
        AysValidDataModel param = context.getValidDataParam();
        Assert.isTrue(ObjectUtil.isNotNull(param), "getValidResltDataParam cannot be empty");
        Assert.isTrue(StrUtil.isNotBlank(param.getStartTime()), "getStartTime cannot be empty");
        Assert.isTrue(StrUtil.isNotBlank(param.getEndTime()), "getEndTime cannot be empty");

//        context.setParsedData(null);
        context.setProcessData(null);

        return true;
    }
}
