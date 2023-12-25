package com.zhongji.simpleshelf.core.strategy;

import com.zhongji.simpleshelf.common.bo.bi.orderandinvoice.OrderAndInvoiceSummary;
import org.springframework.beans.factory.InitializingBean;

import java.util.Date;

public abstract class AbstractOrderAndInvoiceProcessor implements InitializingBean {

    public abstract String handleType();


    /**
     * 列出开票信息
     *
     * @return
     */
    public abstract OrderAndInvoiceSummary listInvoiceSummary(Date startDate, Date endDate);

    /**
     * 列出订单信息
     *
     * @return
     */
    public abstract OrderAndInvoiceSummary listOrderSummary(Date startDate, Date endDate);

    public void afterPropertiesSet() throws Exception {
        OrderAndInvoiceProcessorFactory.registerOrderAndInvoiceProcessor(this);
    }


}
