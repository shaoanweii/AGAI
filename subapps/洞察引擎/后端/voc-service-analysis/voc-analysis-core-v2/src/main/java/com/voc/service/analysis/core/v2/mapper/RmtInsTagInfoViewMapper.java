package com.voc.service.analysis.core.v2.mapper;

import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.util.Map;


@Mapper
@Repository
public interface RmtInsTagInfoViewMapper {

    @MapKey("key")
    Map<String, Object> getTagList();

    String getUserJourneyList(String tagName);
}
