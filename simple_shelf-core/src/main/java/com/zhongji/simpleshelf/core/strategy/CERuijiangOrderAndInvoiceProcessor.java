package com.zhongji.simpleshelf.core.strategy;

import com.zhongji.simpleshelf.common.bo.bi.orderandinvoice.OrderAndInvoiceBo;
import com.zhongji.simpleshelf.common.bo.bi.orderandinvoice.OrderAndInvoiceSummary;
import com.zhongji.simpleshelf.common.enums.BrandEnum;
import com.zhongji.simpleshelf.dao.domain.StatisticsSummary;
import com.zhongji.simpleshelf.dao.service.impl.CwflNewServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class CERuijiangOrderAndInvoiceProcessor extends AbstractOrderAndInvoiceProcessor {

    @Autowired
    private CwflNewServiceImpl cwflNewService;

    @Override
    public String handleType() {
        return BrandEnum.CE_RUI_JIANG.getCode();
    }


    /**
     * 返回某个口径的开票数
     *
     * @param startDate
     * @param endDate
     * @return
     */
    @Override
    public OrderAndInvoiceSummary listInvoiceSummary(Date startDate, Date endDate) {
        //查询开票数
        List<StatisticsSummary> statisticsSummaries = cwflNewService.listErpSidSummary(startDate, endDate);
        //计算总数
        if (CollectionUtils.isEmpty(statisticsSummaries)) {
            return null;
        }
        Integer invoiceNum = 0;
        OrderAndInvoiceSummary orderAndInvoiceSummary = new OrderAndInvoiceSummary();
        orderAndInvoiceSummary.setOrderAndInvoices(new ArrayList<>());
        for (StatisticsSummary statisticsSummary : statisticsSummaries) {
            //todo 注意报错
            Integer selfInvoiceNum = Integer.valueOf(statisticsSummary.getNum());
            invoiceNum += selfInvoiceNum;
            OrderAndInvoiceBo orderAndInvoiceBo = new OrderAndInvoiceBo();
            orderAndInvoiceBo.setProductType(statisticsSummary.getSubType());
            orderAndInvoiceBo.setInvoiceNum(selfInvoiceNum);
            orderAndInvoiceSummary.getOrderAndInvoices().add(orderAndInvoiceBo);
        }
        orderAndInvoiceSummary.setInvoiceNum(invoiceNum);
        return orderAndInvoiceSummary;
    }

    @Override
    public OrderAndInvoiceSummary listOrderSummary(Date startDate, Date endDate) {
        return null;
    }

}
