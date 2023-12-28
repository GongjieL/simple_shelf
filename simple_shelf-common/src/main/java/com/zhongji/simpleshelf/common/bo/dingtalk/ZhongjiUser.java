package com.zhongji.simpleshelf.common.bo.dingtalk;


public class ZhongjiUser {
    private String mobile;
    private String name;
    /**
     * userID
     */
    private String userId;

    /**
     * 部门id
     */
    private String deptId;


    /**
     * 部门id
     */
    private String outerDeptId;

    /**
     * 职能
     */
    private String positionName;

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDeptId() {
        return deptId;
    }

    public void setDeptId(String deptId) {
        this.deptId = deptId;
    }

    public String getOuterDeptId() {
        return outerDeptId;
    }

    public void setOuterDeptId(String outerDeptId) {
        this.outerDeptId = outerDeptId;
    }

    public String getPositionName() {
        return positionName;
    }

    public void setPositionName(String positionName) {
        this.positionName = positionName;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
