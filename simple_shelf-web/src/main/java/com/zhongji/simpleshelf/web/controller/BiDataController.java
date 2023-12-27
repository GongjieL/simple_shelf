package com.zhongji.simpleshelf.web.controller;

import com.zhongji.simpleshelf.common.bo.bi.orderandinvoice.BDOrderAndInvoiceSummary;
import com.zhongji.simpleshelf.core.service.impl.BiStatisticsSummaryServiceImpl;
import com.zhongji.simpleshelf.web.request.BDOrderAndInvoiceSummaryRequest;
import com.zhongji.simpleshelf.web.response.BaseWebResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/bi")
public class BiDataController {
    @Autowired
    private BiStatisticsSummaryServiceImpl biStatisticsSummaryService;

    @PostMapping("/buildBDOrderAndInvoiceSummary")
    public BaseWebResponse<List<BDOrderAndInvoiceSummary>> buildBDOrderAndInvoiceSummary(@RequestBody(required = false) BDOrderAndInvoiceSummaryRequest bdOrderAndInvoiceSummaryRequest) {
        List<BDOrderAndInvoiceSummary> bdOrderAndInvoiceSummaries = biStatisticsSummaryService.buildBDOrderAndInvoiceSummary(bdOrderAndInvoiceSummaryRequest.getReferToDate(), bdOrderAndInvoiceSummaryRequest.getSummaryCalibers());
        return BaseWebResponse.<List<BDOrderAndInvoiceSummary>>builder()
                .success(true)
                .data(bdOrderAndInvoiceSummaries)
                .code(200)
                .build();
    }

}
