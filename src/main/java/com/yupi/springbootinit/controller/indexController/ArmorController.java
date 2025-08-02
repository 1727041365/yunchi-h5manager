package com.yupi.springbootinit.controller.indexController;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yupi.springbootinit.common.BaseResponse;
import com.yupi.springbootinit.common.ResultUtils;
import com.yupi.springbootinit.model.entity.ArmorWorkShop;
import com.yupi.springbootinit.model.vo.ArmorWorkShopVo;
import com.yupi.springbootinit.service.ArmorWorkShopService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/armorWorkShop")
public class ArmorController {
    @Resource
    ArmorWorkShopService armorWorkShopService;
        @GetMapping("get")
    public BaseResponse<ArmorWorkShopVo> getArmorWorkShop() {
            ArmorWorkShopVo armorWorkShopVo= armorWorkShopService.getAromrDate();
            return ResultUtils.success(armorWorkShopVo);
    }

    @PostMapping("page")
    public BaseResponse<List<ArmorWorkShopVo>> getArmorDetailList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize
    ) {
//设置分页参数
        Page<ArmorWorkShop>  pageParam = new Page<>(pageNum, pageSize);
        Page<ArmorWorkShop> entityPage = armorWorkShopService.page(pageParam, Wrappers.lambdaQuery(ArmorWorkShop.class).orderByDesc(ArmorWorkShop::getCreateTime));
        // 3. 将实体类分页对象转换为 VO 分页对象
        Page<ArmorWorkShopVo> voPage = new Page<>();
        // 复制分页元数据（总条数、当前页、页大小等）
        voPage.setTotal(entityPage.getTotal()); // 总记录数
        voPage.setCurrent(entityPage.getCurrent()); // 当前页码
        voPage.setSize(entityPage.getSize()); // 每页条数
        voPage.setPages(entityPage.getPages()); // 总页数
        List<ArmorWorkShopVo> voList = entityPage.getRecords().stream().map(armorWorkShop -> {
                    ArmorWorkShopVo vo = new ArmorWorkShopVo();
                    vo.setDailyTotalr(armorWorkShop.getDailyTotalr());
                    vo.setWonArmor(armorWorkShop.getWonArmor());
                    vo.setDate(new Date());
                    return vo;
                }).collect(Collectors.toList());
        return ResultUtils.success(voList);
    }
}
