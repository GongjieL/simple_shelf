package com.zhongji.simpleshelf.common.bo.bi.orderandinvoice;

import java.util.List;

public class BDOrderAndInvoiceSummary {
    /**
     * bd名
     */
    private String businessDepartmentType;


    /**
     * bd名
     */
    private String businessDepartmentName;

    /**
     * 统计信息
     */
    private List<BrandOrderAndInvoiceSummary> brandOrderAndInvoiceSummaries;

    /**
     * 总计
     */

    private List<OrderAndInvoiceSummary> summaryCalibers;


    public String getBusinessDepartmentType() {
        return businessDepartmentType;
    }

    public void setBusinessDepartmentType(String businessDepartmentType) {
        this.businessDepartmentType = businessDepartmentType;
    }

    public List<BrandOrderAndInvoiceSummary> getBrandOrderAndInvoiceSummaries() {
        return brandOrderAndInvoiceSummaries;
    }

    public void setBrandOrderAndInvoiceSummaries(List<BrandOrderAndInvoiceSummary> brandOrderAndInvoiceSummaries) {
        this.brandOrderAndInvoiceSummaries = brandOrderAndInvoiceSummaries;
    }

    public List<OrderAndInvoiceSummary> getSummaryCalibers() {
        return summaryCalibers;
    }

    public void setSummaryCalibers(List<OrderAndInvoiceSummary> summaryCalibers) {
        this.summaryCalibers = summaryCalibers;
    }

    public String getBusinessDepartmentName() {
        return businessDepartmentName;
    }

    public void setBusinessDepartmentName(String businessDepartmentName) {
        this.businessDepartmentName = businessDepartmentName;
    }
}
