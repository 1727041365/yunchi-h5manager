package com.yupi.springbootinit.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@TableName(value = "detail")
@Data
public class InformationDetail implements Serializable {
    /**
     * id
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    /**
     * informationId
     */
    private Long informationId;
    private String name;
    /**
     * 头图
     */
    private String photoPath;  // 存储图片路径
    /**
     * 合成成功率
     */
    private String successRate;//合成成功率
    /**
     * 平均战力
     */
    private String evaluateCombatPower;//平均战力
    /**
     * 日采矿数量
     */
    private String dailyRewardOre;
    /**
     * 月利润
     */
    private String monthlyProfit;

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
