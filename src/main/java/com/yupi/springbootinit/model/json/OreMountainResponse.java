package com.yupi.springbootinit.model.json;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.deser.std.NumberDeserializers;
import lombok.Data;

@Data
public class OreMountainResponse {
    // 字符串转int
    @JsonProperty("myOre")
    @JsonDeserialize(using = NumberDeserializers.IntegerDeserializer.class)
    private int myOre;

    @JsonProperty("title")
    private String title;

    @JsonProperty("tips")
    private String tips;

    @JsonProperty("maxOrePut")
    @JsonDeserialize(using = NumberDeserializers.IntegerDeserializer.class)
    private int maxOrePut;

    @JsonProperty("minOrePut")
    @JsonDeserialize(using = NumberDeserializers.IntegerDeserializer.class)
    private int minOrePut;

    @JsonProperty("maxOreOut")
    @JsonDeserialize(using = NumberDeserializers.IntegerDeserializer.class)
    private int maxOreOut;

    @JsonProperty("minOreOut")
    @JsonDeserialize(using = NumberDeserializers.IntegerDeserializer.class)
    private int minOreOut;

    @JsonProperty("putTips")
    private String putTips;

    @JsonProperty("outTips")
    private String outTips;

    @JsonProperty("resourceDataTips")
    private String resourceDataTips;

    @JsonProperty("pledgeOre")
    @JsonDeserialize(using = NumberDeserializers.IntegerDeserializer.class)
    private int pledgeOre;

    // 字符串转double
    @JsonProperty("todayTotalRewardOre")
    @JsonDeserialize(using = NumberDeserializers.DoubleDeserializer.class)
    private double todayTotalRewardOre;

    @JsonProperty("todayUnitOre")
    @JsonDeserialize(using = NumberDeserializers.DoubleDeserializer.class)
    private double todayUnitOre;

    @JsonProperty("myTodayOre")
    @JsonDeserialize(using = NumberDeserializers.DoubleDeserializer.class)
    private double myTodayOre;

    @JsonProperty("myTodayOreTips")
    private String myTodayOreTips;

    @JsonProperty("ruleImage")
    private String ruleImage;

    // 字符串转long
    @JsonProperty("stockOre")
    @JsonDeserialize(using = NumberDeserializers.LongDeserializer.class)
    private long stockOre;

    @JsonProperty("stockOreDelta1day")
    @JsonDeserialize(using = NumberDeserializers.IntegerDeserializer.class)
    private int stockOreDelta1day;

    @JsonProperty("stockOreDelta7day")
    @JsonDeserialize(using = NumberDeserializers.IntegerDeserializer.class)
    private int stockOreDelta7day;

    @JsonProperty("stockOreDelta30day")
    @JsonDeserialize(using = NumberDeserializers.IntegerDeserializer.class)
    private int stockOreDelta30day;

    @JsonProperty("currentUserOre")
    @JsonDeserialize(using = NumberDeserializers.LongDeserializer.class)
    private long currentUserOre;

    @JsonProperty("currentUserOreDelta1day")
    @JsonDeserialize(using = NumberDeserializers.IntegerDeserializer.class)
    private int currentUserOreDelta1day;

    @JsonProperty("currentUserOreTips")
    private String currentUserOreTips;

    @JsonProperty("palaceOre")
    @JsonDeserialize(using = NumberDeserializers.LongDeserializer.class)
    private long palaceOre;

    @JsonProperty("palaceOreDelta1day")
    @JsonDeserialize(using = NumberDeserializers.IntegerDeserializer.class)
    private int palaceOreDelta1day;

    @JsonProperty("palaceTips")
    private String palaceTips;

    @JsonProperty("wageOre")
    @JsonDeserialize(using = NumberDeserializers.LongDeserializer.class)
    private long wageOre;

    @JsonProperty("wageOreDelta1day")
    @JsonDeserialize(using = NumberDeserializers.IntegerDeserializer.class)
    private int wageOreDelta1day;

    @JsonProperty("wageTips")
    private String wageTips;

    @JsonProperty("otherOre")
    @JsonDeserialize(using = NumberDeserializers.LongDeserializer.class)
    private long otherOre;

    @JsonProperty("otherOreDelta1day")
    @JsonDeserialize(using = NumberDeserializers.IntegerDeserializer.class)
    private int otherOreDelta1day;

    @JsonProperty("otherOreTips")
    private String otherOreTips;
}