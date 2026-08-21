package com.voc.service.security.impl;

import cn.hutool.cache.CacheUtil;
import cn.hutool.cache.impl.TimedCache;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONArray;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.voc.service.common.exception.BussinessException;
import com.voc.service.common.exception.CommonErrorEnum;
import com.voc.service.security.api.IAppService;
import com.voc.service.security.impl.converts.SecurityConverMapperService;
import com.voc.service.security.impl.entity.AppEntity;
import com.voc.service.security.impl.mapper.AppMapper;
import com.voc.service.security.model.AppModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author ckcui
 * @version 1.0.0
 * @ClassName AppService
 * @Description ckcui
 * @createTime 2023年11月30日 13:59
 * @Copyright futong
 */
@Service
public class AppService extends ServiceImpl<AppMapper, AppEntity> implements IAppService {
    public static TimedCache<String, AppModel> MODEL_CACHE = CacheUtil.newTimedCache(1000 * 60 * 60 * 24 * 365);
//    public static Map<String, AppModel> MODEL_CACHE = new HashMap<>();
private static final Logger logger = LoggerFactory.getLogger(AppService.class);
    @Autowired
    SecurityConverMapperService securityConverMapperService;
    static List<AppModel> list = new ArrayList<>();

    static {
        list.add(AppModel.builder().appId("insights").build());
        list.add(AppModel.builder().appId("report-cqca").build());
    }


    @Override
    public AppModel find(AppModel model) {
//        Assert.notNull(model.getAppId(), "app_id cannot be empty");
        Assert.isTrue(StrUtil.isNotBlank(model.getAppId()), "app_id cannot be empty");

//        final List<AppModel> list = this.findAll();
        logger.info("appId: {}", model.getAppId());
        if(ObjectUtil.isEmpty(list)){
            list.add(AppModel.builder().appId("insights").build());
            list.add(AppModel.builder().appId("report-cqca").build());
        }
        logger.info("返回所有 AppModel: {}", JSONArray.toJSONString(list));


        return list.stream().filter(e -> StrUtil.isNotBlank(e.getAppId()))
                .filter(e -> e.getAppId().equalsIgnoreCase(model.getAppId()))
                .findFirst()
                .orElseThrow(() -> new BussinessException(CommonErrorEnum.APP_ID_DISABLE));
    }

    @Override
    public List<AppModel> findAll() {
        if (!MODEL_CACHE.isEmpty()) {
            logger.info("从缓存中读取 MODEL_CACHE");
            List<AppModel> collect = MODEL_CACHE.keySet().stream().map(key -> MODEL_CACHE.get(key)).collect(Collectors.toList());
            logger.info("返回所有 AppModel: {}", JSONArray.toJSONString(collect));
            return collect;
        }

        final List<AppEntity> list = Optional.ofNullable(baseMapper.selectList(new QueryWrapper<>()))
                .orElseThrow(() -> new BussinessException(CommonErrorEnum.APP_ID_DISABLE));
        logger.info("从数据库获取 AppEntity: {}", JSONArray.toJSONString(list));

        final List<AppModel> modelList = list.stream().filter(ObjectUtil::isNotNull).map(entity -> securityConverMapperService.converTo(entity)).collect(Collectors.toList());
        if (CollUtil.isNotEmpty(modelList)) {
            modelList.stream().forEach(e -> {
                logger.info("modelList中appId的值:{}",e.getAppId());
                MODEL_CACHE.put(e.getAppId(), e);
            });
        } else {
            new BussinessException(CommonErrorEnum.APP_ID_DISABLE);
        }
        return modelList;
    }

    @Override
    public void add(AppModel app) {
        MODEL_CACHE.put(app.getAppId(), app);
    }

    @Override
    public String getAppIdByURL(String redirect) {
        Assert.isTrue(StrUtil.isNotBlank(redirect), "redirect cannot be empty");
        final Optional<String> appId = this.findAll().stream().filter(ObjectUtil::isNotNull)
                .filter(e -> StrUtil.startWith(e.getUrls(), redirect))
                .map(AppModel::getAppId)
                .collect(Collectors.toSet()).stream().findFirst();
        logger.info("返回对应 appId: {}, url:{}", appId.get(), redirect);
        return appId.get();
    }
}
