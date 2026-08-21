package com.voc.service.security.impl.converts;

import com.voc.service.common.model.AccountModel;
import com.voc.service.common.model.UserModel;
import com.voc.service.security.impl.entity.*;
import com.voc.service.security.model.AppModel;
import com.voc.service.security.model.ChangePasswordRequest;
import com.voc.service.security.model.CredentialsModel;
import com.voc.service.security.model.LoginHistroyModel;
import org.mapstruct.*;
import org.springframework.security.core.userdetails.UserDetails;

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
public interface SecurityConverMapperService {
    //    SecurityConverMapperService instace = Mappers.getMapper(SecurityConverMapperService.class);


    CredentialsModel converToCredential(CredentialsEntity entity);
    AccountModel converToAccount(CredentialsEntity entity);
    List<AccountModel> converToAccountList(List<CredentialsEntity> entity);

    CredentialsEntity converToCredential(CredentialsModel model);

    AppModel converTo(AppEntity entity);

    //    UserModel converTo(RegisterRequest request);

    UserModel converTo(UserDetails user);

    @Mappings({
            @Mapping(source = "identityType", target = "type")
    })
    UserModel converTo(UserEntity user);

    @Mappings({
            @Mapping(source = "identityType", target = "type")
    })
    UserModel userChangeRecordEntityConvertToModel(UserChangeRecordEntity user);

    UserEntity converTo(CredentialsModel model);

    @Mappings({
            @Mapping(source = "type", target = "identityType")
    })
    UserEntity converTo(UserModel user);

    @Mappings({
            @Mapping(source = "type", target = "identityType")
    })
    UserChangeRecordEntity userModelConverToChangeRecordEntity(UserModel user);

    UserEntity converTo(ChangePasswordRequest user);

    @Mappings({
            @Mapping(source = "type", target = "identityType")
    })
    UserEntity converToUserDetails(UserModel user);

    UserEntity converToEntity(UserModel request);

    LoginHistroyEntity converTo(LoginHistroyModel model);

    List<CredentialsChangeRecordEntity> credentialsModelListConverToChangeRecordList(List<CredentialsModel> models);

    List<CredentialsModel> credentialsEntityListConverToModelList(List<CredentialsEntity> credentialsEntities);

    List<CredentialsModel> changeRecordListConverToModelList(List<CredentialsChangeRecordEntity> credentialsChangeRecordEntities);

    List<CredentialsEntity> credentialsModelListConverToEntityList(List<CredentialsModel> credentialsModels);
}
