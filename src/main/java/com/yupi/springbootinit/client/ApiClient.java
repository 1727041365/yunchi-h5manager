package com.yupi.springbootinit.client;

import org.apache.tomcat.util.http.parser.Host;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.apache.commons.codec.digest.DigestUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

@Component
public class ApiClient {

    private final RestTemplate restTemplate;
    private static final String TOKEN = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiIiLCJhdWQiOiIiLCJqdGkiOiIxMjQyMjQ1NyIsImlhdCI6MTc1MjExNTU1MCwibmJmIjoxNzUyMTE1NTUwLCJleHAiOjE3NTM5NTI5MjMsInR5cGUiOiIyMDk3NV92MTFhcHAiLCJhdXRoZW50aWNhdGlvblN0YXRlIjpmYWxzZX0.aVoPWd1th4W27pG1dFcugvFLngZ8zEkyRoy9JldZ-1I";
    private static final String ANDROID_ID = "12432b87b3b19b25";
    private static final String CHANNEL = "official";
    private static final String PACKAGE_ID = "com.caike.union";
    private static final String User_Agent= "com.caike.union/5.2.2-official Dalvik/2.1.0 (Linux; U; Android 9; SM-N9760 Build/PQ3B.190801.11071530";
    private static final String Host= "fks-api.lucklyworld.com";
    private static final String VERSION= "5.2.2";
    private static final String USER_ID= "12422457";
    public ApiClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * 发送表单POST请求
     */
    public <T> T postFormWithSign(
            String url,
            Map<String, String> urlParams, // URL参数（uid、version）
            Map<String, String> formParams, // 表单请求体参数
            String SingResult,
            String ts,
            Class<T> responseType
    ) {
        // 1. 构建完整URL（拼接URL参数）
        String fullUrl = buildUrlWithParams(url, urlParams);
        System.out.println("fullUrl:"+fullUrl);
//        formParams.put("sign", SingResult);
        // 5. 构建请求体（表单格式）
        MultiValueMap<String, String> formData = convertToFormData(formParams);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED); // 改成表单格式
        headers.set("Host", Host);
        headers.set("User-Agent", User_Agent);
        headers.set("androidId", ANDROID_ID);
        headers.set("channel", CHANNEL);
        headers.set("packageId", PACKAGE_ID);
        headers.set("sign", SingResult); // 此处需填写实际签名值
        headers.set("token",TOKEN); // 此处需填写实际 token
        headers.set("ts",ts);
        headers.set("userId", USER_ID); // 此处需填写实际用户 ID
        headers.set("version", VERSION);
        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(formData, headers);
        System.out.println(entity);
       // 6. 发送请求并返回结果
        try {
            return restTemplate.postForEntity(fullUrl, entity, responseType).getBody();
        } catch (Exception e) {
            System.err.println("POST表单请求失败：" + fullUrl + "，错误：" + e.getMessage());
            return null;
        }
    }

//    /**
//     * 发送JSON POST请求（带签名，适用于龟蛋、龟仔等接口）
//     */
//    public <T> T postJsonWithSign(
//            String url,
//            Map<String, String> urlParams, // URL参数（uid、version）
//            Object jsonBody, // JSON请求体（如{"species":"0","page":1}）
//            String version,
//            Class<T> responseType
//    ) {
//        // 1. 构建完整URL
//        String fullUrl = buildUrlWithParams(url, urlParams);
//
//        // 2. 构建请求头
//        String ts = headers.getFirst("ts");
//        String userId = urlParams.get("uid");
//
//        // 3. 合并参数（URL参数+JSON参数+固定参数）
//        Map<String, String> allParams = new HashMap<>();
//        allParams.putAll(urlParams);
//        allParams.putAll(convertJsonToMap(jsonBody)); // JSON参数转为Map
//        allParams.put("ts", ts);
//        allParams.put("userId", userId);
//        allParams.put("androidId", ANDROID_ID);
//        allParams.put("channel", CHANNEL);
//        allParams.put("packageId", PACKAGE_ID);
//        allParams.put("token", TOKEN);
//        headers.setContentType(MediaType.APPLICATION_JSON); // JSON格式
//
//        // 5. 发送请求
//        HttpEntity<Object> entity = new HttpEntity<>(jsonBody, headers);
//        try {
//            return restTemplate.postForEntity(fullUrl, entity, responseType).getBody();
//        } catch (Exception e) {
//            System.err.println("POST JSON请求失败：" + fullUrl + "，错误：" + e.getMessage());
//            return null;
//        }
//    }

    // 工具方法：拼接URL参数（如?uid=123&version=4.1.2）
    private String buildUrlWithParams(String url, Map<String, String> params) {
        if (params == null || params.isEmpty()) return url;
        StringBuilder sb = new StringBuilder(url).append("?");
        params.forEach((k, v) -> sb.append(k).append("=").append(v).append("&"));
        sb.deleteCharAt(sb.length() - 1);
        return sb.toString();
    }


    // 转换为表单数据
    private MultiValueMap<String, String> convertToFormData(Map<String, String> params) {
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        if (params != null) {
            params.forEach((k, v) -> formData.add(k, v));
        }
        return formData;
    }

    // 将JSON对象转为Map（简化版，复杂对象需用Jackson）
    private Map<String, String> convertJsonToMap(Object jsonBody) {
        // 实际项目中使用Jackson的ObjectMapper转换
        // 此处以龟蛋接口为例，手动转换（根据实际JSON结构调整）
        if (jsonBody instanceof Map) {
            Map<?, ?> map = (Map<?, ?>) jsonBody;
            Map<String, String> result = new HashMap<>();
            map.forEach((k, v) -> result.put(k.toString(), v.toString()));
            return result;
        }
        return new HashMap<>();
    }
}