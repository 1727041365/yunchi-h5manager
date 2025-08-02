package com.yupi.springbootinit.model.vo;

import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class ArmorWorkShopVo {
    /**
     * 护甲产出总量
     */
    private Double dailyTotalr;
    /**
     * 摇中护甲
     */
    private Double wonArmor;
    /**
     * 投入每份需要消费矿石
     */
    private Double costPerShare;
    /**
     * 月利润
     */
    private Double monthlyProfit;

    /**
     * 创建时间
     */
    private Date date;
}
