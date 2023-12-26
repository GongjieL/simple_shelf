package com.zhongji.simpleshelf.common.enums;

public enum BrandEnum {
    LT_TONG_HUA("TONG_HUA", "LT_TONG_HUA", "通华牌", BusinessDeptEnum.LT),
    LT_RUI_JIANG("RUI_JIANG", "LT_RUI_JIANG", "瑞江牌", BusinessDeptEnum.LT),
    LT_WAN_SHI_DA("WAN_SHI_DA", "LT_WAN_SHI_DA", "万事达牌", BusinessDeptEnum.LT),
    LT_LING_YU("LING_YU", "LT_LING_YU", "凌宇牌", BusinessDeptEnum.LT),


    CE_TONG_HUA("TONG_HUA", "CE_TONG_HUA", "通华牌", BusinessDeptEnum.CE),
    CE_RUI_JIANG("RUI_JIANG", "CE_RUI_JIANG", "瑞江牌", BusinessDeptEnum.CE),
    CE_WAN_SHI_DA("WAN_SHI_DA", "CE_WAN_SHI_DA", "万事达牌", BusinessDeptEnum.CE),
    CE_LING_YU("LING_YU", "CE_LING_YU", "凌宇牌", BusinessDeptEnum.CE),
    ;


    BrandEnum(String mainCode, String code, String name, BusinessDeptEnum dept) {
        this.mainCode = mainCode;
        this.code = code;
        this.name = name;
        this.dept = dept;
    }

    private String mainCode;
    private String code;
    private String name;
    private BusinessDeptEnum dept;

    public String getMainCode() {
        return mainCode;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public BusinessDeptEnum getDept() {
        return dept;
    }

    public static BrandEnum getByCode(String subCode) {
        for (BrandEnum value : values()) {
            if (value.getCode().equals(subCode)) {
                return value;
            }
        }
        return null;
    }


    public static BrandEnum getByMainCodeAndDept(String mainCode, String dept) {
        BusinessDeptEnum businessDept = BusinessDeptEnum.getByCode(dept);
        for (BrandEnum value : values()) {
            if (value.getMainCode().equals(mainCode) && value.getDept().equals(businessDept)) {
                return value;
            }
        }
        return null;
    }

}
