package com.voc.service.insights.engine.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.voc.service.common.response.Result;
import com.voc.service.common.util.ServiceContextHolder;
import com.voc.service.insights.engine.api.IInsDictItemService;
import com.voc.service.insights.engine.api.IInsDictService;
import com.voc.service.insights.engine.entity.InsDictItemEntity;
import com.voc.service.insights.engine.impl.converts.InsConvertMapperService;
import com.voc.service.insights.engine.mapper.InsDictItemMapper;
import com.voc.service.insights.engine.model.InsDictItemModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
/*@SofaService(
        interfaceType = IInsDictItemService.class,
        bindings = {@SofaServiceBinding(bindingType = "jvm"), @SofaServiceBinding(bindingType = "bolt")
        })*/
public class InsDictItemServiceImpl extends ServiceImpl<InsDictItemMapper, InsDictItemEntity> implements IInsDictItemService {

    @Autowired
    InsConvertMapperService insConvertMapperService;
    @Autowired
    IInsDictService insDictService;
    @Autowired
    private InsDictItemMapper baseMapper;

    @Override
    public List<InsDictItemModel> selectItemsByMainId(String mainId) {
        List<InsDictItemModel> list = baseMapper.selectItemsByMainId(mainId)
                .stream().map(e -> insConvertMapperService.converTo(e))
                .collect(Collectors.toList());
        return list;
    }

    @Override
    public List<InsDictItemModel> getInsDictItemByItemValueAndDictId(String key, String dictId) {
        if (StrUtil.isBlank(key) && StrUtil.isBlank(dictId)) {
            return null;
        }
        QueryWrapper<InsDictItemEntity> insDictItemQueryWrapper = new QueryWrapper<>();
        if (StrUtil.isNotBlank(key)) {
            insDictItemQueryWrapper.lambda().eq(InsDictItemEntity::getItemValue, key);
        }
        if (StrUtil.isNotBlank(dictId)) {
            insDictItemQueryWrapper.lambda().eq(InsDictItemEntity::getDictId, dictId);
        }

        List<InsDictItemEntity> insDictItems = baseMapper.selectList(insDictItemQueryWrapper);
        if (CollUtil.isNotEmpty(insDictItems)) {
            List<InsDictItemModel> list = insDictItems
                    .stream().map(e -> insConvertMapperService.converTo(e))
                    .collect(Collectors.toList());
            return list;
        }

        return null;
    }

    @Override
    public String getInsDictItemTextByItemValueAndDictId(String itemValue, String dictId) {
        if (StrUtil.isBlank(itemValue) && StrUtil.isBlank(dictId)) {
            return null;
        }
        QueryWrapper<InsDictItemEntity> insDictItemQueryWrapper = new QueryWrapper<>();
        if (StrUtil.isNotBlank(itemValue)) {
            insDictItemQueryWrapper.lambda().eq(InsDictItemEntity::getItemValue, itemValue);
        }
        if (StrUtil.isNotBlank(dictId)) {
            insDictItemQueryWrapper.lambda().eq(InsDictItemEntity::getDictId, dictId);
        }

        List<InsDictItemEntity> insDictItems = baseMapper.selectList(insDictItemQueryWrapper);
        if (null != insDictItems && insDictItems.size() > 0) {
            return insDictItems.get(0).getItemText();
        }

        return "";
    }

    @Override
    public Result<JSONObject> insAllDictItems() {
        JSONObject obj = new JSONObject();
        obj.putOpt("insAllDictItems", insDictService.queryAllDictItems());
        return Result.OK(obj);
    }

    @Override
    public Integer save(InsDictItemModel insDictItemModel) {
        InsDictItemEntity dictItemEntity = insConvertMapperService.converTo(insDictItemModel);
        final String username = ServiceContextHolder.getUsername();
        dictItemEntity.setCreateTime(LocalDateTime.now());
        dictItemEntity.setOperator(username);
        return baseMapper.insert(dictItemEntity);
    }

    @Override
    public Integer updateByDictId(InsDictItemModel insDictItemModel) {
        InsDictItemEntity dictItemEntity = insConvertMapperService.converTo(insDictItemModel);
        QueryWrapper<InsDictItemEntity> updateWrapper = new QueryWrapper<>();
        return baseMapper.update(dictItemEntity, updateWrapper);
    }

    @Override
    public void deleteList(List<Serializable> idList) {
        this.baseMapper.deleteBatchIds(idList);
    }

    @Override
    public Result<?> queryBySelect(InsDictItemModel model) {
        PageHelper.startPage(model.getPageNum(), model.getPageSize());
        List<InsDictItemEntity> list = this.list(this.createQueryWrapper(model));
        PageInfo page = new PageInfo<>(list);
        return Result.OK(page);
    }

    @Override
    public InsDictItemModel getById(String id) {
        return insConvertMapperService.converTo(baseMapper.selectById(id));
    }

    private QueryWrapper<InsDictItemEntity> createQueryWrapper(InsDictItemModel insDictModel) {
        InsDictItemEntity entity = insConvertMapperService.converTo(insDictModel);
        QueryWrapper<InsDictItemEntity> queryWrapper = new QueryWrapper<>(entity);
        return queryWrapper;
    }

}
