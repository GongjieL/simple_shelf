package com.zhongji.simpleshelf.common.enums;

public enum BusinessDeptEnum {
    LT("LT", "LT战略事业部", 1),
    CE("CE", "CE战略事业部", 2),

    ;
    private String code;
    private String name;

    private Integer order;

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public Integer getOrder() {
        return order;
    }

    BusinessDeptEnum(String code, String name, Integer order) {
        this.code = code;
        this.name = name;
        this.order = order;
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
