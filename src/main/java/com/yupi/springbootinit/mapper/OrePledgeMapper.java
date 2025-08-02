package com.yupi.springbootinit.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yupi.springbootinit.model.entity.OrePledge;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

@Mapper
public interface OrePledgeMapper extends BaseMapper<OrePledge> {
    // 查询当天的数据
    OrePledge selectTodayData();
    // 查询30天前当天的数据（日期相等）
    OrePledge selectDataExactlyAgo(@Param("exactDate") Date exactDate);
}
