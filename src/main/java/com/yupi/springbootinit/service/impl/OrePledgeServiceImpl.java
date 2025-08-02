package com.yupi.springbootinit.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yupi.springbootinit.mapper.OrePledgeBreakDownMapper;
import com.yupi.springbootinit.mapper.OrePledgeMapper;
import com.yupi.springbootinit.model.entity.OrePledge;
import com.yupi.springbootinit.model.vo.OrePledgeVo;
import com.yupi.springbootinit.service.OrePledgeService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

@Service
public class OrePledgeServiceImpl extends ServiceImpl<OrePledgeMapper, OrePledge> implements OrePledgeService {
    @Resource
    private OrePledgeMapper orePledgeMapper;
    @Resource
    private OrePledgeBreakDownMapper orePledgeBreakDownMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrePledgeVo getDate() {
        OrePledge orePledgesToday = orePledgeMapper.selectTodayData();
        OrePledgeVo orePledgeTodayVo = new OrePledgeVo();
        BeanUtils.copyProperties(orePledgesToday, orePledgeTodayVo);
        return orePledgeTodayVo;
    }


}
