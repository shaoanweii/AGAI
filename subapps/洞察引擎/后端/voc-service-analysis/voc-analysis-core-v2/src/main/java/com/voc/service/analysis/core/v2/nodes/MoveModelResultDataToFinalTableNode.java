package com.voc.service.analysis.core.v2.nodes;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONArray;
import com.voc.service.analysis.api.IAysModelResltAnalysisService;
import com.voc.service.analysis.core.v2.nodes.abstracts.AbstractNode;
import com.voc.service.analysis.core.v2.nodes.context.AnlysisDefaultContext;
import com.voc.service.analysis.core.v2.service.SelectdbApiServiceImpl;
import com.yomahub.liteflow.annotation.LiteflowComponent;
import lombok.Cleanup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.RetryException;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Title: IsExistPreDataNode
 * @Package: com.voc.service.analysis.core.v2.nodes.abstracts
 * @Description:
 * @Author: cuick
 * @Date: 2024/4/19 10:34
 * @Version:1.0
 */
@LiteflowComponent(id = "moveModelResultDataToFinalTableNode", name = "未完成写入结果表的数据节点")
public class MoveModelResultDataToFinalTableNode extends AbstractNode {

    private static final Logger log = LoggerFactory.getLogger(MoveModelResultDataToFinalTableNode.class);

    @Autowired
    IAysModelResltAnalysisService iAysModelResltAnalysisService;

    @Override
    public void process() throws RetryException {
        try {
            AnlysisDefaultContext context = this.getRequestData();
            final String clientId = context.getClientId();
            log.info("未完成写入结果表的数据节点 tag: {} ,clientId: {}", this.getTag(), clientId);

            final List<String> result = this.getCurrLoopObj();
            if(CollUtil.isEmpty(result)) {
                log.warn("未完成写入结果表的数据节点2 tag: {} ,clientId: {} ,size: {}", this.getTag(), clientId, result.size());
                return;
            }

            iAysModelResltAnalysisService.moveModelResultDataToFinalTable(context.getClientId(), new HashSet<>(result));
            log.info("未完成写入结果表的数据节点 tag: {} ,clientId: {} ,size: {}", this.getTag(), clientId, result.size());

            iAysModelResltAnalysisService.modifyUnmigratedDataScopToDone(clientId, new HashSet<>(result));
            log.info("修改未完成写入结果表数据范围 tag: {} ,clientId: {} ,size: {}", this.getTag(), clientId, result.size());

        } catch (Exception e) {
            throw new RetryException(e.getMessage(), e);
        }
    }

    @Override
    public boolean isAccess() {
        AnlysisDefaultContext context = this.getRequestData();
//        Assert.isTrue(StrUtil.isNotBlank(context.getClientId()), "getClientId cannot be empty");

        return true;
    }
}
