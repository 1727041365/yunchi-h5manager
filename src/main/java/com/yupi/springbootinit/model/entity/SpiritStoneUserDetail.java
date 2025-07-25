package com.yupi.springbootinit.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.Date;
@TableName(value = "spirit_detail")
@Data
public class SpiritStoneUserDetail implements Serializable {
    /**
     * id
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    /**
     * roomId
     */
    private Long roomId;
    /**
     * 姓名
     */
    private String name;
    /**
     * 灵石总数
     */
    private BigInteger stoneTotal;
    /**
     * 灵石阶级
     */
    private String stoneLevel;

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
