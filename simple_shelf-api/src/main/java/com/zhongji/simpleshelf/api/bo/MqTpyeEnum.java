package com.zhongji.simpleshelf.api.bo;

import com.zhongji.simpleshelf.api.client.mq.kafka.KafkaProducerClient;
import com.zhongji.simpleshelf.api.client.mq.rabbitmq.KgBootRabbitmqClient;

public enum MqTpyeEnum {
    KAFKA(KafkaProducerClient.class), RABBITMQ(KgBootRabbitmqClient.class);

    private Class mqClientClass;

    MqTpyeEnum(Class mqClientClass) {
        this.mqClientClass = mqClientClass;
    }

    public Class getMqClientClass() {
        return mqClientClass;
    }
}
