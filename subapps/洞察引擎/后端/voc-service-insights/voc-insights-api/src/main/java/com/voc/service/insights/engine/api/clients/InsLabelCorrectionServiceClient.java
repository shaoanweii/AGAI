package com.voc.service.insights.engine.api.clients;

import com.voc.service.common.exception.BussinessException;
import com.voc.service.common.exception.CommonErrorEnum;
import com.voc.service.common.response.Result;
import com.voc.service.insights.engine.api.model.InsCqCaLabelCorrectionRecordModel;
import com.voc.service.insights.engine.model.InsLabelCorrectionRecordQueryModel;
import com.voc.service.insights.engine.model.InsTagLibClientModel;
import com.voc.service.insights.engine.model.data.InsCqCaDataQueryModel;
import com.voc.service.insights.engine.vo.TagLibClientTreeVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;


@FeignClient(name = "service.ins.labelCorrection", url = "${service.ins.v1}", configuration = InsDataServiceClientConfig.class)
public interface InsLabelCorrectionServiceClient {

    @PostMapping("/addLabel/insertLabelCorrection")
    Result<?> insertLabelCorrection(@RequestBody InsCqCaLabelCorrectionRecordModel model);

    @PostMapping("/insCqCaDataSource/getResultData")
    Result<?> getResultData(@RequestBody InsCqCaDataQueryModel InsCqCaDataQueryModel);

    @PostMapping("/insCqCaDataSource/getSentimentResultData")
    Result<?> getSentimentResultData(@RequestBody InsCqCaDataQueryModel insCqCaDataQueryModel);


    @PostMapping("/insCqCaDataSource/queryCarSeriesList")
    Result<?> queryCarSeriesList(@RequestBody InsCqCaDataQueryModel InsCqCaDataQueryModel);

    @PostMapping("/insCqCaDataSource/findAllFinalTagLibClientVoList")
    Result<?> findAllFinalTagLibClientVoList(@RequestBody InsCqCaDataQueryModel InsCqCaDataQueryModel);

    @PostMapping("/insCqCaDataSource/queryBrandList")
    Result<?> queryBrandList(@RequestBody InsCqCaDataQueryModel InsCqCaDataQueryModel);

    @PostMapping("/insTagLibClient/findAllFinalTagLibClientVoList")
    Result<List<TagLibClientTreeVo>> findAllTopicList(@RequestBody InsTagLibClientModel tagLibClientModel);
}
