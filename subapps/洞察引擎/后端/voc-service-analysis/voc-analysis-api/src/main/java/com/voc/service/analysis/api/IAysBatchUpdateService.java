package com.voc.service.analysis.api;

import com.voc.service.analysis.model.ModifyDataModel;

public interface IAysBatchUpdateService {
    void modifyResultdata() throws Exception;

    void modifyResultdataD() throws Exception;

    void moveResultDataToFinalTable() throws Exception;

    void save(ModifyDataModel model) throws Exception;
}
