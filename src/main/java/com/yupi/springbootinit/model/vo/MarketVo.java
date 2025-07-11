package com.yupi.springbootinit.model.vo;


import lombok.Data;
import java.io.Serializable;

@Data
public class MarketVo implements Serializable {
    private String itemName;     // 物品名称
    private double price;        // 价格
    private String currency;     // 货币单位（宝石/矿石）
}