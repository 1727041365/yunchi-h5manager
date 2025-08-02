package com.yupi.springbootinit.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yupi.springbootinit.mapper.ArmorWorkShopMapper;
import com.yupi.springbootinit.model.entity.ArmorWorkShop;
import com.yupi.springbootinit.model.vo.ArmorWorkShopVo;
import com.yupi.springbootinit.service.ArmorWorkShopService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class ArmorWorkShopServiceImpl extends ServiceImpl<ArmorWorkShopMapper, ArmorWorkShop> implements ArmorWorkShopService {

    @Override
    public ArmorWorkShopVo getAromrDate() {
        ArmorWorkShopVo armorWorkShopVo = new ArmorWorkShopVo();
        ArmorWorkShop one = this.getOne(Wrappers.<ArmorWorkShop>lambdaQuery().orderByDesc(ArmorWorkShop::getCreateTime), false);
      BeanUtils.copyProperties(one,armorWorkShopVo);
      armorWorkShopVo.setDate(new Date());
        return armorWorkShopVo;
    }
}
