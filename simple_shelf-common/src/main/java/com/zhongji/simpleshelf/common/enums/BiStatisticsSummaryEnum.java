package com.zhongji.simpleshelf.common.enums;

/**
 * 统计类型
 */
public enum BiStatisticsSummaryEnum {
    TY("TY", "碳钢液罐", 3),
    JY("JY", "铝合金液罐", 2),
    UY("UY", "特罐", 4),
    YY("YY", "不锈钢液罐", 1),
    OTHER("OTHER", "其他", 999),
    ;

    BiStatisticsSummaryEnum(String subType, String name, Integer order) {
        this.subType = subType;
        this.name = name;
        this.order = order;
    }

    public String getSubType() {
        return subType;
    }


    public String getName() {
        return name;
    }

    public Integer getOrder() {
        return order;
    }

    private String subType;

    private String name;

    private Integer order;


    public static BiStatisticsSummaryEnum getByCode(String subCode) {
        for (BiStatisticsSummaryEnum value : values()) {
            if (value.getSubType().equals(subCode)) {
                return value;
            }
        }
        return OTHER;
    }

}
