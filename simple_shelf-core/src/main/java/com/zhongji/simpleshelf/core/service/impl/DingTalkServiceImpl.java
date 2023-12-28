package com.zhongji.simpleshelf.core.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.zhongji.simpleshelf.common.bo.dingtalk.ZhongjiDepartment;
import com.zhongji.simpleshelf.core.service.DingTalkService;
import com.zhongji.simpleshelf.externaldata.dingtalk.DingTalkInnerClient;
import com.zhongji.simpleshelf.externaldata.dingtalk.ZhongjiHrServiceClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class DingTalkServiceImpl implements DingTalkService {
    @Autowired
    private ZhongjiHrServiceClient zhongjiHrServiceClient;

    @Autowired
    private DingTalkInnerClient dingTalkInnerClient;


    /**
     * 全量导入部门
     *
     * @return
     */
    @Override
    public List<String> batchCreateDepartments() {
        //获取部门信息，数量级小，不增加数据库，全部获取后处理
        int i = 1;
        JSONArray departments = new JSONArray();
        while (true) {
            JSONArray records = zhongjiHrServiceClient.listDepartments(i);
            if (CollectionUtils.isEmpty(records)) {
                break;
            }
            departments.addAll(records);
            i++;
        }
        // unitId唯一，构造map
        ZhongjiDepartment root = buildZhongjiDepartmentTree(departments);
        //进行钉钉部门创建
        createDingTalkDepartment(root);
        return null;
    }


    /**
     * 构建zhongji部门树
     *
     * @param departments
     * @return
     */
    private ZhongjiDepartment buildZhongjiDepartmentTree(JSONArray departments) {
        Map<String, ZhongjiDepartment> data = new HashMap<>();
        AtomicReference<ZhongjiDepartment> root = new AtomicReference<>();
        departments.forEach(d -> {
            ZhongjiDepartment zhongjiSelfDepartment = createZhongjiSelfDepartment((JSONObject) d);
            if ("10".equals(((JSONObject) d).getString("LEVEL_ID"))) {
                zhongjiSelfDepartment.setOuterParentDeptId("1");
                root.set(zhongjiSelfDepartment);
            }
            data.put(zhongjiSelfDepartment.getDeptId(), zhongjiSelfDepartment);
        });
        //构建树
        data.forEach((k, v) -> {
            //父不是空
            if (data.get(v.getParentDeptId()) != null) {
                data.get(v.getParentDeptId()).getChildren().add(v);
            }
        });
        return root.get();
    }


    public void batchUpdateDepartments() {
        //钉钉更新部门结构，自动迁移成员和子部门
        //获取钉钉部门(树)
        ZhongjiDepartment dingTalkDepartment = null;
        //获取hr系统部门(树)
        ZhongjiDepartment hrDepartment = null;
        //比较差别，有差别更新


    }


    @Override
    public String addUsers(ZhongjiDepartment zhongjiDepartment) {
        return null;
    }


    /**
     * 创建钉钉部门
     *
     * @param root
     */
    private void createDingTalkDepartment(ZhongjiDepartment root) {
        String outerDeptId = dingTalkInnerClient.createDepartment(root);
        List<ZhongjiDepartment> children = root.getChildren();
        if (!CollectionUtils.isEmpty(children)) {
            for (ZhongjiDepartment child : children) {
                child.setOuterParentDeptId(outerDeptId);
                createDingTalkDepartment(child);
            }
        }
    }


    /**
     * 创建zhongji部门
     *
     * @param self
     * @return
     */

    private ZhongjiDepartment createZhongjiSelfDepartment(JSONObject self) {
        ZhongjiDepartment zhongjiDepartment = new ZhongjiDepartment();
        zhongjiDepartment.setDeptId(self.getString("UNIT_ID"));
        zhongjiDepartment.setParentDeptId(self.getString("PARENT_ID"));
        zhongjiDepartment.setDeptName(self.getString("UNIT_NAME"));
        zhongjiDepartment.setChildren(new ArrayList<>());
        return zhongjiDepartment;
    }


}

