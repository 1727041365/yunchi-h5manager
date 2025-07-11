package com.yupi.springbootinit.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yupi.springbootinit.mapper.HotMapper;
import com.yupi.springbootinit.model.entity.Hot;
import com.yupi.springbootinit.service.HotService;
import org.springframework.stereotype.Service;

@Service
public class HotServiceImpl extends ServiceImpl<HotMapper, Hot> implements HotService {


}
