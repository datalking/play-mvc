package com.soecode.lyf.config;

import com.github.datalking.annotation.Bean;
import com.github.datalking.annotation.Configuration;
import com.github.datalking.annotation.Import;
import com.github.datalking.common.env.PropertySourcesPlaceholderConfigurer;

@Configuration
//@Import({DaoConfig.class, ServiceConfig.class, WebConfig.class, LogbackConfig.class})
@Import({DaoConfig.class, ServiceConfig.class, WebConfig.class})
public class RootConfig {

    @Bean
    public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {

        return new PropertySourcesPlaceholderConfigurer();
    }

}
