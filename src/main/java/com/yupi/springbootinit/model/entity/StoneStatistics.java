package com.yupi.springbootinit.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.Date;

@TableName(value = "stone_statistics")
@Data
public class StoneStatistics implements Serializable {
    /**
     * id
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private BigInteger oneTotal;
    private BigInteger twoTotal;
    private BigInteger threeTotal;
    private BigInteger fourTotal;
    private BigInteger fiveTotal;
    private BigInteger sixTotal;
    private BigInteger sevenTotal;
    private BigInteger eightTotal;
    private BigInteger nineTotal;
    private BigInteger tenTotal;
    private BigInteger total;
    private String userQuantity;
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
