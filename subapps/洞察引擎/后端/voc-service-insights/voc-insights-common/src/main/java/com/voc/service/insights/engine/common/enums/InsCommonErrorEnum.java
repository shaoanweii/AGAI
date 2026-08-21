package com.voc.service.insights.engine.common.enums;

import com.voc.service.common.exception.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @创建者: fanrong
 * @创建时间: 2024/2/22 10:34
 * @描述:
 **/
@Getter
@AllArgsConstructor
public enum InsCommonErrorEnum implements ErrorCode {
    SAVE_CUSTOMER_ERROR(60001,"保存客户信息失败"),
    UPDATE_CUSTOMER_ERROR(60002,"更新客户信息失败"),
    DELETE_CUSTOMER_ERROR(60003,"删除客户信息失败"),
    CONSTRAINT_VIOLATION_ERROR(60000, "{0}"),
    SAVE_REGULATION_ERROR(60004,"保存规则信息失败"),
    SAVE_REGULATION_DETAILS_ERROR(60005,"保存规则详情信息失败"),
    UPDATE_REGULATION_ERROR(60006,"更新规则信息失败"),
    UPDATE_REGULATION_DETAILS_ERROR(60007,"更新规则详情信息失败"),
    DELETE_REGULATION_ERROR(60008,"删除规则信息失败"),
    DELETE_REGULATION_DETAILS_ERROR(60009,"删除规则详情信息失败"),
    REGISTER_ACCOUNT_ERROR(60010,"新增账号失败"),
    REGISTER_ROLE_ERROR(60090,"账户关联角色信息失败"),
    MENU_NOT_EXIST(60011,"菜单不存在"),
    SAVE_MENU_PERMS_ERROR(60012,"保存菜单权限失败"),
    MODIFY_ACCOUNT_ERROR(60013,"更新账号失败"),
    UPDATE_MENU_PERMS_ERROR(60014,"更新菜单权限失败"),
    REMOVE_ACCOUNT_ERROR(60015,"删除账号失败"),
    REMOVE_MENU_PERMS_ERROR(60016,"删除菜单权限失败"),
    RESET_PASSWORD_ERROR(60017,"重置密码失败"),
    SAVE_TAGLIB_ERROR(60018,"保存标签信息失败"),
    TAGLIB_EXIST(60019,"标签已存在"),
    UPDATE_TAGLIB_ERROR(60020,"更新标签信息失败"),
    UPDATE_BATCH_TAGLIB_ERROR(60021,"批量更新标签信息失败"),
    SAVE_TAGLIB_CLIENT_ERROR(60022,"保存客户标签信息失败"),
    UPDATE_TAGLIB_CLIENT_ERROR(60023,"更新客户标签信息失败"),
    DELETE_TAGLIB_CLIENT_ERROR(60024,"删除客户标签信息失败"),
    SAVE_BATCH_TAGLIB_CLIENT_ERROR(60025,"批量保存客户标签信息失败"),
    SAVE_CHANNEL_ERROR(60026,"保存渠道信息失败"),
    UPDATE_CHANNEL_ERROR(60027,"更新渠道信息失败"),
    DELETE_CHANNEL_ERROR(60028,"删除渠道分类及其子分类失败"),
    SAVE_DATA_SOURCE_ERROR(60029,"保存数据源信息失败"),
    SAVE_BATCH_DATA_SOURCE_DETAIL_ERROR(60030,"批量保存数据源详情信息失败"),
    UPDATE_DATA_SOURCE_ERROR(60031,"更新规则词库信息失败"),
    SAVE_REGION_ERROR(60032,"保存区域分类失败"),
    SAVE_REGION_DETAIL_ERROR(60033,"保存区域分类失败"),
    UPDATE_REGION_ERROR(60034,"更新区域分类失败"),
    UPDATE_REGION_DETAIL_ERROR(60035,"更新区域失败"),
    DELETE_REGION_ERROR(60036,"删除区域分类失败"),
    SAVE_PROJECT_ERROR(60037,"保存项目失败"),
    UPDATE_PROJECT_ERROR(60038,"更新项目失败"),
    SAVE_BATCH_PROJECT_DETAILS_ERROR(60039,"批量保存项目详情失败"),
    UPDATE_BATCH_PROJECT_DETAILS_ERROR(60040,"批量更新项目详情失败"),
    DELETE_BATCH_TAGLIB_CLIENT_ERROR(60041,"批量删除标签失败"),
    BATCH_MOVE_TAGLIB_CLIENT_ERROR(60042, "批量移动标签失败"),
    BATCH_UPDATE_STATUS_TAGLIB_CLIENT_ERROR(60043,"批量更新标签状态失败" ),
    REMOVE_TAGLIB_ERROR(60044,"标签已启用,禁止删除" ),
    REMOVE_TAGLIB_CATEGORY_ERROR(60045,"当前分类下存在下级,禁止删除"),
    SAVE_CLOSED_LOOP_RULE_CATEGORY_ERROR(60046,"保存闭环规则分类失败"),
    UPDATE_CLOSED_LOOP_RULE_CATEGORY_ERROR(60047,"更新闭环规则分类失败"),
    REMOVE_CATEGORY_ERROR(60048,"删除分类失败"),
    SAVE_DATA_RESOURCE_ERROR(60049,"新增关键词失败"),
    SAVE_ACCOUNT_LEXICON_ERROR(60050,"新增账号词库详情失败"),
    UPDATE_ACCOUNT_LEXICON_ERROR(60051,"更新账号词库详情失败"),
    QUOTE_COUNT_NOT_ZERO(60052,"当前信息被引用,不允许被禁用"),
    BATCH_UPDATE_TAGLIB_CLIENT_ERROR(60053, "批量更新标签失败"),
    TOPIC_EXIST(60054,"观点已存在"),
    TOPIC_NOT_EXIST(60055,"观点不存在"),
    SAVE_BRAND_ERROR(60056,"新增品牌失败"),
    BRAND_NOT_EXIST(60057,"品牌不存在"),
    BATCH_CHANGE_STATUS_ERROR(60058,"批量更新状态失败"),
    SAVE_CAR_SERIES_ERROR(60059, "新增车系失败"),
    UPDATE_CAR_SERIES_ERROR(60060, "更新车系失败"),
    UPDATE_BRAND_ERROR(60061,"更新品牌失败"),
    CAR_SERIES_NOT_EXIST(60062,"车系不存在"),
    COMPETITIVE_CAR_SERIES_NOT_EXIST(60063,"竞品车系不存在"),
    UPDATE_COMPETITIVE_CAR_SERIES_ERROR(60064,"更新竞品车系失败"),
    UPDATE_COMPETITIVE_BRAND_ERROR(60065,"更新竞品品牌失败"),
    COMPETITIVE_BRAND_NOT_EXIST(60066,"竞品品牌不存在"),
    UPDATE_COMPETITIVE_AUTOMARK_ERROR(60067,"更新竞品车企失败"),
    COMPETITIVE_AUTOMARK_NOT_EXIST(60068,"竞品车企不存在"),
    AUTOMARK_NOT_EXIST(60069,"车企不存在"),
    SAVE_CAR_SCENE_CATEGORY_ERROR(60070,"新增用车场景分类失败"),
    UPDATE_CAR_SCENE_CATEGORY_ERROR(60071, "更新用车场景分类失败"),
    SAVE_CAR_SCENE_ERROR(60070,"新增用车场景失败"),
    UPDATE_CAR_SCENE_ERROR(60071, "更新用车场景失败"),
    SAVE_ATTRIBUTE_LABEL_ERROR(60072, "新增属性标签失败"),
    UPDATE_ATTRIBUTE_LABEL_ERROR(60073, "更新属性标签失败"),
    ATTRIBUTE_LABEL_NOT_EXIST(60074, "属性标签不存在"),
            ;

    String message;
    Integer code;


    InsCommonErrorEnum(Integer code,String message) {
        this.code = code;
        this.message = message;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public Integer getCode(){return code;}
}
