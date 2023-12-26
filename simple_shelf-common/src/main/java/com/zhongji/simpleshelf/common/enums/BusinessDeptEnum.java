package com.zhongji.simpleshelf.common.enums;

public enum BusinessDeptEnum {
    LT("LT", "LT战略事业部"),
    CE("CE", "CE战略事业部"),

    ;
    private String code;
    private String name;

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    BusinessDeptEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public static BusinessDeptEnum getByCode(String subCode) {
        for (BusinessDeptEnum value : values()) {
            if (value.getCode().equals(subCode)) {
                return value;
            }
        }
        return null;
    }

}
