package com.yupi.springbootinit.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yupi.springbootinit.mapper.TurtleResultMapper;
import com.yupi.springbootinit.model.entity.TurtleResult;
import com.yupi.springbootinit.service.TurtlerResultService;
import org.springframework.stereotype.Service;

@Service
public class TurtlerResultServiceImpl extends ServiceImpl<TurtleResultMapper, TurtleResult> implements TurtlerResultService {
}
