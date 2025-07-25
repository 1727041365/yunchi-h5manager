package com.yupi.springbootinit.model.vo;

import lombok.Data;
import org.w3c.dom.Text;
import com.baomidou.mybatisplus.annotation.TableName;

/**
 * 热点Dao
 *
 * @author <a href="https://github.com/liyupi">程序员鱼皮</a>
 * @from <a href="https://yupi.icu">编程导航知识星球</a>
 */

@Data
public class HotVo {
        /**
         * 头图url
         */
        private String photoPath;  // 存储图片路径
        /**
         * 标题
         */
        private String title;

        /**
         * 内容
         */
        private String content;

}
