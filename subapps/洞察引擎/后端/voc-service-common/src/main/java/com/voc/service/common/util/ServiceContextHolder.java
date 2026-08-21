package com.voc.service.common.util;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.ttl.TransmittableThreadLocal;
import com.voc.service.common.model.UserModel;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Setter;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.skywalking.apm.toolkit.trace.TraceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Executor;

/**
 * @author ckcui
 * @version 1.0.0
 * @ClassName ServiceContext
 * @Description ckcui
 * @createTime 2023年11月21日 18:33
 * @Copyright futong
 */

@Component
public class ServiceContextHolder implements ApplicationContextAware {
    public static TransmittableThreadLocal<UserModel> userThreadLocal = new TransmittableThreadLocal<>();
    public static TransmittableThreadLocal<String> traceIdThreadLocal = new TransmittableThreadLocal<>();
    public static TransmittableThreadLocal<String> appIdThreadLocal = new TransmittableThreadLocal<>();
    public static TransmittableThreadLocal<String> identityTypeThreadLocal = new TransmittableThreadLocal<>();
    public static TransmittableThreadLocal<String> tokenThreadLocal = new TransmittableThreadLocal<>();
    public static TransmittableThreadLocal<HttpServletRequest> requestThreadLocal = new TransmittableThreadLocal<>();
    public static TransmittableThreadLocal<String> appCodeThreadLocal = new TransmittableThreadLocal<>();
    public static TransmittableThreadLocal<Map<String,String>> deptInfoThreadLocal = new TransmittableThreadLocal<>();
    private static ApplicationContext applicationContext;

    private static Executor executor;
    //系统标识
    @Value("${system.appid:")
    @Setter
    static String appid;
    private static final Logger logger = LoggerFactory.getLogger(ServiceContextHolder.class);
    /**
     * 获取applicationContext
     *
     * @return
     */
    public static ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        appid = applicationContext.getEnvironment().getProperty("system.appid");
        logger.info("appid={}", appid);
        appIdThreadLocal.set(appid);
        ServiceContextHolder.applicationContext = applicationContext;
    }


    public static Executor getExecutor() {
        return executor;
    }

    public static void setExecutor(Executor executor) {
        ServiceContextHolder.executor = executor;
    }

    public static String getSystemId() {
        return appid;
    }

    /**
     * 系统标识
     *
     * @return
     */
    public static String getAppId() {
        Optional<UserModel> user = Optional.ofNullable(getUser());
        if (user.isPresent()) {
            String appId = user.get().getAppId();
            if(ObjectUtils.isNotEmpty(appId)){
                return appId;
            }else if(ObjectUtils.isNotEmpty(appIdThreadLocal.get())){
                return appIdThreadLocal.get();
            }
        }

        return null;
    }

    public static void setAppId(String appId) {
        if (StrUtil.isNotBlank(appId)) {
            appIdThreadLocal.set(appId);
        }
    }



    public static Map<String,String> getDeptInfo(){
        UserModel userModel = Optional.ofNullable(getUser()).orElse(UserModel.builder().build());
        if(ObjectUtils.isNotEmpty(userModel.getBusinessPermissions())&&userModel.getBusinessPermissions().getValues().containsKey("PERMS_DEPART_KEY")){
            return (Map<String, String>) userModel.getBusinessPermissions().getValue("PERMS_DEPART_KEY");
        }
        return Map.of();
    }

    public static String getDeptId(){
        UserModel userModel = Optional.ofNullable(getUser()).orElse(UserModel.builder().build());
        if(ObjectUtils.isNotEmpty(userModel.getBusinessPermissions())&&userModel.getBusinessPermissions().getValues().containsKey("PERMS_DEPART_KEY")){
            Map<String,String> deptInfo = (Map<String, String>) userModel.getBusinessPermissions().getValue("PERMS_DEPART_KEY");
            if(ObjectUtils.isNotEmpty(deptInfo)&&deptInfo.containsKey("deptId")){
                return deptInfo.get("deptId");
            }
        }
        return null;
    }

    public static String getDeptName(){
        UserModel userModel = Optional.ofNullable(getUser()).orElse(UserModel.builder().build());
        if(ObjectUtils.isNotEmpty(userModel.getBusinessPermissions())&&userModel.getBusinessPermissions().getValues().containsKey("PERMS_DEPART_KEY")){
            Map<String,String> deptInfo = (Map<String, String>) userModel.getBusinessPermissions().getValue("PERMS_DEPART_KEY");
            if(ObjectUtils.isNotEmpty(deptInfo)&&deptInfo.containsKey("deptName")){
                return deptInfo.get("deptName");
            }
        }
        return null;
    }

    public static String getDeptCode(){
        UserModel userModel = Optional.ofNullable(getUser()).orElse(UserModel.builder().build());
        if(ObjectUtils.isNotEmpty(userModel.getBusinessPermissions())&&userModel.getBusinessPermissions().getValues().containsKey("PERMS_DEPART_KEY")){
            Map<String,String> deptInfo = (Map<String, String>) userModel.getBusinessPermissions().getValue("PERMS_DEPART_KEY");
            if(ObjectUtils.isNotEmpty(deptInfo)&&deptInfo.containsKey("deptCode")){
                return deptInfo.get("deptCode");
            }
        }
        return null;
    }

    public static Map<String, Object> getSingleEventInfo(){
        UserModel userModel = Optional.ofNullable(getUser()).orElse(UserModel.builder().build());
        if(ObjectUtils.isNotEmpty(userModel.getBusinessPermissions())&&userModel.getBusinessPermissions().getValues().containsKey("PERMS_SINGLE_EVENT_PERMISSION_KEY")){
            return (Map<String, Object>) userModel.getBusinessPermissions().getValue("PERMS_SINGLE_EVENT_PERMISSION_KEY");
        }
        return Map.of();
    }

    public static Object getSingleEventPermission(){
        UserModel userModel = Optional.ofNullable(getUser()).orElse(UserModel.builder().build());
        if(ObjectUtils.isNotEmpty(userModel.getBusinessPermissions())&&userModel.getBusinessPermissions().getValues().containsKey("PERMS_SINGLE_EVENT_PERMISSION_KEY")){
           Map<String,Object> map =   (Map<String, Object>) userModel.getBusinessPermissions().getValue("PERMS_SINGLE_EVENT_PERMISSION_KEY");
           if(ObjectUtils.isNotEmpty( map)&&map.containsKey("single_event_permission")){
               return map.get("single_event_permission");
           }
        }
        return null;
    }

    public static Object getSingleEventScope(){
        UserModel userModel = Optional.ofNullable(getUser()).orElse(UserModel.builder().build());
        if(ObjectUtils.isNotEmpty(userModel.getBusinessPermissions())&&userModel.getBusinessPermissions().getValues().containsKey("PERMS_SINGLE_EVENT_PERMISSION_KEY")){
            Map<String,Object> map =   (Map<String, Object>) userModel.getBusinessPermissions().getValue("PERMS_SINGLE_EVENT_PERMISSION_KEY");
            if(ObjectUtils.isNotEmpty( map)&&map.containsKey("single_event_scope")){
                return map.get("single_event_scope");
            }
        }
        return null;
    }

    public static Object getSingleEventOperation(){
        UserModel userModel = Optional.ofNullable(getUser()).orElse(UserModel.builder().build());
        if(ObjectUtils.isNotEmpty(userModel.getBusinessPermissions())&&userModel.getBusinessPermissions().getValues().containsKey("PERMS_SINGLE_EVENT_PERMISSION_KEY")){
            Map<String,Object> map =   (Map<String, Object>) userModel.getBusinessPermissions().getValue("PERMS_SINGLE_EVENT_PERMISSION_KEY");
            if(ObjectUtils.isNotEmpty( map)&&map.containsKey("single_event_operation")){
                return map.get("single_event_operation");
            }
        }
        return null;
    }

    public static void setAppCode(String appCode){
        if(StrUtil.isNotBlank(appCode)){
            appCodeThreadLocal.set(appCode);
        }
    }

    public static String getAppCode(){
        return Optional.ofNullable(appCodeThreadLocal.get()).orElse(appid);
    }

    public static void setToken(String token) {
        if (StrUtil.isNotBlank(token)) {
            tokenThreadLocal.set(token);
        }
    }

    public static String getToken() {
        return Optional.ofNullable(tokenThreadLocal.get()).orElse(getUser().getToken());
    }

    public static String getIdentityType() {
        Optional<String> identityType = Optional.ofNullable(identityTypeThreadLocal.get());
        if (identityType.isPresent()) {
            return identityType.get();
        }

        Optional<UserModel> user = Optional.ofNullable(getUser());
        if (user.isPresent()) {
            return user.get().getType();
        }

        return null;
    }

    public static void setIdentityType(String identityType) {
        if (StrUtil.isNotBlank(identityType)) {
            identityTypeThreadLocal.set(identityType);
        }
    }

    public static UserModel getUser() {
        try {
            if (ObjectUtil.isNotNull(userThreadLocal.get())) {
                Optional<Object> model = Optional.ofNullable(userThreadLocal.get());
                if (model.isPresent()) {
                    UserModel userModel = (UserModel) model.get();
                    return userModel;
                }
            }
        } catch (Exception e) {
            logger.error("获取当前用户失败！", e.getMessage());
            e.printStackTrace();
        }
        return UserModel.builder().build();
    }

    public static void setUser(UserModel user) {
        userThreadLocal.set(user);
    }

    public static String getUsername() {
        return Optional.ofNullable(getUser()).orElse(UserModel.builder().build()).getUsername();
    }

    public static String getUserId() {
        return Optional.ofNullable(getUser()).orElse(UserModel.builder().build()).getUserId();
    }

    public static boolean isAdmin() {
        UserModel userModel = Optional.ofNullable(getUser()).orElse(UserModel.builder().build());
        if (ObjectUtil.isNotEmpty(userModel.getAdmin()) && "true".equalsIgnoreCase(userModel.getAdmin())) {
            return true;
        }
        return false;
    }

    public static Object getMenus() {
        UserModel userModel = Optional.ofNullable(getUser()).orElse(UserModel.builder().build());
        if (ObjectUtils.isNotEmpty(userModel.getSystemPermissions()) && userModel.getSystemPermissions().getValues().containsKey("PERMS_MENUS_TREE_KEY")) {
            return userModel.getSystemPermissions().getValue("PERMS_MENUS_TREE_KEY");
        }
        return List.of();
    }

    public static Object getDrillDown() {
        UserModel userModel = Optional.ofNullable(getUser()).orElse(UserModel.builder().build());
        if (ObjectUtils.isNotEmpty(userModel.getSystemPermissions()) && userModel.getSystemPermissions().getValues().containsKey("PERMS_DRILL_DOWN_KEY")) {
            return userModel.getSystemPermissions().getValue("PERMS_DRILL_DOWN_KEY");
        }
        return List.of();
    }

    public static Object getButtons() {
        UserModel userModel = Optional.ofNullable(getUser()).orElse(UserModel.builder().build());
        if (ObjectUtils.isNotEmpty(userModel.getSystemPermissions()) && userModel.getSystemPermissions().getValues().containsKey("PERMS_BUTTON_KEY")) {
            return userModel.getSystemPermissions().getValue("PERMS_BUTTON_KEY");
        }
        return List.of();
    }

    public static String getClientId() {
        return Optional.ofNullable(getUser()).orElse(UserModel.builder().build()).getClientId();
    }

    public static void setTraceId(String traceId) {
        traceIdThreadLocal.set(traceId);
    }

    public static String traceId() {
        return Optional.ofNullable(traceIdThreadLocal.get()).orElse(TraceContext.traceId());
    }

    public static HttpServletRequest getRequest() {
        return requestThreadLocal.get();
    }

    public static void setRequest(HttpServletRequest request) {
        requestThreadLocal.set(request);
    }
}
