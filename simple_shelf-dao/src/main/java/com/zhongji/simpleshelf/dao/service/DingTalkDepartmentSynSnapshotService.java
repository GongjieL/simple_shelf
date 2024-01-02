package com.zhongji.simpleshelf.dao.service;

import com.zhongji.simpleshelf.dao.domain.dingtalk.DingTalkDepartmentSynSnapshot;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * @author gongjie
 * @description 针对表【ding_talk_department_syn_snapshot】的数据库操作Service
 * @createDate 2023-12-29 21:56:56
 */
public interface DingTalkDepartmentSynSnapshotService extends IService<DingTalkDepartmentSynSnapshot> {

    public abstract DingTalkDepartmentSynSnapshot getLatestOne();
}
