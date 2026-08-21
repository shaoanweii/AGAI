package com.voc.service.data.integration.mpp.cenvert;

import com.voc.service.data.integration.api.model.ChannelInfoDataModel;
import com.voc.service.data.integration.api.model.ChannelMetaDataModel;
import com.voc.service.data.integration.api.model.DataIntegrationRecordModel;
import com.voc.service.data.integration.entity.ChannelExecutionResultEntity;
import com.voc.service.data.integration.entity.ChannelInfoDataEntity;
import com.voc.service.data.integration.entity.ChannelMetaDataEntity;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-03-11T15:23:00+0800",
    comments = "version: 1.6.3, compiler: javac, environment: Java 17.0.9 (Oracle Corporation)"
)
@Component
public class DataIntegrationConvertMapperServiceImpl implements DataIntegrationConvertMapperService {

    @Override
    public List<ChannelExecutionResultEntity> cenvertToEntityList(List<DataIntegrationRecordModel> list) {
        if ( list == null ) {
            return null;
        }

        List<ChannelExecutionResultEntity> list1 = new ArrayList<ChannelExecutionResultEntity>( list.size() );
        for ( DataIntegrationRecordModel dataIntegrationRecordModel : list ) {
            list1.add( dataIntegrationRecordModelToChannelExecutionResultEntity( dataIntegrationRecordModel ) );
        }

        return list1;
    }

    @Override
    public List<DataIntegrationRecordModel> cenvertToModelList(List<ChannelExecutionResultEntity> entityList) {
        if ( entityList == null ) {
            return null;
        }

        List<DataIntegrationRecordModel> list = new ArrayList<DataIntegrationRecordModel>( entityList.size() );
        for ( ChannelExecutionResultEntity channelExecutionResultEntity : entityList ) {
            list.add( channelExecutionResultEntityToDataIntegrationRecordModel( channelExecutionResultEntity ) );
        }

        return list;
    }

    @Override
    public List<ChannelMetaDataModel> cenvertToChannelMetaDataModelList(List<ChannelMetaDataEntity> entityList) {
        if ( entityList == null ) {
            return null;
        }

        List<ChannelMetaDataModel> list = new ArrayList<ChannelMetaDataModel>( entityList.size() );
        for ( ChannelMetaDataEntity channelMetaDataEntity : entityList ) {
            list.add( channelMetaDataEntityToChannelMetaDataModel( channelMetaDataEntity ) );
        }

        return list;
    }

    @Override
    public List<ChannelInfoDataModel> convertToChannelInfoDataModelList(List<ChannelInfoDataEntity> list) {
        if ( list == null ) {
            return null;
        }

        List<ChannelInfoDataModel> list1 = new ArrayList<ChannelInfoDataModel>( list.size() );
        for ( ChannelInfoDataEntity channelInfoDataEntity : list ) {
            list1.add( channelInfoDataEntityToChannelInfoDataModel( channelInfoDataEntity ) );
        }

        return list1;
    }

    protected ChannelExecutionResultEntity dataIntegrationRecordModelToChannelExecutionResultEntity(DataIntegrationRecordModel dataIntegrationRecordModel) {
        if ( dataIntegrationRecordModel == null ) {
            return null;
        }

        ChannelExecutionResultEntity.ChannelExecutionResultEntityBuilder channelExecutionResultEntity = ChannelExecutionResultEntity.builder();

        channelExecutionResultEntity.id( dataIntegrationRecordModel.getId() );
        channelExecutionResultEntity.dataId( dataIntegrationRecordModel.getDataId() );
        channelExecutionResultEntity.workId( dataIntegrationRecordModel.getWorkId() );
        channelExecutionResultEntity.channelType( dataIntegrationRecordModel.getChannelType() );
        channelExecutionResultEntity.retryCount( dataIntegrationRecordModel.getRetryCount() );
        channelExecutionResultEntity.errorCode( dataIntegrationRecordModel.getErrorCode() );
        channelExecutionResultEntity.errorMsg( dataIntegrationRecordModel.getErrorMsg() );
        channelExecutionResultEntity.createTime( dataIntegrationRecordModel.getCreateTime() );
        channelExecutionResultEntity.lastExecTime( dataIntegrationRecordModel.getLastExecTime() );
        channelExecutionResultEntity.status( dataIntegrationRecordModel.getStatus() );
        channelExecutionResultEntity.tid( dataIntegrationRecordModel.getTid() );
        channelExecutionResultEntity.data( dataIntegrationRecordModel.getData() );

        return channelExecutionResultEntity.build();
    }

    protected DataIntegrationRecordModel channelExecutionResultEntityToDataIntegrationRecordModel(ChannelExecutionResultEntity channelExecutionResultEntity) {
        if ( channelExecutionResultEntity == null ) {
            return null;
        }

        DataIntegrationRecordModel.DataIntegrationRecordModelBuilder dataIntegrationRecordModel = DataIntegrationRecordModel.builder();

        dataIntegrationRecordModel.id( channelExecutionResultEntity.getId() );
        dataIntegrationRecordModel.dataId( channelExecutionResultEntity.getDataId() );
        dataIntegrationRecordModel.workId( channelExecutionResultEntity.getWorkId() );
        dataIntegrationRecordModel.channelType( channelExecutionResultEntity.getChannelType() );
        dataIntegrationRecordModel.retryCount( channelExecutionResultEntity.getRetryCount() );
        dataIntegrationRecordModel.errorCode( channelExecutionResultEntity.getErrorCode() );
        dataIntegrationRecordModel.errorMsg( channelExecutionResultEntity.getErrorMsg() );
        dataIntegrationRecordModel.createTime( channelExecutionResultEntity.getCreateTime() );
        dataIntegrationRecordModel.lastExecTime( channelExecutionResultEntity.getLastExecTime() );
        dataIntegrationRecordModel.status( channelExecutionResultEntity.getStatus() );
        dataIntegrationRecordModel.tid( channelExecutionResultEntity.getTid() );
        dataIntegrationRecordModel.data( channelExecutionResultEntity.getData() );

        return dataIntegrationRecordModel.build();
    }

    protected ChannelMetaDataModel channelMetaDataEntityToChannelMetaDataModel(ChannelMetaDataEntity channelMetaDataEntity) {
        if ( channelMetaDataEntity == null ) {
            return null;
        }

        ChannelMetaDataModel.ChannelMetaDataModelBuilder channelMetaDataModel = ChannelMetaDataModel.builder();

        channelMetaDataModel.id( channelMetaDataEntity.getId() );
        channelMetaDataModel.oneId( channelMetaDataEntity.getOneId() );
        channelMetaDataModel.channelBiz( channelMetaDataEntity.getChannelBiz() );
        channelMetaDataModel.channelDc( channelMetaDataEntity.getChannelDc() );
        channelMetaDataModel.dataId( channelMetaDataEntity.getDataId() );
        channelMetaDataModel.createTime( channelMetaDataEntity.getCreateTime() );
        channelMetaDataModel.bizCreateTime( channelMetaDataEntity.getBizCreateTime() );
        channelMetaDataModel.content( channelMetaDataEntity.getContent() );
        channelMetaDataModel.dealershipCodePurchase( channelMetaDataEntity.getDealershipCodePurchase() );
        channelMetaDataModel.extAttrs( channelMetaDataEntity.getExtAttrs() );
        channelMetaDataModel.extAttrs2( channelMetaDataEntity.getExtAttrs2() );
        channelMetaDataModel.extAttrs3( channelMetaDataEntity.getExtAttrs3() );

        return channelMetaDataModel.build();
    }

    protected ChannelInfoDataModel channelInfoDataEntityToChannelInfoDataModel(ChannelInfoDataEntity channelInfoDataEntity) {
        if ( channelInfoDataEntity == null ) {
            return null;
        }

        ChannelInfoDataModel.ChannelInfoDataModelBuilder channelInfoDataModel = ChannelInfoDataModel.builder();

        channelInfoDataModel.code( channelInfoDataEntity.getCode() );
        channelInfoDataModel.name( channelInfoDataEntity.getName() );

        return channelInfoDataModel.build();
    }
}
