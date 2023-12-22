package com.zhongji.simpleshelf.api.bo.mq.kafka;

import com.zhongji.simpleshelf.api.bo.mq.MqCharacteristic;

/**
 * kafka消息特征
 */
public class KafkaCharacteristic extends MqCharacteristic {
    private String topic;

    private Integer partition;

    private String partitionKey;

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public Integer getPartition() {
        return partition;
    }

    public void setPartition(Integer partition) {
        this.partition = partition;
    }

    public String getPartitionKey() {
        return partitionKey;
    }

    public void setPartitionKey(String partitionKey) {
        this.partitionKey = partitionKey;
    }
}
