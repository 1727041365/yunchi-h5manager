package com.yupi.springbootinit.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yupi.springbootinit.mapper.OrePledgeBreakDownMapper;
import com.yupi.springbootinit.model.entity.OrePledgeBreakDown;
import com.yupi.springbootinit.model.vo.OrePledgeBreakDownVo;
import com.yupi.springbootinit.service.OrePledgeBreakDownService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrePledgeBreakDownServiceImpl extends ServiceImpl<OrePledgeBreakDownMapper, OrePledgeBreakDown> implements OrePledgeBreakDownService {
    @Override
    public Page<OrePledgeBreakDownVo> pageQuery(int pageNum, int pageSize) {
        // 1. 分页查询实体数据，按 createTime 升序（从老到新）排序
        Page<OrePledgeBreakDown> entityPage = new Page<>(pageNum, pageSize);
        QueryWrapper<OrePledgeBreakDown> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByAsc("create_time");
        baseMapper.selectPage(entityPage, queryWrapper);
        // 2. 将实体分页数据转换为 VO 分页数据
        List<OrePledgeBreakDownVo> voList = entityPage.getRecords().stream()
                .map(this::convertEntityToVo)
                .collect(Collectors.toList());
        Page<OrePledgeBreakDownVo> voPage = new Page<>();
        voPage.setCurrent(entityPage.getCurrent());
        voPage.setSize(entityPage.getSize());
        voPage.setTotal(entityPage.getTotal());
        voPage.setRecords(voList);
        return voPage;
    }

    private OrePledgeBreakDownVo convertEntityToVo(OrePledgeBreakDown entity) {
        OrePledgeBreakDownVo vo = new OrePledgeBreakDownVo();
        vo.setCreateTime(new Date());
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }
}
