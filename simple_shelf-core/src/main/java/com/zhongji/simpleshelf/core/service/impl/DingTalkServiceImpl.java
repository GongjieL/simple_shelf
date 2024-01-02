package com.zhongji.simpleshelf.core.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.zhongji.simpleshelf.common.bo.dingtalk.ZhongjiDepartment;
import com.zhongji.simpleshelf.core.service.DingTalkService;
import com.zhongji.simpleshelf.dao.domain.dingtalk.DingTalkDepartmentSynSnapshot;
import com.zhongji.simpleshelf.dao.service.impl.DingTalkDepartmentSynSnapshotServiceImpl;
import com.zhongji.simpleshelf.externaldata.dingtalk.DingTalkInnerClient;
import com.zhongji.simpleshelf.externaldata.dingtalk.ZhongjiHrServiceClient;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Service
public class DingTalkServiceImpl implements DingTalkService {


    private static final String MODIFIED = "MODIFIED";
    private static final String DELETED = "DELETED";
    private static final String CREATED = "CREATED";
    private static final String ALL_PRE = "ALL_PRE";
    private static final String ALL_NOW = "ALL_NOW";


    @Autowired
    private ZhongjiHrServiceClient zhongjiHrServiceClient;

    @Autowired
    private DingTalkInnerClient dingTalkInnerClient;


    @Autowired
    private DingTalkDepartmentSynSnapshotServiceImpl dingTalkDepartmentSynSnapshotService;


    @Override
    /**
     * 批量更新
     * //钉钉更新部门结构，自动迁移成员和子部门
     * //获取钉钉部门(树)
     *
     * @throws IOException
     */
    public void batchUpdateDepartments() throws IOException {
        //1、获取钉钉数据
        DingTalkDepartmentSynSnapshot lastSnapshot = dingTalkDepartmentSynSnapshotService.getLatestOne();
        //如果查询不到，dingtalk就是null
        Map<String, ZhongjiDepartment> dingTalkDepartmentData = new HashMap<>();
        Long comparedId = null;
        Map<String, String> dingTalkMap = new HashMap<>();
        if (lastSnapshot != null) {
            comparedId = lastSnapshot.getId();
            dingTalkMap = JSON.parseObject(lastSnapshot.getDingTalkMap(), new TypeReference<Map<String, String>>() {
            });
            dingTalkDepartmentData = buildZhongjiDepartmentMap(JSON.parseArray(lastSnapshot.getHrContent()));
            dingTalkDepartmentData.forEach((k, v) -> {
                v.setOuterDeptId(JSON.parseObject(lastSnapshot.getDingTalkMap()).getString(k));
            });
        }
        //2、获取hr系统信息
//        JSONArray hrDepartments = listHrDepartments();
        JSONArray hrDepartments = JSON.parseArray(
                FileUtils.readFileToString(new File("/users/gongjie/zhongjihr_2.txt"), "utf-8"));

        Map<String, ZhongjiDepartment> hrDepartmentData =
                buildZhongjiDepartmentMap(hrDepartments);
        //3、修改的部门信息
        Map<String, Map<String, ZhongjiDepartment>> changedDepartments = listChangedDepartments(dingTalkDepartmentData, hrDepartmentData);
        //处理部门更新
        handlerChangedDept(changedDepartments, dingTalkMap, hrDepartments, comparedId);
    }


    /**
     * 列出hr系统的部门信息
     *
     * @return
     */
    private JSONArray listHrDepartments() {
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
        return departments;
    }


    /**
     * 构建部门id和部门对应关系
     *
     * @return
     */
    private Map<String, ZhongjiDepartment> buildZhongjiDepartmentMap(JSONArray departments) {
        Map<String, ZhongjiDepartment> data = new HashMap<>();
        departments.forEach(d -> {
            ZhongjiDepartment zhongjiSelfDepartment = buildZhongjiSelfDepartment((JSONObject) d);
            if ("10".equals(((JSONObject) d).getString("LEVEL_ID"))) {
                zhongjiSelfDepartment.setOuterParentDeptId("1");
            }
            data.put(zhongjiSelfDepartment.getDeptId(), zhongjiSelfDepartment);
        });
        return data;
    }


    /**
     * 列出所有删除、更新、新增的部门
     *
     * @param dingTalkDepartmentData
     * @return
     */
    private Map<String, Map<String, ZhongjiDepartment>> listChangedDepartments(Map<String, ZhongjiDepartment> dingTalkDepartmentData,
                                                                               Map<String, ZhongjiDepartment> hrDepartmentData) {
        //删除的数据
        Map<String, ZhongjiDepartment> deletedData = new HashMap<>();
        //修改的数据
        Map<String, ZhongjiDepartment> modifiedData = new HashMap<>();
        //需要创建的数据
        Map<String, ZhongjiDepartment> createdData = new HashMap<>();
        dingTalkDepartmentData.forEach((k, v) -> {
            //删除了
            if (!hrDepartmentData.containsKey(k)) {
                //需要更新的，塞入钉钉系统数据
                deletedData.put(k, v);
            } else if (!hrDepartmentData.get(k).getParentDeptId().equals(v.getParentDeptId())) {
                //需要更新的，塞入hr系统的数据
                modifiedData.put(k, hrDepartmentData.get(k));
            }
        });
        //增加的部门
        Map<String, ZhongjiDepartment> finalDingTalkDepartmentData = dingTalkDepartmentData;
        hrDepartmentData.forEach((k, v) -> {
            //删除了
            if (!finalDingTalkDepartmentData.containsKey(k)) {
                //创建部门
                createdData.put(k, v);
            }
        });
        Map<String, Map<String, ZhongjiDepartment>> map = new HashMap<>();
        map.put(DELETED, deletedData);
        map.put(MODIFIED, modifiedData);
        map.put(CREATED, createdData);
        map.put(ALL_PRE, dingTalkDepartmentData);
        map.put(ALL_NOW, hrDepartmentData);
        return map;
    }

    /**
     * 构建zhongji部门树
     *
     * @param departments
     * @return
     */
    private ZhongjiDepartment buildZhongjiDepartmentTree(JSONArray departments) {
        AtomicReference<ZhongjiDepartment> root = new AtomicReference<>(new ZhongjiDepartment());
        //
        Map<String, ZhongjiDepartment> zhongjiDepartmentMap = buildZhongjiDepartmentMap(departments);
        //找到root
        zhongjiDepartmentMap.forEach((k, v) -> {
            if ("1".equals(v.getOuterParentDeptId())) {
                root.set(v);
                return;
            }
        });
        //构建树
        zhongjiDepartmentMap.forEach((k, v) -> {
            //父不是空
            if (zhongjiDepartmentMap.get(v.getParentDeptId()) != null) {
                zhongjiDepartmentMap.get(v.getParentDeptId()).getChildren().add(v);
            }
        });
        return root.get();
    }


    /**
     * 处理更新
     *
     * @param changedDepartments
     * @param dingTalkMap
     * @param hrDepartments
     * @param comparedId
     */
    private void handlerChangedDept(Map<String, Map<String, ZhongjiDepartment>> changedDepartments,
                                    Map<String, String> dingTalkMap, JSONArray hrDepartments, Long comparedId) {
        //删除
        dealDeletedDept(changedDepartments);
        //修改
        dealModifiedDept(changedDepartments);
        //创建
        dealCreatedDept(changedDepartments);
        //更新数据库
        //创建的outer
        Map<String, String> createdOuterData = changedDepartments.get(CREATED).values().stream()
                .collect(Collectors.toMap(ZhongjiDepartment::getDeptId, ZhongjiDepartment::getOuterDeptId));
        //删除的
        if (dingTalkMap == null) {
            dingTalkMap = new HashMap<>();
        }
        Map<String, String> toDingTalkDeptId = new HashMap<>();
        dingTalkMap.forEach((k, v) -> {
            if (!changedDepartments.get(DELETED).containsKey(k)) {
                toDingTalkDeptId.put(k, v);
            }
        });
        toDingTalkDeptId.putAll(createdOuterData);
        //修改
        Map<String, List<String>> modifiedData = new HashMap<>();
        modifiedData.put(DELETED, changedDepartments.get(DELETED).values()
                .stream().map(ZhongjiDepartment::getDeptId).collect(Collectors.toList()));
        modifiedData.put(CREATED, changedDepartments.get(CREATED).values()
                .stream().map(ZhongjiDepartment::getDeptId).collect(Collectors.toList()));
        modifiedData.put(MODIFIED, changedDepartments.get(MODIFIED).values()
                .stream().map(ZhongjiDepartment::getDeptId).collect(Collectors.toList()));
        //保存数据
        //todo 写到哪里就保存到哪里，最好支持续写
        DingTalkDepartmentSynSnapshot snapshot = new DingTalkDepartmentSynSnapshot();
        snapshot.setComparedId(comparedId);
        snapshot.setHrContent(JSON.toJSONString(hrDepartments));
        snapshot.setDingTalkMap(JSON.toJSONString(toDingTalkDeptId));
        snapshot.setModifiedData(JSON.toJSONString(modifiedData));
        dingTalkDepartmentSynSnapshotService.save(snapshot);
    }


    /**
     * 处理删除的部门
     *
     * @param changedDepartments
     */
    private void dealDeletedDept(Map<String, Map<String, ZhongjiDepartment>> changedDepartments) {
        Map<String, ZhongjiDepartment> deletedData = changedDepartments.get(DELETED);
        if (CollectionUtils.isEmpty(deletedData)) {
            return;
        }
        Map<String, List<ZhongjiDepartment>> parentToChildren =
                changedDepartments.get(ALL_PRE).values().stream().collect(Collectors.groupingBy(ZhongjiDepartment::getParentDeptId));
        deletedData.forEach((k, v) -> {
            deleteDingTalkDepartment(v, parentToChildren, deletedData, changedDepartments.get(MODIFIED));
        });
    }

    /**
     * 处理修改的部门
     *
     * @param changedDepartments
     */
    private void dealModifiedDept(Map<String, Map<String, ZhongjiDepartment>> changedDepartments) {
        if (CollectionUtils.isEmpty(changedDepartments.get(MODIFIED))) {
            return;
        }
        changedDepartments.get(MODIFIED).forEach((k, v) -> {
            modifyDingTalkDepartment(v);
        });
    }


    /**
     * 处理新增的部门
     *
     * @param changedDepartments
     */
    private void dealCreatedDept(Map<String, Map<String, ZhongjiDepartment>> changedDepartments) {
        if (CollectionUtils.isEmpty(changedDepartments.get(CREATED))) {
            return;
        }
        //内部和外部对应关系
        Map<String, String> originalDeptIdToDingTalkId = changedDepartments.get(ALL_PRE).values().stream()
                .collect(Collectors.toMap(ZhongjiDepartment::getDeptId, ZhongjiDepartment::getOuterDeptId));
        changedDepartments.get(CREATED).forEach((k, v) -> {
            createDingTalkDepartment(v, originalDeptIdToDingTalkId, changedDepartments.get(ALL_NOW));
        });
    }


    private void modifyDingTalkDepartment(ZhongjiDepartment zhongjiDepartment) {
        //直接更新
        System.out.println("改变父节点:" + zhongjiDepartment.getDeptId());
    }

    /**
     * 递归删除部门
     *
     * @param zhongjiDepartment
     * @param parentToChildren
     * @param deletedData
     */
    private void deleteDingTalkDepartment(ZhongjiDepartment zhongjiDepartment, Map<String, List<ZhongjiDepartment>> parentToChildren,
                                          Map<String, ZhongjiDepartment> deletedData, Map<String, ZhongjiDepartment> modifiedData) {
        List<ZhongjiDepartment> children = parentToChildren.get(zhongjiDepartment.getDeptId());
        //子为空
        if (!CollectionUtils.isEmpty(children)) {
            Iterator<ZhongjiDepartment> iterator = children.iterator();
            while (iterator.hasNext()) {
                ZhongjiDepartment child = iterator.next();
                //查询改变的
                ZhongjiDepartment changedDept = modifiedData.remove(child.getDeptId());
                //只要不在改变中，就是删除
                if (changedDept != null) {
                    modifyDingTalkDepartment(changedDept);
                    iterator.remove();
                } else {
                    deleteDingTalkDepartment(child, parentToChildren, deletedData, modifiedData);
                }
            }
        }
        //直接删除
        //todo 删除人员，删除自身
        System.out.println("直接删除:" + zhongjiDepartment.getDeptId());
        deletedData.remove(zhongjiDepartment);
        if (parentToChildren.get(zhongjiDepartment.getParentDeptId()) != null) {
            parentToChildren.get(zhongjiDepartment.getParentDeptId()).remove(zhongjiDepartment.getDeptId());
        }
    }


    /**
     * 递归创建部门
     *
     * @param zhongjiDepartment    当前部门
     * @param data                 （原始部门信息）部门号-》钉钉部门号
     * @param zhongjiDepartmentMap （部门信息）部门号-》部门
     */

    private void createDingTalkDepartment(ZhongjiDepartment zhongjiDepartment,
                                          Map<String, String> data,
                                          Map<String, ZhongjiDepartment> zhongjiDepartmentMap) {
        if (zhongjiDepartment == null || data.containsKey(zhongjiDepartment.getDeptId())) {
            return;
        }
        //父不在则要先创建父
        if (!data.containsKey(zhongjiDepartment.getParentDeptId())) {
            //创建
            ZhongjiDepartment parent = zhongjiDepartmentMap.get(zhongjiDepartment.getParentDeptId());
            //创建父
            createDingTalkDepartment(parent, data, zhongjiDepartmentMap);
        }
        if (zhongjiDepartment.getOuterParentDeptId() == null) {
            zhongjiDepartment.setOuterParentDeptId(data.get(zhongjiDepartment.getParentDeptId()) == null ?
                    "1" : data.get(zhongjiDepartment.getParentDeptId()));
        }
        String outerDeptId = dingTalkInnerClient.createDepartment(zhongjiDepartment);
        System.out.println("创建节点:" + zhongjiDepartment.getDeptId() + ",outerId:" + outerDeptId);
        //将创建节点和outer放入
        data.put(zhongjiDepartment.getDeptId(), outerDeptId);
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

    private ZhongjiDepartment buildZhongjiSelfDepartment(JSONObject self) {
        ZhongjiDepartment zhongjiDepartment = new ZhongjiDepartment();
        zhongjiDepartment.setDeptId(self.getString("UNIT_ID"));
        zhongjiDepartment.setParentDeptId(self.getString("PARENT_ID"));
        zhongjiDepartment.setDeptName(self.getString("UNIT_NAME"));
        zhongjiDepartment.setChildren(new ArrayList<>());
        return zhongjiDepartment;
    }

    public static void main(String[] args) throws IOException {
        String dingTalkStr = "{\"children\":[{\"children\":[{\"children\":[{\"children\":[],\"deptId\":\"781626\",\"deptName\":\"质量部\",\"parentDeptId\":\"769230\"},{\"children\":[],\"deptId\":\"781625\",\"deptName\":\"售后服务部\",\"parentDeptId\":\"769230\"},{\"children\":[],\"deptId\":\"773690\",\"deptName\":\"产品开发部\",\"parentDeptId\":\"769230\"},{\"children\":[{\"children\":[],\"deptId\":\"781329\",\"deptName\":\"项目部\",\"parentDeptId\":\"773687\"},{\"children\":[],\"deptId\":\"781328\",\"deptName\":\"客户拓展部\",\"parentDeptId\":\"773687\"}],\"deptId\":\"773687\",\"deptName\":\"战略客户部\",\"parentDeptId\":\"769230\"},{\"children\":[{\"children\":[],\"deptId\":\"769257\",\"deptName\":\"华北战区\",\"parentDeptId\":\"748109\"},{\"children\":[],\"deptId\":\"769258\",\"deptName\":\"陕晋大区\",\"parentDeptId\":\"748109\"},{\"children\":[],\"deptId\":\"769255\",\"deptName\":\"河南大区\",\"parentDeptId\":\"748109\"},{\"children\":[],\"deptId\":\"769256\",\"deptName\":\"西南战区\",\"parentDeptId\":\"748109\"},{\"children\":[{\"children\":[],\"deptId\":\"782409\",\"deptName\":\"新疆大区\",\"parentDeptId\":\"769253\"},{\"children\":[],\"deptId\":\"782408\",\"deptName\":\"陕晋大区\",\"parentDeptId\":\"769253\"},{\"children\":[],\"deptId\":\"782407\",\"deptName\":\"蒙甘宁大区\",\"parentDeptId\":\"769253\"}],\"deptId\":\"769253\",\"deptName\":\"西北战区\",\"parentDeptId\":\"748109\"},{\"children\":[],\"deptId\":\"769254\",\"deptName\":\"蒙甘宁大区\",\"parentDeptId\":\"748109\"},{\"children\":[],\"deptId\":\"769252\",\"deptName\":\"两广大区\",\"parentDeptId\":\"748109\"},{\"children\":[],\"deptId\":\"769248\",\"deptName\":\"山东战区\",\"parentDeptId\":\"748109\"},{\"children\":[],\"deptId\":\"779120\",\"deptName\":\"特罐\",\"parentDeptId\":\"748109\"},{\"children\":[],\"deptId\":\"779119\",\"deptName\":\"船罐储罐\",\"parentDeptId\":\"748109\"},{\"children\":[],\"deptId\":\"774109\",\"deptName\":\"江苏大区\",\"parentDeptId\":\"748109\"},{\"children\":[],\"deptId\":\"769543\",\"deptName\":\"湘鄂大区\",\"parentDeptId\":\"748109\"},{\"children\":[],\"deptId\":\"769542\",\"deptName\":\"苏沪大区\",\"parentDeptId\":\"748109\"},{\"children\":[],\"deptId\":\"769530\",\"deptName\":\"闽浙大区\",\"parentDeptId\":\"748109\"},{\"children\":[],\"deptId\":\"771598\",\"deptName\":\"大客户部\",\"parentDeptId\":\"748109\"},{\"children\":[],\"deptId\":\"748114\",\"deptName\":\"项目部\",\"parentDeptId\":\"748109\"}],\"deptId\":\"748109\",\"deptName\":\"营销部\",\"parentDeptId\":\"769230\"},{\"children\":[],\"deptId\":\"748110\",\"deptName\":\"运营部\",\"parentDeptId\":\"769230\"}],\"deptId\":\"769230\",\"deptName\":\"LT事业部\",\"parentDeptId\":\"765049\"},{\"children\":[{\"children\":[{\"children\":[],\"deptId\":\"781985\",\"deptName\":\"计划采购部\",\"parentDeptId\":\"781984\"}],\"deptId\":\"781984\",\"deptName\":\"制造服务中心\",\"parentDeptId\":\"781983\"},{\"children\":[],\"deptId\":\"781869\",\"deptName\":\"技术服务部\",\"parentDeptId\":\"781983\"}],\"deptId\":\"781983\",\"deptName\":\"芜湖生产中心\",\"parentDeptId\":\"765049\"},{\"children\":[{\"children\":[{\"children\":[],\"deptId\":\"773066\",\"deptName\":\"主机部\",\"parentDeptId\":\"773065\"},{\"children\":[],\"deptId\":\"773067\",\"deptName\":\"委改业务部\",\"parentDeptId\":\"773065\"}],\"deptId\":\"773065\",\"deptName\":\"主机委改业务部\",\"parentDeptId\":\"769229\"},{\"children\":[{\"children\":[],\"deptId\":\"773062\",\"deptName\":\"华润水泥项目部\",\"parentDeptId\":\"773060\"},{\"children\":[],\"deptId\":\"773061\",\"deptName\":\"中国建材项目部\",\"parentDeptId\":\"773060\"},{\"children\":[],\"deptId\":\"773064\",\"deptName\":\"海螺水泥项目部\",\"parentDeptId\":\"773060\"},{\"children\":[],\"deptId\":\"773063\",\"deptName\":\"中建西部项目部\",\"parentDeptId\":\"773060\"}],\"deptId\":\"773060\",\"deptName\":\"物流业务部\",\"parentDeptId\":\"769229\"},{\"children\":[{\"children\":[],\"deptId\":\"774849\",\"deptName\":\"京津冀战区\",\"parentDeptId\":\"773054\"},{\"children\":[],\"deptId\":\"778754\",\"deptName\":\"湘鄂战区\",\"parentDeptId\":\"773054\"},{\"children\":[],\"deptId\":\"773059\",\"deptName\":\"西北特区\",\"parentDeptId\":\"773054\"},{\"children\":[],\"deptId\":\"773058\",\"deptName\":\"华南战区\",\"parentDeptId\":\"773054\"},{\"children\":[],\"deptId\":\"773055\",\"deptName\":\"华东战区\",\"parentDeptId\":\"773054\"},{\"children\":[],\"deptId\":\"773057\",\"deptName\":\"西南搅拌战区\",\"parentDeptId\":\"773054\"},{\"children\":[],\"deptId\":\"773056\",\"deptName\":\"鲁豫战区\",\"parentDeptId\":\"773054\"},{\"children\":[],\"deptId\":\"779152\",\"deptName\":\"搅拌半挂车项目部\",\"parentDeptId\":\"773054\"},{\"children\":[],\"deptId\":\"779153\",\"deptName\":\"新能源项目部\",\"parentDeptId\":\"773054\"},{\"children\":[],\"deptId\":\"779104\",\"deptName\":\"华北战区\",\"parentDeptId\":\"773054\"},{\"children\":[],\"deptId\":\"779105\",\"deptName\":\"推板罐项目组\",\"parentDeptId\":\"773054\"},{\"children\":[],\"deptId\":\"779101\",\"deptName\":\"陕晋战区\",\"parentDeptId\":\"773054\"},{\"children\":[],\"deptId\":\"779102\",\"deptName\":\"皖赣闵战区\",\"parentDeptId\":\"773054\"},{\"children\":[],\"deptId\":\"779103\",\"deptName\":\"西南粉罐战区\",\"parentDeptId\":\"773054\"}],\"deptId\":\"773054\",\"deptName\":\"车辆销售业务部\",\"parentDeptId\":\"769229\"},{\"children\":[],\"deptId\":\"781628\",\"deptName\":\"质量部\",\"parentDeptId\":\"769229\"},{\"children\":[{\"children\":[],\"deptId\":\"781871\",\"deptName\":\"客户拓展部\",\"parentDeptId\":\"781627\"},{\"children\":[],\"deptId\":\"781872\",\"deptName\":\"项目部\",\"parentDeptId\":\"781627\"}],\"deptId\":\"781627\",\"deptName\":\"战略客户部\",\"parentDeptId\":\"769229\"},{\"children\":[],\"deptId\":\"748162\",\"deptName\":\"产品开发部\",\"parentDeptId\":\"769229\"},{\"children\":[],\"deptId\":\"748123\",\"deptName\":\"运营部\",\"parentDeptId\":\"769229\"}],\"deptId\":\"769229\",\"deptName\":\"CE事业部\",\"parentDeptId\":\"765049\"},{\"children\":[{\"children\":[{\"children\":[],\"deptId\":\"778511\",\"deptName\":\"计划采购部\",\"parentDeptId\":\"748083\"},{\"children\":[],\"deptId\":\"748096\",\"deptName\":\"储运部\",\"parentDeptId\":\"748083\"},{\"children\":[],\"deptId\":\"748091\",\"deptName\":\"精益HSE部\",\"parentDeptId\":\"748083\"},{\"children\":[],\"deptId\":\"781781\",\"deptName\":\"设备部\",\"parentDeptId\":\"748083\"}],\"deptId\":\"748083\",\"deptName\":\"制造服务中心\",\"parentDeptId\":\"748075\"},{\"children\":[],\"deptId\":\"748362\",\"deptName\":\"技术服务部\",\"parentDeptId\":\"748075\"},{\"children\":[{\"children\":[{\"children\":[],\"deptId\":\"781947\",\"deptName\":\"收尾交车工作岛\",\"parentDeptId\":\"781831\"},{\"children\":[],\"deptId\":\"781945\",\"deptName\":\"管道工作岛\",\"parentDeptId\":\"781831\"},{\"children\":[],\"deptId\":\"781946\",\"deptName\":\"附件装配工作岛\",\"parentDeptId\":\"781831\"},{\"children\":[],\"deptId\":\"781944\",\"deptName\":\"电、气路工作岛\",\"parentDeptId\":\"781831\"}],\"deptId\":\"781831\",\"deptName\":\"总装工段\",\"parentDeptId\":\"765374\"},{\"children\":[{\"children\":[],\"deptId\":\"781938\",\"deptName\":\"喷粉工作岛\",\"parentDeptId\":\"774913\"},{\"children\":[],\"deptId\":\"781936\",\"deptName\":\"抛光工作岛\",\"parentDeptId\":\"774913\"},{\"children\":[],\"deptId\":\"781937\",\"deptName\":\"喷漆工作岛\",\"parentDeptId\":\"774913\"}],\"deptId\":\"774913\",\"deptName\":\"涂装工段\",\"parentDeptId\":\"765374\"},{\"children\":[{\"children\":[],\"deptId\":\"781941\",\"deptName\":\"小件拼焊工作岛\",\"parentDeptId\":\"748092\"},{\"children\":[],\"deptId\":\"781942\",\"deptName\":\"封头隔仓工作岛\",\"parentDeptId\":\"748092\"},{\"children\":[],\"deptId\":\"781940\",\"deptName\":\"剪折工作岛\",\"parentDeptId\":\"748092\"},{\"children\":[],\"deptId\":\"781943\",\"deptName\":\"备料班\",\"parentDeptId\":\"748092\"},{\"children\":[],\"deptId\":\"781939\",\"deptName\":\"数控工作岛\",\"parentDeptId\":\"748092\"}],\"deptId\":\"748092\",\"deptName\":\"下料工段\",\"parentDeptId\":\"765374\"},{\"children\":[{\"children\":[],\"deptId\":\"781934\",\"deptName\":\"筒体工作岛\",\"parentDeptId\":\"748173\"},{\"children\":[],\"deptId\":\"781935\",\"deptName\":\"附件工作岛\",\"parentDeptId\":\"748173\"}],\"deptId\":\"748173\",\"deptName\":\"特罐铆焊工段\",\"parentDeptId\":\"765374\"},{\"children\":[{\"children\":[],\"deptId\":\"781923\",\"deptName\":\"铝合金拼卷工作岛\",\"parentDeptId\":\"781810\"},{\"children\":[],\"deptId\":\"781924\",\"deptName\":\"铝合金高端生产线\",\"parentDeptId\":\"781810\"}],\"deptId\":\"781810\",\"deptName\":\"A1铆焊工段\",\"parentDeptId\":\"765374\"},{\"children\":[{\"children\":[],\"deptId\":\"781929\",\"deptName\":\"碳钢拼卷工作岛\",\"parentDeptId\":\"748117\"},{\"children\":[],\"deptId\":\"781930\",\"deptName\":\"碳钢筒体工作岛\",\"parentDeptId\":\"748117\"},{\"children\":[],\"deptId\":\"781931\",\"deptName\":\"碳钢焊接工作岛\",\"parentDeptId\":\"748117\"},{\"children\":[],\"deptId\":\"781932\",\"deptName\":\"碳钢附件工作岛\",\"parentDeptId\":\"748117\"},{\"children\":[],\"deptId\":\"781933\",\"deptName\":\"碳钢试水工作岛\",\"parentDeptId\":\"748117\"}],\"deptId\":\"748117\",\"deptName\":\"碳钢铆焊工段\",\"parentDeptId\":\"765374\"},{\"children\":[{\"children\":[],\"deptId\":\"781927\",\"deptName\":\"附件工作岛\",\"parentDeptId\":\"781811\"},{\"children\":[],\"deptId\":\"781928\",\"deptName\":\"试水工作岛\",\"parentDeptId\":\"781811\"},{\"children\":[],\"deptId\":\"781925\",\"deptName\":\"筒体工作岛\",\"parentDeptId\":\"781811\"},{\"children\":[],\"deptId\":\"781926\",\"deptName\":\"焊接工作岛\",\"parentDeptId\":\"781811\"}],\"deptId\":\"781811\",\"deptName\":\"A2铆焊工段\",\"parentDeptId\":\"765374\"}],\"deptId\":\"765374\",\"deptName\":\"LTP1\",\"parentDeptId\":\"748075\"}],\"deptId\":\"748075\",\"deptName\":\"扬州生产中心\",\"parentDeptId\":\"765049\"},{\"children\":[{\"children\":[{\"children\":[],\"deptId\":\"780342\",\"deptName\":\"东南亚一区\",\"parentDeptId\":\"748426\"},{\"children\":[],\"deptId\":\"780343\",\"deptName\":\"东南亚二区\",\"parentDeptId\":\"748426\"}],\"deptId\":\"748426\",\"deptName\":\"东南亚战区\",\"parentDeptId\":\"748353\"},{\"children\":[{\"children\":[],\"deptId\":\"780336\",\"deptName\":\"亚俄一区\",\"parentDeptId\":\"748428\"},{\"children\":[],\"deptId\":\"780337\",\"deptName\":\"亚俄二区\",\"parentDeptId\":\"748428\"}],\"deptId\":\"748428\",\"deptName\":\"亚俄战区\",\"parentDeptId\":\"748353\"},{\"children\":[{\"children\":[],\"deptId\":\"780339\",\"deptName\":\"非洲中东二区\",\"parentDeptId\":\"748427\"},{\"children\":[],\"deptId\":\"780338\",\"deptName\":\"非洲中东一区\",\"parentDeptId\":\"748427\"},{\"children\":[],\"deptId\":\"780340\",\"deptName\":\"非洲中东三区\",\"parentDeptId\":\"748427\"},{\"children\":[],\"deptId\":\"780341\",\"deptName\":\"非洲中东四区\",\"parentDeptId\":\"748427\"}],\"deptId\":\"748427\",\"deptName\":\"非洲中东战区\",\"parentDeptId\":\"748353\"},{\"children\":[],\"deptId\":\"748429\",\"deptName\":\"美澳战区\",\"parentDeptId\":\"748353\"},{\"children\":[{\"children\":[],\"deptId\":\"780327\",\"deptName\":\"售中售后服务部\",\"parentDeptId\":\"780332\"},{\"children\":[],\"deptId\":\"781780\",\"deptName\":\"产品开发部\",\"parentDeptId\":\"780332\"}],\"deptId\":\"780332\",\"deptName\":\"营销部\",\"parentDeptId\":\"748353\"},{\"children\":[],\"deptId\":\"778488\",\"deptName\":\"LAG项目部\",\"parentDeptId\":\"748353\"},{\"children\":[],\"deptId\":\"779684\",\"deptName\":\"质量部\",\"parentDeptId\":\"748353\"},{\"children\":[],\"deptId\":\"748363\",\"deptName\":\"运营部\",\"parentDeptId\":\"748353\"}],\"deptId\":\"748353\",\"deptName\":\"IC事业部\",\"parentDeptId\":\"765049\"}],\"deptId\":\"765049\",\"deptName\":\"扬州中集通华数字化罐车工厂\",\"parentDeptId\":\"747981\"},{\"children\":[{\"children\":[],\"deptId\":\"773968\",\"deptName\":\"战略管理小组\",\"parentDeptId\":\"779674\"},{\"children\":[{\"children\":[],\"deptId\":\"778384\",\"deptName\":\"模块化部\",\"parentDeptId\":\"748347\"},{\"children\":[],\"deptId\":\"748354\",\"deptName\":\"技术管理部\",\"parentDeptId\":\"748347\"},{\"children\":[],\"deptId\":\"748356\",\"deptName\":\"ME部\",\"parentDeptId\":\"748347\"},{\"children\":[],\"deptId\":\"748355\",\"deptName\":\"技术开发部\",\"parentDeptId\":\"748347\"}],\"deptId\":\"748347\",\"deptName\":\"技术中心\",\"parentDeptId\":\"779674\"},{\"children\":[{\"children\":[],\"deptId\":\"748425\",\"deptName\":\"数字流程部\",\"parentDeptId\":\"748349\"},{\"children\":[],\"deptId\":\"778943\",\"deptName\":\"质量管理部\",\"parentDeptId\":\"748349\"},{\"children\":[],\"deptId\":\"748358\",\"deptName\":\"EPS中心\",\"parentDeptId\":\"748349\"},{\"children\":[],\"deptId\":\"748357\",\"deptName\":\"精益HSE部\",\"parentDeptId\":\"748349\"}],\"deptId\":\"748349\",\"deptName\":\"质量运营中心\",\"parentDeptId\":\"779674\"},{\"children\":[{\"children\":[],\"deptId\":\"767126\",\"deptName\":\"HRBP\",\"parentDeptId\":\"748348\"},{\"children\":[],\"deptId\":\"767125\",\"deptName\":\"COE\",\"parentDeptId\":\"748348\"},{\"children\":[],\"deptId\":\"767127\",\"deptName\":\"SSC\",\"parentDeptId\":\"748348\"}],\"deptId\":\"748348\",\"deptName\":\"人力资源中心\",\"parentDeptId\":\"779674\"},{\"children\":[{\"children\":[],\"deptId\":\"767129\",\"deptName\":\"资金管理部\",\"parentDeptId\":\"748352\"},{\"children\":[],\"deptId\":\"767130\",\"deptName\":\"预算分析部\",\"parentDeptId\":\"748352\"},{\"children\":[],\"deptId\":\"767128\",\"deptName\":\"会计核算部\",\"parentDeptId\":\"748352\"},{\"children\":[],\"deptId\":\"781589\",\"deptName\":\"CE财务管理部\",\"parentDeptId\":\"748352\"},{\"children\":[],\"deptId\":\"781587\",\"deptName\":\"LT财务管理部\",\"parentDeptId\":\"748352\"},{\"children\":[],\"deptId\":\"781588\",\"deptName\":\"IC财务管理部\",\"parentDeptId\":\"748352\"}],\"deptId\":\"748352\",\"deptName\":\"财务中心\",\"parentDeptId\":\"779674\"},{\"children\":[{\"children\":[],\"deptId\":\"748360\",\"deptName\":\"市场营销部\",\"parentDeptId\":\"748351\"},{\"children\":[],\"deptId\":\"748359\",\"deptName\":\"新业务拓展部\",\"parentDeptId\":\"748351\"}],\"deptId\":\"748351\",\"deptName\":\"NR营销中心\",\"parentDeptId\":\"779674\"}],\"deptId\":\"779674\",\"deptName\":\"强冠BG\",\"parentDeptId\":\"747981\"},{\"children\":[{\"children\":[{\"children\":[{\"children\":[],\"deptId\":\"774706\",\"deptName\":\"装备技术\",\"parentDeptId\":\"781579\"},{\"children\":[],\"deptId\":\"781978\",\"deptName\":\"采购\",\"parentDeptId\":\"781579\"},{\"children\":[],\"deptId\":\"721952\",\"deptName\":\"精益HSE\",\"parentDeptId\":\"781579\"},{\"children\":[],\"deptId\":\"721951\",\"deptName\":\"行政\",\"parentDeptId\":\"781579\"},{\"children\":[],\"deptId\":\"698849\",\"deptName\":\"仓储\",\"parentDeptId\":\"781579\"}],\"deptId\":\"781579\",\"deptName\":\"制造服务中心\",\"parentDeptId\":\"748210\"},{\"children\":[{\"children\":[{\"children\":[],\"deptId\":\"698870\",\"deptName\":\"下料工作岛\",\"parentDeptId\":\"781979\"}],\"deptId\":\"781979\",\"deptName\":\"下料工段\",\"parentDeptId\":\"773668\"},{\"children\":[{\"children\":[],\"deptId\":\"698869\",\"deptName\":\"总装工作岛\",\"parentDeptId\":\"781980\"}],\"deptId\":\"781980\",\"deptName\":\"总装工段\",\"parentDeptId\":\"773668\"},{\"children\":[{\"children\":[],\"deptId\":\"698868\",\"deptName\":\"筒体工作岛\",\"parentDeptId\":\"779749\"},{\"children\":[],\"deptId\":\"698867\",\"deptName\":\"下盘工作岛\",\"parentDeptId\":\"779749\"}],\"deptId\":\"779749\",\"deptName\":\"铆焊工段\",\"parentDeptId\":\"773668\"}],\"deptId\":\"773668\",\"deptName\":\"LTP4\",\"parentDeptId\":\"748210\"}],\"deptId\":\"748210\",\"deptName\":\"梁山生产中心\",\"parentDeptId\":\"647265\"},{\"children\":[{\"children\":[],\"deptId\":\"781728\",\"deptName\":\"售后服务部\",\"parentDeptId\":\"781727\"}],\"deptId\":\"781727\",\"deptName\":\"LT事业部\",\"parentDeptId\":\"647265\"},{\"children\":[{\"children\":[],\"deptId\":\"781581\",\"deptName\":\"产品开发部\",\"parentDeptId\":\"769227\"},{\"children\":[],\"deptId\":\"781580\",\"deptName\":\"质量部\",\"parentDeptId\":\"769227\"},{\"children\":[],\"deptId\":\"698852\",\"deptName\":\"运营部\",\"parentDeptId\":\"769227\"},{\"children\":[{\"children\":[],\"deptId\":\"770400\",\"deptName\":\"华北战区\",\"parentDeptId\":\"698858\"},{\"children\":[],\"deptId\":\"770402\",\"deptName\":\"西北特区\",\"parentDeptId\":\"698858\"}],\"deptId\":\"698858\",\"deptName\":\"车辆销售业务部\",\"parentDeptId\":\"769227\"}],\"deptId\":\"769227\",\"deptName\":\"CE事业部\",\"parentDeptId\":\"647265\"},{\"children\":[{\"children\":[],\"deptId\":\"782362\",\"deptName\":\"质量部\",\"parentDeptId\":\"782310\"}],\"deptId\":\"782310\",\"deptName\":\"IC事业部\",\"parentDeptId\":\"647265\"}],\"deptId\":\"647265\",\"deptName\":\"山东万事达专用汽车制造有限公司\",\"parentDeptId\":\"747981\"},{\"children\":[{\"children\":[{\"children\":[],\"deptId\":\"781868\",\"deptName\":\"技术服务部\",\"parentDeptId\":\"782147\"}],\"deptId\":\"782147\",\"deptName\":\"芜湖生产中心\",\"parentDeptId\":\"774651\"},{\"children\":[{\"children\":[],\"deptId\":\"781743\",\"deptName\":\"技术服务部\",\"parentDeptId\":\"781741\"},{\"children\":[],\"deptId\":\"781742\",\"deptName\":\"运营部\",\"parentDeptId\":\"781741\"}],\"deptId\":\"781741\",\"deptName\":\"IC事业部\",\"parentDeptId\":\"774651\"},{\"children\":[{\"children\":[],\"deptId\":\"774700\",\"deptName\":\"产品开发部\",\"parentDeptId\":\"774697\"},{\"children\":[{\"children\":[],\"deptId\":\"777998\",\"deptName\":\"山东大区\",\"parentDeptId\":\"774701\"},{\"children\":[],\"deptId\":\"780666\",\"deptName\":\"西南战区\",\"parentDeptId\":\"774701\"},{\"children\":[],\"deptId\":\"777996\",\"deptName\":\"河南大区\",\"parentDeptId\":\"774701\"},{\"children\":[],\"deptId\":\"780669\",\"deptName\":\"渝贵大区\",\"parentDeptId\":\"774701\"},{\"children\":[],\"deptId\":\"780667\",\"deptName\":\"西北战区\",\"parentDeptId\":\"774701\"},{\"children\":[],\"deptId\":\"780668\",\"deptName\":\"川云大区\",\"parentDeptId\":\"774701\"},{\"children\":[],\"deptId\":\"780670\",\"deptName\":\"陕晋大区\",\"parentDeptId\":\"774701\"},{\"children\":[],\"deptId\":\"780671\",\"deptName\":\"蒙甘宁大区\",\"parentDeptId\":\"774701\"}],\"deptId\":\"774701\",\"deptName\":\"营销部\",\"parentDeptId\":\"774697\"},{\"children\":[],\"deptId\":\"779707\",\"deptName\":\"运营部\",\"parentDeptId\":\"774697\"},{\"children\":[],\"deptId\":\"781655\",\"deptName\":\"质量部\",\"parentDeptId\":\"774697\"}],\"deptId\":\"774697\",\"deptName\":\"LT事业部\",\"parentDeptId\":\"774651\"},{\"children\":[{\"children\":[{\"children\":[{\"children\":[],\"deptId\":\"779984\",\"deptName\":\"仓储\",\"parentDeptId\":\"781524\"},{\"children\":[],\"deptId\":\"774703\",\"deptName\":\"运营\",\"parentDeptId\":\"781524\"}],\"deptId\":\"781524\",\"deptName\":\"综合服务组\",\"parentDeptId\":\"774699\"},{\"children\":[],\"deptId\":\"781527\",\"deptName\":\"采购组\",\"parentDeptId\":\"774699\"}],\"deptId\":\"774699\",\"deptName\":\"制造服务中心\",\"parentDeptId\":\"774695\"},{\"children\":[{\"children\":[{\"children\":[],\"deptId\":\"781949\",\"deptName\":\"交车工作岛\",\"parentDeptId\":\"779847\"},{\"children\":[],\"deptId\":\"781948\",\"deptName\":\"电气路工作岛\",\"parentDeptId\":\"779847\"},{\"children\":[],\"deptId\":\"781950\",\"deptName\":\"外围工作岛\",\"parentDeptId\":\"779847\"}],\"deptId\":\"779847\",\"deptName\":\"总装工段\",\"parentDeptId\":\"774698\"},{\"children\":[{\"children\":[],\"deptId\":\"774702\",\"deptName\":\"碳钢不锈钢工作岛\",\"parentDeptId\":\"781576\"},{\"children\":[],\"deptId\":\"781530\",\"deptName\":\"铝合金工作岛\",\"parentDeptId\":\"781576\"},{\"children\":[],\"deptId\":\"781577\",\"deptName\":\"物流备料工作岛\",\"parentDeptId\":\"781576\"}],\"deptId\":\"781576\",\"deptName\":\"铆焊工段\",\"parentDeptId\":\"774698\"}],\"deptId\":\"774698\",\"deptName\":\"LTP5\",\"parentDeptId\":\"774695\"}],\"deptId\":\"774695\",\"deptName\":\"洛阳生产中心\",\"parentDeptId\":\"774651\"}],\"deptId\":\"774651\",\"deptName\":\"洛阳中集凌宇液罐工厂\",\"parentDeptId\":\"747981\"},{\"children\":[{\"children\":[{\"children\":[{\"children\":[],\"deptId\":\"778040\",\"deptName\":\"储运部\",\"parentDeptId\":\"748573\"},{\"children\":[],\"deptId\":\"748616\",\"deptName\":\"精益HSE部\",\"parentDeptId\":\"748573\"},{\"children\":[],\"deptId\":\"748613\",\"deptName\":\"设备部\",\"parentDeptId\":\"748573\"},{\"children\":[],\"deptId\":\"781692\",\"deptName\":\"计划采购部\",\"parentDeptId\":\"748573\"}],\"deptId\":\"748573\",\"deptName\":\"制造服务中心\",\"parentDeptId\":\"748570\"},{\"children\":[{\"children\":[{\"children\":[],\"deptId\":\"779048\",\"deptName\":\"焊接工作岛\",\"parentDeptId\":\"781958\"},{\"children\":[],\"deptId\":\"779041\",\"deptName\":\"包罐工作岛\",\"parentDeptId\":\"781958\"},{\"children\":[],\"deptId\":\"779044\",\"deptName\":\"卷板工作岛\",\"parentDeptId\":\"781958\"}],\"deptId\":\"781958\",\"deptName\":\"标罐工段\",\"parentDeptId\":\"766087\"},{\"children\":[{\"children\":[],\"deptId\":\"779017\",\"deptName\":\"LAG工作岛\",\"parentDeptId\":\"779016\"}],\"deptId\":\"779016\",\"deptName\":\"LAG工段\",\"parentDeptId\":\"766087\"},{\"children\":[{\"children\":[],\"deptId\":\"779062\",\"deptName\":\"铝合金工作岛\",\"parentDeptId\":\"781882\"},{\"children\":[],\"deptId\":\"781959\",\"deptName\":\"管道工作岛\",\"parentDeptId\":\"781882\"},{\"children\":[],\"deptId\":\"779081\",\"deptName\":\"小件工作岛\",\"parentDeptId\":\"781882\"},{\"children\":[],\"deptId\":\"779080\",\"deptName\":\"焊接工作岛\",\"parentDeptId\":\"781882\"},{\"children\":[],\"deptId\":\"779075\",\"deptName\":\"试水工作岛\",\"parentDeptId\":\"781882\"}],\"deptId\":\"781882\",\"deptName\":\"非标工段\",\"parentDeptId\":\"766087\"},{\"children\":[{\"children\":[],\"deptId\":\"779068\",\"deptName\":\"喷粉工作岛\",\"parentDeptId\":\"766102\"}],\"deptId\":\"766102\",\"deptName\":\"喷粉工段\",\"parentDeptId\":\"766087\"},{\"children\":[{\"children\":[],\"deptId\":\"779064\",\"deptName\":\"配送工作岛\",\"parentDeptId\":\"766101\"},{\"children\":[],\"deptId\":\"781960\",\"deptName\":\"收尾工作岛\",\"parentDeptId\":\"766101\"},{\"children\":[],\"deptId\":\"779040\",\"deptName\":\"售后服务岛\",\"parentDeptId\":\"766101\"},{\"children\":[],\"deptId\":\"773139\",\"deptName\":\"装箱发运工作岛\",\"parentDeptId\":\"766101\"},{\"children\":[],\"deptId\":\"764642\",\"deptName\":\"保温附件工作岛\",\"parentDeptId\":\"766101\"},{\"children\":[],\"deptId\":\"779085\",\"deptName\":\"总装工作岛\",\"parentDeptId\":\"766101\"}],\"deptId\":\"766101\",\"deptName\":\"总装工段\",\"parentDeptId\":\"766087\"}],\"deptId\":\"766087\",\"deptName\":\"LTP2\",\"parentDeptId\":\"748570\"},{\"children\":[{\"children\":[{\"children\":[],\"deptId\":\"782361\",\"deptName\":\"推板罐工作岛\",\"parentDeptId\":\"781879\"},{\"children\":[],\"deptId\":\"779056\",\"deptName\":\"罐体总成工作岛\",\"parentDeptId\":\"781879\"},{\"children\":[],\"deptId\":\"779059\",\"deptName\":\"节圆总成工作岛\",\"parentDeptId\":\"781879\"},{\"children\":[],\"deptId\":\"779051\",\"deptName\":\"车架工作岛\",\"parentDeptId\":\"781879\"},{\"children\":[],\"deptId\":\"766110\",\"deptName\":\"机动工作岛\",\"parentDeptId\":\"781879\"},{\"children\":[],\"deptId\":\"772447\",\"deptName\":\"改制工作岛\",\"parentDeptId\":\"781879\"}],\"deptId\":\"781879\",\"deptName\":\"铆焊工段\",\"parentDeptId\":\"766088\"},{\"children\":[{\"children\":[],\"deptId\":\"779073\",\"deptName\":\"喷粉工作岛\",\"parentDeptId\":\"781880\"}],\"deptId\":\"781880\",\"deptName\":\"喷粉工段\",\"parentDeptId\":\"766088\"},{\"children\":[{\"children\":[],\"deptId\":\"764613\",\"deptName\":\"车架装配工作岛\",\"parentDeptId\":\"766120\"},{\"children\":[],\"deptId\":\"764615\",\"deptName\":\"总装装配工作岛\",\"parentDeptId\":\"766120\"}],\"deptId\":\"766120\",\"deptName\":\"总装工段\",\"parentDeptId\":\"766088\"},{\"children\":[{\"children\":[],\"deptId\":\"779057\",\"deptName\":\"剪折钻冲工作岛\",\"parentDeptId\":\"768989\"},{\"children\":[],\"deptId\":\"779076\",\"deptName\":\"开平激光工作岛\",\"parentDeptId\":\"768989\"}],\"deptId\":\"768989\",\"deptName\":\"下料工段\",\"parentDeptId\":\"766088\"}],\"deptId\":\"766088\",\"deptName\":\"LTP3\",\"parentDeptId\":\"748570\"},{\"children\":[],\"deptId\":\"778930\",\"deptName\":\"技术服务部\",\"parentDeptId\":\"748570\"}],\"deptId\":\"748570\",\"deptName\":\"芜湖生产中心\",\"parentDeptId\":\"600462\"},{\"children\":[{\"children\":[{\"children\":[],\"deptId\":\"781736\",\"deptName\":\"东南亚战区一区\",\"parentDeptId\":\"781732\"},{\"children\":[],\"deptId\":\"781737\",\"deptName\":\"东南亚战区二区\",\"parentDeptId\":\"781732\"}],\"deptId\":\"781732\",\"deptName\":\"东南亚战区\",\"parentDeptId\":\"781729\"},{\"children\":[{\"children\":[],\"deptId\":\"781738\",\"deptName\":\"亚俄战区一区\",\"parentDeptId\":\"781733\"},{\"children\":[],\"deptId\":\"781739\",\"deptName\":\"亚俄战区二区\",\"parentDeptId\":\"781733\"}],\"deptId\":\"781733\",\"deptName\":\"亚俄战区\",\"parentDeptId\":\"781729\"},{\"children\":[],\"deptId\":\"781731\",\"deptName\":\"LAG项目部\",\"parentDeptId\":\"781729\"},{\"children\":[{\"children\":[],\"deptId\":\"781740\",\"deptName\":\"非洲中东战区一区\",\"parentDeptId\":\"781734\"},{\"children\":[],\"deptId\":\"781987\",\"deptName\":\"非洲中东战区二区\",\"parentDeptId\":\"781734\"}],\"deptId\":\"781734\",\"deptName\":\"非洲中东战区\",\"parentDeptId\":\"781729\"},{\"children\":[],\"deptId\":\"781735\",\"deptName\":\"运营部\",\"parentDeptId\":\"781729\"},{\"children\":[{\"children\":[],\"deptId\":\"781982\",\"deptName\":\"售后售中服务部\",\"parentDeptId\":\"781981\"}],\"deptId\":\"781981\",\"deptName\":\"营销部\",\"parentDeptId\":\"781729\"}],\"deptId\":\"781729\",\"deptName\":\"IC事业部\",\"parentDeptId\":\"600462\"},{\"children\":[],\"deptId\":\"720388\",\"deptName\":\"数字运营中心\",\"parentDeptId\":\"600462\"},{\"children\":[{\"children\":[],\"deptId\":\"781975\",\"deptName\":\"技术服务部\",\"parentDeptId\":\"781915\"},{\"children\":[],\"deptId\":\"781976\",\"deptName\":\"计划采购部\",\"parentDeptId\":\"781915\"},{\"children\":[{\"children\":[],\"deptId\":\"781977\",\"deptName\":\"总装工段\",\"parentDeptId\":\"781916\"}],\"deptId\":\"781916\",\"deptName\":\"LTP1\",\"parentDeptId\":\"781915\"}],\"deptId\":\"781915\",\"deptName\":\"扬州生产中心\",\"parentDeptId\":\"600462\"},{\"children\":[{\"children\":[],\"deptId\":\"781654\",\"deptName\":\"质量部\",\"parentDeptId\":\"720368\"},{\"children\":[],\"deptId\":\"768688\",\"deptName\":\"售后服务部\",\"parentDeptId\":\"720368\"},{\"children\":[],\"deptId\":\"730947\",\"deptName\":\"运营部\",\"parentDeptId\":\"720368\"},{\"children\":[{\"children\":[],\"deptId\":\"780909\",\"deptName\":\"客户拓展部\",\"parentDeptId\":\"773672\"},{\"children\":[],\"deptId\":\"780910\",\"deptName\":\"项目部\",\"parentDeptId\":\"773672\"}],\"deptId\":\"773672\",\"deptName\":\"战略客户部\",\"parentDeptId\":\"720368\"},{\"children\":[{\"children\":[],\"deptId\":\"764656\",\"deptName\":\"河南大区\",\"parentDeptId\":\"720493\"},{\"children\":[],\"deptId\":\"764655\",\"deptName\":\"湘鄂大区\",\"parentDeptId\":\"720493\"},{\"children\":[],\"deptId\":\"764654\",\"deptName\":\"西南大区\",\"parentDeptId\":\"720493\"},{\"children\":[],\"deptId\":\"764658\",\"deptName\":\"闽浙大区\",\"parentDeptId\":\"720493\"},{\"children\":[],\"deptId\":\"764646\",\"deptName\":\"苏沪大区\",\"parentDeptId\":\"720493\"},{\"children\":[],\"deptId\":\"764645\",\"deptName\":\"吉黑大区\",\"parentDeptId\":\"720493\"},{\"children\":[],\"deptId\":\"764644\",\"deptName\":\"华北大区\",\"parentDeptId\":\"720493\"},{\"children\":[],\"deptId\":\"764647\",\"deptName\":\"两广大区\",\"parentDeptId\":\"720493\"},{\"children\":[],\"deptId\":\"764653\",\"deptName\":\"西北大区\",\"parentDeptId\":\"720493\"},{\"children\":[],\"deptId\":\"764652\",\"deptName\":\"皖赣大区\",\"parentDeptId\":\"720493\"},{\"children\":[],\"deptId\":\"764650\",\"deptName\":\"山东大区\",\"parentDeptId\":\"720493\"},{\"children\":[],\"deptId\":\"778910\",\"deptName\":\"山东战区\",\"parentDeptId\":\"720493\"},{\"children\":[{\"children\":[],\"deptId\":\"779753\",\"deptName\":\"船罐项目\",\"parentDeptId\":\"778915\"},{\"children\":[],\"deptId\":\"779754\",\"deptName\":\"特罐项目\",\"parentDeptId\":\"778915\"}],\"deptId\":\"778915\",\"deptName\":\"项目部\",\"parentDeptId\":\"720493\"},{\"children\":[{\"children\":[],\"deptId\":\"779151\",\"deptName\":\"渝贵大区\",\"parentDeptId\":\"778909\"},{\"children\":[],\"deptId\":\"779150\",\"deptName\":\"川云大区\",\"parentDeptId\":\"778909\"}],\"deptId\":\"778909\",\"deptName\":\"西南战区\",\"parentDeptId\":\"720493\"},{\"children\":[{\"children\":[],\"deptId\":\"779148\",\"deptName\":\"东北大区\",\"parentDeptId\":\"778908\"},{\"children\":[],\"deptId\":\"779149\",\"deptName\":\"京津冀大区\",\"parentDeptId\":\"778908\"}],\"deptId\":\"778908\",\"deptName\":\"华北战区\",\"parentDeptId\":\"720493\"},{\"children\":[{\"children\":[],\"deptId\":\"779147\",\"deptName\":\"蒙甘宁大区\",\"parentDeptId\":\"778907\"},{\"children\":[],\"deptId\":\"764657\",\"deptName\":\"新疆大区\",\"parentDeptId\":\"778907\"},{\"children\":[],\"deptId\":\"764651\",\"deptName\":\"陕晋大区\",\"parentDeptId\":\"778907\"}],\"deptId\":\"778907\",\"deptName\":\"西北战区\",\"parentDeptId\":\"720493\"}],\"deptId\":\"720493\",\"deptName\":\"营销部\",\"parentDeptId\":\"720368\"},{\"children\":[],\"deptId\":\"720497\",\"deptName\":\"产品开发部\",\"parentDeptId\":\"720368\"}],\"deptId\":\"720368\",\"deptName\":\"LT事业部\",\"parentDeptId\":\"600462\"},{\"children\":[{\"children\":[],\"deptId\":\"781841\",\"deptName\":\"质量部\",\"parentDeptId\":\"720367\"},{\"children\":[{\"children\":[],\"deptId\":\"773048\",\"deptName\":\"海螺水泥项目部\",\"parentDeptId\":\"773037\"},{\"children\":[],\"deptId\":\"773047\",\"deptName\":\"中建西部项目部\",\"parentDeptId\":\"773037\"},{\"children\":[],\"deptId\":\"773046\",\"deptName\":\"华润水泥项目部\",\"parentDeptId\":\"773037\"},{\"children\":[],\"deptId\":\"773045\",\"deptName\":\"中国建材项目部\",\"parentDeptId\":\"773037\"}],\"deptId\":\"773037\",\"deptName\":\"物流业务部\",\"parentDeptId\":\"720367\"},{\"children\":[{\"children\":[],\"deptId\":\"778627\",\"deptName\":\"西北特区\",\"parentDeptId\":\"773036\"},{\"children\":[],\"deptId\":\"778629\",\"deptName\":\"湘鄂战区\",\"parentDeptId\":\"773036\"},{\"children\":[],\"deptId\":\"773040\",\"deptName\":\"华东战区\",\"parentDeptId\":\"773036\"},{\"children\":[],\"deptId\":\"773041\",\"deptName\":\"华北战区\",\"parentDeptId\":\"773036\"},{\"children\":[],\"deptId\":\"773044\",\"deptName\":\"陕晋战区\",\"parentDeptId\":\"773036\"},{\"children\":[],\"deptId\":\"773043\",\"deptName\":\"华南战区\",\"parentDeptId\":\"773036\"},{\"children\":[],\"deptId\":\"773712\",\"deptName\":\"京津冀战区\",\"parentDeptId\":\"773036\"},{\"children\":[],\"deptId\":\"773711\",\"deptName\":\"鲁豫战区\",\"parentDeptId\":\"773036\"},{\"children\":[],\"deptId\":\"773714\",\"deptName\":\"新能源项目部\",\"parentDeptId\":\"773036\"},{\"children\":[],\"deptId\":\"773713\",\"deptName\":\"搅拌半挂车项目部\",\"parentDeptId\":\"773036\"},{\"children\":[],\"deptId\":\"779112\",\"deptName\":\"西南搅拌战区\",\"parentDeptId\":\"773036\"},{\"children\":[],\"deptId\":\"779113\",\"deptName\":\"皖赣闵战区\",\"parentDeptId\":\"773036\"},{\"children\":[],\"deptId\":\"779114\",\"deptName\":\"西南粉罐战区\",\"parentDeptId\":\"773036\"}],\"deptId\":\"773036\",\"deptName\":\"车辆销售业务部\",\"parentDeptId\":\"720367\"},{\"children\":[{\"children\":[],\"deptId\":\"773039\",\"deptName\":\"委改业务部\",\"parentDeptId\":\"773038\"},{\"children\":[],\"deptId\":\"764629\",\"deptName\":\"主机部\",\"parentDeptId\":\"773038\"}],\"deptId\":\"773038\",\"deptName\":\"主机委改业务部\",\"parentDeptId\":\"720367\"},{\"children\":[],\"deptId\":\"731412\",\"deptName\":\"运营部\",\"parentDeptId\":\"720367\"},{\"children\":[{\"children\":[],\"deptId\":\"780636\",\"deptName\":\"客户拓展部\",\"parentDeptId\":\"778922\"},{\"children\":[],\"deptId\":\"779115\",\"deptName\":\"项目部\",\"parentDeptId\":\"778922\"}],\"deptId\":\"778922\",\"deptName\":\"战略客户部\",\"parentDeptId\":\"720367\"},{\"children\":[],\"deptId\":\"720488\",\"deptName\":\"金融部\",\"parentDeptId\":\"720367\"},{\"children\":[],\"deptId\":\"720487\",\"deptName\":\"产品开发部\",\"parentDeptId\":\"720367\"}],\"deptId\":\"720367\",\"deptName\":\"CE事业部\",\"parentDeptId\":\"600462\"}],\"deptId\":\"600462\",\"deptName\":\"芜湖中集瑞江汽车有限公司\",\"parentDeptId\":\"747981\"}],\"deptId\":\"747981\",\"deptName\":\"强冠罐车业务集团\",\"outerParentDeptId\":\"1\",\"parentDeptId\":\"640072\"}";
        String hrStr = "{\"children\":[{\"children\":[{\"children\":[{\"children\":[],\"deptId\":\"781626\",\"deptName\":\"质量部\",\"parentDeptId\":\"769230\"},{\"children\":[],\"deptId\":\"781625\",\"deptName\":\"售后服务部\",\"parentDeptId\":\"769230\"},{\"children\":[],\"deptId\":\"773690\",\"deptName\":\"产品开发部\",\"parentDeptId\":\"769230\"},{\"children\":[{\"children\":[],\"deptId\":\"781329\",\"deptName\":\"项目部\",\"parentDeptId\":\"773687\"},{\"children\":[],\"deptId\":\"781328\",\"deptName\":\"客户拓展部\",\"parentDeptId\":\"773687\"}],\"deptId\":\"773687\",\"deptName\":\"战略客户部\",\"parentDeptId\":\"769230\"},{\"children\":[{\"children\":[],\"deptId\":\"769257\",\"deptName\":\"华北战区\",\"parentDeptId\":\"748109\"},{\"children\":[],\"deptId\":\"769258\",\"deptName\":\"陕晋大区\",\"parentDeptId\":\"748109\"},{\"children\":[],\"deptId\":\"769255\",\"deptName\":\"河南大区\",\"parentDeptId\":\"748109\"},{\"children\":[],\"deptId\":\"769256\",\"deptName\":\"西南战区\",\"parentDeptId\":\"748109\"},{\"children\":[{\"children\":[],\"deptId\":\"782409\",\"deptName\":\"新疆大区\",\"parentDeptId\":\"769253\"},{\"children\":[],\"deptId\":\"782408\",\"deptName\":\"陕晋大区\",\"parentDeptId\":\"769253\"},{\"children\":[],\"deptId\":\"782407\",\"deptName\":\"蒙甘宁大区\",\"parentDeptId\":\"769253\"}],\"deptId\":\"769253\",\"deptName\":\"西北战区\",\"parentDeptId\":\"748109\"},{\"children\":[],\"deptId\":\"769254\",\"deptName\":\"蒙甘宁大区\",\"parentDeptId\":\"748109\"},{\"children\":[],\"deptId\":\"769252\",\"deptName\":\"两广大区\",\"parentDeptId\":\"748109\"},{\"children\":[],\"deptId\":\"769248\",\"deptName\":\"山东战区\",\"parentDeptId\":\"748109\"},{\"children\":[],\"deptId\":\"779120\",\"deptName\":\"特罐\",\"parentDeptId\":\"748109\"},{\"children\":[],\"deptId\":\"779119\",\"deptName\":\"船罐储罐\",\"parentDeptId\":\"748109\"},{\"children\":[],\"deptId\":\"774109\",\"deptName\":\"江苏大区\",\"parentDeptId\":\"748109\"},{\"children\":[],\"deptId\":\"769543\",\"deptName\":\"湘鄂大区\",\"parentDeptId\":\"748109\"},{\"children\":[],\"deptId\":\"769542\",\"deptName\":\"苏沪大区\",\"parentDeptId\":\"748109\"},{\"children\":[],\"deptId\":\"769530\",\"deptName\":\"闽浙大区\",\"parentDeptId\":\"748109\"},{\"children\":[],\"deptId\":\"771598\",\"deptName\":\"大客户部\",\"parentDeptId\":\"748109\"},{\"children\":[],\"deptId\":\"748114\",\"deptName\":\"项目部\",\"parentDeptId\":\"748109\"}],\"deptId\":\"748109\",\"deptName\":\"营销部\",\"parentDeptId\":\"769230\"},{\"children\":[],\"deptId\":\"748110\",\"deptName\":\"运营部\",\"parentDeptId\":\"769230\"}],\"deptId\":\"769230\",\"deptName\":\"LT事业部\",\"parentDeptId\":\"765049\"},{\"children\":[{\"children\":[{\"children\":[],\"deptId\":\"781985\",\"deptName\":\"计划采购部\",\"parentDeptId\":\"781984\"}],\"deptId\":\"781984\",\"deptName\":\"制造服务中心\",\"parentDeptId\":\"781983\"},{\"children\":[],\"deptId\":\"781869\",\"deptName\":\"技术服务部\",\"parentDeptId\":\"781983\"}],\"deptId\":\"781983\",\"deptName\":\"芜湖生产中心\",\"parentDeptId\":\"765049\"},{\"children\":[{\"children\":[{\"children\":[],\"deptId\":\"773066\",\"deptName\":\"主机部\",\"parentDeptId\":\"773065\"},{\"children\":[],\"deptId\":\"773067\",\"deptName\":\"委改业务部\",\"parentDeptId\":\"773065\"}],\"deptId\":\"773065\",\"deptName\":\"主机委改业务部\",\"parentDeptId\":\"769229\"},{\"children\":[{\"children\":[],\"deptId\":\"773062\",\"deptName\":\"华润水泥项目部\",\"parentDeptId\":\"773060\"},{\"children\":[],\"deptId\":\"773061\",\"deptName\":\"中国建材项目部\",\"parentDeptId\":\"773060\"},{\"children\":[],\"deptId\":\"773064\",\"deptName\":\"海螺水泥项目部\",\"parentDeptId\":\"773060\"},{\"children\":[],\"deptId\":\"773063\",\"deptName\":\"中建西部项目部\",\"parentDeptId\":\"773060\"}],\"deptId\":\"773060\",\"deptName\":\"物流业务部\",\"parentDeptId\":\"769229\"},{\"children\":[{\"children\":[],\"deptId\":\"774849\",\"deptName\":\"京津冀战区\",\"parentDeptId\":\"773054\"},{\"children\":[],\"deptId\":\"778754\",\"deptName\":\"湘鄂战区\",\"parentDeptId\":\"773054\"},{\"children\":[],\"deptId\":\"773059\",\"deptName\":\"西北特区\",\"parentDeptId\":\"773054\"},{\"children\":[],\"deptId\":\"773058\",\"deptName\":\"华南战区\",\"parentDeptId\":\"773054\"},{\"children\":[],\"deptId\":\"773055\",\"deptName\":\"华东战区\",\"parentDeptId\":\"773054\"},{\"children\":[],\"deptId\":\"773057\",\"deptName\":\"西南搅拌战区\",\"parentDeptId\":\"773054\"},{\"children\":[],\"deptId\":\"773056\",\"deptName\":\"鲁豫战区\",\"parentDeptId\":\"773054\"},{\"children\":[],\"deptId\":\"779152\",\"deptName\":\"搅拌半挂车项目部\",\"parentDeptId\":\"773054\"},{\"children\":[],\"deptId\":\"779153\",\"deptName\":\"新能源项目部\",\"parentDeptId\":\"773054\"},{\"children\":[],\"deptId\":\"779104\",\"deptName\":\"华北战区\",\"parentDeptId\":\"773054\"},{\"children\":[],\"deptId\":\"779105\",\"deptName\":\"推板罐项目组\",\"parentDeptId\":\"773054\"},{\"children\":[],\"deptId\":\"779101\",\"deptName\":\"陕晋战区\",\"parentDeptId\":\"773054\"},{\"children\":[],\"deptId\":\"779102\",\"deptName\":\"皖赣闵战区\",\"parentDeptId\":\"773054\"},{\"children\":[],\"deptId\":\"779103\",\"deptName\":\"西南粉罐战区\",\"parentDeptId\":\"773054\"}],\"deptId\":\"773054\",\"deptName\":\"车辆销售业务部\",\"parentDeptId\":\"769229\"},{\"children\":[],\"deptId\":\"781628\",\"deptName\":\"质量部\",\"parentDeptId\":\"769229\"},{\"children\":[{\"children\":[],\"deptId\":\"781871\",\"deptName\":\"客户拓展部\",\"parentDeptId\":\"781627\"},{\"children\":[],\"deptId\":\"781872\",\"deptName\":\"项目部\",\"parentDeptId\":\"781627\"}],\"deptId\":\"781627\",\"deptName\":\"战略客户部\",\"parentDeptId\":\"769229\"},{\"children\":[],\"deptId\":\"748162\",\"deptName\":\"产品开发部\",\"parentDeptId\":\"769229\"},{\"children\":[],\"deptId\":\"748123\",\"deptName\":\"运营部\",\"parentDeptId\":\"769229\"}],\"deptId\":\"769229\",\"deptName\":\"CE事业部\",\"parentDeptId\":\"765049\"},{\"children\":[{\"children\":[{\"children\":[],\"deptId\":\"778511\",\"deptName\":\"计划采购部\",\"parentDeptId\":\"748083\"},{\"children\":[],\"deptId\":\"748096\",\"deptName\":\"储运部\",\"parentDeptId\":\"748083\"},{\"children\":[],\"deptId\":\"748091\",\"deptName\":\"精益HSE部\",\"parentDeptId\":\"748083\"},{\"children\":[],\"deptId\":\"781781\",\"deptName\":\"设备部\",\"parentDeptId\":\"748083\"}],\"deptId\":\"748083\",\"deptName\":\"制造服务中心\",\"parentDeptId\":\"748075\"},{\"children\":[],\"deptId\":\"748362\",\"deptName\":\"技术服务部\",\"parentDeptId\":\"748075\"},{\"children\":[{\"children\":[{\"children\":[],\"deptId\":\"781947\",\"deptName\":\"收尾交车工作岛\",\"parentDeptId\":\"781831\"},{\"children\":[],\"deptId\":\"781945\",\"deptName\":\"管道工作岛\",\"parentDeptId\":\"781831\"},{\"children\":[],\"deptId\":\"781946\",\"deptName\":\"附件装配工作岛\",\"parentDeptId\":\"781831\"},{\"children\":[],\"deptId\":\"781944\",\"deptName\":\"电、气路工作岛\",\"parentDeptId\":\"781831\"}],\"deptId\":\"781831\",\"deptName\":\"总装工段\",\"parentDeptId\":\"765374\"},{\"children\":[{\"children\":[],\"deptId\":\"781938\",\"deptName\":\"喷粉工作岛\",\"parentDeptId\":\"774913\"},{\"children\":[],\"deptId\":\"781936\",\"deptName\":\"抛光工作岛\",\"parentDeptId\":\"774913\"},{\"children\":[],\"deptId\":\"781937\",\"deptName\":\"喷漆工作岛\",\"parentDeptId\":\"774913\"}],\"deptId\":\"774913\",\"deptName\":\"涂装工段\",\"parentDeptId\":\"765374\"},{\"children\":[{\"children\":[],\"deptId\":\"781941\",\"deptName\":\"小件拼焊工作岛\",\"parentDeptId\":\"748092\"},{\"children\":[],\"deptId\":\"781942\",\"deptName\":\"封头隔仓工作岛\",\"parentDeptId\":\"748092\"},{\"children\":[],\"deptId\":\"781940\",\"deptName\":\"剪折工作岛\",\"parentDeptId\":\"748092\"},{\"children\":[],\"deptId\":\"781943\",\"deptName\":\"备料班\",\"parentDeptId\":\"748092\"},{\"children\":[],\"deptId\":\"781939\",\"deptName\":\"数控工作岛\",\"parentDeptId\":\"748092\"}],\"deptId\":\"748092\",\"deptName\":\"下料工段\",\"parentDeptId\":\"765374\"},{\"children\":[{\"children\":[],\"deptId\":\"781934\",\"deptName\":\"筒体工作岛\",\"parentDeptId\":\"748173\"},{\"children\":[],\"deptId\":\"781935\",\"deptName\":\"附件工作岛\",\"parentDeptId\":\"748173\"}],\"deptId\":\"748173\",\"deptName\":\"特罐铆焊工段\",\"parentDeptId\":\"765374\"},{\"children\":[{\"children\":[],\"deptId\":\"781923\",\"deptName\":\"铝合金拼卷工作岛\",\"parentDeptId\":\"781810\"},{\"children\":[],\"deptId\":\"781924\",\"deptName\":\"铝合金高端生产线\",\"parentDeptId\":\"781810\"}],\"deptId\":\"781810\",\"deptName\":\"A1铆焊工段\",\"parentDeptId\":\"765374\"},{\"children\":[{\"children\":[],\"deptId\":\"781929\",\"deptName\":\"碳钢拼卷工作岛\",\"parentDeptId\":\"748117\"},{\"children\":[],\"deptId\":\"781930\",\"deptName\":\"碳钢筒体工作岛\",\"parentDeptId\":\"748117\"},{\"children\":[],\"deptId\":\"781931\",\"deptName\":\"碳钢焊接工作岛\",\"parentDeptId\":\"748117\"},{\"children\":[],\"deptId\":\"781932\",\"deptName\":\"碳钢附件工作岛\",\"parentDeptId\":\"748117\"},{\"children\":[],\"deptId\":\"781933\",\"deptName\":\"碳钢试水工作岛\",\"parentDeptId\":\"748117\"}],\"deptId\":\"748117\",\"deptName\":\"碳钢铆焊工段\",\"parentDeptId\":\"765374\"},{\"children\":[{\"children\":[],\"deptId\":\"781927\",\"deptName\":\"附件工作岛\",\"parentDeptId\":\"781811\"},{\"children\":[],\"deptId\":\"781928\",\"deptName\":\"试水工作岛\",\"parentDeptId\":\"781811\"},{\"children\":[],\"deptId\":\"781925\",\"deptName\":\"筒体工作岛\",\"parentDeptId\":\"781811\"},{\"children\":[],\"deptId\":\"781926\",\"deptName\":\"焊接工作岛\",\"parentDeptId\":\"781811\"}],\"deptId\":\"781811\",\"deptName\":\"A2铆焊工段\",\"parentDeptId\":\"765374\"}],\"deptId\":\"765374\",\"deptName\":\"LTP1\",\"parentDeptId\":\"748075\"}],\"deptId\":\"748075\",\"deptName\":\"扬州生产中心\",\"parentDeptId\":\"765049\"},{\"children\":[{\"children\":[{\"children\":[],\"deptId\":\"780342\",\"deptName\":\"东南亚一区\",\"parentDeptId\":\"748426\"},{\"children\":[],\"deptId\":\"780343\",\"deptName\":\"东南亚二区\",\"parentDeptId\":\"748426\"}],\"deptId\":\"748426\",\"deptName\":\"东南亚战区\",\"parentDeptId\":\"748353\"},{\"children\":[{\"children\":[],\"deptId\":\"780336\",\"deptName\":\"亚俄一区\",\"parentDeptId\":\"748428\"},{\"children\":[],\"deptId\":\"780337\",\"deptName\":\"亚俄二区\",\"parentDeptId\":\"748428\"}],\"deptId\":\"748428\",\"deptName\":\"亚俄战区\",\"parentDeptId\":\"748353\"},{\"children\":[{\"children\":[],\"deptId\":\"780339\",\"deptName\":\"非洲中东二区\",\"parentDeptId\":\"748427\"},{\"children\":[],\"deptId\":\"780338\",\"deptName\":\"非洲中东一区\",\"parentDeptId\":\"748427\"},{\"children\":[],\"deptId\":\"780340\",\"deptName\":\"非洲中东三区\",\"parentDeptId\":\"748427\"},{\"children\":[],\"deptId\":\"780341\",\"deptName\":\"非洲中东四区\",\"parentDeptId\":\"748427\"}],\"deptId\":\"748427\",\"deptName\":\"非洲中东战区\",\"parentDeptId\":\"748353\"},{\"children\":[],\"deptId\":\"748429\",\"deptName\":\"美澳战区\",\"parentDeptId\":\"748353\"},{\"children\":[{\"children\":[],\"deptId\":\"780327\",\"deptName\":\"售中售后服务部\",\"parentDeptId\":\"780332\"},{\"children\":[],\"deptId\":\"781780\",\"deptName\":\"产品开发部\",\"parentDeptId\":\"780332\"}],\"deptId\":\"780332\",\"deptName\":\"营销部\",\"parentDeptId\":\"748353\"},{\"children\":[],\"deptId\":\"778488\",\"deptName\":\"LAG项目部\",\"parentDeptId\":\"748353\"},{\"children\":[],\"deptId\":\"779684\",\"deptName\":\"质量部\",\"parentDeptId\":\"748353\"},{\"children\":[],\"deptId\":\"748363\",\"deptName\":\"运营部\",\"parentDeptId\":\"748353\"}],\"deptId\":\"748353\",\"deptName\":\"IC事业部\",\"parentDeptId\":\"765049\"}],\"deptId\":\"765049\",\"deptName\":\"扬州中集通华数字化罐车工厂\",\"parentDeptId\":\"747981\"},{\"children\":[{\"children\":[],\"deptId\":\"773968\",\"deptName\":\"战略管理小组\",\"parentDeptId\":\"779674\"},{\"children\":[{\"children\":[],\"deptId\":\"778384\",\"deptName\":\"模块化部\",\"parentDeptId\":\"748347\"},{\"children\":[],\"deptId\":\"748354\",\"deptName\":\"技术管理部\",\"parentDeptId\":\"748347\"},{\"children\":[],\"deptId\":\"748356\",\"deptName\":\"ME部\",\"parentDeptId\":\"748347\"},{\"children\":[],\"deptId\":\"748355\",\"deptName\":\"技术开发部\",\"parentDeptId\":\"748347\"}],\"deptId\":\"748347\",\"deptName\":\"技术中心\",\"parentDeptId\":\"779674\"},{\"children\":[{\"children\":[],\"deptId\":\"748425\",\"deptName\":\"数字流程部\",\"parentDeptId\":\"748349\"},{\"children\":[],\"deptId\":\"778943\",\"deptName\":\"质量管理部\",\"parentDeptId\":\"748349\"},{\"children\":[],\"deptId\":\"748358\",\"deptName\":\"EPS中心\",\"parentDeptId\":\"748349\"},{\"children\":[],\"deptId\":\"748357\",\"deptName\":\"精益HSE部\",\"parentDeptId\":\"748349\"}],\"deptId\":\"748349\",\"deptName\":\"质量运营中心\",\"parentDeptId\":\"779674\"},{\"children\":[{\"children\":[],\"deptId\":\"767126\",\"deptName\":\"HRBP\",\"parentDeptId\":\"748348\"},{\"children\":[],\"deptId\":\"767125\",\"deptName\":\"COE\",\"parentDeptId\":\"748348\"},{\"children\":[],\"deptId\":\"767127\",\"deptName\":\"SSC\",\"parentDeptId\":\"748348\"}],\"deptId\":\"748348\",\"deptName\":\"人力资源中心\",\"parentDeptId\":\"779674\"},{\"children\":[{\"children\":[],\"deptId\":\"767129\",\"deptName\":\"资金管理部\",\"parentDeptId\":\"748352\"},{\"children\":[],\"deptId\":\"767130\",\"deptName\":\"预算分析部\",\"parentDeptId\":\"748352\"},{\"children\":[],\"deptId\":\"767128\",\"deptName\":\"会计核算部\",\"parentDeptId\":\"748352\"},{\"children\":[],\"deptId\":\"781589\",\"deptName\":\"CE财务管理部\",\"parentDeptId\":\"748352\"},{\"children\":[],\"deptId\":\"781587\",\"deptName\":\"LT财务管理部\",\"parentDeptId\":\"748352\"},{\"children\":[],\"deptId\":\"781588\",\"deptName\":\"IC财务管理部\",\"parentDeptId\":\"748352\"}],\"deptId\":\"748352\",\"deptName\":\"财务中心\",\"parentDeptId\":\"779674\"},{\"children\":[{\"children\":[],\"deptId\":\"748360\",\"deptName\":\"市场营销部\",\"parentDeptId\":\"748351\"},{\"children\":[],\"deptId\":\"748359\",\"deptName\":\"新业务拓展部\",\"parentDeptId\":\"748351\"}],\"deptId\":\"748351\",\"deptName\":\"NR营销中心\",\"parentDeptId\":\"779674\"}],\"deptId\":\"779674\",\"deptName\":\"强冠BG\",\"parentDeptId\":\"747981\"},{\"children\":[{\"children\":[{\"children\":[{\"children\":[],\"deptId\":\"774706\",\"deptName\":\"装备技术\",\"parentDeptId\":\"781579\"},{\"children\":[],\"deptId\":\"781978\",\"deptName\":\"采购\",\"parentDeptId\":\"781579\"},{\"children\":[],\"deptId\":\"721952\",\"deptName\":\"精益HSE\",\"parentDeptId\":\"781579\"},{\"children\":[],\"deptId\":\"721951\",\"deptName\":\"行政\",\"parentDeptId\":\"781579\"},{\"children\":[],\"deptId\":\"698849\",\"deptName\":\"仓储\",\"parentDeptId\":\"781579\"}],\"deptId\":\"781579\",\"deptName\":\"制造服务中心\",\"parentDeptId\":\"748210\"},{\"children\":[{\"children\":[{\"children\":[],\"deptId\":\"698870\",\"deptName\":\"下料工作岛\",\"parentDeptId\":\"781979\"}],\"deptId\":\"781979\",\"deptName\":\"下料工段\",\"parentDeptId\":\"773668\"},{\"children\":[{\"children\":[],\"deptId\":\"698869\",\"deptName\":\"总装工作岛\",\"parentDeptId\":\"781980\"}],\"deptId\":\"781980\",\"deptName\":\"总装工段\",\"parentDeptId\":\"773668\"},{\"children\":[{\"children\":[],\"deptId\":\"698868\",\"deptName\":\"筒体工作岛\",\"parentDeptId\":\"779749\"},{\"children\":[],\"deptId\":\"698867\",\"deptName\":\"下盘工作岛\",\"parentDeptId\":\"779749\"}],\"deptId\":\"779749\",\"deptName\":\"铆焊工段\",\"parentDeptId\":\"773668\"}],\"deptId\":\"773668\",\"deptName\":\"LTP4\",\"parentDeptId\":\"748210\"}],\"deptId\":\"748210\",\"deptName\":\"梁山生产中心\",\"parentDeptId\":\"647265\"},{\"children\":[{\"children\":[],\"deptId\":\"781728\",\"deptName\":\"售后服务部\",\"parentDeptId\":\"781727\"}],\"deptId\":\"781727\",\"deptName\":\"LT事业部\",\"parentDeptId\":\"647265\"},{\"children\":[{\"children\":[],\"deptId\":\"781581\",\"deptName\":\"产品开发部\",\"parentDeptId\":\"769227\"},{\"children\":[],\"deptId\":\"781580\",\"deptName\":\"质量部\",\"parentDeptId\":\"769227\"},{\"children\":[],\"deptId\":\"698852\",\"deptName\":\"运营部\",\"parentDeptId\":\"769227\"},{\"children\":[{\"children\":[],\"deptId\":\"770400\",\"deptName\":\"华北战区\",\"parentDeptId\":\"698858\"},{\"children\":[],\"deptId\":\"770402\",\"deptName\":\"西北特区\",\"parentDeptId\":\"698858\"}],\"deptId\":\"698858\",\"deptName\":\"车辆销售业务部\",\"parentDeptId\":\"769227\"}],\"deptId\":\"769227\",\"deptName\":\"CE事业部\",\"parentDeptId\":\"647265\"},{\"children\":[{\"children\":[],\"deptId\":\"782362\",\"deptName\":\"质量部\",\"parentDeptId\":\"782310\"}],\"deptId\":\"782310\",\"deptName\":\"IC事业部\",\"parentDeptId\":\"647265\"}],\"deptId\":\"647265\",\"deptName\":\"山东万事达专用汽车制造有限公司\",\"parentDeptId\":\"747981\"},{\"children\":[{\"children\":[{\"children\":[],\"deptId\":\"781868\",\"deptName\":\"技术服务部\",\"parentDeptId\":\"782147\"}],\"deptId\":\"782147\",\"deptName\":\"芜湖生产中心\",\"parentDeptId\":\"774651\"},{\"children\":[{\"children\":[],\"deptId\":\"781743\",\"deptName\":\"技术服务部\",\"parentDeptId\":\"781741\"},{\"children\":[],\"deptId\":\"781742\",\"deptName\":\"运营部\",\"parentDeptId\":\"781741\"}],\"deptId\":\"781741\",\"deptName\":\"IC事业部\",\"parentDeptId\":\"774651\"},{\"children\":[{\"children\":[],\"deptId\":\"774700\",\"deptName\":\"产品开发部\",\"parentDeptId\":\"774697\"},{\"children\":[{\"children\":[],\"deptId\":\"777998\",\"deptName\":\"山东大区\",\"parentDeptId\":\"774701\"},{\"children\":[],\"deptId\":\"780666\",\"deptName\":\"西南战区\",\"parentDeptId\":\"774701\"},{\"children\":[],\"deptId\":\"777996\",\"deptName\":\"河南大区\",\"parentDeptId\":\"774701\"},{\"children\":[],\"deptId\":\"780669\",\"deptName\":\"渝贵大区\",\"parentDeptId\":\"774701\"},{\"children\":[],\"deptId\":\"780667\",\"deptName\":\"西北战区\",\"parentDeptId\":\"774701\"},{\"children\":[],\"deptId\":\"780668\",\"deptName\":\"川云大区\",\"parentDeptId\":\"774701\"},{\"children\":[],\"deptId\":\"780670\",\"deptName\":\"陕晋大区\",\"parentDeptId\":\"774701\"},{\"children\":[],\"deptId\":\"780671\",\"deptName\":\"蒙甘宁大区\",\"parentDeptId\":\"774701\"}],\"deptId\":\"774701\",\"deptName\":\"营销部\",\"parentDeptId\":\"774697\"},{\"children\":[],\"deptId\":\"779707\",\"deptName\":\"运营部\",\"parentDeptId\":\"774697\"},{\"children\":[],\"deptId\":\"781655\",\"deptName\":\"质量部\",\"parentDeptId\":\"774697\"}],\"deptId\":\"774697\",\"deptName\":\"LT事业部\",\"parentDeptId\":\"774651\"},{\"children\":[{\"children\":[{\"children\":[{\"children\":[],\"deptId\":\"779984\",\"deptName\":\"仓储\",\"parentDeptId\":\"781524\"},{\"children\":[],\"deptId\":\"774703\",\"deptName\":\"运营\",\"parentDeptId\":\"781524\"}],\"deptId\":\"781524\",\"deptName\":\"综合服务组\",\"parentDeptId\":\"774699\"},{\"children\":[],\"deptId\":\"781527\",\"deptName\":\"采购组\",\"parentDeptId\":\"774699\"}],\"deptId\":\"774699\",\"deptName\":\"制造服务中心\",\"parentDeptId\":\"774695\"},{\"children\":[{\"children\":[{\"children\":[],\"deptId\":\"781949\",\"deptName\":\"交车工作岛\",\"parentDeptId\":\"779847\"},{\"children\":[],\"deptId\":\"781948\",\"deptName\":\"电气路工作岛\",\"parentDeptId\":\"779847\"},{\"children\":[],\"deptId\":\"781950\",\"deptName\":\"外围工作岛\",\"parentDeptId\":\"779847\"}],\"deptId\":\"779847\",\"deptName\":\"总装工段\",\"parentDeptId\":\"774698\"},{\"children\":[{\"children\":[],\"deptId\":\"774702\",\"deptName\":\"碳钢不锈钢工作岛\",\"parentDeptId\":\"781576\"},{\"children\":[],\"deptId\":\"781530\",\"deptName\":\"铝合金工作岛\",\"parentDeptId\":\"781576\"},{\"children\":[],\"deptId\":\"781577\",\"deptName\":\"物流备料工作岛\",\"parentDeptId\":\"781576\"}],\"deptId\":\"781576\",\"deptName\":\"铆焊工段\",\"parentDeptId\":\"774698\"}],\"deptId\":\"774698\",\"deptName\":\"LTP5\",\"parentDeptId\":\"774695\"}],\"deptId\":\"774695\",\"deptName\":\"洛阳生产中心\",\"parentDeptId\":\"774651\"}],\"deptId\":\"774651\",\"deptName\":\"洛阳中集凌宇液罐工厂\",\"parentDeptId\":\"747981\"},{\"children\":[{\"children\":[{\"children\":[{\"children\":[],\"deptId\":\"778040\",\"deptName\":\"储运部\",\"parentDeptId\":\"748573\"},{\"children\":[],\"deptId\":\"748616\",\"deptName\":\"精益HSE部\",\"parentDeptId\":\"748573\"},{\"children\":[],\"deptId\":\"748613\",\"deptName\":\"设备部\",\"parentDeptId\":\"748573\"},{\"children\":[],\"deptId\":\"781692\",\"deptName\":\"计划采购部\",\"parentDeptId\":\"748573\"}],\"deptId\":\"748573\",\"deptName\":\"制造服务中心\",\"parentDeptId\":\"748570\"},{\"children\":[{\"children\":[{\"children\":[],\"deptId\":\"779048\",\"deptName\":\"焊接工作岛\",\"parentDeptId\":\"781958\"},{\"children\":[],\"deptId\":\"779041\",\"deptName\":\"包罐工作岛\",\"parentDeptId\":\"781958\"},{\"children\":[],\"deptId\":\"779044\",\"deptName\":\"卷板工作岛\",\"parentDeptId\":\"781958\"}],\"deptId\":\"781958\",\"deptName\":\"标罐工段\",\"parentDeptId\":\"766087\"},{\"children\":[{\"children\":[],\"deptId\":\"779017\",\"deptName\":\"LAG工作岛\",\"parentDeptId\":\"779016\"}],\"deptId\":\"779016\",\"deptName\":\"LAG工段\",\"parentDeptId\":\"766087\"},{\"children\":[{\"children\":[],\"deptId\":\"779062\",\"deptName\":\"铝合金工作岛\",\"parentDeptId\":\"781882\"},{\"children\":[],\"deptId\":\"781959\",\"deptName\":\"管道工作岛\",\"parentDeptId\":\"781882\"},{\"children\":[],\"deptId\":\"779081\",\"deptName\":\"小件工作岛\",\"parentDeptId\":\"781882\"},{\"children\":[],\"deptId\":\"779080\",\"deptName\":\"焊接工作岛\",\"parentDeptId\":\"781882\"},{\"children\":[],\"deptId\":\"779075\",\"deptName\":\"试水工作岛\",\"parentDeptId\":\"781882\"}],\"deptId\":\"781882\",\"deptName\":\"非标工段\",\"parentDeptId\":\"766087\"},{\"children\":[{\"children\":[],\"deptId\":\"779068\",\"deptName\":\"喷粉工作岛\",\"parentDeptId\":\"766102\"}],\"deptId\":\"766102\",\"deptName\":\"喷粉工段\",\"parentDeptId\":\"766087\"},{\"children\":[{\"children\":[],\"deptId\":\"779064\",\"deptName\":\"配送工作岛\",\"parentDeptId\":\"766101\"},{\"children\":[],\"deptId\":\"781960\",\"deptName\":\"收尾工作岛\",\"parentDeptId\":\"766101\"},{\"children\":[],\"deptId\":\"779040\",\"deptName\":\"售后服务岛\",\"parentDeptId\":\"766101\"},{\"children\":[],\"deptId\":\"773139\",\"deptName\":\"装箱发运工作岛\",\"parentDeptId\":\"766101\"},{\"children\":[],\"deptId\":\"764642\",\"deptName\":\"保温附件工作岛\",\"parentDeptId\":\"766101\"},{\"children\":[],\"deptId\":\"779085\",\"deptName\":\"总装工作岛\",\"parentDeptId\":\"766101\"}],\"deptId\":\"766101\",\"deptName\":\"总装工段\",\"parentDeptId\":\"766087\"}],\"deptId\":\"766087\",\"deptName\":\"LTP2\",\"parentDeptId\":\"748570\"},{\"children\":[{\"children\":[{\"children\":[],\"deptId\":\"782361\",\"deptName\":\"推板罐工作岛\",\"parentDeptId\":\"781879\"},{\"children\":[],\"deptId\":\"779056\",\"deptName\":\"罐体总成工作岛\",\"parentDeptId\":\"781879\"},{\"children\":[],\"deptId\":\"779059\",\"deptName\":\"节圆总成工作岛\",\"parentDeptId\":\"781879\"},{\"children\":[],\"deptId\":\"779051\",\"deptName\":\"车架工作岛\",\"parentDeptId\":\"781879\"},{\"children\":[],\"deptId\":\"766110\",\"deptName\":\"机动工作岛\",\"parentDeptId\":\"781879\"},{\"children\":[],\"deptId\":\"772447\",\"deptName\":\"改制工作岛\",\"parentDeptId\":\"781879\"}],\"deptId\":\"781879\",\"deptName\":\"铆焊工段\",\"parentDeptId\":\"766088\"},{\"children\":[{\"children\":[],\"deptId\":\"779073\",\"deptName\":\"喷粉工作岛\",\"parentDeptId\":\"781880\"}],\"deptId\":\"781880\",\"deptName\":\"喷粉工段\",\"parentDeptId\":\"766088\"},{\"children\":[{\"children\":[],\"deptId\":\"764613\",\"deptName\":\"车架装配工作岛\",\"parentDeptId\":\"766120\"},{\"children\":[],\"deptId\":\"764615\",\"deptName\":\"总装装配工作岛\",\"parentDeptId\":\"766120\"}],\"deptId\":\"766120\",\"deptName\":\"总装工段\",\"parentDeptId\":\"766088\"},{\"children\":[{\"children\":[],\"deptId\":\"779057\",\"deptName\":\"剪折钻冲工作岛\",\"parentDeptId\":\"768989\"},{\"children\":[],\"deptId\":\"779076\",\"deptName\":\"开平激光工作岛\",\"parentDeptId\":\"768989\"}],\"deptId\":\"768989\",\"deptName\":\"下料工段\",\"parentDeptId\":\"766088\"}],\"deptId\":\"766088\",\"deptName\":\"LTP3\",\"parentDeptId\":\"748570\"},{\"children\":[],\"deptId\":\"778930\",\"deptName\":\"技术服务部\",\"parentDeptId\":\"748570\"}],\"deptId\":\"748570\",\"deptName\":\"芜湖生产中心\",\"parentDeptId\":\"600462\"},{\"children\":[{\"children\":[{\"children\":[],\"deptId\":\"781736\",\"deptName\":\"东南亚战区一区\",\"parentDeptId\":\"781732\"},{\"children\":[],\"deptId\":\"781737\",\"deptName\":\"东南亚战区二区\",\"parentDeptId\":\"781732\"}],\"deptId\":\"781732\",\"deptName\":\"东南亚战区\",\"parentDeptId\":\"781729\"},{\"children\":[{\"children\":[],\"deptId\":\"781738\",\"deptName\":\"亚俄战区一区\",\"parentDeptId\":\"781733\"},{\"children\":[],\"deptId\":\"781739\",\"deptName\":\"亚俄战区二区\",\"parentDeptId\":\"781733\"}],\"deptId\":\"781733\",\"deptName\":\"亚俄战区\",\"parentDeptId\":\"781729\"},{\"children\":[],\"deptId\":\"781731\",\"deptName\":\"LAG项目部\",\"parentDeptId\":\"781729\"},{\"children\":[{\"children\":[],\"deptId\":\"781740\",\"deptName\":\"非洲中东战区一区\",\"parentDeptId\":\"781734\"},{\"children\":[],\"deptId\":\"781987\",\"deptName\":\"非洲中东战区二区\",\"parentDeptId\":\"781734\"}],\"deptId\":\"781734\",\"deptName\":\"非洲中东战区\",\"parentDeptId\":\"781729\"},{\"children\":[],\"deptId\":\"781735\",\"deptName\":\"运营部\",\"parentDeptId\":\"781729\"},{\"children\":[{\"children\":[],\"deptId\":\"781982\",\"deptName\":\"售后售中服务部\",\"parentDeptId\":\"781981\"}],\"deptId\":\"781981\",\"deptName\":\"营销部\",\"parentDeptId\":\"781729\"}],\"deptId\":\"781729\",\"deptName\":\"IC事业部\",\"parentDeptId\":\"600462\"},{\"children\":[],\"deptId\":\"720388\",\"deptName\":\"数字运营中心\",\"parentDeptId\":\"600462\"},{\"children\":[{\"children\":[],\"deptId\":\"781975\",\"deptName\":\"技术服务部\",\"parentDeptId\":\"781915\"},{\"children\":[],\"deptId\":\"781976\",\"deptName\":\"计划采购部\",\"parentDeptId\":\"781915\"},{\"children\":[{\"children\":[],\"deptId\":\"781977\",\"deptName\":\"总装工段\",\"parentDeptId\":\"781916\"}],\"deptId\":\"781916\",\"deptName\":\"LTP1\",\"parentDeptId\":\"781915\"}],\"deptId\":\"781915\",\"deptName\":\"扬州生产中心\",\"parentDeptId\":\"600462\"},{\"children\":[{\"children\":[],\"deptId\":\"781654\",\"deptName\":\"质量部\",\"parentDeptId\":\"720368\"},{\"children\":[],\"deptId\":\"768688\",\"deptName\":\"售后服务部\",\"parentDeptId\":\"720368\"},{\"children\":[],\"deptId\":\"730947\",\"deptName\":\"运营部\",\"parentDeptId\":\"720368\"},{\"children\":[{\"children\":[],\"deptId\":\"780909\",\"deptName\":\"客户拓展部\",\"parentDeptId\":\"773672\"},{\"children\":[],\"deptId\":\"780910\",\"deptName\":\"项目部\",\"parentDeptId\":\"773672\"}],\"deptId\":\"773672\",\"deptName\":\"战略客户部\",\"parentDeptId\":\"720368\"},{\"children\":[{\"children\":[],\"deptId\":\"764656\",\"deptName\":\"河南大区\",\"parentDeptId\":\"720493\"},{\"children\":[],\"deptId\":\"764655\",\"deptName\":\"湘鄂大区\",\"parentDeptId\":\"720493\"},{\"children\":[],\"deptId\":\"764654\",\"deptName\":\"西南大区\",\"parentDeptId\":\"720493\"},{\"children\":[],\"deptId\":\"764658\",\"deptName\":\"闽浙大区\",\"parentDeptId\":\"720493\"},{\"children\":[],\"deptId\":\"764646\",\"deptName\":\"苏沪大区\",\"parentDeptId\":\"720493\"},{\"children\":[],\"deptId\":\"764645\",\"deptName\":\"吉黑大区\",\"parentDeptId\":\"720493\"},{\"children\":[],\"deptId\":\"764644\",\"deptName\":\"华北大区\",\"parentDeptId\":\"720493\"},{\"children\":[],\"deptId\":\"764647\",\"deptName\":\"两广大区\",\"parentDeptId\":\"720493\"},{\"children\":[],\"deptId\":\"764653\",\"deptName\":\"西北大区\",\"parentDeptId\":\"720493\"},{\"children\":[],\"deptId\":\"764652\",\"deptName\":\"皖赣大区\",\"parentDeptId\":\"720493\"},{\"children\":[],\"deptId\":\"764650\",\"deptName\":\"山东大区\",\"parentDeptId\":\"720493\"},{\"children\":[],\"deptId\":\"778910\",\"deptName\":\"山东战区\",\"parentDeptId\":\"720493\"},{\"children\":[{\"children\":[],\"deptId\":\"779753\",\"deptName\":\"船罐项目\",\"parentDeptId\":\"778915\"},{\"children\":[],\"deptId\":\"779754\",\"deptName\":\"特罐项目\",\"parentDeptId\":\"778915\"}],\"deptId\":\"778915\",\"deptName\":\"项目部\",\"parentDeptId\":\"720493\"},{\"children\":[{\"children\":[],\"deptId\":\"779151\",\"deptName\":\"渝贵大区\",\"parentDeptId\":\"778909\"},{\"children\":[],\"deptId\":\"779150\",\"deptName\":\"川云大区\",\"parentDeptId\":\"778909\"}],\"deptId\":\"778909\",\"deptName\":\"西南战区\",\"parentDeptId\":\"720493\"},{\"children\":[{\"children\":[],\"deptId\":\"779148\",\"deptName\":\"东北大区\",\"parentDeptId\":\"778908\"},{\"children\":[],\"deptId\":\"779149\",\"deptName\":\"京津冀大区\",\"parentDeptId\":\"778908\"}],\"deptId\":\"778908\",\"deptName\":\"华北战区\",\"parentDeptId\":\"720493\"},{\"children\":[{\"children\":[],\"deptId\":\"779147\",\"deptName\":\"蒙甘宁大区\",\"parentDeptId\":\"778907\"},{\"children\":[],\"deptId\":\"764657\",\"deptName\":\"新疆大区\",\"parentDeptId\":\"778907\"},{\"children\":[],\"deptId\":\"764651\",\"deptName\":\"陕晋大区\",\"parentDeptId\":\"778907\"}],\"deptId\":\"778907\",\"deptName\":\"西北战区\",\"parentDeptId\":\"720493\"}],\"deptId\":\"720493\",\"deptName\":\"营销部\",\"parentDeptId\":\"720368\"},{\"children\":[],\"deptId\":\"720497\",\"deptName\":\"产品开发部\",\"parentDeptId\":\"720368\"}],\"deptId\":\"720368\",\"deptName\":\"LT事业部\",\"parentDeptId\":\"600462\"},{\"children\":[{\"children\":[],\"deptId\":\"781841\",\"deptName\":\"质量部\",\"parentDeptId\":\"720367\"},{\"children\":[{\"children\":[],\"deptId\":\"773048\",\"deptName\":\"海螺水泥项目部\",\"parentDeptId\":\"773037\"},{\"children\":[],\"deptId\":\"773047\",\"deptName\":\"中建西部项目部\",\"parentDeptId\":\"773037\"},{\"children\":[],\"deptId\":\"773046\",\"deptName\":\"华润水泥项目部\",\"parentDeptId\":\"773037\"},{\"children\":[],\"deptId\":\"773045\",\"deptName\":\"中国建材项目部\",\"parentDeptId\":\"773037\"}],\"deptId\":\"773037\",\"deptName\":\"物流业务部\",\"parentDeptId\":\"720367\"},{\"children\":[{\"children\":[],\"deptId\":\"778627\",\"deptName\":\"西北特区\",\"parentDeptId\":\"773036\"},{\"children\":[],\"deptId\":\"778629\",\"deptName\":\"湘鄂战区\",\"parentDeptId\":\"773036\"},{\"children\":[],\"deptId\":\"773040\",\"deptName\":\"华东战区\",\"parentDeptId\":\"773036\"},{\"children\":[],\"deptId\":\"773041\",\"deptName\":\"华北战区\",\"parentDeptId\":\"773036\"},{\"children\":[],\"deptId\":\"773044\",\"deptName\":\"陕晋战区\",\"parentDeptId\":\"773036\"},{\"children\":[],\"deptId\":\"773043\",\"deptName\":\"华南战区\",\"parentDeptId\":\"773036\"},{\"children\":[],\"deptId\":\"773712\",\"deptName\":\"京津冀战区\",\"parentDeptId\":\"773036\"},{\"children\":[],\"deptId\":\"773711\",\"deptName\":\"鲁豫战区\",\"parentDeptId\":\"773036\"},{\"children\":[],\"deptId\":\"773714\",\"deptName\":\"新能源项目部\",\"parentDeptId\":\"773036\"},{\"children\":[],\"deptId\":\"773713\",\"deptName\":\"搅拌半挂车项目部\",\"parentDeptId\":\"773036\"},{\"children\":[],\"deptId\":\"779112\",\"deptName\":\"西南搅拌战区\",\"parentDeptId\":\"773036\"},{\"children\":[],\"deptId\":\"779113\",\"deptName\":\"皖赣闵战区\",\"parentDeptId\":\"773036\"},{\"children\":[],\"deptId\":\"779114\",\"deptName\":\"西南粉罐战区\",\"parentDeptId\":\"773036\"}],\"deptId\":\"773036\",\"deptName\":\"车辆销售业务部\",\"parentDeptId\":\"720367\"},{\"children\":[{\"children\":[],\"deptId\":\"773039\",\"deptName\":\"委改业务部\",\"parentDeptId\":\"773038\"},{\"children\":[],\"deptId\":\"764629\",\"deptName\":\"主机部\",\"parentDeptId\":\"773038\"}],\"deptId\":\"773038\",\"deptName\":\"主机委改业务部\",\"parentDeptId\":\"720367\"},{\"children\":[],\"deptId\":\"731412\",\"deptName\":\"运营部\",\"parentDeptId\":\"720367\"},{\"children\":[{\"children\":[],\"deptId\":\"780636\",\"deptName\":\"客户拓展部\",\"parentDeptId\":\"778922\"},{\"children\":[],\"deptId\":\"779115\",\"deptName\":\"项目部\",\"parentDeptId\":\"778922\"}],\"deptId\":\"778922\",\"deptName\":\"战略客户部\",\"parentDeptId\":\"720367\"},{\"children\":[],\"deptId\":\"720488\",\"deptName\":\"金融部\",\"parentDeptId\":\"720367\"},{\"children\":[],\"deptId\":\"720487\",\"deptName\":\"产品开发部\",\"parentDeptId\":\"720367\"}],\"deptId\":\"720367\",\"deptName\":\"CE事业部\",\"parentDeptId\":\"600462\"}],\"deptId\":\"600462\",\"deptName\":\"芜湖中集瑞江汽车有限公司\",\"parentDeptId\":\"747981\"}],\"deptId\":\"747981000\",\"deptName\":\"强冠罐车业务集团\",\"outerParentDeptId\":\"1\",\"parentDeptId\":\"640072\"}";

//        ZhongjiDepartment dingTalkDepartment = JSON.parseObject(dingTalkStr, ZhongjiDepartment.class);
//        ZhongjiDepartment hrDepartment = JSON.parseObject(hrStr, ZhongjiDepartment.class);
//        if (dingTalkDepartment == null) {
//            compareAndProcess(dingTalkDepartment, hrDepartment);
//        } else if (!hrDepartment.getDeptId().equals(dingTalkDepartment.getDeptId())) {
//            compareAndProcess(dingTalkDepartment, null);
//        }
//        Map<String, String> data = new HashMap<>();
//        buildPath(dingTalkDepartment, null, data);
        //只需要找到父节点改变的就行

        new DingTalkServiceImpl().batchUpdateDepartments();

        System.out.println(1);
    }


}

