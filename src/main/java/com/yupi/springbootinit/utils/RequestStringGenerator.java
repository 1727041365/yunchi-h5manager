package com.yupi.springbootinit.utils;

import com.yupi.springbootinit.model.enums.MarketConfigEnum;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

public class RequestStringGenerator {

    public static String generateRequestString(String requestUrl, String userId, String version, String token,String paramsPart3 ) {
        // 生成时间戳
        long ts = System.currentTimeMillis();
//        String ts ="1752240841802";
        // 生成随机androidId
       String androidId = generateRandomAndroidId();
        
        // 构建参数部分
        String paramsPart1 = "uid=" + userId + "&version=" + version;
        String paramsPart2 = "androidId="+ MarketConfigEnum.ANDROID_ID.getValue() +
                           "&userId=" + userId + 
                           "&token=" + token + 
                           "&packageId=com.caike.union" + 
                           "&version=" + version + 
                           "&channel=official";
        
        // 计算MD5
        String md5 = calculateMD5(paramsPart3);
        
        // 构建完整请求字符串
        return "post|" + requestUrl + "|" + paramsPart1 + "|" + ts + "|" + paramsPart2 + "|" + md5+","+ts;
    }
    private static String generateRandomAndroidId() {
        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        String chars = "0123456789abcdef";

        for (int i = 0; i < 16; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }

    private static String calculateMD5(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(input.getBytes());
            StringBuilder sb = new StringBuilder();
            
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MD5 algorithm not found", e);
        }
    }
}    