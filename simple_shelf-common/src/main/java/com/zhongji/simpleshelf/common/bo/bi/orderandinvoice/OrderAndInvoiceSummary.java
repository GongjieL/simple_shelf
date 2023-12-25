package com.zhongji.simpleshelf.common.bo.bi.orderandinvoice;

import java.util.List;

public class OrderAndInvoiceSummary {

    /**
     * 统计口径
     */
    private String summaryCaliber;


    /**
     * 统计口径list(11月，年度)
     */
    private List<OrderAndInvoiceBo> orderAndInvoices;


    /**
     * 订单总数
     */
    private Integer orderNum;

    /**
     * 开票总数
     */
    private Integer invoiceNum;


    public String getSummaryCaliber() {
        return summaryCaliber;
    }

    public void setSummaryCaliber(String summaryCaliber) {
        this.summaryCaliber = summaryCaliber;
    }

    public List<OrderAndInvoiceBo> getOrderAndInvoices() {
        return orderAndInvoices;
    }

    public void setOrderAndInvoices(List<OrderAndInvoiceBo> orderAndInvoices) {
        this.orderAndInvoices = orderAndInvoices;
    }

    public Integer getOrderNum() {
        return orderNum;
    }

    public void setOrderNum(Integer orderNum) {
        this.orderNum = orderNum;
    }

    public Integer getInvoiceNum() {
        return invoiceNum;
    }

    public void setInvoiceNum(Integer invoiceNum) {
        this.invoiceNum = invoiceNum;
    }
}
