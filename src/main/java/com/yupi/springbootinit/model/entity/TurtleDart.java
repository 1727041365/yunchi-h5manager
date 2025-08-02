package com.yupi.springbootinit.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 乌龟镖局实体类
 */
@Data
@TableName("turtle_dart")
public class TurtleDart implements Serializable {
    /**
     * id
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    /**
     * 乌龟等级
     */
    private String turtleLevel;
    /**
     * 乌龟数量
     */
    private Double turtleSize;
    /**
     * 乌龟价格
     */
    private Double turtlePrice;
    /**
     * 乌龟倍数
     */
    private String turtleMultiple;
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
