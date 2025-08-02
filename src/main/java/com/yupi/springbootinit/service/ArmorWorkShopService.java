package com.yupi.springbootinit.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yupi.springbootinit.model.entity.ArmorWorkShop;
import com.yupi.springbootinit.model.vo.ArmorWorkShopVo;

public interface ArmorWorkShopService extends IService<ArmorWorkShop> {
    ArmorWorkShopVo getAromrDate();

}
