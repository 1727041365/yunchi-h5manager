package com.yupi.springbootinit.utils;

import java.math.BigInteger;

public class SpiritStoneLevelUtils {
    // 定义各阶级的阈值（单位：万）
    private static final BigInteger[] THRESHOLDS = new BigInteger[]{
        new BigInteger("100000"),   // 10万
        new BigInteger("300000"),   // 30万
        new BigInteger("600000"),   // 60万
        new BigInteger("1100000"),  // 110万
        new BigInteger("1900000"),  // 190万
        new BigInteger("2900000"),  // 290万
        new BigInteger("4900000"),  // 490万
        new BigInteger("7900000"),  // 790万
        new BigInteger("12900000"), // 1290万
        new BigInteger("22900000")  // 2290万
    };
    
    /**
     * 根据灵石数量判断对应的阶级
     * @param oreCount 灵石数量
     * @return 阶级（1-10）
     */
    public static int getStoneLevel(BigInteger oreCount) {
        if (oreCount == null || oreCount.compareTo(BigInteger.ZERO) <= 0) {
            return 0; // 可根据实际需求决定返回值，比如返回 1 等
        }
        // 遍历阈值，找到对应的阶级
        for (int i = 0; i < THRESHOLDS.length; i++) {
            if (oreCount.compareTo(THRESHOLDS[i]) > 0) {
                // 大于当前阈值，继续往后找
                continue;
            } else if (oreCount.compareTo(THRESHOLDS[i]) == 0) {
                // 等于当前阈值，对应到 i + 1 级（因为阈值索引从 0 开始，阶级从 1 开始）
                return i + 1;
            } else {
                // 小于当前阈值，对应到 i 级
                return i;
            }
        }
        // 如果大于所有阈值（即大于 22900000），返回 10 级（可根据实际需求调整）
        return 10;
    }
}