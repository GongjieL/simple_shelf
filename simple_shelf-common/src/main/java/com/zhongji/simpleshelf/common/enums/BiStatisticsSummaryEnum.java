package com.zhongji.simpleshelf.common.enums;

/**
 * 统计类型
 */
public enum BiStatisticsSummaryEnum {
    TY("TY","液罐"),
    JY("JY","液罐2"),
    TS("TS","液罐3"),
    YY("YY","液罐4"),
    OTHER("OTHER","其他"),
    ;

    BiStatisticsSummaryEnum(String subType, String name) {
        this.subType = subType;
        this.name = name;
    }

    public String getSubType() {
        return subType;
    }

    public void setSubType(String subType) {
        this.subType = subType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    private String subType;

    private String name;


    public static BiStatisticsSummaryEnum getByCode(String subCode){
        for (BiStatisticsSummaryEnum value : values()) {
            if(value.getSubType().equals(subCode)){
                return value;
            }
        }
        return OTHER;
    }

}
