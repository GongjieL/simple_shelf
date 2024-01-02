package com.zhongji.simpleshelf.dao.service.impl;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhongji.simpleshelf.dao.domain.dingtalk.DingTalkDepartmentSynSnapshot;
import com.zhongji.simpleshelf.dao.service.DingTalkDepartmentSynSnapshotService;
import com.zhongji.simpleshelf.dao.mapper.dingtalk.DingTalkDepartmentSynSnapshotMapper;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author gongjie
 * @description 针对表【ding_talk_department_syn_snapshot】的数据库操作Service实现
 * @createDate 2023-12-29 21:56:56
 */
@Service
@DS("local")
public class DingTalkDepartmentSynSnapshotServiceImpl extends ServiceImpl<DingTalkDepartmentSynSnapshotMapper, DingTalkDepartmentSynSnapshot>
        implements DingTalkDepartmentSynSnapshotService {
    public DingTalkDepartmentSynSnapshot getLatestOne() {

        QueryWrapper<DingTalkDepartmentSynSnapshot> queryWrapper = new QueryWrapper<>();
        queryWrapper.lt("updated_at", new
                Date());
        queryWrapper.orderByDesc("updated_at");
        return getOne(
                queryWrapper);
    }


    public Boolean updateDingTalkCallBack(String outerMap,
                                          String modifiedData,
                                          Long id) {
        DingTalkDepartmentSynSnapshot updated = new DingTalkDepartmentSynSnapshot();
        updated.setId(id);
        updated.setModifiedData(modifiedData);
        updated.setDingTalkMap(outerMap);
        updateById(updated);
        return true;
    }
}




