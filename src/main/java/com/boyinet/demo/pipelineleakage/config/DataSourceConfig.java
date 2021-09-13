package com.boyinet.demo.pipelineleakage.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;


/**
 * 配置多个数据连接源
 *
 * @author lengchunyun
 */
@Configuration
public class DataSourceConfig {

    /**
     * 第一个数据连接，默认优先级最高
     *
     * @return primaryDataSource
     */
    @Primary
    @Bean(name = "primaryDataSource")
    @Qualifier("primaryDataSource")
    @ConfigurationProperties(prefix = "spring.datasource.primary")
    public DataSource primaryDataSource() {
        return DataSourceBuilder.create().build();
    }

    /**
     * 第二个数据源
     *
     * @return secondaryDataSource
     */
    @Bean(name = "secondDataSource")
    @Qualifier("secondDataSource")
    @ConfigurationProperties(prefix = "spring.datasource.second")
    public DataSource secondaryDataSource() {
        return DataSourceBuilder.create().build();
    }
}
