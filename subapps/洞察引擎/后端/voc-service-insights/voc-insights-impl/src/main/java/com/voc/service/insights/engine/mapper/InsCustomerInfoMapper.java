package com.voc.service.insights.engine.mapper;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.voc.service.insights.engine.entity.InsCustomerInfoEntity;
import com.voc.service.insights.engine.model.InsCustomerInfoModel;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @创建者: fanrong
 * @创建时间: 2024/2/22 10:23
 * @描述:
 **/
@Mapper
@Repository
public interface InsCustomerInfoMapper extends BaseMapper<InsCustomerInfoEntity> {

    void deleteCustomerInfo(@Param("id") String id, @Param("del") Integer del, @Param("updateTime") LocalDateTime updateTime);

    InsCustomerInfoEntity findCustomerInfo(@Param("id") String id);

    List<InsCustomerInfoEntity> findAllCustomerList();

    List<InsCustomerInfoEntity> findCustomerListByCondition(@Param("customerInfoModel") InsCustomerInfoModel customerInfoModel);

    Integer checkCustomerCode(@Param("customerInfoModel") InsCustomerInfoModel customerInfoModel);

    List<JSONObject> findTest(List<String> colums);

    void updateCustomerInfo(@Param("customerInfo") InsCustomerInfoEntity customerInfo);
}
