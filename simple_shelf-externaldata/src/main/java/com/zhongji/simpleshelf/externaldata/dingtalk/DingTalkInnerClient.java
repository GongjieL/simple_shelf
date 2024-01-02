package com.zhongji.simpleshelf.externaldata.dingtalk;

import com.alibaba.fastjson.JSON;
import com.dingtalk.api.DefaultDingTalkClient;
import com.dingtalk.api.DingTalkClient;
import com.dingtalk.api.request.OapiGettokenRequest;
import com.dingtalk.api.request.OapiV2DepartmentCreateRequest;
import com.dingtalk.api.request.OapiV2UserCreateRequest;
import com.dingtalk.api.request.OapiV2UserUpdateRequest;
import com.dingtalk.api.response.OapiGettokenResponse;
import com.dingtalk.api.response.OapiV2DepartmentCreateResponse;
import com.dingtalk.api.response.OapiV2UserCreateResponse;
import com.dingtalk.api.response.OapiV2UserUpdateResponse;
import com.taobao.api.ApiException;
import com.zhongji.simpleshelf.common.bo.dingtalk.ZhongjiDepartment;
import com.zhongji.simpleshelf.common.bo.dingtalk.ZhongjiUser;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class DingTalkInnerClient {


    private static String clientId = "dingfyab7fsucs50om2h";
    private static String clientSecret = "t1rJ0wImdW5Vi-l1jfJvJjlJXaK4PRd6r1TQiKWLyzCqtIjf-ihrlxfXOJsl8Mzw";


    /**
     * 创建部门
     *
     * @param zhongjiDepartment
     * @return
     */
    public String createDepartment(ZhongjiDepartment zhongjiDepartment) {
        //获取token
        String token = getToken();
        try {
            DingTalkClient client = new DefaultDingTalkClient("https://oapi.dingtalk.com/topapi/v2/department/create");
            OapiV2DepartmentCreateRequest req = new OapiV2DepartmentCreateRequest();
            req.setParentId(Long.valueOf(
                    zhongjiDepartment.getOuterParentDeptId()));
            req.setName(zhongjiDepartment.getDeptName());
            //设置zhongji内部id
            req.setSourceIdentifier(zhongjiDepartment.getDeptId());
            req.setBrief(zhongjiDepartment.getDeptDesc());
            OapiV2DepartmentCreateResponse rsp = client.execute(req, token);
            if (!rsp.isSuccess()) {
                //找到父节点
                if (rsp.getErrcode().equals(111)) {
                    //之前的节点
                    //todo 获取父的子列表，筛选同名称
                    return null;
                }
            }
            String deptId = rsp.getResult().getDeptId() + "";
            zhongjiDepartment.setOuterDeptId(deptId);
            return deptId;
        } catch (ApiException e) {

        } catch (Exception e) {
            System.out.println(JSON.toJSONString(zhongjiDepartment));
        }
        return null;

    }

    public ZhongjiUser createUser(ZhongjiUser user) {
        //获取token
        String token = getToken();
        try {
            DingTalkClient client = new DefaultDingTalkClient("https://oapi.dingtalk.com/topapi/v2/user/create");
            OapiV2UserCreateRequest req = new OapiV2UserCreateRequest();
            //用户基本信息
            req.setName(user.getName());
            req.setMobile(user.getMobile());
            req.setUserid(user.getUserId());
            //部门信息,创建只考虑一个，创建失败更新
            req.setDeptIdList(user.getOuterDeptId());
            List<OapiV2UserCreateRequest.DeptTitle> deptTitles = new ArrayList<OapiV2UserCreateRequest.DeptTitle>();
            OapiV2UserCreateRequest.DeptTitle deptTitle = new OapiV2UserCreateRequest.DeptTitle();
            deptTitles.add(deptTitle);
            deptTitle.setDeptId(Long.valueOf(user.getOuterDeptId()));
            deptTitle.setTitle(user.getPositionName());
            req.setDeptTitleList(deptTitles);
            //创建用户
            OapiV2UserCreateResponse rsp = client.execute(req, token);
            if ("60102".equals(rsp.getCode()) ||
                    "60104".equals(rsp.getCode())) {
                //有重复，先返回，最后处理
                return null;
            }
            return user;
        } catch (ApiException e) {
            //todo 错误日志
            return null;
        }
    }


    public String updateUser(ZhongjiUser user) {
        //获取token
        String token = getToken();
        try {
            DingTalkClient client = new DefaultDingTalkClient("https://oapi.dingtalk.com/topapi/v2/user/update");
            OapiV2UserUpdateRequest req = new OapiV2UserUpdateRequest();
            req.setName(user.getName());
            req.setMobile(user.getMobile());
            //以此更新
            req.setUserid(user.getUserId());
            //部门信息
            req.setDeptIdList(user.getDeptId());
            List<OapiV2UserUpdateRequest.DeptTitle> deptTitles = new ArrayList<OapiV2UserUpdateRequest.DeptTitle>();
            OapiV2UserUpdateRequest.DeptTitle deptTitle = new OapiV2UserUpdateRequest.DeptTitle();
            deptTitles.add(deptTitle);
            deptTitle.setDeptId(Long.valueOf(user.getOuterDeptId()));
            deptTitle.setTitle(user.getPositionName());
            req.setDeptTitleList(deptTitles);
            OapiV2UserUpdateResponse rsp = client.execute(req, token);
        } catch (ApiException e) {
            e.printStackTrace();
        }
        return null;
    }


    private String getToken() {
        try {
            DingTalkClient client = new DefaultDingTalkClient("https://oapi.dingtalk.com/gettoken");
            OapiGettokenRequest req = new OapiGettokenRequest();
            req.setHttpMethod("GET");
            req.setAppkey(clientId);
            req.setAppsecret(clientSecret);
            OapiGettokenResponse rsp = client.execute(req);
            return rsp.getAccessToken();
        } catch (ApiException e) {
            //todo 报错信息
        }
        return null;
    }


}
