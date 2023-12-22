package com.zhongji.simpleshelf.api.bo.mq;


public class KgMessage<H, T> {
    /**
     * 消息id
     */
    private String msgId;

    /**
     * 消息topic等特征
     */
    private MqCharacteristic mqCharacteristic;

    /**
     * 消息头
     */
    private H header;

    /**
     * 消息内容
     */
    private T body;

    public String getMsgId() {
        return msgId;
    }

    public void setMsgId(String msgId) {
        this.msgId = msgId;
    }

    public MqCharacteristic getMqCharacteristic() {
        return mqCharacteristic;
    }

    public void setMqCharacteristic(MqCharacteristic mqCharacteristic) {
        this.mqCharacteristic = mqCharacteristic;
    }

    public H getHeader() {
        return header;
    }

    public void setHeader(H header) {
        this.header = header;
    }

    public T getBody() {
        return body;
    }

    public void setBody(T body) {
        this.body = body;
    }
}
