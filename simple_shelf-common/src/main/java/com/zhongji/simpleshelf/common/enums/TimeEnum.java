package com.zhongji.simpleshelf.common.enums;


/**
 * 统计口径
 */
public enum TimeEnum {


    DAY("DAY", "当日"),
    WEEK("WEEK", "本周"),
    MONTH("MONTH", "月"),
    QUARTER("QUARTER", "季度"),
    YEAR("YEAR", "年"),


    ;

    TimeEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    private String code;

    private String name;


    public static TimeEnum getByCode(String subCode) {
        for (TimeEnum value : values()) {
            if (value.getCode().equals(subCode)) {
                return value;
            }
        }
        return null;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }


}
