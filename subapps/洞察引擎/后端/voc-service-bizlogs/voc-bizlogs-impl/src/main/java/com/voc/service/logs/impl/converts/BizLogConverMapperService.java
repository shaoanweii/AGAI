package com.voc.service.logs.impl.converts;

import com.voc.service.logs.impl.entity.OpsRecordLogEntity;
import com.voc.service.logs.model.OpsLogModel;
import org.mapstruct.*;

import java.util.List;

/**
 * @author ckcui
 * @version 1.0.0
 * @ClassName OpsConverMapper
 * @Description ckcui
 * @createTime 2023年09月12日 9:52
 * @Copyright futong
 */
@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface BizLogConverMapperService {
    //    SecurityConverMapperService instace = Mappers.getMapper(SecurityConverMapperService.class);

    OpsRecordLogEntity converTo(OpsLogModel user);

    List<OpsLogModel> converToModelList(List<OpsRecordLogEntity> records);
}
