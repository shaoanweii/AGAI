package com.voc.service.data.integration.api;

import java.util.Set;

/**
 * @Title: IModelProcessedResultData
 * @Package: com.voc.service.data.integration.api
 * @Description:
 * @Author: cuick
 * @Date: 2024/11/12 13:19
 * @Version:1.0
 */
public interface IModelOutputRecordService {

    long record(Set<String> channelList, Set<String> attrs);
}
