package com.zhongji.simpleshelf.common.enums;


/**
 * 时间
 */
public enum TimeDescEnum {
    PRE("PRE", "前"),
    NOW("NOW", "当前"),
    NEXT("NEXT", "下一个"),


    ;
    private String code;

    private String name;


    TimeDescEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public static TimeDescEnum getByCode(String subCode) {
        for (TimeDescEnum value : values()) {
            if (value.getClass().equals(subCode)) {
                return value;
            }
        }
        return null;
    }


}
