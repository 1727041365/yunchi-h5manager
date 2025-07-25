package com.yupi.springbootinit.model.vo;

import lombok.Data;

import java.math.BigInteger;
import java.util.Date;

@Data
public class StoneQuantityDetailVo {
    private String userQuantity;//用户数量
    private BigInteger oneTotal;//一阶灵石
    private BigInteger twoTotal;//二阶灵石
    private BigInteger threeTotal;//三阶灵石
    private BigInteger fourTotal;//四阶灵石
    private BigInteger fiveTotal;//五阶灵石
    private BigInteger sixTotal;//六阶灵石
    private BigInteger sevenTotal;//七阶灵石
    private BigInteger eightTotal;//八阶灵石
    private BigInteger nineTotal;//九阶灵石
    private BigInteger tenTotal;//十阶灵石
    private BigInteger total;//全部灵石
    private Date updateTime;//更新时间
}
