package com.zhongji.simpleshelf.common.bo.bi.orderandinvoice;

import java.util.List;

public class BDOrderAndInvoiceSummary {
    /**
     * bd名
     */
    private String businessDepartmentType;

    /**
     * 统计信息
     */
    private List<BrandOrderAndInvoiceSummary> brandOrderAndInvoiceSummaries;

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
}
