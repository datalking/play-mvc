package com.soecode.lyf.config;

import com.github.datalking.annotation.Bean;
import com.github.datalking.annotation.ComponentScan;
import com.github.datalking.annotation.Configuration;
import com.github.datalking.jdbc.datasource.DataSourceTransactionManager;

import javax.sql.DataSource;

//import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@ComponentScan(basePackages = {"com.soecode.lyf.service"})
//@ComponentScan("com.soecode.lyf.service")
//@EnableTransactionManagement
public class ServiceConfig {

    @Bean
    public DataSourceTransactionManager transactionManager(DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }

}
