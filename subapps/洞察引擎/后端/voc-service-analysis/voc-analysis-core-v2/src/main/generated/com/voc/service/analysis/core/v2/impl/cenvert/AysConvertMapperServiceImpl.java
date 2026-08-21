package com.voc.service.analysis.core.v2.impl.cenvert;

import com.voc.service.analysis.core.v2.entity.AysBatchPushRecordEntity;
import com.voc.service.analysis.core.v2.entity.AysMetaDataAnalysisEntity;
import com.voc.service.analysis.core.v2.entity.AysMetaDataEntity;
import com.voc.service.analysis.core.v2.entity.AysMetaDataExtAnalysisEntity;
import com.voc.service.analysis.core.v2.entity.AysModelResltDataAnalysisValidEntity;
import com.voc.service.analysis.core.v2.entity.AysModelResltDataEntity;
import com.voc.service.analysis.core.v2.entity.AysModelResultDataAnalysisEntity;
import com.voc.service.analysis.core.v2.entity.AysPostprocessDataEntity;
import com.voc.service.analysis.core.v2.entity.AysPostprocessValidDataEntity;
import com.voc.service.analysis.core.v2.entity.AysPreprocessDataEntity;
import com.voc.service.analysis.model.AysBatchPushRecordModel;
import com.voc.service.analysis.model.AysMetaDataAnalysisModel;
import com.voc.service.analysis.model.AysMetaDataModel;
import com.voc.service.analysis.model.AysModelResltDataAnalysisModel;
import com.voc.service.analysis.model.AysModelResltDataAnalysisValidModel;
import com.voc.service.analysis.model.AysPreprocessDataModel;
import com.voc.service.analysis.model.AysProcessDataModel;
import com.voc.service.analysis.model.RuleModel;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-04-28T11:32:32+0800",
    comments = "version: 1.6.3, compiler: javac, environment: Java 17.0.14 (Red Hat, Inc.)"
)
@Component
public class AysConvertMapperServiceImpl implements AysConvertMapperService {

    @Override
    public AysMetaDataAnalysisModel cenvertToModel(AysMetaDataAnalysisEntity entity) {
        if ( entity == null ) {
            return null;
        }

        AysMetaDataAnalysisModel.AysMetaDataAnalysisModelBuilder aysMetaDataAnalysisModel = AysMetaDataAnalysisModel.builder();

        aysMetaDataAnalysisModel.dataId( entity.getDataId() );
        aysMetaDataAnalysisModel.id( entity.getId() );
        aysMetaDataAnalysisModel.workId( entity.getWorkId() );
        aysMetaDataAnalysisModel.data( entity.getData() );
        aysMetaDataAnalysisModel.clientId( entity.getClientId() );
        aysMetaDataAnalysisModel.channelId( entity.getChannelId() );
        aysMetaDataAnalysisModel.contentType( entity.getContentType() );
        aysMetaDataAnalysisModel.done( entity.getDone() );
        aysMetaDataAnalysisModel.dataStatus( entity.getDataStatus() );
        aysMetaDataAnalysisModel.title( entity.getTitle() );
        aysMetaDataAnalysisModel.content( entity.getContent() );
        aysMetaDataAnalysisModel.userName( entity.getUserName() );
        aysMetaDataAnalysisModel.publishTime( entity.getPublishTime() );
        aysMetaDataAnalysisModel.modelType( entity.getModelType() );
        aysMetaDataAnalysisModel.extFields( entity.getExtFields() );
        aysMetaDataAnalysisModel.bizExtAttrs( entity.getBizExtAttrs() );
        aysMetaDataAnalysisModel.bizExtAttrs2( entity.getBizExtAttrs2() );
        aysMetaDataAnalysisModel.bizExtAttrs3( entity.getBizExtAttrs3() );
        aysMetaDataAnalysisModel.custExtAttrs( entity.getCustExtAttrs() );
        aysMetaDataAnalysisModel.vhlExtAttrs( entity.getVhlExtAttrs() );
        aysMetaDataAnalysisModel.dealerExtAttrs( entity.getDealerExtAttrs() );
        aysMetaDataAnalysisModel.prdExtAttrs( entity.getPrdExtAttrs() );
        aysMetaDataAnalysisModel.oneId( entity.getOneId() );
        aysMetaDataAnalysisModel.createTime( entity.getCreateTime() );

        return aysMetaDataAnalysisModel.build();
    }

    @Override
    public List<AysMetaDataAnalysisModel> cenvertToModelList(List<AysMetaDataAnalysisEntity> entityList) {
        if ( entityList == null ) {
            return null;
        }

        List<AysMetaDataAnalysisModel> list = new ArrayList<AysMetaDataAnalysisModel>( entityList.size() );
        for ( AysMetaDataAnalysisEntity aysMetaDataAnalysisEntity : entityList ) {
            list.add( cenvertToModel( aysMetaDataAnalysisEntity ) );
        }

        return list;
    }

    @Override
    public List<AysMetaDataAnalysisModel> cenvertToModelExtList(List<AysMetaDataExtAnalysisEntity> entityList) {
        if ( entityList == null ) {
            return null;
        }

        List<AysMetaDataAnalysisModel> list = new ArrayList<AysMetaDataAnalysisModel>( entityList.size() );
        for ( AysMetaDataExtAnalysisEntity aysMetaDataExtAnalysisEntity : entityList ) {
            list.add( aysMetaDataExtAnalysisEntityToAysMetaDataAnalysisModel( aysMetaDataExtAnalysisEntity ) );
        }

        return list;
    }

    @Override
    public AysProcessDataModel converToAysProcessDataModel(AysMetaDataAnalysisModel data) {
        if ( data == null ) {
            return null;
        }

        AysProcessDataModel.AysProcessDataModelBuilder aysProcessDataModel = AysProcessDataModel.builder();

        aysProcessDataModel.dataId( data.getDataId() );
        aysProcessDataModel.id( data.getId() );
        aysProcessDataModel.modelType( data.getModelType() );
        aysProcessDataModel.workId( data.getWorkId() );
        aysProcessDataModel.clientId( data.getClientId() );
        aysProcessDataModel.channelId( data.getChannelId() );
        aysProcessDataModel.contentType( data.getContentType() );
        aysProcessDataModel.data( data.getData() );
        aysProcessDataModel.extFields( data.getExtFields() );
        aysProcessDataModel.bizExtAttrs( data.getBizExtAttrs() );
        aysProcessDataModel.bizExtAttrs2( data.getBizExtAttrs2() );
        aysProcessDataModel.bizExtAttrs3( data.getBizExtAttrs3() );
        aysProcessDataModel.custExtAttrs( data.getCustExtAttrs() );
        aysProcessDataModel.vhlExtAttrs( data.getVhlExtAttrs() );
        aysProcessDataModel.dealerExtAttrs( data.getDealerExtAttrs() );
        aysProcessDataModel.prdExtAttrs( data.getPrdExtAttrs() );
        aysProcessDataModel.oneId( data.getOneId() );
        aysProcessDataModel.publishTime( data.getPublishTime() );
        aysProcessDataModel.createTime( data.getCreateTime() );
        aysProcessDataModel.done( data.getDone() );
        List<RuleModel> list = data.getHitRuleList();
        if ( list != null ) {
            aysProcessDataModel.hitRuleList( new ArrayList<RuleModel>( list ) );
        }

        return aysProcessDataModel.build();
    }

    @Override
    public AysPreprocessDataEntity converToAysPreprocessDataEntity(AysProcessDataModel model) {
        if ( model == null ) {
            return null;
        }

        AysPreprocessDataEntity.AysPreprocessDataEntityBuilder aysPreprocessDataEntity = AysPreprocessDataEntity.builder();

        aysPreprocessDataEntity.id( model.getId() );
        aysPreprocessDataEntity.dataId( model.getDataId() );
        aysPreprocessDataEntity.workId( model.getWorkId() );
        aysPreprocessDataEntity.clientId( model.getClientId() );
        aysPreprocessDataEntity.contentType( model.getContentType() );
        aysPreprocessDataEntity.channelId( model.getChannelId() );
        aysPreprocessDataEntity.data( model.getData() );
        aysPreprocessDataEntity.dataMd5( model.getDataMd5() );
        aysPreprocessDataEntity.publishTime( model.getPublishTime() );
        aysPreprocessDataEntity.createTime( model.getCreateTime() );
        aysPreprocessDataEntity.abandon( model.getAbandon() );
        aysPreprocessDataEntity.done( model.getDone() );
        aysPreprocessDataEntity.modelType( model.getModelType() );
        aysPreprocessDataEntity.extFields( model.getExtFields() );
        aysPreprocessDataEntity.bizExtAttrs( model.getBizExtAttrs() );
        aysPreprocessDataEntity.bizExtAttrs2( model.getBizExtAttrs2() );
        aysPreprocessDataEntity.bizExtAttrs3( model.getBizExtAttrs3() );
        aysPreprocessDataEntity.custExtAttrs( model.getCustExtAttrs() );
        aysPreprocessDataEntity.vhlExtAttrs( model.getVhlExtAttrs() );
        aysPreprocessDataEntity.dealerExtAttrs( model.getDealerExtAttrs() );
        aysPreprocessDataEntity.prdExtAttrs( model.getPrdExtAttrs() );
        aysPreprocessDataEntity.oneId( model.getOneId() );

        return aysPreprocessDataEntity.build();
    }

    @Override
    public List<AysProcessDataModel> converToAysPreprocessDataModel(List<AysPreprocessDataEntity> saveList) {
        if ( saveList == null ) {
            return null;
        }

        List<AysProcessDataModel> list = new ArrayList<AysProcessDataModel>( saveList.size() );
        for ( AysPreprocessDataEntity aysPreprocessDataEntity : saveList ) {
            list.add( aysPreprocessDataEntityToAysProcessDataModel( aysPreprocessDataEntity ) );
        }

        return list;
    }

    @Override
    public List<AysProcessDataModel> converToAysProcessDataModel2(List<AysPreprocessDataEntity> list) {
        if ( list == null ) {
            return null;
        }

        List<AysProcessDataModel> list1 = new ArrayList<AysProcessDataModel>( list.size() );
        for ( AysPreprocessDataEntity aysPreprocessDataEntity : list ) {
            list1.add( aysPreprocessDataEntityToAysProcessDataModel( aysPreprocessDataEntity ) );
        }

        return list1;
    }

    @Override
    public List<AysProcessDataModel> converToAysPostprocessValidDataModel(List<AysPostprocessValidDataEntity> saveList) {
        if ( saveList == null ) {
            return null;
        }

        List<AysProcessDataModel> list = new ArrayList<AysProcessDataModel>( saveList.size() );
        for ( AysPostprocessValidDataEntity aysPostprocessValidDataEntity : saveList ) {
            list.add( aysPostprocessValidDataEntityToAysProcessDataModel( aysPostprocessValidDataEntity ) );
        }

        return list;
    }

    @Override
    public List<AysProcessDataModel> converToAysPostprocessDataModel(List<AysPostprocessDataEntity> saveList) {
        if ( saveList == null ) {
            return null;
        }

        List<AysProcessDataModel> list = new ArrayList<AysProcessDataModel>( saveList.size() );
        for ( AysPostprocessDataEntity aysPostprocessDataEntity : saveList ) {
            list.add( aysPostprocessDataEntityToAysProcessDataModel( aysPostprocessDataEntity ) );
        }

        return list;
    }

    @Override
    public AysModelResltDataAnalysisValidModel converToAysModelResltDataAnalysisValidModel2(AysModelResltDataAnalysisValidEntity entity) {
        if ( entity == null ) {
            return null;
        }

        AysModelResltDataAnalysisValidModel.AysModelResltDataAnalysisValidModelBuilder aysModelResltDataAnalysisValidModel = AysModelResltDataAnalysisValidModel.builder();

        aysModelResltDataAnalysisValidModel.newId( entity.getNewId() );
        aysModelResltDataAnalysisValidModel.id( entity.getId() );
        aysModelResltDataAnalysisValidModel.workId( entity.getWorkId() );
        aysModelResltDataAnalysisValidModel.oldWorkId( entity.getOldWorkId() );
        aysModelResltDataAnalysisValidModel.clientId( entity.getClientId() );
        aysModelResltDataAnalysisValidModel.channelId( entity.getChannelId() );
        aysModelResltDataAnalysisValidModel.contentType( entity.getContentType() );
        aysModelResltDataAnalysisValidModel.inputDataId( entity.getInputDataId() );
        aysModelResltDataAnalysisValidModel.originalId( entity.getOriginalId() );
        aysModelResltDataAnalysisValidModel.sampleDataType( entity.getSampleDataType() );
        aysModelResltDataAnalysisValidModel.originalTextScene( entity.getOriginalTextScene() );
        aysModelResltDataAnalysisValidModel.brandCodeName( entity.getBrandCodeName() );
        aysModelResltDataAnalysisValidModel.carSeriesName( entity.getCarSeriesName() );
        aysModelResltDataAnalysisValidModel.labelType( entity.getLabelType() );
        aysModelResltDataAnalysisValidModel.labelTypeLevelFirst( entity.getLabelTypeLevelFirst() );
        aysModelResltDataAnalysisValidModel.labelTypeLevelSecond( entity.getLabelTypeLevelSecond() );
        aysModelResltDataAnalysisValidModel.labelTypeLevelThree( entity.getLabelTypeLevelThree() );
        aysModelResltDataAnalysisValidModel.labelTypeLevelFour( entity.getLabelTypeLevelFour() );
        aysModelResltDataAnalysisValidModel.labelTypeLevelFive( entity.getLabelTypeLevelFive() );
        aysModelResltDataAnalysisValidModel.scenario( entity.getScenario() );
        aysModelResltDataAnalysisValidModel.sentiment( entity.getSentiment() );
        aysModelResltDataAnalysisValidModel.intentionType( entity.getIntentionType() );
        aysModelResltDataAnalysisValidModel.topic( entity.getTopic() );
        aysModelResltDataAnalysisValidModel.opinion( entity.getOpinion() );
        aysModelResltDataAnalysisValidModel.subject( entity.getSubject() );
        aysModelResltDataAnalysisValidModel.faultLevel( entity.getFaultLevel() );
        aysModelResltDataAnalysisValidModel.description( entity.getDescription() );
        aysModelResltDataAnalysisValidModel.sentimentScore( entity.getSentimentScore() );
        aysModelResltDataAnalysisValidModel.keywords( entity.getKeywords() );
        aysModelResltDataAnalysisValidModel.publishTime( entity.getPublishTime() );
        aysModelResltDataAnalysisValidModel.createTime( entity.getCreateTime() );
        aysModelResltDataAnalysisValidModel.updateTime( entity.getUpdateTime() );
        aysModelResltDataAnalysisValidModel.done( entity.getDone() );
        aysModelResltDataAnalysisValidModel.hitRules( entity.getHitRules() );
        aysModelResltDataAnalysisValidModel.hitValidRules( entity.getHitValidRules() );

        return aysModelResltDataAnalysisValidModel.build();
    }

    @Override
    public List<AysModelResltDataAnalysisValidModel> converToAysModelResltDataAnalysisValidModel(List<AysModelResltDataAnalysisValidEntity> list) {
        if ( list == null ) {
            return null;
        }

        List<AysModelResltDataAnalysisValidModel> list1 = new ArrayList<AysModelResltDataAnalysisValidModel>( list.size() );
        for ( AysModelResltDataAnalysisValidEntity aysModelResltDataAnalysisValidEntity : list ) {
            list1.add( converToAysModelResltDataAnalysisValidModel2( aysModelResltDataAnalysisValidEntity ) );
        }

        return list1;
    }

    @Override
    public List<AysModelResltDataAnalysisModel> converToAysModelResltDataAnalysisModel(List<AysModelResultDataAnalysisEntity> entityList) {
        if ( entityList == null ) {
            return null;
        }

        List<AysModelResltDataAnalysisModel> list = new ArrayList<AysModelResltDataAnalysisModel>( entityList.size() );
        for ( AysModelResultDataAnalysisEntity aysModelResultDataAnalysisEntity : entityList ) {
            list.add( cenvertToAysModelResltDataAnalysisModel( aysModelResultDataAnalysisEntity ) );
        }

        return list;
    }

    @Override
    public List<AysMetaDataAnalysisModel> converToMetaItemsDataModel(List<AysMetaDataAnalysisEntity> saveList) {
        if ( saveList == null ) {
            return null;
        }

        List<AysMetaDataAnalysisModel> list = new ArrayList<AysMetaDataAnalysisModel>( saveList.size() );
        for ( AysMetaDataAnalysisEntity aysMetaDataAnalysisEntity : saveList ) {
            list.add( cenvertToModel( aysMetaDataAnalysisEntity ) );
        }

        return list;
    }

    @Override
    public List<AysMetaDataAnalysisEntity> converToMetaItemsEntity(List<AysMetaDataAnalysisModel> errorList) {
        if ( errorList == null ) {
            return null;
        }

        List<AysMetaDataAnalysisEntity> list = new ArrayList<AysMetaDataAnalysisEntity>( errorList.size() );
        for ( AysMetaDataAnalysisModel aysMetaDataAnalysisModel : errorList ) {
            list.add( aysMetaDataAnalysisModelToAysMetaDataAnalysisEntity( aysMetaDataAnalysisModel ) );
        }

        return list;
    }

    @Override
    public AysModelResltDataAnalysisModel cenvertToAysModelResltDataAnalysisModel(AysModelResultDataAnalysisEntity entity) {
        if ( entity == null ) {
            return null;
        }

        AysModelResltDataAnalysisModel.AysModelResltDataAnalysisModelBuilder aysModelResltDataAnalysisModel = AysModelResltDataAnalysisModel.builder();

        aysModelResltDataAnalysisModel.dataId( entity.getDataId() );
        aysModelResltDataAnalysisModel.id( entity.getId() );
        aysModelResltDataAnalysisModel.workId( entity.getWorkId() );
        aysModelResltDataAnalysisModel.clientId( entity.getClientId() );
        aysModelResltDataAnalysisModel.channelId( entity.getChannelId() );
        aysModelResltDataAnalysisModel.contentType( entity.getContentType() );
        aysModelResltDataAnalysisModel.inputDataId( entity.getInputDataId() );
        aysModelResltDataAnalysisModel.originalId( entity.getOriginalId() );
        aysModelResltDataAnalysisModel.sampleDataType( entity.getSampleDataType() );
        aysModelResltDataAnalysisModel.originalTextScene( entity.getOriginalTextScene() );
        aysModelResltDataAnalysisModel.brandCode( entity.getBrandCode() );
        aysModelResltDataAnalysisModel.carSeriesCode( entity.getCarSeriesCode() );
        aysModelResltDataAnalysisModel.labelType( entity.getLabelType() );
        aysModelResltDataAnalysisModel.scenario( entity.getScenario() );
        aysModelResltDataAnalysisModel.sentiment( entity.getSentiment() );
        aysModelResltDataAnalysisModel.intentionType( entity.getIntentionType() );
        aysModelResltDataAnalysisModel.topic( entity.getTopic() );
        aysModelResltDataAnalysisModel.opinion( entity.getOpinion() );
        aysModelResltDataAnalysisModel.subject( entity.getSubject() );
        aysModelResltDataAnalysisModel.faultLevel( entity.getFaultLevel() );
        aysModelResltDataAnalysisModel.description( entity.getDescription() );
        aysModelResltDataAnalysisModel.sentimentScore( entity.getSentimentScore() );
        aysModelResltDataAnalysisModel.keywords( entity.getKeywords() );
        aysModelResltDataAnalysisModel.publishTime( entity.getPublishTime() );
        aysModelResltDataAnalysisModel.createTime( entity.getCreateTime() );
        aysModelResltDataAnalysisModel.updateTime( entity.getUpdateTime() );
        aysModelResltDataAnalysisModel.modelType( entity.getModelType() );
        aysModelResltDataAnalysisModel.extFields( entity.getExtFields() );
        aysModelResltDataAnalysisModel.bizExtAttrs( entity.getBizExtAttrs() );
        aysModelResltDataAnalysisModel.bizExtAttrs2( entity.getBizExtAttrs2() );
        aysModelResltDataAnalysisModel.bizExtAttrs3( entity.getBizExtAttrs3() );
        aysModelResltDataAnalysisModel.rawData( entity.getRawData() );
        aysModelResltDataAnalysisModel.custExtAttrs( entity.getCustExtAttrs() );
        aysModelResltDataAnalysisModel.vhlExtAttrs( entity.getVhlExtAttrs() );
        aysModelResltDataAnalysisModel.dealerExtAttrs( entity.getDealerExtAttrs() );
        aysModelResltDataAnalysisModel.prdExtAttrs( entity.getPrdExtAttrs() );
        aysModelResltDataAnalysisModel.oneId( entity.getOneId() );
        aysModelResltDataAnalysisModel.done( entity.getDone() );

        return aysModelResltDataAnalysisModel.build();
    }

    @Override
    public AysProcessDataModel converToAysProcessDataModel3(AysModelResltDataAnalysisModel data) {
        if ( data == null ) {
            return null;
        }

        AysProcessDataModel.AysProcessDataModelBuilder aysProcessDataModel = AysProcessDataModel.builder();

        aysProcessDataModel.dataId( data.getDataId() );
        aysProcessDataModel.id( data.getId() );
        aysProcessDataModel.originalId( data.getOriginalId() );
        aysProcessDataModel.modelType( data.getModelType() );
        aysProcessDataModel.workId( data.getWorkId() );
        aysProcessDataModel.clientId( data.getClientId() );
        aysProcessDataModel.channelId( data.getChannelId() );
        aysProcessDataModel.contentType( data.getContentType() );
        aysProcessDataModel.extFields( data.getExtFields() );
        aysProcessDataModel.bizExtAttrs( data.getBizExtAttrs() );
        aysProcessDataModel.bizExtAttrs2( data.getBizExtAttrs2() );
        aysProcessDataModel.bizExtAttrs3( data.getBizExtAttrs3() );
        aysProcessDataModel.custExtAttrs( data.getCustExtAttrs() );
        aysProcessDataModel.vhlExtAttrs( data.getVhlExtAttrs() );
        aysProcessDataModel.dealerExtAttrs( data.getDealerExtAttrs() );
        aysProcessDataModel.prdExtAttrs( data.getPrdExtAttrs() );
        aysProcessDataModel.oneId( data.getOneId() );
        aysProcessDataModel.publishTime( data.getPublishTime() );
        aysProcessDataModel.createTime( data.getCreateTime() );
        aysProcessDataModel.abandon( data.getAbandon() );
        aysProcessDataModel.done( data.getDone() );

        return aysProcessDataModel.build();
    }

    @Override
    public List<AysPreprocessDataModel> cenvertToAysPreprocessDataModelList(List<AysPreprocessDataEntity> entityList) {
        if ( entityList == null ) {
            return null;
        }

        List<AysPreprocessDataModel> list = new ArrayList<AysPreprocessDataModel>( entityList.size() );
        for ( AysPreprocessDataEntity aysPreprocessDataEntity : entityList ) {
            list.add( aysPreprocessDataEntityToAysPreprocessDataModel( aysPreprocessDataEntity ) );
        }

        return list;
    }

    @Override
    public AysProcessDataModel converToAysProcessDataModel4(AysPreprocessDataModel data) {
        if ( data == null ) {
            return null;
        }

        AysProcessDataModel.AysProcessDataModelBuilder aysProcessDataModel = AysProcessDataModel.builder();

        aysProcessDataModel.dataId( data.getDataId() );
        aysProcessDataModel.id( data.getId() );
        aysProcessDataModel.modelType( data.getModelType() );
        aysProcessDataModel.workId( data.getWorkId() );
        aysProcessDataModel.clientId( data.getClientId() );
        aysProcessDataModel.channelId( data.getChannelId() );
        aysProcessDataModel.contentType( data.getContentType() );
        aysProcessDataModel.data( data.getData() );
        aysProcessDataModel.extFields( data.getExtFields() );
        aysProcessDataModel.bizExtAttrs( data.getBizExtAttrs() );
        aysProcessDataModel.bizExtAttrs2( data.getBizExtAttrs2() );
        aysProcessDataModel.bizExtAttrs3( data.getBizExtAttrs3() );
        aysProcessDataModel.custExtAttrs( data.getCustExtAttrs() );
        aysProcessDataModel.vhlExtAttrs( data.getVhlExtAttrs() );
        aysProcessDataModel.dealerExtAttrs( data.getDealerExtAttrs() );
        aysProcessDataModel.prdExtAttrs( data.getPrdExtAttrs() );
        aysProcessDataModel.oneId( data.getOneId() );
        aysProcessDataModel.dataMd5( data.getDataMd5() );
        aysProcessDataModel.publishTime( data.getPublishTime() );
        aysProcessDataModel.createTime( data.getCreateTime() );
        aysProcessDataModel.abandon( data.getAbandon() );
        aysProcessDataModel.done( data.getDone() );

        return aysProcessDataModel.build();
    }

    @Override
    public AysProcessDataModel convertToAysModelResltDataAnalysisModel(AysModelResltDataAnalysisModel model) {
        if ( model == null ) {
            return null;
        }

        AysProcessDataModel.AysProcessDataModelBuilder aysProcessDataModel = AysProcessDataModel.builder();

        aysProcessDataModel.dataId( model.getDataId() );
        aysProcessDataModel.id( model.getId() );
        aysProcessDataModel.originalId( model.getOriginalId() );
        aysProcessDataModel.modelType( model.getModelType() );
        aysProcessDataModel.workId( model.getWorkId() );
        aysProcessDataModel.clientId( model.getClientId() );
        aysProcessDataModel.channelId( model.getChannelId() );
        aysProcessDataModel.contentType( model.getContentType() );
        aysProcessDataModel.extFields( model.getExtFields() );
        aysProcessDataModel.bizExtAttrs( model.getBizExtAttrs() );
        aysProcessDataModel.bizExtAttrs2( model.getBizExtAttrs2() );
        aysProcessDataModel.bizExtAttrs3( model.getBizExtAttrs3() );
        aysProcessDataModel.custExtAttrs( model.getCustExtAttrs() );
        aysProcessDataModel.vhlExtAttrs( model.getVhlExtAttrs() );
        aysProcessDataModel.dealerExtAttrs( model.getDealerExtAttrs() );
        aysProcessDataModel.prdExtAttrs( model.getPrdExtAttrs() );
        aysProcessDataModel.oneId( model.getOneId() );
        aysProcessDataModel.publishTime( model.getPublishTime() );
        aysProcessDataModel.createTime( model.getCreateTime() );
        aysProcessDataModel.abandon( model.getAbandon() );
        aysProcessDataModel.done( model.getDone() );

        return aysProcessDataModel.build();
    }

    @Override
    public List<AysModelResltDataAnalysisModel> cenvertToAysModelResltDataAnalysisModelList2(List<AysModelResultDataAnalysisEntity> entityList) {
        if ( entityList == null ) {
            return null;
        }

        List<AysModelResltDataAnalysisModel> list = new ArrayList<AysModelResltDataAnalysisModel>( entityList.size() );
        for ( AysModelResultDataAnalysisEntity aysModelResultDataAnalysisEntity : entityList ) {
            list.add( cenvertToAysModelResltDataAnalysisModel( aysModelResultDataAnalysisEntity ) );
        }

        return list;
    }

    @Override
    public AysProcessDataModel converToAysModelResltDataAnalysisValidModel1(AysModelResltDataAnalysisValidModel data) {
        if ( data == null ) {
            return null;
        }

        AysProcessDataModel.AysProcessDataModelBuilder aysProcessDataModel = AysProcessDataModel.builder();

        aysProcessDataModel.id( data.getId() );
        aysProcessDataModel.originalId( data.getOriginalId() );
        aysProcessDataModel.modelType( data.getModelType() );
        aysProcessDataModel.workId( data.getWorkId() );
        aysProcessDataModel.clientId( data.getClientId() );
        aysProcessDataModel.channelId( data.getChannelId() );
        aysProcessDataModel.contentType( data.getContentType() );
        aysProcessDataModel.extFields( data.getExtFields() );
        aysProcessDataModel.publishTime( data.getPublishTime() );
        aysProcessDataModel.createTime( data.getCreateTime() );
        aysProcessDataModel.done( data.getDone() );

        return aysProcessDataModel.build();
    }

    @Override
    public List<AysModelResltDataAnalysisValidModel> cenvertToAysModelResltDataAnalysisValidEntityList(List<AysModelResltDataAnalysisValidEntity> entityList) {
        if ( entityList == null ) {
            return null;
        }

        List<AysModelResltDataAnalysisValidModel> list = new ArrayList<AysModelResltDataAnalysisValidModel>( entityList.size() );
        for ( AysModelResltDataAnalysisValidEntity aysModelResltDataAnalysisValidEntity : entityList ) {
            list.add( converToAysModelResltDataAnalysisValidModel2( aysModelResltDataAnalysisValidEntity ) );
        }

        return list;
    }

    @Override
    public AysProcessDataModel convertToAysModelResltDataAnalysisValidModel(AysModelResltDataAnalysisValidModel model) {
        if ( model == null ) {
            return null;
        }

        AysProcessDataModel.AysProcessDataModelBuilder aysProcessDataModel = AysProcessDataModel.builder();

        aysProcessDataModel.id( model.getId() );
        aysProcessDataModel.originalId( model.getOriginalId() );
        aysProcessDataModel.modelType( model.getModelType() );
        aysProcessDataModel.workId( model.getWorkId() );
        aysProcessDataModel.clientId( model.getClientId() );
        aysProcessDataModel.channelId( model.getChannelId() );
        aysProcessDataModel.contentType( model.getContentType() );
        aysProcessDataModel.extFields( model.getExtFields() );
        aysProcessDataModel.publishTime( model.getPublishTime() );
        aysProcessDataModel.createTime( model.getCreateTime() );
        aysProcessDataModel.done( model.getDone() );

        return aysProcessDataModel.build();
    }

    @Override
    public AysMetaDataModel converToAysMetaDataEntity(AysMetaDataEntity entity) {
        if ( entity == null ) {
            return null;
        }

        AysMetaDataModel.AysMetaDataModelBuilder aysMetaDataModel = AysMetaDataModel.builder();

        aysMetaDataModel.id( entity.getId() );
        aysMetaDataModel.workId( entity.getWorkId() );
        aysMetaDataModel.data( entity.getData() );
        aysMetaDataModel.tid( entity.getTid() );
        aysMetaDataModel.source( entity.getSource() );
        aysMetaDataModel.createTime( entity.getCreateTime() );
        aysMetaDataModel.operator( entity.getOperator() );

        return aysMetaDataModel.build();
    }

    @Override
    public List<AysMetaDataModel> converToAysMetaDataList(List<AysMetaDataEntity> entity) {
        if ( entity == null ) {
            return null;
        }

        List<AysMetaDataModel> list = new ArrayList<AysMetaDataModel>( entity.size() );
        for ( AysMetaDataEntity aysMetaDataEntity : entity ) {
            list.add( converToAysMetaDataEntity( aysMetaDataEntity ) );
        }

        return list;
    }

    @Override
    public AysBatchPushRecordModel converToAiBatchPushModel(AysBatchPushRecordEntity aysBatchPushRecordEntity) {
        if ( aysBatchPushRecordEntity == null ) {
            return null;
        }

        AysBatchPushRecordModel.AysBatchPushRecordModelBuilder aysBatchPushRecordModel = AysBatchPushRecordModel.builder();

        aysBatchPushRecordModel.id( aysBatchPushRecordEntity.getId() );
        aysBatchPushRecordModel.workId( aysBatchPushRecordEntity.getWorkId() );
        aysBatchPushRecordModel.reqeutId( aysBatchPushRecordEntity.getReqeutId() );
        aysBatchPushRecordModel.tid( aysBatchPushRecordEntity.getTid() );
        aysBatchPushRecordModel.processStatus( aysBatchPushRecordEntity.getProcessStatus() );
        aysBatchPushRecordModel.createTime( aysBatchPushRecordEntity.getCreateTime() );
        aysBatchPushRecordModel.updateTime( aysBatchPushRecordEntity.getUpdateTime() );
        aysBatchPushRecordModel.batchTotal( aysBatchPushRecordEntity.getBatchTotal() );
        aysBatchPushRecordModel.receivedTotal( aysBatchPushRecordEntity.getReceivedTotal() );
        aysBatchPushRecordModel.preFinishedDataSize( aysBatchPushRecordEntity.getPreFinishedDataSize() );
        aysBatchPushRecordModel.modelMissAnalysisSize( aysBatchPushRecordEntity.getModelMissAnalysisSize() );
        aysBatchPushRecordModel.postFinishedDataSize( aysBatchPushRecordEntity.getPostFinishedDataSize() );

        return aysBatchPushRecordModel.build();
    }

    @Override
    public List<AysPreprocessDataModel> converToAysPreprocessDataModelList(List<AysPreprocessDataEntity> entityList) {
        if ( entityList == null ) {
            return null;
        }

        List<AysPreprocessDataModel> list = new ArrayList<AysPreprocessDataModel>( entityList.size() );
        for ( AysPreprocessDataEntity aysPreprocessDataEntity : entityList ) {
            list.add( aysPreprocessDataEntityToAysPreprocessDataModel( aysPreprocessDataEntity ) );
        }

        return list;
    }

    @Override
    public List<AysPreprocessDataModel> converToAysPostprocessDataModelList(List<AysPostprocessDataEntity> entityList) {
        if ( entityList == null ) {
            return null;
        }

        List<AysPreprocessDataModel> list = new ArrayList<AysPreprocessDataModel>( entityList.size() );
        for ( AysPostprocessDataEntity aysPostprocessDataEntity : entityList ) {
            list.add( aysPostprocessDataEntityToAysPreprocessDataModel( aysPostprocessDataEntity ) );
        }

        return list;
    }

    @Override
    public List<AysProcessDataModel> converToAysProcessDataModelList(List<AysModelResltDataEntity> entityList) {
        if ( entityList == null ) {
            return null;
        }

        List<AysProcessDataModel> list = new ArrayList<AysProcessDataModel>( entityList.size() );
        for ( AysModelResltDataEntity aysModelResltDataEntity : entityList ) {
            list.add( aysModelResltDataEntityToAysProcessDataModel( aysModelResltDataEntity ) );
        }

        return list;
    }

    protected AysMetaDataAnalysisModel aysMetaDataExtAnalysisEntityToAysMetaDataAnalysisModel(AysMetaDataExtAnalysisEntity aysMetaDataExtAnalysisEntity) {
        if ( aysMetaDataExtAnalysisEntity == null ) {
            return null;
        }

        AysMetaDataAnalysisModel.AysMetaDataAnalysisModelBuilder aysMetaDataAnalysisModel = AysMetaDataAnalysisModel.builder();

        aysMetaDataAnalysisModel.dataId( aysMetaDataExtAnalysisEntity.getDataId() );
        aysMetaDataAnalysisModel.id( aysMetaDataExtAnalysisEntity.getId() );
        aysMetaDataAnalysisModel.workId( aysMetaDataExtAnalysisEntity.getWorkId() );
        aysMetaDataAnalysisModel.clientId( aysMetaDataExtAnalysisEntity.getClientId() );
        aysMetaDataAnalysisModel.contentType( aysMetaDataExtAnalysisEntity.getContentType() );
        aysMetaDataAnalysisModel.done( aysMetaDataExtAnalysisEntity.getDone() );
        aysMetaDataAnalysisModel.dataStatus( aysMetaDataExtAnalysisEntity.getDataStatus() );
        aysMetaDataAnalysisModel.title( aysMetaDataExtAnalysisEntity.getTitle() );
        aysMetaDataAnalysisModel.content( aysMetaDataExtAnalysisEntity.getContent() );
        aysMetaDataAnalysisModel.userName( aysMetaDataExtAnalysisEntity.getUserName() );
        aysMetaDataAnalysisModel.modelType( aysMetaDataExtAnalysisEntity.getModelType() );
        aysMetaDataAnalysisModel.custExtAttrs( aysMetaDataExtAnalysisEntity.getCustExtAttrs() );
        aysMetaDataAnalysisModel.vhlExtAttrs( aysMetaDataExtAnalysisEntity.getVhlExtAttrs() );
        aysMetaDataAnalysisModel.dealerExtAttrs( aysMetaDataExtAnalysisEntity.getDealerExtAttrs() );
        aysMetaDataAnalysisModel.prdExtAttrs( aysMetaDataExtAnalysisEntity.getPrdExtAttrs() );
        aysMetaDataAnalysisModel.oneId( aysMetaDataExtAnalysisEntity.getOneId() );
        aysMetaDataAnalysisModel.createTime( aysMetaDataExtAnalysisEntity.getCreateTime() );

        return aysMetaDataAnalysisModel.build();
    }

    protected AysProcessDataModel aysPreprocessDataEntityToAysProcessDataModel(AysPreprocessDataEntity aysPreprocessDataEntity) {
        if ( aysPreprocessDataEntity == null ) {
            return null;
        }

        AysProcessDataModel.AysProcessDataModelBuilder aysProcessDataModel = AysProcessDataModel.builder();

        aysProcessDataModel.dataId( aysPreprocessDataEntity.getDataId() );
        aysProcessDataModel.id( aysPreprocessDataEntity.getId() );
        aysProcessDataModel.modelType( aysPreprocessDataEntity.getModelType() );
        aysProcessDataModel.workId( aysPreprocessDataEntity.getWorkId() );
        aysProcessDataModel.clientId( aysPreprocessDataEntity.getClientId() );
        aysProcessDataModel.channelId( aysPreprocessDataEntity.getChannelId() );
        aysProcessDataModel.contentType( aysPreprocessDataEntity.getContentType() );
        aysProcessDataModel.data( aysPreprocessDataEntity.getData() );
        aysProcessDataModel.extFields( aysPreprocessDataEntity.getExtFields() );
        aysProcessDataModel.bizExtAttrs( aysPreprocessDataEntity.getBizExtAttrs() );
        aysProcessDataModel.bizExtAttrs2( aysPreprocessDataEntity.getBizExtAttrs2() );
        aysProcessDataModel.bizExtAttrs3( aysPreprocessDataEntity.getBizExtAttrs3() );
        aysProcessDataModel.custExtAttrs( aysPreprocessDataEntity.getCustExtAttrs() );
        aysProcessDataModel.vhlExtAttrs( aysPreprocessDataEntity.getVhlExtAttrs() );
        aysProcessDataModel.dealerExtAttrs( aysPreprocessDataEntity.getDealerExtAttrs() );
        aysProcessDataModel.prdExtAttrs( aysPreprocessDataEntity.getPrdExtAttrs() );
        aysProcessDataModel.oneId( aysPreprocessDataEntity.getOneId() );
        aysProcessDataModel.dataMd5( aysPreprocessDataEntity.getDataMd5() );
        aysProcessDataModel.publishTime( aysPreprocessDataEntity.getPublishTime() );
        aysProcessDataModel.createTime( aysPreprocessDataEntity.getCreateTime() );
        aysProcessDataModel.abandon( aysPreprocessDataEntity.getAbandon() );
        aysProcessDataModel.done( aysPreprocessDataEntity.getDone() );

        return aysProcessDataModel.build();
    }

    protected AysProcessDataModel aysPostprocessValidDataEntityToAysProcessDataModel(AysPostprocessValidDataEntity aysPostprocessValidDataEntity) {
        if ( aysPostprocessValidDataEntity == null ) {
            return null;
        }

        AysProcessDataModel.AysProcessDataModelBuilder aysProcessDataModel = AysProcessDataModel.builder();

        aysProcessDataModel.id( aysPostprocessValidDataEntity.getId() );
        aysProcessDataModel.originalId( aysPostprocessValidDataEntity.getOriginalId() );
        aysProcessDataModel.modelType( aysPostprocessValidDataEntity.getModelType() );
        aysProcessDataModel.workId( aysPostprocessValidDataEntity.getWorkId() );
        aysProcessDataModel.clientId( aysPostprocessValidDataEntity.getClientId() );
        aysProcessDataModel.channelId( aysPostprocessValidDataEntity.getChannelId() );
        aysProcessDataModel.contentType( aysPostprocessValidDataEntity.getContentType() );
        aysProcessDataModel.extFields( aysPostprocessValidDataEntity.getExtFields() );
        aysProcessDataModel.publishTime( aysPostprocessValidDataEntity.getPublishTime() );
        aysProcessDataModel.createTime( aysPostprocessValidDataEntity.getCreateTime() );
        aysProcessDataModel.abandon( aysPostprocessValidDataEntity.getAbandon() );
        aysProcessDataModel.done( aysPostprocessValidDataEntity.getDone() );

        return aysProcessDataModel.build();
    }

    protected AysProcessDataModel aysPostprocessDataEntityToAysProcessDataModel(AysPostprocessDataEntity aysPostprocessDataEntity) {
        if ( aysPostprocessDataEntity == null ) {
            return null;
        }

        AysProcessDataModel.AysProcessDataModelBuilder aysProcessDataModel = AysProcessDataModel.builder();

        aysProcessDataModel.dataId( aysPostprocessDataEntity.getDataId() );
        aysProcessDataModel.id( aysPostprocessDataEntity.getId() );
        aysProcessDataModel.originalId( aysPostprocessDataEntity.getOriginalId() );
        aysProcessDataModel.modelType( aysPostprocessDataEntity.getModelType() );
        aysProcessDataModel.workId( aysPostprocessDataEntity.getWorkId() );
        aysProcessDataModel.clientId( aysPostprocessDataEntity.getClientId() );
        aysProcessDataModel.channelId( aysPostprocessDataEntity.getChannelId() );
        aysProcessDataModel.contentType( aysPostprocessDataEntity.getContentType() );
        aysProcessDataModel.extFields( aysPostprocessDataEntity.getExtFields() );
        aysProcessDataModel.bizExtAttrs( aysPostprocessDataEntity.getBizExtAttrs() );
        aysProcessDataModel.bizExtAttrs2( aysPostprocessDataEntity.getBizExtAttrs2() );
        aysProcessDataModel.bizExtAttrs3( aysPostprocessDataEntity.getBizExtAttrs3() );
        aysProcessDataModel.oneId( aysPostprocessDataEntity.getOneId() );
        aysProcessDataModel.publishTime( aysPostprocessDataEntity.getPublishTime() );
        aysProcessDataModel.createTime( aysPostprocessDataEntity.getCreateTime() );
        aysProcessDataModel.abandon( aysPostprocessDataEntity.getAbandon() );
        aysProcessDataModel.done( aysPostprocessDataEntity.getDone() );

        return aysProcessDataModel.build();
    }

    protected AysMetaDataAnalysisEntity aysMetaDataAnalysisModelToAysMetaDataAnalysisEntity(AysMetaDataAnalysisModel aysMetaDataAnalysisModel) {
        if ( aysMetaDataAnalysisModel == null ) {
            return null;
        }

        AysMetaDataAnalysisEntity.AysMetaDataAnalysisEntityBuilder aysMetaDataAnalysisEntity = AysMetaDataAnalysisEntity.builder();

        aysMetaDataAnalysisEntity.id( aysMetaDataAnalysisModel.getId() );
        aysMetaDataAnalysisEntity.dataId( aysMetaDataAnalysisModel.getDataId() );
        aysMetaDataAnalysisEntity.workId( aysMetaDataAnalysisModel.getWorkId() );
        aysMetaDataAnalysisEntity.clientId( aysMetaDataAnalysisModel.getClientId() );
        aysMetaDataAnalysisEntity.channelId( aysMetaDataAnalysisModel.getChannelId() );
        aysMetaDataAnalysisEntity.contentType( aysMetaDataAnalysisModel.getContentType() );
        aysMetaDataAnalysisEntity.data( aysMetaDataAnalysisModel.getData() );
        aysMetaDataAnalysisEntity.done( aysMetaDataAnalysisModel.getDone() );
        aysMetaDataAnalysisEntity.dataStatus( aysMetaDataAnalysisModel.getDataStatus() );
        aysMetaDataAnalysisEntity.title( aysMetaDataAnalysisModel.getTitle() );
        aysMetaDataAnalysisEntity.content( aysMetaDataAnalysisModel.getContent() );
        aysMetaDataAnalysisEntity.userName( aysMetaDataAnalysisModel.getUserName() );
        aysMetaDataAnalysisEntity.createTime( aysMetaDataAnalysisModel.getCreateTime() );
        aysMetaDataAnalysisEntity.publishTime( aysMetaDataAnalysisModel.getPublishTime() );
        aysMetaDataAnalysisEntity.modelType( aysMetaDataAnalysisModel.getModelType() );
        aysMetaDataAnalysisEntity.extFields( aysMetaDataAnalysisModel.getExtFields() );
        aysMetaDataAnalysisEntity.bizExtAttrs( aysMetaDataAnalysisModel.getBizExtAttrs() );
        aysMetaDataAnalysisEntity.bizExtAttrs2( aysMetaDataAnalysisModel.getBizExtAttrs2() );
        aysMetaDataAnalysisEntity.bizExtAttrs3( aysMetaDataAnalysisModel.getBizExtAttrs3() );
        aysMetaDataAnalysisEntity.custExtAttrs( aysMetaDataAnalysisModel.getCustExtAttrs() );
        aysMetaDataAnalysisEntity.vhlExtAttrs( aysMetaDataAnalysisModel.getVhlExtAttrs() );
        aysMetaDataAnalysisEntity.dealerExtAttrs( aysMetaDataAnalysisModel.getDealerExtAttrs() );
        aysMetaDataAnalysisEntity.prdExtAttrs( aysMetaDataAnalysisModel.getPrdExtAttrs() );
        aysMetaDataAnalysisEntity.oneId( aysMetaDataAnalysisModel.getOneId() );

        return aysMetaDataAnalysisEntity.build();
    }

    protected AysPreprocessDataModel aysPreprocessDataEntityToAysPreprocessDataModel(AysPreprocessDataEntity aysPreprocessDataEntity) {
        if ( aysPreprocessDataEntity == null ) {
            return null;
        }

        AysPreprocessDataModel.AysPreprocessDataModelBuilder aysPreprocessDataModel = AysPreprocessDataModel.builder();

        aysPreprocessDataModel.dataId( aysPreprocessDataEntity.getDataId() );
        aysPreprocessDataModel.id( aysPreprocessDataEntity.getId() );
        aysPreprocessDataModel.workId( aysPreprocessDataEntity.getWorkId() );
        aysPreprocessDataModel.clientId( aysPreprocessDataEntity.getClientId() );
        aysPreprocessDataModel.contentType( aysPreprocessDataEntity.getContentType() );
        aysPreprocessDataModel.channelId( aysPreprocessDataEntity.getChannelId() );
        aysPreprocessDataModel.data( aysPreprocessDataEntity.getData() );
        aysPreprocessDataModel.dataMd5( aysPreprocessDataEntity.getDataMd5() );
        aysPreprocessDataModel.publishTime( aysPreprocessDataEntity.getPublishTime() );
        aysPreprocessDataModel.createTime( aysPreprocessDataEntity.getCreateTime() );
        aysPreprocessDataModel.hitRules( aysPreprocessDataEntity.getHitRules() );
        aysPreprocessDataModel.abandon( aysPreprocessDataEntity.getAbandon() );
        aysPreprocessDataModel.done( aysPreprocessDataEntity.getDone() );
        aysPreprocessDataModel.modelType( aysPreprocessDataEntity.getModelType() );
        aysPreprocessDataModel.extFields( aysPreprocessDataEntity.getExtFields() );
        aysPreprocessDataModel.bizExtAttrs( aysPreprocessDataEntity.getBizExtAttrs() );
        aysPreprocessDataModel.bizExtAttrs2( aysPreprocessDataEntity.getBizExtAttrs2() );
        aysPreprocessDataModel.bizExtAttrs3( aysPreprocessDataEntity.getBizExtAttrs3() );
        aysPreprocessDataModel.custExtAttrs( aysPreprocessDataEntity.getCustExtAttrs() );
        aysPreprocessDataModel.vhlExtAttrs( aysPreprocessDataEntity.getVhlExtAttrs() );
        aysPreprocessDataModel.dealerExtAttrs( aysPreprocessDataEntity.getDealerExtAttrs() );
        aysPreprocessDataModel.prdExtAttrs( aysPreprocessDataEntity.getPrdExtAttrs() );
        aysPreprocessDataModel.oneId( aysPreprocessDataEntity.getOneId() );

        return aysPreprocessDataModel.build();
    }

    protected AysPreprocessDataModel aysPostprocessDataEntityToAysPreprocessDataModel(AysPostprocessDataEntity aysPostprocessDataEntity) {
        if ( aysPostprocessDataEntity == null ) {
            return null;
        }

        AysPreprocessDataModel.AysPreprocessDataModelBuilder aysPreprocessDataModel = AysPreprocessDataModel.builder();

        aysPreprocessDataModel.dataId( aysPostprocessDataEntity.getDataId() );
        aysPreprocessDataModel.id( aysPostprocessDataEntity.getId() );
        aysPreprocessDataModel.workId( aysPostprocessDataEntity.getWorkId() );
        aysPreprocessDataModel.clientId( aysPostprocessDataEntity.getClientId() );
        aysPreprocessDataModel.contentType( aysPostprocessDataEntity.getContentType() );
        aysPreprocessDataModel.channelId( aysPostprocessDataEntity.getChannelId() );
        aysPreprocessDataModel.publishTime( aysPostprocessDataEntity.getPublishTime() );
        aysPreprocessDataModel.createTime( aysPostprocessDataEntity.getCreateTime() );
        aysPreprocessDataModel.hitRules( aysPostprocessDataEntity.getHitRules() );
        aysPreprocessDataModel.abandon( aysPostprocessDataEntity.getAbandon() );
        aysPreprocessDataModel.done( aysPostprocessDataEntity.getDone() );
        aysPreprocessDataModel.modelType( aysPostprocessDataEntity.getModelType() );
        aysPreprocessDataModel.extFields( aysPostprocessDataEntity.getExtFields() );
        aysPreprocessDataModel.bizExtAttrs( aysPostprocessDataEntity.getBizExtAttrs() );
        aysPreprocessDataModel.bizExtAttrs2( aysPostprocessDataEntity.getBizExtAttrs2() );
        aysPreprocessDataModel.bizExtAttrs3( aysPostprocessDataEntity.getBizExtAttrs3() );
        aysPreprocessDataModel.oneId( aysPostprocessDataEntity.getOneId() );

        return aysPreprocessDataModel.build();
    }

    protected AysProcessDataModel aysModelResltDataEntityToAysProcessDataModel(AysModelResltDataEntity aysModelResltDataEntity) {
        if ( aysModelResltDataEntity == null ) {
            return null;
        }

        AysProcessDataModel.AysProcessDataModelBuilder aysProcessDataModel = AysProcessDataModel.builder();

        aysProcessDataModel.dataId( aysModelResltDataEntity.getDataId() );
        aysProcessDataModel.id( aysModelResltDataEntity.getId() );
        aysProcessDataModel.originalId( aysModelResltDataEntity.getOriginalId() );
        aysProcessDataModel.modelType( aysModelResltDataEntity.getModelType() );
        aysProcessDataModel.workId( aysModelResltDataEntity.getWorkId() );
        aysProcessDataModel.clientId( aysModelResltDataEntity.getClientId() );
        aysProcessDataModel.channelId( aysModelResltDataEntity.getChannelId() );
        aysProcessDataModel.contentType( aysModelResltDataEntity.getContentType() );
        aysProcessDataModel.data( aysModelResltDataEntity.getData() );
        aysProcessDataModel.extFields( aysModelResltDataEntity.getExtFields() );
        aysProcessDataModel.bizExtAttrs( aysModelResltDataEntity.getBizExtAttrs() );
        aysProcessDataModel.bizExtAttrs2( aysModelResltDataEntity.getBizExtAttrs2() );
        aysProcessDataModel.bizExtAttrs3( aysModelResltDataEntity.getBizExtAttrs3() );
        aysProcessDataModel.oneId( aysModelResltDataEntity.getOneId() );
        aysProcessDataModel.dataMd5( aysModelResltDataEntity.getDataMd5() );
        aysProcessDataModel.publishTime( aysModelResltDataEntity.getPublishTime() );
        aysProcessDataModel.createTime( aysModelResltDataEntity.getCreateTime() );
        aysProcessDataModel.done( aysModelResltDataEntity.getDone() );

        return aysProcessDataModel.build();
    }
}
