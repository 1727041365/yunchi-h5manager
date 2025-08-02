package com.yupi.springbootinit.controller.indexController;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.yupi.springbootinit.common.BaseResponse;
import com.yupi.springbootinit.common.ResultUtils;
import com.yupi.springbootinit.model.entity.TurtleDart;
import com.yupi.springbootinit.model.vo.TurtleDartVo;
import com.yupi.springbootinit.service.TurtleDartService;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.client.Response;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/turtleDart")
@Slf4j
public class TurtleDartController {
    @Resource
    private TurtleDartService turtleDartService;

    @GetMapping("list")
    public BaseResponse< List<TurtleDartVo> > getTurtleDartList() throws JsonProcessingException {
        List<TurtleDart> one = turtleDartService.list(Wrappers.lambdaQuery(TurtleDart.class).orderByDesc(TurtleDart::getCreateTime).last("limit 6"));
        List<TurtleDartVo> result = one.stream().map(item -> {
            TurtleDartVo turtleDartVo = new TurtleDartVo();
            BeanUtils.copyProperties(item, turtleDartVo);
            return turtleDartVo;
        }).collect(Collectors.toList());
        return ResultUtils.success(result);
    }



    /**
     * 定时任务获取乌龟总量和价格
     */
    @GetMapping("test")
    public void getTurtleDart() throws JsonProcessingException {
        turtleDartService.getTurtleDartAbord();
        try {
            Thread.sleep(1000+new Random().nextInt(200)); // 线程暂停 1000毫秒（即 1 秒）
            turtleDartService.getTurtleDartCn();
        } catch (InterruptedException e) {
            e.printStackTrace();
            // 当线程在睡眠时被中断，会抛出此异常
        }
    }
}