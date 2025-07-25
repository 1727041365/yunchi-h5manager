package com.yupi.springbootinit.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yupi.springbootinit.model.entity.SpiritMineDescription;

import java.text.ParseException;

public interface SpiritMineDescriptionService extends IService<SpiritMineDescription> {
    void saveOrUpdateDetail();
}
