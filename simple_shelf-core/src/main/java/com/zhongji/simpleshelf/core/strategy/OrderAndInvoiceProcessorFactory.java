package com.zhongji.simpleshelf.core.strategy;

import java.util.HashMap;
import java.util.Map;

public class OrderAndInvoiceProcessorFactory {
    private static Map<String, AbstractOrderAndInvoiceProcessor> data = new HashMap<>();

    public static void registerOrderAndInvoiceProcessor(AbstractOrderAndInvoiceProcessor orderAndInvoiceProcessor) {
        data.put(orderAndInvoiceProcessor.handleType(), orderAndInvoiceProcessor);
    }

    public static AbstractOrderAndInvoiceProcessor getOrderAndInvoiceProcessor(String code) {
        return data.get(code);
    }

}
