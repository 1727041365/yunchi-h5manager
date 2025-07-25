package com.yupi.springbootinit.model.dto.Market;

import lombok.Data;
import org.w3c.dom.Text;

@Data
public class HotDao {
    /**
     * 标题
     */
    private String title;

    /**
     * 内容
     */
    private String content;
}
