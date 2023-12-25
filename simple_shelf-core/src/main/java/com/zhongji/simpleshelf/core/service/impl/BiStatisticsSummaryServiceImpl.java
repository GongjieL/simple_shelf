package com.zhongji.simpleshelf.core.service.impl;

import com.zhongji.simpleshelf.core.service.BiStatisticsSummaryService;
import com.zhongji.simpleshelf.core.strategy.AbstractOrderAndInvoiceProcessor;
import com.zhongji.simpleshelf.core.strategy.OrderAndInvoiceProcessorFactory;
import com.zhongji.simpleshelf.common.bo.bi.orderandinvoice.BDOrderAndInvoiceSummary;
import com.zhongji.simpleshelf.common.bo.bi.orderandinvoice.BrandOrderAndInvoiceSummary;
import com.zhongji.simpleshelf.common.bo.bi.orderandinvoice.OrderAndInvoiceBo;
import com.zhongji.simpleshelf.common.bo.bi.orderandinvoice.OrderAndInvoiceSummary;
import com.zhongji.simpleshelf.common.enums.TimeDescEnum;
import com.zhongji.simpleshelf.common.enums.TimeEnum;
import com.zhongji.simpleshelf.util.DateUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class BiStatisticsSummaryServiceImpl implements BiStatisticsSummaryService {


    @Override
    public List<BDOrderAndInvoiceSummary> buildBDOrderAndInvoiceSummary(String type, List<String> statisticsCalibers) {
        //获取时间
        LocalDate now = LocalDate.now();
        int interval = 0;
        if (TimeDescEnum.PRE.equals(TimeDescEnum.getByCode(type))) {
            interval = -1;
        }
        //todo 部门
        List<String> departments = new ArrayList<>();
        departments.add("LT战略事业部");
        departments.add("CE战略事业部");
        //todo mock品牌
        List<String> brands = new ArrayList<>();
        brands.add("TONG_HUA");
        List<BDOrderAndInvoiceSummary> bdOrderAndInvoiceSummaries = new ArrayList<>();
        for (String department : departments) {
            BDOrderAndInvoiceSummary bdOrderAndInvoiceSummary = new BDOrderAndInvoiceSummary();
            bdOrderAndInvoiceSummary.setBusinessDepartmentType(department);
            bdOrderAndInvoiceSummary.setBrandOrderAndInvoiceSummaries(new ArrayList<>());
            for (String brand : brands) {
                BrandOrderAndInvoiceSummary brandOrderAndInvoiceSummary = new BrandOrderAndInvoiceSummary();
                brandOrderAndInvoiceSummary.setBrand(brand);
                brandOrderAndInvoiceSummary.setOrderAndInvoices(new ArrayList<>());
                for (String statisticsCaliber : statisticsCalibers) {
                    TimeEnum timeEnum = TimeEnum.getByCode(statisticsCaliber);
                    if (timeEnum == null) {
                        continue;
                    }
                    //开始时间
                    LocalDate startLocalDate = DateUtils.getIntervalTime(now, timeEnum, interval);
                    //结束时间
                    LocalDate endLocalDate = DateUtils.getIntervalTime(startLocalDate, timeEnum, 1);
                    Date startDate = Date.from(startLocalDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
                    Date endDate = Date.from(endLocalDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
                    //时间模式
                    //品牌
                    OrderAndInvoiceSummary orderAndInvoiceSummary = buildOrderAndInvoiceSummary(brand, startDate, endDate);
                    brandOrderAndInvoiceSummary.getOrderAndInvoices().add(orderAndInvoiceSummary);
                }
                bdOrderAndInvoiceSummary.getBrandOrderAndInvoiceSummaries().add(brandOrderAndInvoiceSummary);
            }
            bdOrderAndInvoiceSummaries.add(bdOrderAndInvoiceSummary);
        }
        //时间查询
        return bdOrderAndInvoiceSummaries;
    }


    private OrderAndInvoiceSummary buildOrderAndInvoiceSummary(String brand, Date startDate, Date endDate) {
        AbstractOrderAndInvoiceProcessor orderAndInvoiceProcessor = OrderAndInvoiceProcessorFactory.getOrderAndInvoiceProcessor(brand);
        //订单、开票

        OrderAndInvoiceSummary invoiceSummary = orderAndInvoiceProcessor.listInvoiceSummary(startDate, endDate);
        OrderAndInvoiceSummary orderSummary = orderAndInvoiceProcessor.listOrderSummary(startDate, endDate);
        //判断
        if (invoiceSummary == null) {
            return orderSummary;
        }
        if (orderSummary == null) {
            return invoiceSummary;
        }
        //订单总数
        invoiceSummary.setOrderNum(orderSummary.getOrderNum());
        //合并
        Map<String, OrderAndInvoiceBo> unionData = Stream.concat(invoiceSummary.getOrderAndInvoices().stream(), orderSummary.getOrderAndInvoices().stream()).collect(Collectors.toMap(OrderAndInvoiceBo::getProductType, person -> person,
                (existing, replacement) -> {// 在合并时填充 null 值
                    existing.setOrderNum(replacement.getOrderNum());
                    return existing;
                }));
        //开票列表
        orderSummary.setOrderAndInvoices(new ArrayList<>(unionData.values()));
        return orderSummary;
    }

    public static void main(String[] args) {
        OrderAndInvoiceBo a = new OrderAndInvoiceBo();
        a.setProductType("a");
        a.setOrderNum(2);
        OrderAndInvoiceBo b = new OrderAndInvoiceBo();
        b.setProductType("a");
        b.setInvoiceNum(1);
        OrderAndInvoiceBo c = new OrderAndInvoiceBo();
        c.setProductType("c");
        c.setInvoiceNum(3);
        List<OrderAndInvoiceBo> list1 = new ArrayList<>();
        List<OrderAndInvoiceBo> list2 = new ArrayList<>();
        list1.add(b);
        list1.add(c);
        list2.add(a);
        Map<String, OrderAndInvoiceBo> collect = Stream.concat(list1.stream(), list2.stream()).collect(Collectors.toMap(OrderAndInvoiceBo::getProductType, person -> person, (existing, replacement) -> {// 在合并时填充 null 值
            existing.setOrderNum(replacement.getOrderNum());
            return existing;
        }));
        System.out.println(1);
    }

}
