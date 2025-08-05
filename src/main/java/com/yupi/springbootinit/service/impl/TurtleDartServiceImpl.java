package com.yupi.springbootinit.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.yupi.springbootinit.client.ApiClient;
import com.yupi.springbootinit.mapper.TurtleDartMapper;
import com.yupi.springbootinit.model.entity.Market;
import com.yupi.springbootinit.model.entity.TurtleDart;
import com.yupi.springbootinit.model.enums.MarketConfigEnum;
import com.yupi.springbootinit.service.TurtleDartService;
import com.yupi.springbootinit.utils.HmacSha256Utils;
import com.yupi.springbootinit.utils.RequestStringGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

@Service
@Slf4j
public class TurtleDartServiceImpl extends ServiceImpl<TurtleDartMapper, TurtleDart> implements TurtleDartService {
    @Resource
    private ApiClient apiClient;

    /**
     * 海外镖局
     */
    @Override
    public void getTurtleDartAbord() throws JsonProcessingException {
        String requestUrl = "/v11/api/dart/two/put/index/refresh";
        String url = "https://fks-api.lucklyworld.com/v11/api/dart/two/put/index/refresh";
        String paramsPart3 = "type=1&id=427195";// 入参
        Map<String, String> formParams = new LinkedHashMap<>();
        formParams.put("type", "1");
        formParams.put("id", "427195");
        String version = "5.2.4";
        List<TurtleDart> list = getMapsRwAbord(requestUrl, version, paramsPart3, url, formParams);
        List<TurtleDart> list1 = this.list(Wrappers.lambdaQuery(TurtleDart.class).orderByDesc(TurtleDart::getCreateTime).last("limit 6"));
// 构建等级到对象的映射（添加非空判断）
        Map<String, TurtleDart> levelToTurtleMap = new HashMap<>();
        for (TurtleDart item : list) {
            String level = item.getTurtleLevel();
                levelToTurtleMap.put(level, item);
        }
        if (list1.size() > 0) {
            TurtleDart one = list1.get(0);
            if (isSameDay(new Date(), one.getCreateTime())) {
                list1.forEach(item -> {
                    String turtleLevel = item.getTurtleLevel();
                    TurtleDart turtleDart = levelToTurtleMap .get(turtleLevel);
                    turtleDart.setUpdateTime(new Date());
                    turtleDart.setId(item.getId());
                });
                updateBatchById(list);
            } else {
                saveBatch(list);
            }
        } else {
            saveBatch(list);
        }
    }

    /**
     * 国内镖局
     */
    @Override
    public void getTurtleDartCn() throws JsonProcessingException {
        String requestUrl = "/v11/api/dart/put/index/refresh";
        String url = "https://fks-api.lucklyworld.com/v11/api/dart/put/index/refresh";
        String paramsPart3 = "type=1&id=443032";// 入参
        Map<String, String> formParams = new LinkedHashMap<>();
        formParams.put("type", "1");
        formParams.put("id", "443032");
        String version = "5.2.3";
        List<Map<String, Object>> list =  getMapsCn(requestUrl, version, paramsPart3, url, formParams);
        Map<String, Object> item = list.get(0);

    }

    private boolean isSameDay(Date date1, Date date2) {
        LocalDate localDate1 = date1.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate localDate2 = date2.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        return localDate1.isEqual(localDate2);
    }

    private List<Map<String, Object>> getMapsCn(String requestUrl, String version, String paramsPart3, String url, Map<String, String> formParams) throws JsonProcessingException {
        RequestStringGenerator requestStringGenerator = new RequestStringGenerator();
        String code = requestStringGenerator.generateRequestString(requestUrl, MarketConfigEnum.UID.getValue(), version, MarketConfigEnum.TOKEN.getValue(), paramsPart3);
        String[] split = code.split(",");
        String word = split[0];
        String ts = split[1];
        System.out.println("word" + word);
        HmacSha256Utils hmacSha256Utils = new HmacSha256Utils();
        String result = hmacSha256Utils.hmacSha256Encrypt(word, MarketConfigEnum.KEY.getValue());
        Map<String, String> urlParams = new HashMap<>();
        urlParams.put("uid", MarketConfigEnum.UID.getValue());
        urlParams.put("version", version);
        // 表单参数
        Map<String, List<Map<String, Object>>> response = apiClient.postFormWithTurle(
                url, urlParams, formParams, result, ts, version, "fks-api.lucklyworld.com", Map.class);
        List<Map<String, Object>> list = (List<Map<String, Object>>) response.get("platPets");
        return list;
    }

    private List<TurtleDart> getMapsRwAbord(String requestUrl, String version, String paramsPart3, String url, Map<String, String> formParams) throws JsonProcessingException {
        RequestStringGenerator requestStringGenerator = new RequestStringGenerator();
        String code = requestStringGenerator.generateRequestString(requestUrl, MarketConfigEnum.UID.getValue(), version, MarketConfigEnum.TOKEN.getValue(), paramsPart3);
        String[] split = code.split(",");
        String word = split[0];
        String ts = split[1];
        System.out.println("word" + word);
        HmacSha256Utils hmacSha256Utils = new HmacSha256Utils();
        String result = hmacSha256Utils.hmacSha256Encrypt(word, MarketConfigEnum.KEY.getValue());
        Map<String, String> urlParams = new HashMap<>();
        urlParams.put("uid", MarketConfigEnum.UID.getValue());
        urlParams.put("version", version);
        // 表单参数
        List<TurtleDart> turtleDarts = apiClient.postFormWithTurleAbord(
                url, urlParams, formParams, result, ts, version, "fks-api.lucklyworld.com", TurtleDart.class);
        return turtleDarts;
    }
}
