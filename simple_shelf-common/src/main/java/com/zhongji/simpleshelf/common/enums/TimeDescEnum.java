package com.zhongji.simpleshelf.common.enums;


/**
 * 时间
 */
public enum TimeDescEnum {
    PRE("PRE", "上"),
    NOW("NOW", "本"),
    NEXT("NEXT", "下"),


    ;
    private String code;

    private String name;


    TimeDescEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public static TimeDescEnum getByCode(String subCode) {
        for (TimeDescEnum value : values()) {
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
