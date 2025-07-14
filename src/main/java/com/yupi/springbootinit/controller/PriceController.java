package com.yupi.springbootinit.controller;
import com.yupi.springbootinit.common.BaseResponse;
import com.yupi.springbootinit.common.ResultUtils;
import com.yupi.springbootinit.model.entity.Market;
import com.yupi.springbootinit.model.vo.MarketVo;
import com.yupi.springbootinit.service.PriceService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Random;

@RestController
@RequestMapping("/prices")
public class PriceController {

    private final PriceService priceService;

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

    @GetMapping("updatePriceDate")
    public BaseResponse<String> updateAllPrices() {
        Random random = new Random();
        int sleepTime =800+random.nextInt(200);
        priceService.getLooseFairy();//13散仙no
//        priceService.getSuperCurrency();//1更新超级币no
        priceService.getBase();//2更新基石yes
        priceService.getSeashell();//3更新贝壳yes
        priceService.getPenguins();//4更新胖胖鹅yes
        priceService.getTurtleEggs();//5更新龟蛋yes
        priceService.getTurtleBoys();//6更新龟仔yes
     priceService.getArmor();//7更新护甲yes
        priceService.getChineseCharacter();//8y
        priceService.getEarthFairy();//14地仙yes
        priceService.getSteel();//9y
        priceService.getWindmill();//10y
        priceService.getGravel();//11y
        priceService.getTimePoints();//12y
        try {
            priceService.getSkull();//19骷髅头yes
            Thread.sleep(sleepTime); // 睡眠，反爬虫
        } catch (InterruptedException e) {
            e.printStackTrace();
            // 当线程在睡眠时被中断，会抛出此异常
        }
        sleepTime =1000+random.nextInt(200);
        try {
            priceService.getTrueFairy();//15真仙yes
            Thread.sleep(sleepTime); // 线程暂停 1000毫秒（即 1 秒）
        } catch (InterruptedException e) {
            e.printStackTrace();
            // 当线程在睡眠时被中断，会抛出此异常
        }
        priceService.getHeavenlyFairy();//16天仙no
        priceService.getMagicPower();//18法力yes
        priceService.getGoldStone();//20金石yes
        sleepTime =1000+random.nextInt(170);
        try {
            priceService.getNimbusStone();//21灵石yes
            Thread.sleep(sleepTime); // 线程暂停 1000毫秒（即 1 秒）
        } catch (InterruptedException e) {
            e.printStackTrace();
            // 当线程在睡眠时被中断，会抛出此异常
        }
        priceService.getGoldenFairy();//17金仙yes
        return ResultUtils.success("成功更新");
    }
    /**
     * 根据物品名称获取价格
     */
    @GetMapping("/{itemName}")
    public MarketVo getPriceByItemName(@PathVariable String itemName) {
        return priceService.getPriceByItemName(itemName);
    }
}