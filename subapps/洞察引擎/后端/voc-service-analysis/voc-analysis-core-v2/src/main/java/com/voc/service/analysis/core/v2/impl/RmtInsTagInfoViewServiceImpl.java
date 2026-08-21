package com.voc.service.analysis.core.v2.impl;

import com.voc.service.analysis.api.IRmtInsTagInfoViewService;
import com.voc.service.analysis.core.v2.mapper.RmtInsTagInfoViewMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Map;


@Service
public class RmtInsTagInfoViewServiceImpl implements IRmtInsTagInfoViewService {

    @Resource
    private RmtInsTagInfoViewMapper rmtInsTagInfoViewMapper;


    @Override
    public Map<String, Object> getTagList() {
        return rmtInsTagInfoViewMapper.getTagList();
    }

    @Override
    public String getUserJourneyList(String tagName) {
        return rmtInsTagInfoViewMapper.getUserJourneyList(tagName);
    }
}
