package com.yupi.springbootinit.model.vo;

import lombok.Data;

import java.util.Date;

@Data
public class OrePledgeBreakDownVo {
    /**
     * 产出总量
     */
    private Double totalOre;
    /**
     * 当日收益
     */
    private Double dayProfit;
    /**
     * 累计收益
     */
    private Double cumulativeProfit;
    /**
     * 当日收益
     */
    private Date createTime;
}
