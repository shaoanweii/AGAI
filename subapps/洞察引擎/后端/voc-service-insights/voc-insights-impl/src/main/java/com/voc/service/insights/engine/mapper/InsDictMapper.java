package com.voc.service.insights.engine.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.voc.service.insights.engine.entity.InsDictEntity;
import com.voc.service.insights.engine.entity.InsDictInfoEntity;
import com.voc.service.insights.engine.entity.InsDictItemEntity;
import com.voc.service.insights.engine.entity.InsVehicleInfoEntity;
import com.voc.service.insights.engine.model.TreeSelectModel;
import com.voc.service.insights.engine.vo.DictQueryVo;
import com.voc.service.insights.engine.vo.DictVo;
import com.voc.service.insights.engine.vo.DuplicateCheckVo;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 字典表 Mapper 接口
 * </p>
 *
 * @since 2018-12-28
 */
@Repository
public interface InsDictMapper extends BaseMapper<InsDictEntity> {

    /**
     * 重复检查SQL
     *
     * @return
     */
    public Long duplicateCheckCountSql(DuplicateCheckVo duplicateCheckVo);

    public Long duplicateCheckCountSqlNoDataId(DuplicateCheckVo duplicateCheckVo);

    public List<DictVo> queryDictItemsByCode(@Param("code") String code);

    @Deprecated
    public List<DictVo> queryTableDictItemsByCode(@Param("table") String table, @Param("text") String text, @Param("code") String code);

    @Deprecated
    public List<DictVo> queryTableDictItemsByCodeAndFilter(@Param("table") String table, @Param("text") String text, @Param("code") String code, @Param("filterSql") String filterSql);

    public String queryDictTextByKey(@Param("code") String code, @Param("key") String key);

    @Deprecated
    public String queryTableDictTextByKey(@Param("table") String table, @Param("text") String text, @Param("code") String code, @Param("key") String key);

    @Deprecated
    public List<DictVo> queryTableDictByKeys(@Param("table") String table, @Param("text") String text, @Param("code") String code, @Param("keyArray") String[] keyArray);

    /**
     * 查询所有部门 作为字典信息 id -->value,departName -->text
     *
     * @return
     */
    public List<DictVo> queryAllDepartBackDictVo();

    /**
     * 查询所有用户  作为字典信息 username -->value,realname -->text
     *
     * @return
     */
    public List<DictVo> queryAllUserBackDictVo();

    /**
     * 通过关键字查询出字典表
     *
     * @param table
     * @param text
     * @param code
     * @param keyword
     * @return
     */
    @Deprecated
    public List<DictVo> queryTableDictItems(@Param("table") String table, @Param("text") String text, @Param("code") String code, @Param("keyword") String keyword);

    /**
     * 根据表名、显示字段名、存储字段名 查询树
     *
     * @param table
     * @param text
     * @param code
     * @param pid
     * @param hasChildField
     * @return
     */
    @Deprecated
    List<TreeSelectModel> queryTreeList(@Param("query") Map<String, String> query, @Param("table") String table, @Param("text") String text, @Param("flag") String flag, @Param("code") String code, @Param("pidField") String pidField, @Param("pid") String pid, @Param("hasChildField") String hasChildField);

    /**
     * 删除
     *
     * @param id
     */
    @Select("delete from sys_dict where id = #{id}")
    public void deleteOneById(@Param("id") String id);

    /**
     * 查询被逻辑删除的数据
     *
     * @return
     */
    @Select("select * from sys_dict where del_flag = 1")
    public List<InsDictEntity> queryDeleteList();

    /**
     * 修改状态值
     *
     * @param delFlag
     * @param id
     */
    @Update("update sys_dict set del_flag = #{flag,jdbcType=INTEGER} where id = #{id,jdbcType=VARCHAR}")
    public void updateDictDelFlag(@Param("flag") int delFlag, @Param("id") String id);


    /**
     * 分页查询字典表数据
     *
     * @param page
     * @param query
     * @return
     */
    @Deprecated
    public Page<DictVo> queryDictTablePageList(Page page, @Param("query") DictQueryVo query);

    public List<DictVo> queryDictItemsLanguageByCode(@Param("code") String code);

    List<DictVo> findDictItemsByCode(@Param("code") String code);

    /**
     * @return java.util.List<com.voc.service.insights.setting.entity.InsEnergyInfoEntity>
     * @创建者/修改者 fanrong
     * @创建/更新日期 2024/2/20 10:27
     * @描述 根据code查询下拉列表数据
     **/
    List<InsDictInfoEntity> findDictInfo(@Param("energyType") String energyType);

    /**
     * @return java.util.List<com.voc.service.insights.engine.entity.InsVehicleInfoEntity>
     * @创建者/修改者 fanrong
     * @创建/更新日期 2024/2/23 14:10
     * @描述 查询车辆信息
     **/
    List<InsVehicleInfoEntity> findVehicleInfo();

    List<InsDictItemEntity> getDictItemVoList(@Param("dictType") String dictType);

    List<InsDictInfoEntity> findRelatedItems(@Param("dictType") String dictType);

    List<InsDictInfoEntity> findDictInfoList();

}
