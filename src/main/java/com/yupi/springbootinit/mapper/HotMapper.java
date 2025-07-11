package com.yupi.springbootinit.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yupi.springbootinit.model.entity.Hot;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface HotMapper extends BaseMapper<Hot> {
}