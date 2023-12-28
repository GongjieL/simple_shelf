package com.zhongji.simpleshelf.common.bo.dingtalk;

import java.util.List;

public class ZhongjiDepartment {
    /**
     * 内部部门id
     */
    private String deptId;

    /**
     * 内部部门父id
     */
    private String parentDeptId;

    /**
     * 外部部门id
     */
    private String outerDeptId;

    /**
     * 外部部门父id
     */
    private String outerParentDeptId;

    /**
     * 部门名称
     */
    private String deptName;

    /**
     * 顺序号
     */
    private String index;

    /**
     * 部门简介
     */
    private String deptDesc;


    /**
     * 子部门
     */
    private List<ZhongjiDepartment> children;




    public String getDeptId() {
        return deptId;
    }

    public void setDeptId(String deptId) {
        this.deptId = deptId;
    }

    public String getParentDeptId() {
        return parentDeptId;
    }

    public void setParentDeptId(String parentDeptId) {
        this.parentDeptId = parentDeptId;
    }

    public String getOuterDeptId() {
        return outerDeptId;
    }

    public void setOuterDeptId(String outerDeptId) {
        this.outerDeptId = outerDeptId;
    }

    public String getOuterParentDeptId() {
        return outerParentDeptId;
    }

    public void setOuterParentDeptId(String outerParentDeptId) {
        this.outerParentDeptId = outerParentDeptId;
    }

    public String getDeptName() {
        return deptName;
    }

    public void setDeptName(String deptName) {
        this.deptName = deptName;
    }

    public String getIndex() {
        return index;
    }

    public void setIndex(String index) {
        this.index = index;
    }

    public String getDeptDesc() {
        return deptDesc;
    }

    public void setDeptDesc(String deptDesc) {
        this.deptDesc = deptDesc;
    }

    public List<ZhongjiDepartment> getChildren() {
        return children;
    }

    public void setChildren(List<ZhongjiDepartment> children) {
        this.children = children;
    }
}
