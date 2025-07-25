package com.yupi.springbootinit.model.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import lombok.Data;

import java.util.Date;
@Data
public class SpiritMineDescriptionVo {

    public String name;//灵矿名字
    public String inputStone;//投入灵石
    public String outputStone;//产出灵石
    public String monthlyProfit;//月利润
}
