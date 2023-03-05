package com.diachuk.todoapp;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import org.springframework.transaction.annotation.EnableTransactionManagement;


@Configuration
@EnableTransactionManagement
@EnableJpaRepositories

public class DaoConfig {

    @Bean
    public DriverManagerDataSource getDataSource(){
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setUsername("root");
        dataSource.setPassword("root");
        dataSource.setUrl("jdbc:mysql://localhost:3306/somedb");

        return dataSource;
    }


//    @Bean
//    public LocalContainerEntityManagerFactoryBean getEntityManagerFactoryBean(){
//        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
//        em.setDataSource(getDataSource());
//        Properties props = new Properties();
//        props.setProperty("hibernate.dialect", "org.hibernate.dialect.MySQLDialect");
//        props.setProperty("hibernate.generate-ddl", "true");
//        props.setProperty("hibernate.show_sql", "true");
//        props.setProperty("hibernate.format_sql", "true");
//        props.setProperty("hibernate.hbm2ddl.auto", "create-drop");
//        em.setJpaProperties(props);
////        em.setJpaVendorAdapter(adapter);
//        return em;
//    }
}
