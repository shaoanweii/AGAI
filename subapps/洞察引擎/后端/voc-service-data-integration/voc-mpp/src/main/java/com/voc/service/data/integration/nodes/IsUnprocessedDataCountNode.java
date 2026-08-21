package com.voc.service.data.integration.nodes;

import com.voc.service.data.integration.api.IMppInputDataService;
import com.voc.service.data.integration.config.DataIntegrationConfig;
import com.yomahub.liteflow.annotation.LiteflowComponent;
import com.yomahub.liteflow.core.NodeIfComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;


@LiteflowComponent(id = "isUnprocessedDataCountNode", name = "判断数据清洗未处理量")
public class IsUnprocessedDataCountNode extends NodeIfComponent {

    private static final Logger log = LoggerFactory.getLogger(IsUnprocessedDataCountNode.class);
    @Autowired
    IMppInputDataService iMppInputDataService;
    @Autowired
    DataIntegrationConfig config;

    @Override
    public boolean processIf() throws Exception{
        try {
//            long count = iMppInputDataService.loadDataCount();
//            log.info("读取数据清洗原始数据未处理总数:{}",count);
//            if (count>config.getUnprocessedDataCount()) {
//                return false;
//            }
            return true;
        } catch (Exception e) {
            throw new Exception(e.getMessage(), e);
        }
    }

}
