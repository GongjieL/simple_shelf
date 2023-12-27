package com.zhongji.simpleshelf.common.enums;


/**
 * 统计口径
 */
public enum TimeEnum {


    DAY("DAY", "日","昨日","当天"),
    WEEK("WEEK", "周","上周","本周"),
    MONTH("MONTH", "月","上月","当月"),
    QUARTER("QUARTER", "季度","上季度","本季度"),
    YEAR("YEAR", "年","去年","本年度"),


    ;

    TimeEnum(String code, String name, String preName, String nowName) {
        this.code = code;
        this.name = name;
        this.preName = preName;
        this.nowName = nowName;
    }

    private String code;

    private String name;

    private String preName;


    private String nowName;


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

    public String getPreName() {
        return preName;
    }

    public String getNowName() {
        return nowName;
    }
}
