package com.voc.service.logs.impl.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.voc.service.logs.impl.entity.OpsRecordLogEntity;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;


public interface OpsLogsMapper extends BaseMapper<OpsRecordLogEntity> {

    /**
     * @功能：清空所有日志记录
     */
    public void removeAll();

    /**
     * 获取系统总访问次数
     *
     * @return Long
     */
    Long findTotalVisitCount();

    //update-begin- for：传入开始时间，结束时间参数

    /**
     * 获取系统今日访问次数
     *
     * @return Long
     */
    Long findTodayVisitCount(@Param("dayStart") LocalDateTime dayStart, @Param("dayEnd") LocalDateTime dayEnd);

    /**
     * 获取系统今日访问 IP数
     *
     * @return Long
     */
    Long findTodayIp(@Param("dayStart") LocalDateTime dayStart, @Param("dayEnd") LocalDateTime dayEnd);
    //update-end- for：传入开始时间，结束时间参数

    /**
     * 首页：根据时间统计访问数量/ip数量
     *
     * @param dayStart
     * @param dayEnd
     * @return
     */
    List<Map<String, Object>> findVisitCount(@Param("dayStart") LocalDateTime dayStart, @Param("dayEnd") LocalDateTime dayEnd, @Param("dbType") String dbType);

    @Select("SELECT count(USERID) from SYS_LOG where USERID = #{UserId} and LOG_TYPE = 1 group by USERID ")
    String selectLoginCountByUserId(String UserId);

    @Select("SELECT CREATE_TIME from SYS_LOG where USERID = #{UserId} and LOG_TYPE = 1  ORDER BY CREATE_TIME desc limit 1")
    String selectLastLoginTime(String UserId);

    List<OpsRecordLogEntity> queryPageList(@Param("pageNum") Integer pageNum, @Param("pageSize") Integer pageSize, @Param("syslog") OpsRecordLogEntity syslog);


    Integer queryPageCount(@Param("syslog") OpsRecordLogEntity syslog);

}