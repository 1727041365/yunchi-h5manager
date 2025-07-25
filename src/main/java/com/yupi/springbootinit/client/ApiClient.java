package com.yupi.springbootinit.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yupi.springbootinit.model.enums.MarketConfigEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

@Slf4j
@Component
public class ApiClient {

    private final RestTemplate restTemplate;
    private static final String CHANNEL = "official";
    private static final String PACKAGE_ID = "com.caike.union";
    private static final String User_Agent= "com.caike.union/5.2.2-official Dalvik/2.1.0 (Linux; U; Android 9; SM-N9760 Build/PQ3B.190801.11071530";
    private static final String Stone_User_Agent= "com.ainimei.farmworld/2.1.2 (Linux; U; Android 12; zh-cn) (official; 201002)";
    public ApiClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * 发送表单POST请求
     */
    public <T> T postFormWithSign(
            String url,
            Map<String, String> urlParams, // URL参数（uid、version）
           Map<String,String>  formParams, // 表单请求体参数
            String SingResult,
            String ts,
            String version,
            String Host,
            Class<T> responseType
    ) {
        // 1. 构建完整URL（拼接URL参数）
        String fullUrl = buildUrlWithParams(url, urlParams);
        System.out.println("fullUrl:"+fullUrl);
        // 5. 构建请求体（表单格式）
        MultiValueMap<String, String> formData = convertToFormData(formParams);
        log.info("表单数据formData:"+formData);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED); // 改成表单格式
        headers.set("Host", Host);
        headers.set("User-Agent", User_Agent);
        headers.set("androidId", MarketConfigEnum.ANDROID_ID.getValue());
        headers.set("channel", CHANNEL);
        headers.set("packageId", PACKAGE_ID);
        headers.set("sign", SingResult); // 此处需填写实际签名值
        headers.set("token", MarketConfigEnum.TOKEN.getValue()); // 此处需填写实际 token
        headers.set("ts",ts);
        headers.set("userId", MarketConfigEnum.UID.getValue()); // 此处需填写实际用户 ID
        headers.set("version", version);
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
    /**
     * 发送表单POST请求
     */
    public <T> T postFormWithDetail(
            String url,
            Map<String, String> urlParams, // URL参数（uid、version）
            Map<String, String> formParams, // 表单请求体参数
            String version,
            String Host,
            Class<T> responseType
    ) {
        // 1. 构建完整URL（拼接URL参数）
        String fullUrl = buildUrlWithParams(url, urlParams);
        System.out.println("fullUrl:"+fullUrl);
        // 5. 构建请求体（表单格式）
        MultiValueMap<String, String> formData = convertToFormData(formParams);
        log.info("表单数据formData:"+formData);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED); // 改成表单格式
        headers.set("Host", Host);
        headers.set("User-Agent", User_Agent);
        headers.set("androidId", MarketConfigEnum.ANDROID_ID.getValue());
        headers.set("channel", CHANNEL);
        headers.set("packageId", PACKAGE_ID);
        headers.set("token", MarketConfigEnum.TOKEN.getValue()); // 此处需填写实际 token
        headers.set("userId", MarketConfigEnum.UID.getValue()); // 此处需填写实际用户 ID
        headers.set("version", version);
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

    public <T> T postFormWithStoneDetail(
            String url,
            Map<String, String> urlParams, // URL参数（uid、version）
            Map<String, String> formParams, // 表单请求体参数
            String Host,
            Class<T> responseType
    ) {
        // 1. 构建完整URL（拼接URL参数）
        String fullUrl = buildUrlWithParams(url, urlParams);
        System.out.println("fullUrl:"+fullUrl);
        // 5. 构建请求体（表单格式）
        MultiValueMap<String, String> formData = convertToFormData(formParams);
        log.info("表单数据formData:"+formData);
        HttpHeaders headers = new HttpHeaders();
        headers.set("ANDROIDID", MarketConfigEnum.ANDROID_ID.getValue());
        headers.set("Channel", CHANNEL);
        headers.set("Connection", "Keep-Alive");
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.set("Host", Host);
        headers.set("User-Agent", "com.ainimei.farmworld/2.1.3 (Linux; U; Android 10; zh-cn) (official; 201003)");
        headers.set("test-encrypt", "0");
        headers.set("token", MarketConfigEnum.StoneTOKEN.getValue()); // 此处需填写实际 token
        headers.set("uid", MarketConfigEnum.StoenUID.getValue()); // 此处需填写实际用户 ID
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


    public <T> T postJsonWithCaveHouseDetail(
            String url,
            Map<String, String> urlParams, // URL参数（uid、version）
            Map<String, String> jsonParams, // JSON请求体参数
            String Host,
            String userId,
            Class<T> responseType
    ) {
        // 1. 构建完整URL（拼接URL参数）
        String fullUrl = buildUrlWithParams(url, urlParams);
        System.out.println("fullUrl:" + fullUrl);

        // 2. 构建JSON请求体
        ObjectMapper objectMapper = new ObjectMapper();
        String jsonBody = "";
        try {
            jsonBody = objectMapper.writeValueAsString(jsonParams);
        } catch (JsonProcessingException e) {
            log.error("JSON序列化失败", e);
            return null;
        }
        log.info("JSON请求体:" + jsonBody);

        // 3. 设置请求头
        HttpHeaders headers = new HttpHeaders();
        headers.set("ANDROIDID", MarketConfigEnum.ANDROID_ID.getValue());
        headers.set("Channel", CHANNEL);
        headers.set("Connection", "Keep-Alive");
        headers.setContentType(MediaType.APPLICATION_JSON_UTF8);// 设置为JSON类型
        headers.set("Host", Host);
        headers.set("User-Agent","com.ainimei.farmworld/2.1.2 (Linux; U; Android 12; zh-cn) (official; 201002)");
        headers.set("test-encrypt", "0");
        headers.set("token", MarketConfigEnum.StoneTOKEN.getValue());
        headers.set("uid", MarketConfigEnum.StoenUID.getValue());
        headers.set("x-role-id", userId);

        // 4. 创建HTTP实体（注意这里使用String作为请求体类型）
        HttpEntity<String> entity = new HttpEntity<>(jsonBody, headers);
        log.info("请求实体:::" + entity);

        // 5. 发送请求
        try {
            return restTemplate.postForEntity(fullUrl, entity, responseType).getBody();
        } catch (Exception e) {
            System.err.println("POST JSON请求失败：" + fullUrl + "，错误：" + e.getMessage());
            return null;
        }
    }

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
}