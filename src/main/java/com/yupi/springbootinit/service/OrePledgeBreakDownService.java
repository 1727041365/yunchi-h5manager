package com.yupi.springbootinit.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yupi.springbootinit.model.entity.OrePledgeBreakDown;
import com.yupi.springbootinit.model.vo.OrePledgeBreakDownVo;

public interface OrePledgeBreakDownService extends IService<OrePledgeBreakDown> {
    Page<OrePledgeBreakDownVo> pageQuery(int pageNum, int pageSize);
}
