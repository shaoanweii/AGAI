package com.voc.service.components.mybatis.config;

import com.baomidou.dynamic.datasource.ds.ItemDataSource;
import com.zaxxer.hikari.HikariDataSource;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

/**
 * @Title: DataSourceConfig
 * @Package: com.voc.service.components.mybatis.config
 * @Description:
 * @Author: cuick
 * @Date: 2024/11/27 10:34
 * @Version:1.0
 */
@Configuration
public class DataSourceConfig {
    private static final Logger log = LoggerFactory.getLogger(DataSourceConfig.class);
    @Autowired
    DataSource dataSource;

    @PostConstruct
    public void init(){
        try {
            log.info("--->> dataSource: {}", dataSource);
            if (dataSource instanceof com.zaxxer.hikari.HikariDataSource) {
                com.zaxxer.hikari.HikariDataSource hikariDataSource = (com.zaxxer.hikari.HikariDataSource) dataSource;
                log.info("{} - minimum-idle:{} maximum-pool-size:{} \n\t {} - {}  ", hikariDataSource.getPoolName()
                        , hikariDataSource.getMinimumIdle(),hikariDataSource.getMaximumPoolSize()
                        ,hikariDataSource.getPoolName() ,hikariDataSource.getJdbcUrl());
            } else if (dataSource instanceof com.baomidou.dynamic.datasource.DynamicRoutingDataSource) {
                com.baomidou.dynamic.datasource.DynamicRoutingDataSource dynamicRoutingDataSource = (com.baomidou.dynamic.datasource.DynamicRoutingDataSource) dataSource;

                dynamicRoutingDataSource.getDataSources().keySet().forEach(key -> {
                    DataSource ds = dynamicRoutingDataSource.getDataSources().get(key);
                    if (ds instanceof com.baomidou.dynamic.datasource.ds.ItemDataSource) {
                        if (((ItemDataSource) ds).getDataSource() instanceof com.zaxxer.hikari.HikariDataSource) {
                            com.zaxxer.hikari.HikariDataSource hikariDataSource = (HikariDataSource) ((ItemDataSource) ds).getDataSource();
                            log.info("{} - minimum-idle:{} maximum-pool-size:{} \n\t {} - {}  ", hikariDataSource.getPoolName()
                                    , hikariDataSource.getMinimumIdle(),hikariDataSource.getMaximumPoolSize()
                                    ,hikariDataSource.getPoolName() ,hikariDataSource.getJdbcUrl());
                        }
                    }
                });
            }
        }catch (Exception e){
            log.error(e.getMessage());
        }
    }
}
