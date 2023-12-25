package com.zhongji.simpleshelf.web;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.PropertySource;
import org.springframework.kafka.annotation.EnableKafka;

@SpringBootApplication(scanBasePackages = {"com.zhongji.simpleshelf"})
@PropertySource({"classpath:application.properties",
        "classpath:application-common.properties",
        "classpath:application-core.properties",
        "classpath:application-util.properties",
        "classpath:application-mysql-data.properties",
        "classpath:application-dao.properties"})
@MapperScan({"com.zhongji.simpleshelf.dao"})
@EnableKafka
public class KgbootWebApplication {

    public static void main(String[] args) {
        SpringApplication.run(KgbootWebApplication.class, args);
    }

}
