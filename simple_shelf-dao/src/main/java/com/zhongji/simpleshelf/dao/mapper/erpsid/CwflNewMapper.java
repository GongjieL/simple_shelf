package com.zhongji.simpleshelf.dao.mapper.erpsid;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.zhongji.simpleshelf.dao.domain.erpsid.CwflNew;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
* @author gongjie
* @description 针对表【CWFL_NEW】的数据库操作Mapper
* @createDate 2023-12-22 13:50:25
* @Entity com.gjie.kgboot.dao.domain.CwflNew
*/
@Mapper
@DS("zhongji1")
public interface CwflNewMapper extends BaseMapper<CwflNew> {
    List<CwflNew> listSomeB(String a);

}




