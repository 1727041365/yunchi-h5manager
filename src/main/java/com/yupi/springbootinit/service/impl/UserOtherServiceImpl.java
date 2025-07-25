package com.yupi.springbootinit.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yupi.springbootinit.mapper.OtherMapper;
import com.yupi.springbootinit.model.entity.OtherUser;
import com.yupi.springbootinit.service.UserOtherService;
import org.springframework.stereotype.Service;

@Service
public class UserOtherServiceImpl extends ServiceImpl<OtherMapper, OtherUser> implements UserOtherService {
}
