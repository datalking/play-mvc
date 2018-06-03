package com.soecode.lyf.config;

import com.github.datalking.SqlSessionFactoryBean;
import com.github.datalking.annotation.Bean;
import com.github.datalking.annotation.Configuration;
import com.github.datalking.annotation.MapperScan;
import com.github.datalking.annotation.PropertySource;
import com.github.datalking.annotation.Value;
import com.github.datalking.mapper.MapperFactoryBean;
import com.mchange.v2.c3p0.ComboPooledDataSource;
import com.soecode.lyf.dao.AppointmentDao;
import com.soecode.lyf.dao.BookDao;
import org.apache.ibatis.session.SqlSessionFactory;

import javax.sql.DataSource;
import java.beans.PropertyVetoException;
import java.util.Properties;

@Configuration
//@PropertySource("classpath:jdbc.properties")
@PropertySource(value = "classpath:jdbc.properties")
@MapperScan("com.soecode.lyf.dao")
public class DaoConfig {

    @Value("${jdbc.driver}")
    private String driver;

    @Value("${jdbc.url}")
    private String url;

    @Value("${jdbc.username}")
    private String username;

    @Value("${jdbc.password}")
    private String password;

    @Bean
    public DataSource dataSource() throws PropertyVetoException {
        ComboPooledDataSource dataSource = new ComboPooledDataSource();
        dataSource.setDriverClass(driver);
        dataSource.setJdbcUrl(url);
        dataSource.setUser(username);
        dataSource.setPassword(password);
        dataSource.setMaxPoolSize(30);
        dataSource.setMinPoolSize(10);
        dataSource.setAutoCommitOnClose(false);
        dataSource.setCheckoutTimeout(10000);
        dataSource.setAcquireRetryAttempts(2);
        return dataSource;
    }

    @Bean
    public SqlSessionFactoryBean sqlSessionFactoryBean(DataSource dataSource) {
        Properties properties = new Properties();
        properties.put("useGeneratedKeys", true);
        properties.put("useColumnLabel", true);
        properties.put("mapUnderscoreToCamelCase", true);

        SqlSessionFactoryBean bean = new SqlSessionFactoryBean();

        bean.setTypeAliasesPackage("com.soecode.lyf.entity");

        bean.setConfigurationProperties(properties);

        bean.setDataSource(dataSource);

        return bean;
    }

    @Bean
    public MapperFactoryBean<BookDao> bookDaoMapper(SqlSessionFactory factory) {
        MapperFactoryBean<BookDao> bean = new MapperFactoryBean<>(BookDao.class);
        bean.setSqlSessionFactory(factory);
        return bean;
    }

    @Bean
    public MapperFactoryBean<AppointmentDao> appointmentDaoMapperFactoryBean(SqlSessionFactory factory) {
        MapperFactoryBean<AppointmentDao> bean = new MapperFactoryBean<>(AppointmentDao.class);
        bean.setSqlSessionFactory(factory);
        return bean;
    }

}
