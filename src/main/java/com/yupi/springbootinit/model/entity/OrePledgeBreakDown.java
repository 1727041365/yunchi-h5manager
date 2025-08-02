package com.yupi.springbootinit.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 矿石收益明细
 */
@Data
@TableName("ore_pledge_break_down")
public class OrePledgeBreakDown implements Serializable {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
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
    private Date createTime;
    private Date updateTime;
    @TableLogic
    private Integer isDelete;
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
