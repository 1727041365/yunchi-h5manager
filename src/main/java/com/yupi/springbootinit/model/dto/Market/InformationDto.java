package com.yupi.springbootinit.model.dto.Market;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class InformationDto {
    /**
     * 标题
     */
    private String title;

    /**
     * 内容
     */
    private String content;
}
