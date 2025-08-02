package com.yupi.springbootinit.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 矿石质押实体类
 */
@Data
@TableName("ore_pledge")
public class OrePledge implements Serializable {
    /**
     * id
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    /**
     * 今日产出总量
     */
    private Double todayTotalOre;
    /**
     * 每日矿石资源总量
     */
    private Double everydayOre;
    /**
     * 较昨日
     */
    private Double yesterdayTotalOre;
    /**
     * 较七日前
     */
    private Double weekdayTotalOre;
    /**
     * 较30日前
     */
    private Double monthdayTotalOre;
    /**
     * 玩家持有矿石总理
     */
    private Double userUseOre;
    /**
     * 较昨日
     */
    private Double userYesterdayOre;
    /**
     * 神仙殿持有矿石总理
     */
    private Double immortalHallOre;
    /**
     * 较昨日
     */
    private Double immortalYesterdayOre;
    /**
     * 工资池持有矿石总理
     */
    private Double salaryPoolOre;
    /**
     * 较昨日
     */
    private Double salaryPoolYesterdayOre;
    /**
     * 其他矿石总理
     */
    private Double otherOre;
    /**
     * 较昨日
     */
    private Double otherYesterdayOre;
    /**
     * 今日利润
     */
    private Double myTodayOre;
    private Date createTime;
    private Date updateTime;
    @TableLogic
    private Integer isDelete;
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
