package com.voc.service.security.impl.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.voc.service.security.impl.entity.CredentialsEntity;
import com.voc.service.security.model.CredentialsModel;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Set;

public interface CredentialsMapper extends BaseMapper<CredentialsEntity> {

    int removeTestUsers(@Param("list") Set<String> list);
    List<CredentialsEntity> selectByUserIds(@Param("list") Set<String> list, @Param("appId")String appId);
}
