package com.zhongji.simpleshelf.core.service;

import com.zhongji.simpleshelf.common.bo.dingtalk.ZhongjiDepartment;

import java.io.IOException;
import java.util.List;

public interface DingTalkService {



    /**
     * 批量更新
     * //钉钉更新部门结构，自动迁移成员和子部门
     * //获取钉钉部门(树)
     *
     * @throws IOException
     */
    abstract void batchUpdateDepartments() throws IOException;

    /**
     * 增加用户
     * @param zhongjiDepartment
     * @return
     */
    public abstract String addUsers(ZhongjiDepartment zhongjiDepartment);

}

