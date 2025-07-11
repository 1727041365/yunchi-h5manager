package com.yupi.springbootinit.model.mq;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MarketMessage implements Serializable {
    private String itemName;
    private Double price;
    private String currency;
    private static final long serialVersionUID = 1L;
}
