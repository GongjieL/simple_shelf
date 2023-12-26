package com.zhongji.simpleshelf.core.service.impl;

import com.zhongji.simpleshelf.common.enums.BrandEnum;
import com.zhongji.simpleshelf.common.enums.BusinessDeptEnum;
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
import org.springframework.util.CollectionUtils;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
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
        List<BrandOrderAndInvoiceSummary> brandOrderAndInvoiceSummaries = new ArrayList<>();
        //品牌
        for (BrandEnum brandEnum : BrandEnum.values()) {
            //获取战略部、品牌
            BrandOrderAndInvoiceSummary brandOrderAndInvoiceSummary = new BrandOrderAndInvoiceSummary();
            brandOrderAndInvoiceSummary.setBrand(brandEnum.getCode());
            brandOrderAndInvoiceSummary.setBrandName(brandEnum.getName());
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
                //时间统计
                OrderAndInvoiceSummary orderAndInvoiceSummary = buildOrderAndInvoiceSummary(brandEnum.getCode(), startDate, endDate);
                if (orderAndInvoiceSummary == null) {
                    continue;
                }
                orderAndInvoiceSummary.setSummaryCaliber(statisticsCaliber);
                brandOrderAndInvoiceSummary.getOrderAndInvoices().add(orderAndInvoiceSummary);
            }
            if (!CollectionUtils.isEmpty(brandOrderAndInvoiceSummary.getOrderAndInvoices())) {
                brandOrderAndInvoiceSummaries.add(brandOrderAndInvoiceSummary);
            }
        }
        //部门统计
        Map<String, List<BrandOrderAndInvoiceSummary>> bdOrderAndInvoiceSummaryData = new HashMap<>();
        Map<String, List<OrderAndInvoiceSummary>> deptOrderAndInvoiceSummaryData = new HashMap<>();
        for (BrandOrderAndInvoiceSummary brandOrderAndInvoiceSummary : brandOrderAndInvoiceSummaries) {
            BrandEnum brandEnum = BrandEnum.getByCode(brandOrderAndInvoiceSummary.getBrand());
            //获取bd
            String deptCode = brandEnum.getDept().getCode();
            if (bdOrderAndInvoiceSummaryData.get(deptCode) == null) {
                bdOrderAndInvoiceSummaryData.put(deptCode, new ArrayList<>());
                deptOrderAndInvoiceSummaryData.put(deptCode, new ArrayList<>());
            }
            bdOrderAndInvoiceSummaryData.get(deptCode).add(brandOrderAndInvoiceSummary);
            deptOrderAndInvoiceSummaryData.get(deptCode).addAll(brandOrderAndInvoiceSummary.getOrderAndInvoices());
        }
        //构造部门统计
        List<BDOrderAndInvoiceSummary> bdOrderAndInvoiceSummaries = new ArrayList<>();
        bdOrderAndInvoiceSummaryData.forEach((dept, value) -> {
            BDOrderAndInvoiceSummary bdOrderAndInvoiceSummary = new BDOrderAndInvoiceSummary();
            bdOrderAndInvoiceSummary.setBusinessDepartmentType(BusinessDeptEnum.getByCode(dept).getCode());
            bdOrderAndInvoiceSummary.setBusinessDepartmentName(BusinessDeptEnum.getByCode(dept).getName());
            bdOrderAndInvoiceSummary.setBrandOrderAndInvoiceSummaries(value);
            //设置总计数
            //统计类型分类
            Map<String, OrderAndInvoiceSummary> summaryCalibers = new HashMap<>();
            for (OrderAndInvoiceSummary orderAndInvoiceSummary : deptOrderAndInvoiceSummaryData.get(dept)) {
                OrderAndInvoiceSummary newOrderAndInvoiceSummary = summaryCalibers.get(orderAndInvoiceSummary.getSummaryCaliber());
                if (newOrderAndInvoiceSummary == null) {//不存在
                    newOrderAndInvoiceSummary = new OrderAndInvoiceSummary();
                    newOrderAndInvoiceSummary.setOrderNum(orderAndInvoiceSummary.getOrderNum());
                    newOrderAndInvoiceSummary.setInvoiceNum(orderAndInvoiceSummary.getInvoiceNum());
                } else {
                    newOrderAndInvoiceSummary.setOrderNum(
                            newOrderAndInvoiceSummary.getOrderNum() + orderAndInvoiceSummary.getOrderNum());
                    newOrderAndInvoiceSummary.setInvoiceNum(
                            newOrderAndInvoiceSummary.getInvoiceNum() + orderAndInvoiceSummary.getInvoiceNum());

                }
                newOrderAndInvoiceSummary.setSummaryCaliber(orderAndInvoiceSummary.getSummaryCaliber());
                summaryCalibers.put(orderAndInvoiceSummary.getSummaryCaliber(), newOrderAndInvoiceSummary);
            }
            bdOrderAndInvoiceSummary.setSummaryCalibers(new ArrayList<>(summaryCalibers.values()));
            bdOrderAndInvoiceSummaries.add(bdOrderAndInvoiceSummary);
        });
        //时间查询
        return bdOrderAndInvoiceSummaries;
    }


    private OrderAndInvoiceSummary buildOrderAndInvoiceSummary(String brand, Date startDate, Date endDate) {
        AbstractOrderAndInvoiceProcessor orderAndInvoiceProcessor = OrderAndInvoiceProcessorFactory.getOrderAndInvoiceProcessor(brand);
        if (orderAndInvoiceProcessor == null) {
            return null;
        }
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
