package com.zhongji.simpleshelf.api.config.mq.rabbitmq;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "kgboot.rabbitmq")
public class RabbitMqProperties {
    private String producerFailureHandler;

    public String getProducerFailureHandler() {
        return producerFailureHandler;
    }

    public void setProducerFailureHandler(String producerFailureHandler) {
        this.producerFailureHandler = producerFailureHandler;
    }
}
