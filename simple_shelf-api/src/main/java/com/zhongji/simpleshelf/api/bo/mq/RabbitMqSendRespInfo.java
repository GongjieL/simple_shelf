package com.zhongji.simpleshelf.api.bo.mq;

public class RabbitMqSendRespInfo {
    private String msgType;

    private String msgId;

    private String routingKey;

    private String cause;

    private Boolean success;

}
