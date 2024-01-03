package com.zhongji.simpleshelf.dao.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhongji.simpleshelf.common.enums.BiStatisticsSummaryEnum;
import com.zhongji.simpleshelf.dao.domain.StatisticsSummary;
import com.zhongji.simpleshelf.dao.domain.erpsid.CwflNew;
import com.zhongji.simpleshelf.dao.mapper.erpsid.CwflNewMapper;
import com.zhongji.simpleshelf.dao.service.CwflNewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author gongjie
 * @description 针对表【CWFL_NEW】的数据库操作Service实现
 * @createDate 2023-12-22 13:50:25
 */
@Service
public class CwflNewServiceImpl extends ServiceImpl<CwflNewMapper, CwflNew>
        implements CwflNewService {

    @Autowired
    private CwflNewMapper cwflNewMapper;

    @Override
    public List<StatisticsSummary> listErpSidSummary(Date startDate, Date endDate) {
        List<StatisticsSummary> statisticsSummaries = cwflNewMapper.listErpSidSummary(startDate, endDate);
        if (statisticsSummaries == null) {
            return null;
        }
        statisticsSummaries.forEach(s -> {
            s.setSubTypName(
                    BiStatisticsSummaryEnum.getByCode(s.getSubType()).getName()
            );
            s.setSubType(
                    BiStatisticsSummaryEnum.getByCode(s.getSubType()).getSubType()
            );
        });
        //排序
        statisticsSummaries.sort((s1,s2)->BiStatisticsSummaryEnum.getByCode(s1.getSubType()).getOrder()
                .compareTo(BiStatisticsSummaryEnum.getByCode(s2.getSubType()).getOrder()));
        //暂时去除other
        statisticsSummaries = statisticsSummaries.stream().filter(statisticsSummary ->
                !BiStatisticsSummaryEnum.OTHER.getSubType().equals(statisticsSummary.getSubType())).collect(Collectors.toList());
        return statisticsSummaries;
    }
}




