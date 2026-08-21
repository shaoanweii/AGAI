package com.voc.service.security.impl.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.voc.service.security.impl.entity.CredentialsChangeRecordEntity;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

/**
 * @创建者: fanrong
 * @创建时间: 2024/7/31 上午10:17
 * @描述:
 **/
@Mapper
@Repository
public interface CredentialsChangeRecordMapper extends BaseMapper<CredentialsChangeRecordEntity> {
}
