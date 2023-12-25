package com.zhongji.simpleshelf.dao.service;

import com.zhongji.simpleshelf.dao.domain.StatisticsSummary;
import com.zhongji.simpleshelf.dao.domain.erpsid.CwflNew;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.Date;
import java.util.List;

/**
 * @author gongjie
 * @description 针对表【CWFL_NEW】的数据库操作Service
 * @createDate 2023-12-22 13:50:25
 */
public interface CwflNewService extends IService<CwflNew> {

    public List<StatisticsSummary> listErpSidSummary(Date startDate, Date endDate);

}
