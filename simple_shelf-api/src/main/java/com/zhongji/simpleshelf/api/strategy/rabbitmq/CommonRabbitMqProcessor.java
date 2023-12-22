package com.zhongji.simpleshelf.api.strategy.rabbitmq;

import com.zhongji.simpleshelf.common.constant.CommonConstants;
import org.springframework.stereotype.Service;

@Service
public class CommonRabbitMqProcessor extends AbstractRabbitMqProcessor{

    @Override
    public String handleType() {
        return CommonConstants.RABBIT_EXCHANGE_DEFAULT;
    }

    @Override
    public void handlerSendToExchangeFailure() {

    }

    @Override
    public void handlerSendToQueueFailure() {

    }


}
