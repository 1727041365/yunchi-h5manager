package com.yupi.springbootinit.service;



import com.baomidou.mybatisplus.extension.service.IService;
import com.yupi.springbootinit.model.entity.Market;
import com.yupi.springbootinit.model.vo.MarketVo;

import java.util.List;

public interface PriceService extends IService<Market> {
    /**
     * 获取所有物品价格
     */
    List<MarketVo> getAllPrices();

    /**
     * 获取单个物品价格
     */
    MarketVo getPriceByItemName(String itemName);

    /**
     * 获取基石数据
     * @return
     */
   void getBase();

   void  sendMarketMessage(Market market);
}