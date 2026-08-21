package com.voc.service.security.api;

/**
 * @Title: ICustomInfoService
 * @Package: com.voc.service.security.api
 * @Description:
 * @Author: cuick
 * @Date: 2024/7/1 10:20
 * @Version:1.0
 */
public interface ICustomInfoService {
    /**
     * @创建者/修改者 fanrong
     * @创建/更新日期 2024/7/1 上午11:55
     * @描述 获取用户信息
     * @return java.lang.Object
     **/
    Object getUserInfo();
    /**
     * @创建者/修改者 fanrong
     * @创建/更新日期 2024/7/1 上午11:55
     * @描述 获取用户权限
     * @return java.lang.Object
     **/
    Object getUserPermissions();
}
