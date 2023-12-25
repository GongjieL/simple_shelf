package com.zhongji.simpleshelf.common.bo.bi.orderandinvoice;

import java.util.List;

public class BrandOrderAndInvoiceSummary {
    /**
     * 品牌名
     */
    private String brand;

    /**
     * 统计信息
     */
    private List<OrderAndInvoiceSummary> orderAndInvoices;





    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public List<OrderAndInvoiceSummary> getOrderAndInvoices() {
        return orderAndInvoices;
    }

    public void setOrderAndInvoices(List<OrderAndInvoiceSummary> orderAndInvoices) {
        this.orderAndInvoices = orderAndInvoices;
    }

}
