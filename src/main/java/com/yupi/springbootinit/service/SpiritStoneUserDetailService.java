package com.yupi.springbootinit.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yupi.springbootinit.model.entity.OtherUser;
import com.yupi.springbootinit.model.entity.SpiritStoneUserDetail;
import com.yupi.springbootinit.model.vo.StoneQuantityDetailVo;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface SpiritStoneUserDetailService  extends IService<SpiritStoneUserDetail> {
    /**
     * 获取全网用户数据
     */
    StoneQuantityDetailVo getDate();
    CompletableFuture<Boolean> addOrUpdateDeta();
}
