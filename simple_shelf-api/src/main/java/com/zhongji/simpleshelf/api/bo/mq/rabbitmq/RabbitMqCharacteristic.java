package com.zhongji.simpleshelf.api.bo.mq.rabbitmq;

import com.zhongji.simpleshelf.api.bo.mq.MqCharacteristic;

/**
 * kafka消息特征
 */
public class RabbitMqCharacteristic extends MqCharacteristic {
    private String exchange;

    private Integer routingKey;

    public String getExchange() {
        return exchange;
    }

    public void setExchange(String exchange) {
        this.exchange = exchange;
    }

    public Integer getRoutingKey() {
        return routingKey;
    }

    public void setRoutingKey(Integer routingKey) {
        this.routingKey = routingKey;
    }
}
