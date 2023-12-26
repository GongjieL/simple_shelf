package com.zhongji.simpleshelf.common.bo.bi.orderandinvoice;

public class OrderAndInvoiceBo {
    /**
     * 类型
     */
    private String productType;

    /**
     * 订单数
     */
    private Integer orderNum=0;

    /**
     * 开票数
     */
    private Integer invoiceNum=0;


    public String getProductType() {
        return productType;
    }

    public void setProductType(String productType) {
        this.productType = productType;
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
