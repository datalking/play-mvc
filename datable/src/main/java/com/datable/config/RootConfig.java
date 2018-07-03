package com.datable.config;

import com.github.datalking.annotation.Bean;
import com.github.datalking.annotation.Configuration;
import com.github.datalking.annotation.Import;
import com.github.datalking.common.env.PropertySourcesPlaceholderConfigurer;

@Configuration
//@Import({DaoConfig.class, ServiceConfig.class, WebConfig.class})
@Import({WebConfig.class})
public class RootConfig {

    @Bean
    public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {

        return new PropertySourcesPlaceholderConfigurer();
    }

}
