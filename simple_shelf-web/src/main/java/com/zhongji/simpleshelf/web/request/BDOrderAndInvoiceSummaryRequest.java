package com.zhongji.simpleshelf.web.request;


import com.zhongji.simpleshelf.common.bo.bi.SummaryCaliber;

import java.util.Date;
import java.util.List;

public class BDOrderAndInvoiceSummaryRequest {
    private Date referToDate;

    List<SummaryCaliber> summaryCalibers;

    public Date getReferToDate() {
        return referToDate;
    }

    public void setReferToDate(Date referToDate) {
        this.referToDate = referToDate;
    }

    public List<SummaryCaliber> getSummaryCalibers() {
        return summaryCalibers;
    }

    public void setSummaryCalibers(List<SummaryCaliber> summaryCalibers) {
        this.summaryCalibers = summaryCalibers;
    }

}
