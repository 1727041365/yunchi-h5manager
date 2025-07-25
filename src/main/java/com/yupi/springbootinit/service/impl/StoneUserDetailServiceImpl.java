package com.yupi.springbootinit.service.impl;

import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yupi.springbootinit.mapper.StoneUserDetailMapper;
import com.yupi.springbootinit.model.entity.StoneUserDetail;
import com.yupi.springbootinit.service.PostThumbService;
import com.yupi.springbootinit.service.StoneUserDetailService;
import org.springframework.stereotype.Service;

@Service
public class StoneUserDetailServiceImpl extends ServiceImpl<StoneUserDetailMapper, StoneUserDetail> implements StoneUserDetailService {
}
