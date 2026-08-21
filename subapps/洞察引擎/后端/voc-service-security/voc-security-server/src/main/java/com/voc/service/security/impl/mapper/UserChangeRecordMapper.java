package com.voc.service.security.impl.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.voc.service.security.impl.entity.UserChangeRecordEntity;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

/**
 * @创建者: fanrong
 * @创建时间: 2024/7/31 上午11:25
 * @描述:
 **/
@Mapper
@Repository
public interface UserChangeRecordMapper extends BaseMapper<UserChangeRecordEntity> {
}
