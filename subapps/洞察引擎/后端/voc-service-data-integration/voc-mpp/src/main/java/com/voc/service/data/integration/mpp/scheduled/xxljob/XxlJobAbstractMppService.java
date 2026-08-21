package com.voc.service.data.integration.mpp.scheduled.xxljob;

import cn.hutool.core.util.StrUtil;
import com.voc.service.data.integration.mpp.AbstractMppService;
import com.xxl.job.core.context.XxlJobHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @Title: XxlJobAbstractMppService
 * @Package: com.voc.service.data.integration.in.scheduled.xxljob
 * @Description:
 * @Author: cuick
 * @Date: 2024/9/24 10:51
 * @Version:1.0
 */
public abstract class XxlJobAbstractMppService extends AbstractMppService {

    private static final Logger log = LoggerFactory.getLogger(XxlJobAbstractMppService.class);

    @Override
    public void log(String msg) {
        XxlJobHelper.log(msg);
        log.info(msg);
    }
    @Override
    public void log(String msg,Object...args) {
        XxlJobHelper.log(StrUtil.format(msg,args));
        log.info(StrUtil.format(msg,args));
    }

    @Override
    public void log(Throwable e) {
        XxlJobHelper.log(e);
        log.error(e.getMessage(),e);
    }
}
