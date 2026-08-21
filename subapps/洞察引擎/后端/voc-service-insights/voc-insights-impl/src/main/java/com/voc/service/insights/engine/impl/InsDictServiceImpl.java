package com.voc.service.insights.engine.impl;

import cn.hutool.core.util.StrUtil;
import com.alicp.jetcache.anno.CacheType;
import com.alicp.jetcache.anno.Cached;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.voc.service.common.constant.CommonConstant;
import com.voc.service.common.constant.GlobalConstants;
import com.voc.service.common.exception.BussinessException;
import com.voc.service.common.exception.CommonErrorEnum;
import com.voc.service.common.response.Result;
import com.voc.service.common.util.CacheUtil;
import com.voc.service.common.util.ServiceContextHolder;
import com.voc.service.insights.engine.api.IInsDictService;
import com.voc.service.insights.engine.entity.InsDictEntity;
import com.voc.service.insights.engine.entity.InsDictInfoEntity;
import com.voc.service.insights.engine.entity.InsDictItemEntity;
import com.voc.service.insights.engine.impl.converts.InsConvertMapperService;
import com.voc.service.insights.engine.mapper.InsDictItemMapper;
import com.voc.service.insights.engine.mapper.InsDictMapper;
import com.voc.service.insights.engine.model.InsDictItemModel;
import com.voc.service.insights.engine.model.InsDictModel;
import com.voc.service.insights.engine.model.TreeSelectModel;
import com.voc.service.insights.engine.vo.DictInfoVo;
import com.voc.service.insights.engine.vo.DictItemVo;
import com.voc.service.insights.engine.vo.DictVo;
import org.apache.commons.lang3.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * 字典表 服务实现类
 * </p>
 *
 * @since 2018-12-28
 */
@Service
/*@SofaService(
        interfaceType = ISysDictService.class,
        bindings = {@SofaServiceBinding(bindingType = "jvm"), @SofaServiceBinding(bindingType = "bolt")
        })*/
public class InsDictServiceImpl extends ServiceImpl<InsDictMapper, InsDictEntity> implements IInsDictService {
    private static final Logger log = LoggerFactory.getLogger(InsDictServiceImpl.class);
    @Autowired
    InsConvertMapperService insConvertMapperService;
    @Autowired
    private InsDictMapper baseMapper;
    @Autowired
    private InsDictItemMapper insDictItemMapper;

    @Autowired
    RedisTemplate redisTemplate;
    static final String CACHE_KEY = "dict";

    static Cache<String, List<DictVo>> data_cache = Caffeine.newBuilder()
            // 设置缓存过期时间为10分钟
            .expireAfterWrite(GlobalConstants.TOKEN_EXPIRATION, GlobalConstants.TOKEN_TIME_UNIT)
//            .maximumSize(100)  // 设置最大缓存条目数
            .build();

    static Cache<String, List<DictInfoVo>> dict_cache = Caffeine.newBuilder()
            // 设置缓存过期时间为10分钟
            .expireAfterWrite(GlobalConstants.TOKEN_EXPIRATION, GlobalConstants.TOKEN_TIME_UNIT)
//            .maximumSize(100)  // 设置最大缓存条目数
            .build();

    /**
     * 通过查询指定code 获取字典
     *
     * @param code
     * @return
     */
    @Override
//    @Cacheable(value = CacheConstant.SYS_DICT_CACHE, key = "#code")
    public List<DictVo> queryDictItemsByCode(String code) {
        log.info("无缓存dictCache的时候调用这里！");
        return baseMapper.queryDictItemsByCode(code);
    }

    @Override
    public Map<String, List<DictVo>> queryAllDictItems() {
        Map<String, List<DictVo>> res = new HashMap<String, List<DictVo>>();
        List<InsDictEntity> ls = baseMapper.selectList(null);
        LambdaQueryWrapper<InsDictItemEntity> queryWrapper = new LambdaQueryWrapper<InsDictItemEntity>();
        queryWrapper.eq(InsDictItemEntity::getStatus, 1);
        queryWrapper.orderByAsc(InsDictItemEntity::getSortOrder);
        List<InsDictItemEntity> sysDictItemList = insDictItemMapper.selectList(queryWrapper);

        for (InsDictEntity d : ls) {
            List<DictVo> dictModelList = sysDictItemList.stream().filter(s -> d.getId().equals(s.getDictId())).map(item -> {
                DictVo dictModel = new DictVo();
                dictModel.setText(item.getItemText());
                dictModel.setValue(item.getItemValue());
                return dictModel;
            }).collect(Collectors.toList());
            res.put(d.getDictCode(), dictModelList);
        }
        log.info("-------登录加载系统字典-----" + res.toString());
        return res;
    }

    @Override
    public Result<?> queryBySelect(InsDictModel model) {
        Page<InsDictEntity> page = this.page(new Page<>(model.getPageNum(), model.getPageSize()), this.createQueryWrapper(model));
        return Result.OK(page);
    }

    private QueryWrapper<InsDictEntity> createQueryWrapper(InsDictModel insDictModel) {
        InsDictEntity entity = insConvertMapperService.converTo(insDictModel);
        QueryWrapper<InsDictEntity> queryWrapper = new QueryWrapper<>(entity);
        return queryWrapper;
    }

    /**
     * 通过查询指定code 获取字典值text
     *
     * @param code
     * @param key
     * @return
     */

    @Override
//    @Cacheable(value = CacheConstant.SYS_DICT_CACHE, key = "#code+':'+#key")
    public String queryDictTextByKey(String code, String key) {
        log.info("无缓存dictText的时候调用这里！");
        return baseMapper.queryDictTextByKey(code, key);
    }

    /**
     * 通过查询指定table的 text code 获取字典
     * dictTableCache采用redis缓存有效期10分钟
     *
     * @param table
     * @param text
     * @param code
     * @return
     */
    @Override
    //@Cacheable(value = CacheConstant.SYS_DICT_TABLE_CACHE)
    public List<DictVo> queryTableDictItemsByCode(String table, String text, String code) {
        log.info("无缓存dictTableList的时候调用这里！");
        return baseMapper.queryTableDictItemsByCode(table, text, code);
    }

    @Override
    public List<DictVo> queryTableDictItemsByCodeAndFilter(String table, String text, String code, String filterSql) {
        log.info("无缓存dictTableList的时候调用这里！");
        return baseMapper.queryTableDictItemsByCodeAndFilter(table, text, code, filterSql);
    }

    /**
     * 通过查询指定table的 text code 获取字典值text
     * dictTableCache采用redis缓存有效期10分钟
     *
     * @param table
     * @param text
     * @param code
     * @param key
     * @return
     */
    @Override
//    @Cacheable(value = CacheConstant.SYS_DICT_TABLE_CACHE)
    public String queryTableDictTextByKey(String table, String text, String code, String key) {
        log.info("无缓存dictTable的时候调用这里！");
        return baseMapper.queryTableDictTextByKey(table, text, code, key);
    }

    /**
     * 通过查询指定table的 text code 获取字典，包含text和value
     * dictTableCache采用redis缓存有效期10分钟
     *
     * @param table
     * @param text
     * @param code
     * @param keys  (逗号分隔)
     * @return
     */
    @Override
//    @Cacheable(value = CacheConstant.SYS_DICT_TABLE_BY_KEYS_CACHE)
    public List<String> queryTableDictByKeys(String table, String text, String code, String keys) {
        if (StrUtil.isEmpty(keys)) {
            return null;
        }
        String[] keyArray = keys.split(",");
        List<DictVo> dicts = baseMapper.queryTableDictByKeys(table, text, code, keyArray);
        List<String> texts = new ArrayList<>(dicts.size());
        // 查询出来的顺序可能是乱的，需要排个序
        for (String key : keyArray) {
            for (DictVo dict : dicts) {
                if (key.equals(dict.getValue())) {
                    texts.add(dict.getText());
                    break;
                }
            }
        }
        return texts;
    }


    /**
     * 根据字典类型id删除关联表中其对应的数据
     */
    @Override
    public boolean deleteByDictId(InsDictModel sysDict) {
        try {
            sysDict.setDelFlag(CommonConstant.DEL_FLAG_1);
            InsDictEntity entity = insConvertMapperService.converTo(sysDict);
            baseMapper.updateById(entity);

            return true;
        } catch (Exception e) {
            log.info(e.getMessage(), e);
        }
        return false;
    }

    @Override
    @Transactional
    public Integer saveMain(InsDictModel sysDict, List<InsDictItemModel> sysDictItemList) {
        int insert = 0;
        try {
            insert = baseMapper.insert(insConvertMapperService.converTo(sysDict));
            if (sysDictItemList != null) {
                for (InsDictItemModel model : sysDictItemList) {
                    model.setDictId(sysDict.getId());
                    model.setStatus(1);
                    insDictItemMapper.insert(insConvertMapperService.converTo(model));
                }
            }
        } catch (Exception e) {
            return insert;
        }
        return insert;
    }

    @Override
    public Integer save(InsDictModel insDictModel) {
        InsDictEntity insDictEntity = insConvertMapperService.converTo(insDictModel);
        final String username = ServiceContextHolder.getUsername();
        insDictEntity.setCreateTime(LocalDateTime.now());
        insDictEntity.setUpdateTime(LocalDateTime.now());
        insDictEntity.setOperator(username);
        insDictEntity.setDelFlag(0);
        insDictEntity.setType(0);
        int rs = baseMapper.insert(insDictEntity);
        return rs;
    }


    @Override
    public List<DictVo> queryAllDepartBackDictModel() {
        return baseMapper.queryAllDepartBackDictVo();
    }

    @Override
    public List<DictVo> queryAllUserBackDictModel() {
        return baseMapper.queryAllUserBackDictVo();
    }

    @Override
    public List<DictVo> queryTableDictItems(String table, String text, String code, String keyword) {
        return baseMapper.queryTableDictItems(table, text, code, "%" + keyword + "%");
    }

    @Override
    public List<TreeSelectModel> queryTreeList(Map<String, String> query, String table, String text, String flag, String code, String pidField, String pid, String hasChildField) {
        return baseMapper.queryTreeList(query, table, text, flag, code, pidField, pid, hasChildField);
    }

    @Override
    public void deleteOneDictPhysically(String id) {
        this.baseMapper.deleteOneById(id);
        this.insDictItemMapper.delete(new LambdaQueryWrapper<InsDictItemEntity>().eq(InsDictItemEntity::getDictId, id));
        //删除缓存
        //删除缓存
        CacheUtil.cleanCache(CACHE_KEY, data_cache, dict_cache);
    }

    @Override
    public void updateDictDelFlag(int delFlag, String id) {
        baseMapper.updateDictDelFlag(delFlag, id);
        //删除缓存
        //删除缓存
        CacheUtil.cleanCache(CACHE_KEY, data_cache, dict_cache);
    }

    @Override
    public Integer updateDict(InsDictModel insDictModel) {
        int rs = this.baseMapper.updateById(insConvertMapperService.converTo(insDictModel));
        //删除缓存
        //删除缓存
        CacheUtil.cleanCache(CACHE_KEY, data_cache, dict_cache);
        return rs;
    }

    @Override
    public void deleteList(List<Serializable> idList) {

        this.baseMapper.deleteBatchIds(idList);
        //删除缓存
        //删除缓存
        CacheUtil.cleanCache(CACHE_KEY, data_cache, dict_cache);
    }

    @Override
    public List<InsDictModel> queryDeleteList() {

        List<InsDictModel> list = baseMapper.queryDeleteList().stream()
                .map(e -> insConvertMapperService.converTo(e))
                .collect(Collectors.toList());

        return list;
    }


    @Override
//    @Cacheable(value = CacheConstant.SYS_DICT_CACHE, key = "#code")
    public List<DictVo> queryDictItemsLanguageByCode(String code) {
        log.info("无缓存dictCache的时候调用这里！");
        return baseMapper.queryDictItemsLanguageByCode(code);
    }

    @Override
    @Cached( area="VDP" ,name = ":dict:", key = "#code", expire = 60 * 60, cacheType = CacheType.REMOTE )
    public List<DictInfoVo> findDictInfoByCode(String code) {
        if (StrUtil.isEmpty(code)) {
            throw new BussinessException(CommonErrorEnum.VALIDATION_EXECPTION, "code 不允许为空");
        }
        log.trace("读取数据库");
        List<InsDictInfoEntity> dictInfo = baseMapper.findDictInfo(code);
        if (ObjectUtils.isEmpty(dictInfo)) {
            log.info("暂无字典数据");
            return Collections.EMPTY_LIST;
        }
        List<DictInfoVo> list = insConvertMapperService.dictEntityListConvertVoList(dictInfo);
        return list;
    }

    @Override
    @Cached( area="VDP" ,name = ":dict:", key = "#dictType", expire = 60 * 60, cacheType = CacheType.REMOTE )
    public List<DictItemVo> getDictItemVoList(String dictType) {
        Assert.hasLength(dictType, "dictType 不允许为空");
        List<InsDictItemEntity> dictItemVoList = baseMapper.getDictItemVoList(dictType);
        if (ObjectUtils.isEmpty(dictItemVoList)) {
            log.info("暂无{}字典数据", dictType);
            return Collections.EMPTY_LIST;
        }
        List<DictItemVo> dictItemVos = insConvertMapperService.dictItemEntityListConvertVoList(dictItemVoList);
        return dictItemVos;
    }

    @Override
    public List<DictInfoVo> findRelatedItems(String dictType) {
        List<InsDictInfoEntity> relatedItems = baseMapper.findRelatedItems(dictType);
        if (ObjectUtils.isEmpty(relatedItems)) {
            log.info("暂无{}字典数据", dictType);
            return Collections.EMPTY_LIST;
        }
        relatedItems = relatedItems.stream().filter(e->ObjectUtils.isNotEmpty(e)).collect(Collectors.toList());
        List<DictInfoVo> list = insConvertMapperService.dictEntityListConvertVoList(relatedItems);
        return list;
    }

}
