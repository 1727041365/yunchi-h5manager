package com.yupi.springbootinit.model.vo;

import com.baomidou.mybatisplus.annotation.TableLogic;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
@Data
public class TurtlerResultVo implements Serializable {
    /**
     * 期数
     */
    private String timeLimit;
    /**
     * 地区序号
     */
    private String side;
    /**
     * 被打劫的地区名字
     */
    private String regionName;
    /**
     * 更新时间
     */
    private Date updateTime;
}
