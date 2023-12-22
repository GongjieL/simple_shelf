package com.zhongji.simpleshelf.api.strategy.rabbitmq;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.clients.consumer.OffsetCommitCallback;
import org.apache.kafka.common.TopicPartition;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;

public abstract class AbstractRabbitMqProcessor<Resp, K, V> implements InitializingBean {


    public abstract String handleType();


    public abstract void handlerSendToExchangeFailure();


    public abstract void handlerSendToQueueFailure();

    @Override
    public void afterPropertiesSet() throws Exception {
        RabbitMqProcessorFactory.registerRabbitMqProcessor(this);
    }

}
