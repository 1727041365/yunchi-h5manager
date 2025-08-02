package com.yupi.springbootinit.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.yupi.springbootinit.model.entity.Market;
import com.yupi.springbootinit.model.vo.MarketVo;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.List;

public interface PriceService extends IService<Market> {
    @Scheduled
    void ScheduledUpdateDate();

    @Scheduled(cron = "0 0/10 * * * ?")
    void RwScheduledUpdateDate();

    void sendMarketMessage(Market market);
    /**
     * 获取所有物品价格
     */
    List<MarketVo> getAllPrices();

    /**
     * 获取单个物品价格
     */
    MarketVo getPriceByItemName(String itemName);

    // 1. 超级币
    void getSuperCurrency();

    /**
     * 获取基石数据
     *
     * @return
     */
    void getBase();

    // 1. 贝壳

    void getSeashell();

    // 4. 胖胖鹅
    void getPenguins();

    // 5. 龟蛋
    void getTurtleEggs();

    // 5. 龟仔
    void getTurtleBoys();

    // 7. 护甲
    void getArmor();

    //8.汉字
    void getChineseCharacter();

    //9.钢材
    void getSteel();

    //10.风车
    void getWindmill();

    //11.沙砾
    void getGravel();

    //12. 时间积分
    void getTimePoints();

    //13. 散仙
    void getLooseFairy();

    //14. 地仙
    void getEarthFairy();

    //15.真仙
    void getTrueFairy();

    //16.天仙
    void getHeavenlyFairy();

    //17，金仙
    void getGoldenFairy();

    //18.法力
    void getMagicPower();

    //19.骷髅头
    void getSkull();

    //20.金石
    void getGoldStone();

    //20.灵石
    void getNimbusStone();

    //笔画接口
    void getStroke();

    //笔画接口
    void getOre();

    //24静心丸
    void getJingxinPills();

    //25完璧符
    void getPerfectTalisman();

    //26吉星符
    void getAuspiciousStarTalisman();

    //27造化果
    void getCreationFruit();

    //29功德石包
    void getMeritStonePackage();

    //30人参
    void getGinseng();

    //27仙种
    void getImmortalSpecies();

    //31仙侠宇宙虚拟股
    void getRwVirtualStocks();

    //31仙侠宇宙虚拟股
    void getAirDrop();

    void autoUpdateDate();
}