package com.zhongji.simpleshelf.api.config.mq.rabbitmq;

import com.zhongji.simpleshelf.api.client.mq.rabbitmq.KgBootRabbitmqClient;
import com.zhongji.simpleshelf.api.config.APICommonProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({RabbitMqProperties.class, APICommonProperties.class})
public class RabbitmqConfig {



    @Bean
    public KgBootRabbitmqClient kgBootRabbitmqClient() {
        return new KgBootRabbitmqClient();
    }

}

