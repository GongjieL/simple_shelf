package com.zhongji.simpleshelf.core.service;

import com.zhongji.simpleshelf.common.bo.bi.orderandinvoice.BDOrderAndInvoiceSummary;

import java.util.List;

public interface BiStatisticsSummaryService {

    /**
     * 列出订单和开票报表
     * @param type               当前/上一个
     * @param statisticsCalibers 统计口径
     * @return
     */
    public List<BDOrderAndInvoiceSummary> buildBDOrderAndInvoiceSummary(String type,
                                                                        List<String> statisticsCalibers);

}
