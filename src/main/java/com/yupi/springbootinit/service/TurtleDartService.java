package com.yupi.springbootinit.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.yupi.springbootinit.model.entity.TurtleDart;

public interface TurtleDartService extends IService<TurtleDart> {
    void getTurtleDartAbord() throws JsonProcessingException;

    void getTurtleDartCn() throws JsonProcessingException;
}
