package com.yupi.springbootinit.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yupi.springbootinit.model.entity.Market;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface MarketMapper extends BaseMapper<Market> {
}