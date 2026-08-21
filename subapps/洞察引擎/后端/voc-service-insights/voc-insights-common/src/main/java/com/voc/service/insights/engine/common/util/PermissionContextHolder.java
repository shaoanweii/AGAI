package com.voc.service.insights.engine.common.util;

import com.voc.service.common.model.auth.PermissionModel;
import com.voc.service.common.util.ServiceContextHolder;
import com.voc.service.insights.engine.api.IConditionFilters;
import com.voc.service.insights.engine.api.constants.InsightsConstants;
import com.voc.service.insights.engine.api.model.ClientModel;
import com.voc.service.insights.engine.common.filters.DefaultAbstractConditionFilters;
import com.voc.service.insights.engine.vo.ConditionVo;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author ckcui
 * @version 1.0.0
 * @ClassName PermissionContextUtil
 * @createTime 2024年01月31日 9:14
 * @Copyright futong
 */
//@Component
public class PermissionContextHolder extends ServiceContextHolder {
    public static List<ClientModel> getClientInfo() {
        return getUser().getBusinessPermissions().getValue(InsightsConstants.PERMS_BIZ_USER_INFO_KEY);
    }

    public static ConditionVo getChannelData() {
        DefaultAbstractConditionFilters condition = getApplicationContext().getBean(DefaultAbstractConditionFilters.class);
        return condition.get(IConditionFilters.CHANNEL);
    }

    /**
     * @创建者/修改者 fanrong
     * @创建/更新日期 2024/2/29 14:08
     * @描述   获取当前用户API访问权限
     * @return java.util.Set<java.lang.String>
     **/
    public static Set<String> getApiPaths() {
        PermissionModel accessPermissions = ServiceContextHolder.getUser().getAccessPermissions();
        final Set<String> paths  = accessPermissions.getValue("paths");
        return paths;
    }

    /**
     * @创建者/修改者 fanrong
     * @创建/更新日期 2024/2/29 14:00
     * @描述  获取当前用户API读写权限
     * @return java.util.Map<java.lang.String,java.lang.String>
     **/
    public static Map<String,String> getApiPerms() {
        PermissionModel accessPermissions = ServiceContextHolder.getUser().getAccessPermissions();
        final Map<String,String> perms  = accessPermissions.getValue("api_perms");
        return perms;
    }
}
