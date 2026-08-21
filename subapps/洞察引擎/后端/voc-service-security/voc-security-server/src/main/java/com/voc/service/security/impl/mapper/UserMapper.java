package com.voc.service.security.impl.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.voc.service.common.model.UserModel;
import com.voc.service.security.impl.entity.UserEntity;
import com.voc.service.security.model.CredentialsModel;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Set;

public interface UserMapper extends BaseMapper<UserEntity> {

    UserEntity selectByIdentifier(CredentialsModel param);
    UserEntity selectByUserId(CredentialsModel param);

    boolean enable(UserEntity entity);

    List<UserEntity> getRemoveTestUsers(@Param("identifier") String identifier);
    List<UserEntity> selectAll(UserModel userModel);

    int removeTestUsers(@Param("list") Set<String> list);

    List<UserEntity> selectByConditional(@Param("userModel")UserModel userModel);
    Integer selectCountByConditional(@Param("userModel")UserModel userModel);
    List<UserEntity> selectUserByUserId(@Param("userModel")UserModel userModel);
}
