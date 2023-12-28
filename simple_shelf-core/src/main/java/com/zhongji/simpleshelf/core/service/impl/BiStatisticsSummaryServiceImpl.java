package com.zhongji.simpleshelf.core.service.impl;

import com.alibaba.fastjson.JSON;
import com.zhongji.simpleshelf.common.bo.bi.SummaryCaliber;
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
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.Instant;
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
        //整理品牌下统计
        arrangeBrandOrderAndInvoiceSummaries(brandOrderAndInvoiceSummaries);
        //部门总计
        return buildBDTotalOrderAndInvoiceSummary(brandOrderAndInvoiceSummaries);
        //时间查询
    }


    @Override
    public List<BDOrderAndInvoiceSummary> buildBDOrderAndInvoiceSummary(Date referTo, List<SummaryCaliber> summaryCalibers) {
        //获取时间
        LocalDate now = LocalDate.now();
        if (referTo != null) {
            Instant instant = referTo.toInstant();
            now = instant.atZone(ZoneId.systemDefault()).toLocalDate();
        }
        //间隔时间
        List<BrandOrderAndInvoiceSummary> brandOrderAndInvoiceSummaries = new ArrayList<>();
        //品牌
        //todo 考虑直接从数据库获取
        for (BrandEnum brandEnum : BrandEnum.values()) {
            //获取战略部、品牌
            BrandOrderAndInvoiceSummary brandOrderAndInvoiceSummary = new BrandOrderAndInvoiceSummary();
            brandOrderAndInvoiceSummary.setBrand(brandEnum.getCode());
            brandOrderAndInvoiceSummary.setBrandName(brandEnum.getName());
            brandOrderAndInvoiceSummary.setOrderAndInvoices(new ArrayList<>());
            for (SummaryCaliber statisticsCaliber : summaryCalibers) {
                TimeEnum timeEnum = TimeEnum.getByCode(statisticsCaliber.getTimeUnit());
                if (timeEnum == null) {
                    continue;
                }
                int interval = 0;
                if (TimeDescEnum.PRE.equals(TimeDescEnum.getByCode(statisticsCaliber.getTimeDesc()))) {
                    interval = -1;
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
                //todo 特殊符号维护
                statisticsCaliber.setTimeDetail(buildTimeDetail(startDate, endDate, timeEnum, TimeDescEnum.getByCode(statisticsCaliber.getTimeDesc())));
                //新的统计口径
                orderAndInvoiceSummary.setSummaryCaliberDetail(statisticsCaliber);
                orderAndInvoiceSummary.setSummaryCaliber(StringUtils.join(statisticsCaliber.getTimeDesc(), "_", statisticsCaliber.getTimeUnit()));
                brandOrderAndInvoiceSummary.getOrderAndInvoices().add(orderAndInvoiceSummary);
            }
            if (!CollectionUtils.isEmpty(brandOrderAndInvoiceSummary.getOrderAndInvoices())) {
                brandOrderAndInvoiceSummaries.add(brandOrderAndInvoiceSummary);
            }
        }
        //整理品牌下统计
        arrangeBrandOrderAndInvoiceSummaries(brandOrderAndInvoiceSummaries);
        //部门总计
        return buildBDTotalOrderAndInvoiceSummary(brandOrderAndInvoiceSummaries);
    }


    /**
     * 时间详情
     * @param startDate
     * @param endDate
     * @param timeEnum
     * @param timeDescEnum
     * @return
     */

    private String buildTimeDetail(Date startDate, Date endDate, TimeEnum timeEnum, TimeDescEnum timeDescEnum) {
        String preDetail = StringUtils.join(timeEnum.getPreName(), "(", "%s", ")");
        if (TimeDescEnum.NOW.equals(timeDescEnum)) {
            preDetail = StringUtils.join(timeEnum.getNowName(), "(", "%s", ")");
        }
        //时间拼接
        if (TimeEnum.YEAR.equals(timeEnum)) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(startDate);
            return String.format(preDetail, calendar.get(Calendar.YEAR));
        } else if (TimeEnum.DAY.equals(timeEnum)) {
            return String.format(preDetail, DateUtils.format(startDate));
        } else {
            Instant instant = endDate.toInstant();
            LocalDate localDate = instant.atZone(ZoneId.systemDefault()).toLocalDate();
            return String.format(preDetail, StringUtils.join(DateUtils.format(startDate),
                    "~", DateUtils.format(localDate.plusDays(-1))));
        }
    }


    /**
     * 构建总体统计
     *
     * @param brandOrderAndInvoiceSummaries
     * @return
     */
    private List<BDOrderAndInvoiceSummary> buildBDTotalOrderAndInvoiceSummary(List<BrandOrderAndInvoiceSummary> brandOrderAndInvoiceSummaries) {
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

                newOrderAndInvoiceSummary.setSummaryCaliberDetail(orderAndInvoiceSummary.getSummaryCaliberDetail());

                summaryCalibers.put(orderAndInvoiceSummary.getSummaryCaliber(), newOrderAndInvoiceSummary);
            }
            bdOrderAndInvoiceSummary.setSummaryCalibers(new ArrayList<>(summaryCalibers.values()));
            bdOrderAndInvoiceSummaries.add(bdOrderAndInvoiceSummary);
        });
        return bdOrderAndInvoiceSummaries;
    }

    /**
     * 整理品牌统计
     *
     * @param brandOrderAndInvoiceSummaries
     */
    private void arrangeBrandOrderAndInvoiceSummaries(List<BrandOrderAndInvoiceSummary> brandOrderAndInvoiceSummaries) {
        //并集补充
        for (BrandOrderAndInvoiceSummary brandOrderAndInvoiceSummary : brandOrderAndInvoiceSummaries) {
            //某品牌下的口径统计集合(月、周、年等)
            List<OrderAndInvoiceSummary> orderAndInvoices = brandOrderAndInvoiceSummary.getOrderAndInvoices();
            List<List<String>> allSubTypesList = new ArrayList<>();
            for (OrderAndInvoiceSummary orderAndInvoice : orderAndInvoices) {
                //所有子类型
                List<OrderAndInvoiceBo> subTypeOrderAndInvoices = orderAndInvoice.getOrderAndInvoices();
                allSubTypesList.add(subTypeOrderAndInvoices.stream().map(OrderAndInvoiceBo::getProductType)
                        .collect(Collectors.toList()));

            }
            List<String> allSubTypes = allSubTypesList.stream()
                    .flatMap(List::stream)
                    .distinct()
                    .collect(Collectors.toList());
            //没有的补充
            for (OrderAndInvoiceSummary orderAndInvoice : orderAndInvoices) {
                //所有子类型
                List<OrderAndInvoiceBo> subTypeOrderAndInvoices = orderAndInvoice.getOrderAndInvoices();
                List<String> selfSubTypes = subTypeOrderAndInvoices.stream().map(OrderAndInvoiceBo::getProductType)
                        .collect(Collectors.toList());
                //不在当前的
                List<String> missingInList = allSubTypes.stream()
                        .filter(element -> !selfSubTypes.contains(element))
                        .collect(Collectors.toList());
                if (CollectionUtils.isEmpty(missingInList)) {
                    continue;
                }
                for (String type : missingInList) {
                    OrderAndInvoiceBo orderAndInvoiceBo = new OrderAndInvoiceBo();
                    orderAndInvoiceBo.setProductType(type);
                    orderAndInvoice.getOrderAndInvoices().add(orderAndInvoiceBo);
                }
            }
        }
    }


    private OrderAndInvoiceSummary buildOrderAndInvoiceSummary(String brand, Date startDate, Date endDate) {
//        return JSON.parseObject("{\n" +
//                "              \"summaryCaliber\": \"MONTH\",\n" +
//                "              \"orderAndInvoices\": [\n" +
//                "                {\n" +
//                "                  \"productType\": \"JY\",\n" +
//                "                  \"orderNum\": 0,\n" +
//                "                  \"invoiceNum\": 16\n" +
//                "                },\n" +
//                "                {\n" +
//                "                  \"productType\": \"TY\",\n" +
//                "                  \"orderNum\": 0,\n" +
//                "                  \"invoiceNum\": 75\n" +
//                "                },\n" +
//                "                {\n" +
//                "                  \"productType\": \"YY\",\n" +
//                "                  \"orderNum\": 0,\n" +
//                "                  \"invoiceNum\": 82\n" +
//                "                },\n" +
//                "                {\n" +
//                "                  \"productType\": \"TS\",\n" +
//                "                  \"orderNum\": 0,\n" +
//                "                  \"invoiceNum\": 7\n" +
//                "                }\n" +
//                "              ],\n" +
//                "              \"orderNum\": 0,\n" +
//                "              \"invoiceNum\": 180\n" +
//                "            }", OrderAndInvoiceSummary.class);
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
        Map<String, OrderAndInvoiceBo> unionData = Stream.concat(invoiceSummary.getOrderAndInvoices().stream(), orderSummary.getOrderAndInvoices().stream()).
                collect(Collectors.toMap(OrderAndInvoiceBo::getProductType, person -> person,
                (existing, replacement) -> {// 在合并时填充 null 值
                    existing.setOrderNum(replacement.getOrderNum());
                    return existing;
                }));
        //开票列表
        orderSummary.setOrderAndInvoices(new ArrayList<>(unionData.values()));
        return orderSummary;
    }

    public static void main(String[] args) {
        BiStatisticsSummaryServiceImpl biStatisticsSummaryService = new BiStatisticsSummaryServiceImpl();
        LocalDate localDate = DateUtils.getIntervalTime(LocalDate.now(), TimeEnum.MONTH, 2);
        Date endDate = Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());

        String s1 = biStatisticsSummaryService.buildTimeDetail(new Date(), endDate, TimeEnum.MONTH, TimeDescEnum.PRE);
        String s2 = biStatisticsSummaryService.buildTimeDetail(new Date(), endDate, TimeEnum.YEAR, TimeDescEnum.PRE);
        String s3 = biStatisticsSummaryService.buildTimeDetail(new Date(), endDate, TimeEnum.DAY, TimeDescEnum.PRE);
        System.out.println(s1);
        System.out.println(s2);
        System.out.println(s3);


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
