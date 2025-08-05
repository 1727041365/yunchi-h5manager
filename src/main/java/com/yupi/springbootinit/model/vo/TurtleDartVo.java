package com.yupi.springbootinit.model.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
@Data
public class TurtleDartVo implements Serializable {
    /**
     * 等级
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
}
