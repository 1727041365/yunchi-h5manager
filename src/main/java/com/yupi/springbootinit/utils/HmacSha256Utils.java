package com.yupi.springbootinit.utils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class HmacSha256Utils {
    /**
     * HMAC-SHA256 加密
     * @param word 待加密的字符串（如你生成的请求串）
     * @param key 密钥
     * @return 64位十六进制结果
     */
    public static String hmacSha256Encrypt(String word, String key) {
        try {
            // 创建 HMAC-SHA256 实例
            Mac mac = Mac.getInstance("HmacSHA256");
            // 初始化密钥
            SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(secretKey);
            // 加密并转为十六进制
            byte[] bytes = mac.doFinal(word.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : bytes) {
                String hexStr = Integer.toHexString(0xFF & b);
                if (hexStr.length() == 1) {
                    hex.append("0"); // 补0确保两位十六进制
                }
                hex.append(hexStr);
            }
            return hex.toString();
        } catch (Exception e) {
            throw new RuntimeException("HMAC-SHA256 加密失败", e);
        }
    }

}