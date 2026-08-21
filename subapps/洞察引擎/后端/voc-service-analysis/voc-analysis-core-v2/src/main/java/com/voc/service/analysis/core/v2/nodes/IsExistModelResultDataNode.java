package com.voc.service.analysis.core.v2.nodes;

import cn.hutool.core.collection.CollUtil;
import com.voc.service.analysis.api.IAysModelResltAnalysisService;
import com.voc.service.analysis.core.v2.nodes.abstracts.AbstractNodeIf;
import com.voc.service.analysis.core.v2.nodes.context.AnlysisDefaultContext;
import com.yomahub.liteflow.annotation.LiteflowComponent;
import com.yomahub.liteflow.core.NodeIteratorComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.RetryException;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * @Title: IsExistPreDataNode
 * @Package: com.voc.service.analysis.core.v2.nodes.abstracts
 * @Description:
 * @Author: cuick
 * @Date: 2024/4/19 10:34
 * @Version:1.0
 */
@LiteflowComponent(id = "isExistModelResultDataNode", name = "判断是否有未完成写入结果表的数据节点")
public class IsExistModelResultDataNode extends NodeIteratorComponent {

    private static final Logger log = LoggerFactory.getLogger(IsExistModelResultDataNode.class);
    @Autowired
    IAysModelResltAnalysisService iAysModelResltAnalysisService;

    @Override
    public Iterator<List<String>> processIterator() throws Exception {
        try {
            AnlysisDefaultContext context = this.getRequestData();
            //数据未完入库
            final Set<String> ids = iAysModelResltAnalysisService.findUnmigratedDataScop(context.getClientId());
            if (CollUtil.isEmpty(ids)) {
                log.warn("没有读取数据到源数据，无法继续执行");
                return Collections.EMPTY_LIST.iterator();
            }
            log.info("未完成入库数据范围: size: {}, ids : {}", ids.size(), ids);
//            context.setIds(ids);

            return CollUtil.split(ids, 1000).iterator();
        } catch (Exception e) {
            throw new RetryException(e.getMessage(), e);
        }
    }

    //异常时重跑
    @Override
    public void onError(Exception e) throws Exception {
        super.onError(e);
        log.error(e.getMessage(), e);
        throw new RetryException(e.getMessage(), e);
    }
}
