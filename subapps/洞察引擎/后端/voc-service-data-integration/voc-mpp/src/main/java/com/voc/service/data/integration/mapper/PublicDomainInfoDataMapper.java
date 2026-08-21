package com.voc.service.data.integration.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.voc.service.data.integration.entity.ChannelInfoDataEntity;
import com.voc.service.data.integration.entity.PublicDomainInfoDataEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;


@Mapper
@Repository
public interface PublicDomainInfoDataMapper extends BaseMapper<PublicDomainInfoDataEntity> {

//    Set<String> findAllIds();

    List<PublicDomainInfoDataEntity> findByIds(@Param("ids") final Set<String> ids);
}
