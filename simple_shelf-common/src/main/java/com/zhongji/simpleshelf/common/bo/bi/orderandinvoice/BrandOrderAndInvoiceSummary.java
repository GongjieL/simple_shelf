package com.zhongji.simpleshelf.common.bo.bi.orderandinvoice;

import java.util.List;

public class BrandOrderAndInvoiceSummary {
    /**
     * 品牌名
     */
    private String brand;

    /**
     * 品牌名
     */
    private String brandName;

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

    public String getBrandName() {
        return brandName;
    }

    public void setBrandName(String brandName) {
        this.brandName = brandName;
    }

    public List<OrderAndInvoiceSummary> getOrderAndInvoices() {
        return orderAndInvoices;
    }

    public void setOrderAndInvoices(List<OrderAndInvoiceSummary> orderAndInvoices) {
        this.orderAndInvoices = orderAndInvoices;
    }

}
