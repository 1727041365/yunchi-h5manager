package com.yupi.springbootinit.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 护甲坊实体类
 */
@Data
@TableName("armor_work_shop")
public class ArmorWorkShop implements Serializable {
    /**
     * id
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
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
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 是否删除
     */
    @TableLogic
    private Integer isDelete;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
