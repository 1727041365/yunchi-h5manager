package com.yupi.springbootinit.controller.PriceController;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.yupi.springbootinit.common.BaseResponse;
import com.yupi.springbootinit.common.ErrorCode;
import com.yupi.springbootinit.common.ResultUtils;
import com.yupi.springbootinit.mapper.OtherMapper;
import com.yupi.springbootinit.model.entity.OtherUser;
import com.yupi.springbootinit.model.vo.MarketVo;
import com.yupi.springbootinit.service.PriceService;
import com.yupi.springbootinit.service.UserService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/prices")
public class PriceController {

    private final PriceService priceService;

    @Resource
    private UserService userService;
    @Resource
    private OtherMapper otherMapper;

    public PriceController(PriceService priceService) {
        this.priceService = priceService;
    }

    /**
     * 获取所有物品价格
     */
    @PostMapping
    public List<MarketVo> getAllPrices() {
        return priceService.getAllPrices();
    }

    @GetMapping
    public void updateAllPrices() {
//        priceService.getLooseFairy();//13散仙no
//        priceService.getEarthFairy();//14地仙yes
//        priceService.getTrueFairy();//15真仙yes
//        priceService.getHeavenlyFairy();//16天仙no
//        priceService.getGoldenFairy();//17金仙yes
    }

    @PostMapping("getDate")
    public BaseResponse<String> updateAllPrices(@RequestParam("user") String user) {
        if (user == null) {
            return ResultUtils.error(ErrorCode.PARAMS_ERROR);
        }
        OtherUser otherUser = otherMapper.selectOne(Wrappers.lambdaQuery(OtherUser.class).eq(OtherUser::getUser, user));
        if (otherUser == null) {
            return ResultUtils.error(ErrorCode.User_ERROR);
        }
        String status = otherUser.getStatus();
        return ResultUtils.success(status);
    }

    /**
     * 根据物品名称获取价格
     */
    @GetMapping("/{itemName}")
    public MarketVo getPriceByItemName(@PathVariable String itemName) {
        return priceService.getPriceByItemName(itemName);
    }
}