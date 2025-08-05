package com.yupi.springbootinit.controller.indexController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.yupi.springbootinit.common.BaseResponse;
import com.yupi.springbootinit.common.ResultUtils;
import com.yupi.springbootinit.model.vo.TurtlerResultVo;
import com.yupi.springbootinit.service.TurtleDartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@RestController
@RequestMapping("/turtleDart123")
@Slf4j
public class TurtleResultController {
    @Autowired
    private TurtleDartService turtleDartService;

    @GetMapping("list")
    public BaseResponse<List<TurtlerResultVo>> getTurtleDartList() throws JsonProcessingException {
        TurtlerResultVo turtlerResultVo = new TurtlerResultVo();
        List<TurtlerResultVo> turtlerResultVoList = new ArrayList<>();
        return ResultUtils.success(turtlerResultVoList);
    }

    /**
     * 定时任务获取乌龟总量和价格
     */
    @GetMapping("test")
    public void getTurtleDart() throws JsonProcessingException {
            turtleDartService.getTurtleDartCn();
    }
}
