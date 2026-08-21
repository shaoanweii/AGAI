package com.voc.service.insights.engine.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.voc.service.insights.engine.entity.InsMenuPermsInfoEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @创建者: fanrong
 * @创建时间: 2024/3/4 17:33
 * @描述:
 **/
@Mapper
@Repository
public interface InsMenuPermsInfoMapper extends BaseMapper<InsMenuPermsInfoEntity> {

    List<InsMenuPermsInfoEntity> findMenuPermsInfoByUserId(@Param("userId") String userId);

    void deleteMenuPerms(@Param("userId") String userId);
}
