package com.zhongji.simpleshelf.core.service;

import com.zhongji.simpleshelf.common.bo.dingtalk.ZhongjiDepartment;

import java.util.List;

public interface DingTalkService {

    /**
     * 创建多部门
     * @return
     */
    public abstract List<String> batchCreateDepartments();


    /**
     * 增加用户
     * @param zhongjiDepartment
     * @return
     */
    public abstract String addUsers(ZhongjiDepartment zhongjiDepartment);

}

