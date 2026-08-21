package com.voc.service.insights.engine.impl.converts;

import com.voc.service.insights.engine.entity.*;
import com.voc.service.insights.engine.model.*;
import com.voc.service.insights.engine.vo.*;
import org.mapstruct.*;

import java.util.List;

/**
 * @author ckcui
 * @version 1.0.0
 * @ClassName InsConverMapper
 * @Description ckcui
 * @createTime 2023年09月12日 9:52
 * @Copyright futong
 */
@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface InsConvertMapperService {

    InsDictItemEntity converTo(InsDictItemModel o);

    InsDictItemModel converTo(InsDictItemEntity o);

    InsDictEntity converTo(InsDictModel o);

    InsDictModel converTo(InsDictEntity o);

    InsBusinessTagModel converTo(InsBusinessTagEntity o);

    InsProvinceAreaEntity convertTo(InsProvinceAreaModel o);

    InsProvinceAreaModel convertTo(InsProvinceAreaEntity o);

    InsMenuModel converTo(InsMenuEntity o);

    InsMenuEntity converTo(InsMenuModel o);

    InsBrandInfoModel converTo(InsBrandInfoEntity o);

    InsBrandInfoEntity converTo(InsBrandInfoModel o);

    InsCarSeriesInfoEntity converTo(InsCarSeriesInfoModel o);

    InsCarSeriesInfoModel converTo(InsCarSeriesInfoEntity o);

    List<InsProvinceAreaModel> convertToList(List<InsProvinceAreaEntity> records);

    InsTagInfoEntity converTo(InsTagInfoModel o);

    InsTagInfoModel converTo(InsTagInfoEntity o);

    InsTagClientEntity converTo(InsTagClientModel o);

    InsTagClientModel converTo(InsTagClientEntity o);

    InsProjectInfoModel converTo(InsProjectInfoEntity o);

    InsProjectInfoEntity converTo(InsProjectInfoModel o);

    List<InsTagClientEntity> convertTagClientEntityToList(List<InsTagClientModel> model);

    List<InsTagClientModel> convertTagClientModelToList(List<InsTagClientEntity> entity);

    List<InsTagInfoEntity> convertTagInfoEntityToList(List<InsTagInfoModel> model);

    List<InsBrandInfoModel> brandConvertToList(List<InsBrandInfoEntity> entityList);

    List<InsCarSeriesInfoModel> seriesConvertToList(List<InsCarSeriesInfoEntity> entityList);


    List<VehicleInfoVo> vehicleEntityListConvertVoList(List<InsVehicleInfoEntity> vehicleInfoEntityList);

    List<CarTypeVo> dictEntityListConvertCarVoList(List<InsDictInfoEntity> energyInfo);

    List<DictInfoVo> dictEntityListConvertVoList(List<InsDictInfoEntity> energyInfoEntityList);

    List<EnergyInfoVo> dictEntityListConvertEnergyEntityList(List<InsDictInfoEntity> energyInfoEntityList);

    List<ProvinceAreaVo> provinceAreaEntityListConvertVoList(List<InsProvinceAreaInfoEntity> provinceAreaInfoEntityList);

    List<ChannelInfoVo> channelEntityListConvertVoList(List<InsChannelInfoEntity> insChannelInfoEntityList);

    InsCustomerInfoEntity customerInfoModelConvertEntity(InsCustomerInfoModel insCustomerInfoModel);

    CustomerInfoVo customerInfoEntityConvertVo(InsCustomerInfoEntity customerInfoEntity);

    List<CustomerInfoVo> customerInfoEntityListConvertVoList(List<InsCustomerInfoEntity> customerInfoEntityList);

    List<InsTagInfoModel> tageInfoEntityListConvertModelList(List<InsTagInfoEntity> tageInfoEntityList);

    List<InsMenuPermsInfoEntity> menuPermsInfoModelListConvertEntityList(List<InsMenuPermsInfoModel> menuPermsInfoModel);

    List<InsMenuPermsInfoVo> menuPermsInfoEntitylListConvertVoList(List<InsMenuPermsInfoEntity> menuPermsInfoEntities);

    List<InsProjectInfoModel> projectInfoEntityListConvertModelList(List<InsProjectInfoEntity> projectInfoEntityList);

    List<DictItemVo> dictItemEntityListConvertVoList(List<InsDictItemEntity> insDictItemEntityList);

    InsTagLibEntity tagLibModelConvertEntity(InsTagLibModel insTagLibModel);

    TagLibVo tagLibEntityConvertVo(InsTagLibEntity insTagLibEntity);

    InsTagLibClientEntity tagLibClientModelConvertEntity(InsTagLibClientModel insTagLibClientModel);

    @Mapping(target = "operateUser", source = "updateUser")
    TagLibClientVo tagLibClientEntityConvertVo(InsTagLibClientEntity insTagLibClientEntity);

    List<TagLibCategoryVo> tagLibEntityListConvertCategoryVoList(List<InsTagLibEntity> insTagLibEntityList);

    List<TagLibCategoryVo> tagLibClientEntityListConvertCategoryVoList(List<InsTagLibClientEntity> tagLibClientList);

    InsChannelInfoEntity channelInfoModelConvertEntity(InsChannelInfoModel insChannelInfoModel);

    List<InsRoleRelationPermissionModel> roleRelationEntityListConvertVoList(List<InsRoleRelationPermissionEntity> insRoleRelationPermissionEntityList);

    List<CarInfoVo> carConvertToVo(List<InsCarSeriesInfoModel> insCarSeriesInfoModels);

    List<LabelAndModelVo> labelAndModelConvert(List<InsDictInfoEntity> energyInfo);

    List<TagLibVo> tagLibEntityListConvertVo(List<InsTagLibEntity> tagLibByIds);

    InsRegionEntity regionModelConvertRegionEntity(InsRegionConfigModel regionConfigModel);

    InsRegionDetailEntity regionModelConvertRegionDetailEntity(InsRegionConfigModel regionConfigModel);

    List<RegionConfigVo> regionCategoryEntityListConvertVoList(List<InsRegionEntity> regionCategoryList);
    List<RegionConfigVo> regionEntityListConvertVoList(List<InsRegionDetailEntity> regionList);
    @Mapping(target = "region",ignore = true)
    RegionConfigVo regionEntityConvertVo(InsRegionDetailEntity insRegionDetailEntity);

    InsProjectInfoEntity projectModelConvertEntity(InsProjectInfoModel insProjectInfoModel);

    List<ProjectInfoVo> projectEntityListConvertVoList(List<InsProjectInfoEntity> projectList);
    @Mappings({
            @Mapping(target = "brand",ignore = true)
    })
    ProjectInfoVo projectEntityConvertVo(InsProjectInfoEntity projectInfoEntity);

    InsProjectDetailsEntity projectDetailsModelConvertEntity(BrandModel e);

    @Mappings({
            @Mapping(target = "carSeries",ignore = true),
            @Mapping(target = "competitiveProduct",ignore = true),
            @Mapping(target = "riskEarlyWarning",ignore = true)
    })
    BrandVo projectDetailsEntityConvertBrandVo(InsProjectDetailsEntity insProjectDetailsEntity);

    List<TagLibClientTemplateVo> tagLibClientQYTemplateConvertTemplateVoList(List<TagLibClientQYTemplateVo> tagLibClientList);

    BrandInfoVo brandEntityConvertToVo(InsBrandInfoEntity entity);

    List<BrandInfoVo> brandEntityConvertToVoList(List<InsBrandInfoEntity> entity);

    List<InsClosedRuleModel> closedRuleEntityConvertModelList(List<InsClosedRuleEntity> closedRuleEntityList);

    InsClosedRuleEntity closedRuleModelConvertEntity(InsClosedRuleModel insClosedRuleEntity);

    InsClosedRuleModel closedRuleEntityConvertModel(InsClosedRuleEntity insClosedRuleEntity);

    List<InsClosedRuleConditionModel> closedRuleConditionEntityConvertModelList(List<InsClosedRuleConditionEntity> closedRuleConditionEntityList);

    List<InsClosedRuleConditionEntity> closedRuleConditionModelConvertEntityList(List<InsClosedRuleConditionModel> closedRuleConditionModelList);

    InsClosedRuleAlertModel closedRuleAlertEntityConvertModel(InsClosedRuleAlertEntity closedRuleAlertEntity);

    InsClosedRuleAlertEntity closedRuleAlertModelConvertEntity(InsClosedRuleAlertModel closedRuleAlertModel);

    @Mappings({
            @Mapping(target = "topicCode",source = "tagCode"),
            @Mapping(target = "topicName",source = "tagName"),
            @Mapping(target = "topicDesc",source = "tagDescription")
    })
    TopicVo tagLibClientEntityListConvertTopicVoList(InsTagLibClientEntity records);

    InsTagLibClientEntity topicModelConvertTagLibClientEntity(InsTopicModel insTopicModel);

    InsTopicVo tagLibClientEntityConvertTopicVo(InsTagLibClientEntity insTagLibClientEntity);

    InsBrandInfoEntity brandModelToInsBrandInfoEntity(InsBrandInfoModel insBrandInfoModel);

    List<InsBrandInfoVo> brandEntityConvertToBrandVoList(List<InsBrandInfoEntity> records);

    InsBrandInfoVo brandEntityConvertToBrandVo(InsBrandInfoEntity entity);

    List<InsCarSeriesVo> carSeriesEntityConvertToCarSeriesVoList(List<InsCarSeriesInfoEntity> records);

    InsCarSeriesVo CarSeriesEntityConvertToVo(InsCarSeriesInfoEntity insCarSeriesInfoEntity);

    InsAutomarkEntity automarkModelConvertToEntity(InsAutomarkModel model);

    InsAutomarkInfoVo automarkEntityConvartToVo(InsAutomarkEntity insAutomarkEntity);


    List<InsAutomarkInfoVo> automarkEntityConvartToVoList(List<InsAutomarkEntity> records);

    List<AutomarkVo> automarkEntityConvertToAutomarkList(List<InsAutomarkEntity> list);

    InsCarSceneEntity carSceneModelConvertEntity(InsCarSceneModel model);

    InsCarSceneVo carSceneEntityConvertVo(InsCarSceneEntity entity);

    InsCarSceneCategoryEntity carSceneCategoryModelConvertEntity(InsCarSceneCategoryModel model);


    @Mapping(target = "typeName", source = "categoryName")
    InsCarSceneCategoryVo carSceneCategoryEntityConvertVo(InsCarSceneCategoryEntity entity);

    List<InsCarSceneCategoryVo> carSceneCategoryEntityListConvertVoList(List<InsCarSceneCategoryEntity> entityList);

    InsAttributeLabelEntity attributeLabelModelConvertEntity(InsAttributeLabelModel model);

    InsAttributeLabelVo attributeLabelEntityConvertVo(InsAttributeLabelEntity entity);

    List<InsAttributeLabelVo> attributeLabelEntityConvertVoList(List<InsAttributeLabelEntity> entityList);
}
