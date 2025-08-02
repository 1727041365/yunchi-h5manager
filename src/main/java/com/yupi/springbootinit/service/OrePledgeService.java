package com.yupi.springbootinit.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yupi.springbootinit.model.entity.OrePledge;
import com.yupi.springbootinit.model.vo.OrePledgeVo;

public interface OrePledgeService extends IService<OrePledge> {
    OrePledgeVo getDate();
}
