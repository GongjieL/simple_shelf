package com.zhongji.simpleshelf.common.enums;

public enum BrandEnum {

    TONG_HUA("TONG_HUA","通华牌")
    ;
    private String code;

    private String name;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    BrandEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }


    public static BrandEnum getByCode(String subCode) {
        for (BrandEnum value : values()) {
            if (value.getClass().equals(subCode)) {
                return value;
            }
        }
        return null;
    }
}
