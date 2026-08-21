package com.voc.service.analysis.api;

import java.util.Map;



public interface IRmtInsTagInfoViewService {


    Map<String, Object> getTagList();

    String getUserJourneyList(String tagName);
}
