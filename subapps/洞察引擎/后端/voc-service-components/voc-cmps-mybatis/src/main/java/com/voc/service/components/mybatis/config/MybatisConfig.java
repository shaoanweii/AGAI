package com.voc.service.components.mybatis.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @version 1.0.0
 * @ClassName MybatisPlusConfig.java
 * @Description
 * @createTime 2022年10月09日 10:58
 * @Copyright futong
 */

@Configuration
//@AutoConfigureAfter(DruidConfig.class)
//@EnableTransactionManagement
@MapperScan(
        basePackages = {"com.voc.service.model.mapper","com.voc.service.*.*.mapper.*.xml","com.voc.service.*.mapper", "com.voc.service.*.*.mapper" ,"com.voc.service.*.*.*.mapper","com.voc.service.*.*.*.*.mapper","com.voc.service.*.*.*.*.*.mapper"}
)
public class MybatisConfig {
//    @Autowired
//    DynamicRoutingDataSource dynamicRoutingDataSource;

    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        PaginationInnerInterceptor paginationInnerInterceptor = new PaginationInnerInterceptor();
        // 对于单一数据库类型来说,都建议配置DbType值,避免每次分页都去抓取数据库类型
        paginationInnerInterceptor.setDbType(DbType.MYSQL);
        // 将分页插件加入MyBatis-Plus的插件链
        interceptor.addInnerInterceptor(paginationInnerInterceptor);
        return interceptor;
    }

    /*public MybatisConfig() {
        log.info("--->> init {}", this.getClass().getSimpleName());
        DynamicRoutingDataSource dynamicRoutingDataSource = ServiceContextHolder.getApplicationContext().getBean(DynamicRoutingDataSource.class);
        dynamicRoutingDataSource.getDataSources().keySet().stream().forEach(e -> {
            ItemDataSource ids = (ItemDataSource) dynamicRoutingDataSource.getDataSources().get(e);
            if (ids.getDataSource() instanceof DruidDataSource) {
                DruidDataSource ds = (DruidDataSource) ids.getDataSource();
                log.info("--->> [{}]{}", e, ds.getUrl());
            }
        });

    }*/
}
