package com.voc.service.analysis.core.v2.nodes;

import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONArray;
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

/**
 * @Title: IsExistPreDataNode
 * @Package: com.voc.service.analysis.core.v2.nodes.abstracts
 * @Description:
 * @Author: cuick
 * @Date: 2024/4/19 10:34
 * @Version:1.0
 */
@LiteflowComponent(id = "saveModelResultDataToFinalTableNode", name = "未完成写入结果表的数据JSON写入API Stream节点")
public class SaveModelResultDataToFinalTableNode extends AbstractNode {

    private static final Logger log = LoggerFactory.getLogger(SaveModelResultDataToFinalTableNode.class);
    @Autowired
    SelectdbApiServiceImpl selectdbApiService;

    @Override
    public void process() throws RetryException {
        try {
            AnlysisDefaultContext context = this.getRequestData();
            final String clientId = context.getClientId();
            log.info("加载静态数据 tag: {} ,clientId: {}", this.getTag(), clientId);

            Object jsonListObj = context.getData("modelResultData");
            if (ObjectUtil.isNull(jsonListObj)) {
                log.error(">>>>>>> 模型结果数据文件不存在 <<<<<<<<<<<<<< {}", context.getWorkId());
                return;
            }

            JSONArray jsonList = (JSONArray) jsonListObj;
            @Cleanup
            InputStream inputStream = IoUtil.toStream(jsonList.toStringPretty(), StandardCharsets.UTF_8);

            //调用selectdb api Stream 写入数据
            boolean rs = selectdbApiService.streamInsert(inputStream);
            log.info("写入结果数据 tag: {} ,clientId: {} ,result: {}", this.getTag(), clientId, rs);
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
