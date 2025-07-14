package com.yupi.springbootinit.controller;
import com.yupi.springbootinit.model.entity.Market;
import com.yupi.springbootinit.model.vo.MarketVo;
import com.yupi.springbootinit.service.PriceService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    /**
     * 根据物品名称获取价格
     */
    @GetMapping("/{itemName}")
    public MarketVo getPriceByItemName(@PathVariable String itemName) {
        return priceService.getPriceByItemName(itemName);
    }
}