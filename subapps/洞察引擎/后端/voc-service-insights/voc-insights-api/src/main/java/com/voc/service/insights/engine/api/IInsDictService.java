package com.voc.service.insights.engine.api;


import com.voc.service.common.response.Result;
import com.voc.service.insights.engine.model.InsDictItemModel;
import com.voc.service.insights.engine.model.InsDictModel;
import com.voc.service.insights.engine.model.TreeSelectModel;
import com.voc.service.insights.engine.vo.DictInfoVo;
import com.voc.service.insights.engine.vo.DictItemVo;
import com.voc.service.insights.engine.vo.DictVo;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 字典表 服务类
 * </p>
 *
 * @since 2018-12-28
 */
public interface IInsDictService {

    public List<DictVo> queryDictItemsByCode(String code);

    public Map<String, List<DictVo>> queryAllDictItems();


    List<DictVo> queryTableDictItemsByCode(String table, String text, String code);


    public List<DictVo> queryTableDictItemsByCodeAndFilter(String table, String text, String code, String filterSql);

    public String queryDictTextByKey(String code, String key);

    Result<?> queryBySelect(InsDictModel model);


    String queryTableDictTextByKey(String table, String text, String code, String key);


    List<String> queryTableDictByKeys(String table, String text, String code, String keys);

    /**
     * 根据字典类型删除关联表中其对应的数据
     *
     * @param sysDict
     * @return
     */
    boolean deleteByDictId(InsDictModel sysDict);

    /**
     * 添加一对多
     */
    public Integer saveMain(InsDictModel sysDict, List<InsDictItemModel> sysDictItemList);

    /**
     * 查询所有部门 作为字典信息 id -->value,departName -->text
     *
     * @return
     */
    public List<DictVo> queryAllDepartBackDictModel();

    /**
     * 查询所有用户  作为字典信息 username -->value,realname -->text
     *
     * @return
     */
    public List<DictVo> queryAllUserBackDictModel();

    /**
     * 通过关键字查询字典表
     *
     * @param table
     * @param text
     * @param code
     * @param keyword
     * @return
     */

    public List<DictVo> queryTableDictItems(String table, String text, String code, String keyword);

    /**
     * 根据表名、显示字段名、存储字段名 查询树
     *
     * @param table
     * @param text
     * @param code
     * @param pidField
     * @param pid
     * @param hasChildField
     * @return
     */

    List<TreeSelectModel> queryTreeList(Map<String, String> query, String table, String text, String flag, String code, String pidField, String pid, String hasChildField);

    Integer save(InsDictModel sysDictModel);

    /**
     * 真实删除
     *
     * @param id
     */
    public void deleteOneDictPhysically(String id);

    /**
     * 修改delFlag
     *
     * @param delFlag
     * @param id
     */
    public void updateDictDelFlag(int delFlag, String id);

    public Integer updateDict(InsDictModel sysDictModel);

    public void deleteList(List<Serializable> idList);

    /**
     * 查询被逻辑删除的数据
     *
     * @return
     */
    public List<InsDictModel> queryDeleteList();


    List<DictVo> queryDictItemsLanguageByCode(String all_users);


    List<DictInfoVo> findDictInfoByCode(String code);

    List<DictItemVo> getDictItemVoList(String dictType);

    List<DictInfoVo> findRelatedItems(String dictType);

}
