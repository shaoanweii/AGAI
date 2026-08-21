package com.voc.service.insights.engine.api;


import cn.hutool.json.JSONObject;
import com.voc.service.common.response.Result;
import com.voc.service.insights.engine.model.InsDictItemModel;

import java.io.Serializable;
import java.util.List;

/**
 * <p>
 * 服务类
 * </p>
 *
 * @since 2018-12-28
 */
public interface IInsDictItemService {
    public List<InsDictItemModel> selectItemsByMainId(String mainId);

    List<InsDictItemModel> getInsDictItemByItemValueAndDictId(String key, String dictId);

    String getInsDictItemTextByItemValueAndDictId(String itemValue, String dictId);

    Result<JSONObject> insAllDictItems();

    Integer save(InsDictItemModel insDictItemModel);
    Integer updateByDictId(InsDictItemModel insDictItemModel);
    void  deleteList(List<Serializable> idList);
    Result<?> queryBySelect(InsDictItemModel model);

    InsDictItemModel getById(String id);


}
