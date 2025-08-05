package com.yupi.springbootinit.service.impl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yupi.springbootinit.client.ApiClient;
import com.yupi.springbootinit.mapper.MarketMapper;
import com.yupi.springbootinit.model.entity.Market;
import com.yupi.springbootinit.model.enums.MarketConfigEnum;
import com.yupi.springbootinit.model.mq.MarketMessage;
import com.yupi.springbootinit.model.vo.MarketVo;
import com.yupi.springbootinit.service.PriceService;
import com.yupi.springbootinit.utils.HmacSha256Utils;
import com.yupi.springbootinit.utils.RequestStringGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class PriceServiceImpl extends ServiceImpl<MarketMapper, Market> implements PriceService {

    @Resource
    private RabbitTemplate rabbitTemplate;

    @Value("${app.rabbitmq.exchange}")
    private String exchangeName;

    @Value("${app.rabbitmq.routing-key}")
    private String routingKey;

    private final ApiClient apiClient;
    private static final String Host = "fks-api.lucklyworld.com";
    private static final String SuperHost = "super-link-api.lucklyworld.com";
    private static final String NimbusStoneHost = "farm-api.lucklyworld.com";
    private static final String PenguinsHost = "android-api.lucklyworld.com";

    public PriceServiceImpl(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<MarketVo> getAllPrices() {
        List<Market> marketList = this.list();
        List<MarketVo> marketVoList = marketList.stream().map(market -> {
            MarketVo marketVo = new MarketVo();
            BeanUtils.copyProperties(market, marketVo);
            return marketVo;
        }).collect(Collectors.toList());
        return marketVoList;
    }

    // 1. 超级币
    @Override
    public void getSuperCurrency() {
        String requestUrl = "/v9/api/trade/purchase/list";
        String url = "https://api.chaojilianjie.cn/v9/api/trade/purchase/list";
        String paramsPart3 = "page=1&queryCode=";//入参
        String version = "4.1.5";
        RequestStringGenerator requestStringGenerator = new RequestStringGenerator();
        String code = requestStringGenerator.generateRequestString(requestUrl, MarketConfigEnum.CJBUID.getValue(), version, MarketConfigEnum.CJBTOKEN.getValue(), paramsPart3);
        String[] split = code.split(",");
        String word = split[0];
        String ts = split[1];
        System.out.println("word" + word);
        HmacSha256Utils hmacSha256Utils = new HmacSha256Utils();
        String result = hmacSha256Utils.hmacSha256Encrypt(word, MarketConfigEnum.KEY.getValue());
        Map<String, String> urlParams = new HashMap<>();
        urlParams.put("uid", MarketConfigEnum.CJBUID.getValue());
        urlParams.put("version", version);
        // 表单参数
        Map<String, String> formParams = new LinkedHashMap<>();
        formParams.put("page", "1");
        formParams.put("queryCode", "");
        Map<String, List<Map<String, Object>>> response = apiClient.postFormWithCjlj(
                url, urlParams, formParams, result, ts, version, SuperHost, Map.class
        );
        List<Map<String, Object>> list = (List<Map<String, Object>>) response.get("list");
        Map<String, Object> item = list.get(1);
        Object priceObj = item.get("price");
        // 根据实际类型转换
        Market market = new Market();
        if (priceObj instanceof Double) {
            Double price = (Double) priceObj;
            market.setPrice(price);
        } else if (priceObj instanceof String) {
            // 如果日志中显示的是字符串形式，需要转换
            Double price = Double.parseDouble((String) priceObj);
            market.setPrice(price);
        } else {
            System.out.println("Price type: " + priceObj.getClass().getName());

        }
        market.setItemName("超级币");
        market.setCurrency("宝石");
        MarketVo marketVo = new MarketVo();
        BeanUtils.copyProperties(market, marketVo);
        sendMarketMessage(market);
    }

    // 2. 基石
    @Override
    public void getBase() {
        String requestUrl = "/v11/api/foundation/trade/sale/list";
        String url = "https://fks-api.lucklyworld.com/v11/api/foundation/trade/sale/list";
        String paramsPart3 = "page=1";//入参
        String version = "5.2.2";
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
        Map<String, String> formParams = new LinkedHashMap<>();
        formParams.put("page", "1");
        Map<String, List<Map<String, Object>>> response = apiClient.postFormWithSign(
                url, urlParams, formParams, result, ts, version, Host, Map.class
        );
        // 解析响应（list[1].price，单位宝石）
        List<Map<String, Object>> list = (List<Map<String, Object>>) response.get("list");
        Map<String, Object> item = list.get(0);
        Object priceObj = item.get("price");
        // 根据实际类型转换
        Market market = new Market();
        if (priceObj instanceof Double) {
            Double price = (Double) priceObj;
            market.setPrice(price);
            System.out.println("Price: " + price);
        } else if (priceObj instanceof String) {
            // 如果日志中显示的是字符串形式，需要转换
            Double price = Double.parseDouble((String) priceObj);
            market.setPrice(price);
            System.out.println("Price (parsed): " + price);
        } else {
            System.out.println("Price type: " + priceObj.getClass().getName());

        }
        market.setItemName("基石");
        market.setCurrency("矿石");
        MarketVo marketVo = new MarketVo();
        BeanUtils.copyProperties(market, marketVo);
        sendMarketMessage(market);
    }

    // 3. 贝壳
    @Override
    public void getSeashell() {
        String requestUrl = "/v6/api/pets/shells/trade/sale/list";
        String url = "https://fks-api.lucklyworld.com/v6/api/pets/shells/trade/sale/list";
        String paramsPart3 = "page=1";//入参
        String version = "5.2.2";
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
        Map<String, String> formParams = new LinkedHashMap<>();
        formParams.put("page", "1");
        Map<String, List<Map<String, Object>>> response = apiClient.postFormWithSign(
                url, urlParams, formParams, result, ts, version, Host, Map.class
        );
        // 解析响应（list[1].price，单位宝石）
        List<Map<String, Object>> list = (List<Map<String, Object>>) response.get("list");
        Map<String, Object> item = list.get(0);
        Object priceObj = item.get("price");
        // 根据实际类型转换
        Market market = new Market();
        if (priceObj instanceof Double) {
            Double price = (Double) priceObj;
            market.setPrice(price);
            System.out.println("Price: " + price);
        } else if (priceObj instanceof String) {
            // 如果日志中显示的是字符串形式，需要转换
            Double price = Double.parseDouble((String) priceObj);
            market.setPrice(price);
            System.out.println("Price (parsed): " + price);
        } else {
            System.out.println("Price type: " + priceObj.getClass().getName());

        }
        market.setItemName("贝壳");
        market.setCurrency("宝石");
        MarketVo marketVo = new MarketVo();
        BeanUtils.copyProperties(market, marketVo);
        sendMarketMessage(market);
    }

    // 4. 胖胖鹅
    @Override
    public void getPenguins() {
        String requestUrl = "v13/api/penguins/trade/sale/list";
        String url = "https://android-api.lucklyworld.com/v13/api/penguins/trade/sale/list";
        String paramsPart3 = "page=0&column=rarity&direction=asc";//入参
        String version = "5.2.2";
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
        Map<String, String> formParams = new LinkedHashMap<>();
        formParams.put("page", "0");
        formParams.put("column", "rarity");
        formParams.put("direction", "asc");
        Map<String, List<Map<String, Object>>> response = apiClient.postFormWithSign(
                url, urlParams, formParams, result, ts, version, PenguinsHost, Map.class
        );
        // 解析响应（list[1].price，单位宝石）
        List<Map<String, Object>> list = (List<Map<String, Object>>) response.get("list");
        Map<String, Object> item = list.get(0);
        Object priceObj = item.get("price");
        // 根据实际类型转换
        Market market = new Market();
        if (priceObj instanceof Double) {
            Double price = (Double) priceObj;
            market.setPrice(price);
            System.out.println("Price: " + price);
        } else if (priceObj instanceof String) {
            // 如果日志中显示的是字符串形式，需要转换
            Double price = Double.parseDouble((String) priceObj);
            market.setPrice(price);
            System.out.println("Price (parsed): " + price);
        } else {
            System.out.println("Price type: " + priceObj.getClass().getName());

        }
        market.setItemName("胖胖鹅");
        market.setCurrency("宝石");
        MarketVo marketVo = new MarketVo();
        BeanUtils.copyProperties(market, marketVo);
        sendMarketMessage(market);
    }

    // 5. 龟蛋
    @Override
    public void getTurtleEggs() {
        String requestUrl = "/v6/api/pets/market/index";
        String url = "https://fks-api.lucklyworld.com/v6/api/pets/market/index";
        String paramsPart3 = "species=0&column=price&direction=asc&page=1";//入参
        String version = "5.2.2";
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
        Map<String, String> formParams = new LinkedHashMap<>();
        formParams.put("species", "0");
        formParams.put("column", "price");
        formParams.put("direction", "asc");
        formParams.put("page", "1");
        Map<String, List<Map<String, Object>>> response = apiClient.postFormWithSign(
                url, urlParams, formParams, result, ts, version, Host, Map.class
        );
        // 解析响应（list[1].price，单位宝石）
        List<Map<String, Object>> list = (List<Map<String, Object>>) response.get("list");
        //log.info("price:" + list);
        Map<String, Object> item = list.get(0);
        Object priceObj = item.get("price");
        // 根据实际类型转换
        Market market = new Market();
        if (priceObj instanceof Double) {
            Double price = (Double) priceObj;
            market.setPrice(price);
            System.out.println("Price: " + price);
        } else if (priceObj instanceof String) {
            // 如果日志中显示的是字符串形式，需要转换
            Double price = Double.parseDouble((String) priceObj);
            market.setPrice(price);
            System.out.println("Price (parsed): " + price);
        } else {
            System.out.println("Price type: " + priceObj.getClass().getName());

        }
        market.setItemName("龟蛋");
        market.setCurrency("宝石");
        MarketVo marketVo = new MarketVo();
        BeanUtils.copyProperties(market, marketVo);
        sendMarketMessage(market);
    }

    // 6. 龟仔
    @Override
    public void getTurtleBoys() {
        String requestUrl = "/v6/api/pets/market/index";
        String url = "https://fks-api.lucklyworld.com/v6/api/pets/market/index";
        String paramsPart3 = "species=1&column=price&direction=asc&page=1";//入参
        String version = "5.2.2";
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
        Map<String, String> formParams = new LinkedHashMap<>();
        formParams.put("species", "1");
        formParams.put("column", "price");
        formParams.put("direction", "asc");
        formParams.put("page", "1");
        Map<String, List<Map<String, Object>>> response = apiClient.postFormWithSign(
                url, urlParams, formParams, result, ts, version, Host, Map.class
        );
        // 解析响应（list[1].price，单位宝石）
        List<Map<String, Object>> list = (List<Map<String, Object>>) response.get("list");
        //log.info("price:" + list);
        Map<String, Object> item = list.get(0);
        Object priceObj = item.get("price");
        // 根据实际类型转换
        Market market = new Market();
        if (priceObj instanceof Double) {
            Double price = (Double) priceObj;
            market.setPrice(price);
            System.out.println("Price: " + price);
        } else if (priceObj instanceof String) {
            // 如果日志中显示的是字符串形式，需要转换
            Double price = Double.parseDouble((String) priceObj);
            market.setPrice(price);
            System.out.println("Price (parsed): " + price);
        } else {
            System.out.println("Price type: " + priceObj.getClass().getName());

        }
        market.setItemName("龟仔");
        market.setCurrency("宝石");
        MarketVo marketVo = new MarketVo();
        BeanUtils.copyProperties(market, marketVo);
        sendMarketMessage(market);
    }

    // 7. 护甲
    @Override
    public void getArmor() {
        String requestUrl = "/v11/api/world/armor/trade/sale/list";
        String url = "https://fks-api.lucklyworld.com/v11/api/world/armor/trade/sale/list";
        String paramsPart3 = "page=1";//入参
        String version = "5.2.2";
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
        Map<String, String> formParams = new LinkedHashMap<>();
        formParams.put("page", "1");
        Map<String, List<Map<String, Object>>> response = apiClient.postFormWithSign(
                url, urlParams, formParams, result, ts, version, Host, Map.class
        );

        // 解析响应（list[1].price，单位宝石）
        List<Map<String, Object>> list = (List<Map<String, Object>>) response.get("list");
        //log.info("price:" + list);
        Map<String, Object> item = list.get(0);
        Object priceObj = item.get("price");
        // 根据实际类型转换
        Market market = new Market();
        if (priceObj instanceof Double) {
            Double price = (Double) priceObj;
            market.setPrice(price);
            System.out.println("Price: " + price);
        } else if (priceObj instanceof String) {
            // 如果日志中显示的是字符串形式，需要转换
            Double price = Double.parseDouble((String) priceObj);
            market.setPrice(price);
            System.out.println("Price (parsed): " + price);
        } else {
            System.out.println("Price type: " + priceObj.getClass().getName());

        }
        market.setItemName("护甲");
        market.setCurrency("矿石");
        MarketVo marketVo = new MarketVo();
        BeanUtils.copyProperties(market, marketVo);
        sendMarketMessage(market);
    }

    //8.汉字
    @Override
    public void getChineseCharacter() {
        String requestUrl = "/v11/api/word/trade/sale/list";
        String url = "https://fks-api.lucklyworld.com/v11/api/word/trade/sale/list";
        String paramsPart3 = "page=1";// 入参
        String version = "5.2.2";
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
        Map<String, String> formParams = new LinkedHashMap<>();
        formParams.put("page", "1");
        Map<String, List<Map<String, Object>>> response = apiClient.postFormWithSign(
                url, urlParams, formParams, result, ts, version, Host, Map.class
        );

        // 解析响应（list[0].price，单位宝石）
        List<Map<String, Object>> list = (List<Map<String, Object>>) response.get("list");
        //log.info("price:" + list);
        Map<String, Object> item = list.get(0);
        Object priceObj = item.get("price");
        // 根据实际类型转换
        Market market = new Market();
        if (priceObj instanceof Double) {
            Double price = (Double) priceObj;
            market.setPrice(price);
            System.out.println("Price: " + price);
        } else if (priceObj instanceof String) {
            Double price = Double.parseDouble((String) priceObj);
            market.setPrice(price);
            System.out.println("Price (parsed): " + price);
        } else {
            System.out.println("Price type: " + priceObj.getClass().getName());
        }
        market.setItemName("汉字");
        market.setCurrency("宝石");
        MarketVo marketVo = new MarketVo();
        BeanUtils.copyProperties(market, marketVo);
        sendMarketMessage(market);
    }

    //9.钢材
    @Override
    public void getSteel() {
        String requestUrl = "/v11/api/sw/trade/props/sale/list";
        String url = "https://fks-api.lucklyworld.com/v11/api/sw/trade/props/sale/list";
        String paramsPart3 = "propsId=1&page=1";// 入参
        String version = "5.2.2";
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
        Map<String, String> formParams = new LinkedHashMap<>();
        formParams.put("propsId", "1");
        formParams.put("page", "1");
        Map<String, List<Map<String, Object>>> response = apiClient.postFormWithSign(
                url, urlParams, formParams, result, ts, version, Host, Map.class
        );

        // 解析响应（list[0].price，单位宝石）
        List<Map<String, Object>> list = (List<Map<String, Object>>) response.get("list");
        //log.info("price:" + list);
        Map<String, Object> item = list.get(0);
        Object priceObj = item.get("price");
        // 根据实际类型转换
        Market market = new Market();
        if (priceObj instanceof Double) {
            Double price = (Double) priceObj;
            market.setPrice(price);
            System.out.println("Price: " + price);
        } else if (priceObj instanceof String) {
            Double price = Double.parseDouble((String) priceObj);
            market.setPrice(price);
            System.out.println("Price (parsed): " + price);
        } else {
            System.out.println("Price type: " + priceObj.getClass().getName());
        }
        market.setItemName("钢材");
        market.setCurrency("宝石");
        MarketVo marketVo = new MarketVo();
        BeanUtils.copyProperties(market, marketVo);
        sendMarketMessage(market);
    }

    //10.风车
    @Override

    public void getWindmill() {
        String requestUrl = "/v11/api/fks/turbine/trade/sale/list";
        String url = "https://fks-api.lucklyworld.com/v11/api/fks/turbine/trade/sale/list";
        String paramsPart3 = "page=1";// 入参
        String version = "5.2.2";
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
        Map<String, String> formParams = new LinkedHashMap<>();
        formParams.put("page", "1");
        Map<String, List<Map<String, Object>>> response = apiClient.postFormWithSign(
                url, urlParams, formParams, result, ts, version, Host, Map.class
        );

        // 解析响应（list[0].price，单位宝石）
        List<Map<String, Object>> list = (List<Map<String, Object>>) response.get("list");
        //log.info("price:" + list);
        Map<String, Object> item = list.get(0);
        Object priceObj = item.get("price");
        // 根据实际类型转换
        Market market = new Market();
        if (priceObj instanceof Double) {
            Double price = (Double) priceObj;
            market.setPrice(price);
            System.out.println("Price: " + price);
        } else if (priceObj instanceof String) {
            Double price = Double.parseDouble((String) priceObj);
            market.setPrice(price);
            System.out.println("Price (parsed): " + price);
        } else {
            System.out.println("Price type: " + priceObj.getClass().getName());
        }
        market.setItemName("风车");
        market.setCurrency("宝石");
        MarketVo marketVo = new MarketVo();
        BeanUtils.copyProperties(market, marketVo);
        sendMarketMessage(market);
    }

    //11.沙砾
    @Override
    public void getGravel() {
        String requestUrl = "/v11/api/sand/trade/sale/list";
        String url = "https://fks-api.lucklyworld.com/v11/api/sand/trade/sale/list";
        String paramsPart3 = "page=1";// 入参
        String version = "5.2.2";
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
        Map<String, String> formParams = new LinkedHashMap<>();
        formParams.put("page", "1");
        Map<String, List<Map<String, Object>>> response = apiClient.postFormWithSign(
                url, urlParams, formParams, result, ts, version, Host, Map.class
        );

        // 解析响应（list[0].price，单位宝石）
        List<Map<String, Object>> list = (List<Map<String, Object>>) response.get("list");
        //log.info("price:" + list);
        Map<String, Object> item = list.get(0);
        Object priceObj = item.get("price");
        // 根据实际类型转换
        Market market = new Market();
        if (priceObj instanceof Double) {
            Double price = (Double) priceObj;
            market.setPrice(price);
            System.out.println("Price: " + price);
        } else if (priceObj instanceof String) {
            Double price = Double.parseDouble((String) priceObj);
            market.setPrice(price);
            System.out.println("Price (parsed): " + price);
        } else {
            System.out.println("Price type: " + priceObj.getClass().getName());
        }
        market.setItemName("沙砾");
        market.setCurrency("宝石");
        MarketVo marketVo = new MarketVo();
        BeanUtils.copyProperties(market, marketVo);
        sendMarketMessage(market);
    }

    //12. 时间积分
    @Override
    public void getTimePoints() {
        String requestUrl = "/v11/api/mr/time/points/trade/sale/list";
        String url = "https://fks-api.lucklyworld.com/v11/api/mr/time/points/trade/sale/list";
        String paramsPart3 = "page=1";// 入参
        String version = "5.2.2";
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
        Map<String, String> formParams = new LinkedHashMap<>();
        formParams.put("page", "1");
        Map<String, List<Map<String, Object>>> response = apiClient.postFormWithSign(
                url, urlParams, formParams, result, ts, version, Host, Map.class
        );
        // 解析响应（list[0].price，单位宝石）
        List<Map<String, Object>> list = (List<Map<String, Object>>) response.get("list");
        //log.info("price:" + list);
        Map<String, Object> item = list.get(0);
        Object priceObj = item.get("price");
        // 根据实际类型转换
        Market market = new Market();
        if (priceObj instanceof Double) {
            Double price = (Double) priceObj;
            market.setPrice(price);
            System.out.println("Price: " + price);
        } else if (priceObj instanceof String) {
            Double price = Double.parseDouble((String) priceObj);
            market.setPrice(price);
            System.out.println("Price (parsed): " + price);
        } else {
            System.out.println("Price type: " + priceObj.getClass().getName());
        }
        market.setItemName("时间积分");
        market.setCurrency("宝石");
        MarketVo marketVo = new MarketVo();
        BeanUtils.copyProperties(market, marketVo);
        sendMarketMessage(market);
    }

    //13. 散仙
    @Override
    public void getLooseFairy() {
        String requestUrl = "/v11/api/jinnee/trade/sale/list";
        String url = "https://fks-api.lucklyworld.com/v11/api/jinnee/trade/sale/list";
        String paramsPart3 = "page=1&orderByField=3&orderBy=2&level=1";// 入参
        String version = "5.2.2";
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
        Map<String, String> formParams = new LinkedHashMap<>();
        formParams.put("page", "1");
        formParams.put("orderByField", "3");
        formParams.put("orderBy", "2");
        formParams.put("level", "1");
        Map<String, List<Map<String, Object>>> response = apiClient.postFormWithSign(
                url, urlParams, formParams, result, ts, version, Host, Map.class
        );
        // 解析响应（list[0].price，单位矿石）
        List<Map<String, Object>> list = (List<Map<String, Object>>) response.get("list");
        //log.info("price:" + list);
        Map<String, Object> item = list.get(0);
        Object priceObj = item.get("price");
        // 根据实际类型转换
        Market market = new Market();
        if (priceObj instanceof Double) {
            Double price = (Double) priceObj;
            market.setPrice(price);
            System.out.println("Price: " + price);
        } else if (priceObj instanceof String) {
            Double price = Double.parseDouble((String) priceObj);
            market.setPrice(price);
            System.out.println("Price (parsed): " + price);
        } else {
            System.out.println("Price type: " + priceObj.getClass().getName());
        }
        market.setItemName("散仙");
        market.setCurrency("矿石");
        MarketVo marketVo = new MarketVo();
        BeanUtils.copyProperties(market, marketVo);
        sendMarketMessage(market);
    }

    //14. 地仙
    @Override
    public void getEarthFairy() {
        String requestUrl = "/v11/api/jinnee/trade/sale/list";
        String url = "https://fks-api.lucklyworld.com/v11/api/jinnee/trade/sale/list";
        String paramsPart3 = "page=1&orderByField=3&orderBy=2&level=2";// 入参
        String version = "5.2.2";
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
        Map<String, String> formParams = new LinkedHashMap<>();
        formParams.put("page", "1");
        formParams.put("orderByField", "3");
        formParams.put("orderBy", "2");
        formParams.put("level", "2");
        Map<String, List<Map<String, Object>>> response = apiClient.postFormWithSign(
                url, urlParams, formParams, result, ts, version, Host, Map.class
        );
        // 解析响应（list[0].price，单位矿石）
        List<Map<String, Object>> list = (List<Map<String, Object>>) response.get("list");
        //log.info("price:" + list);
        Map<String, Object> item = list.get(0);
        Object priceObj = item.get("price");
        // 根据实际类型转换
        Market market = new Market();
        if (priceObj instanceof Double) {
            Double price = (Double) priceObj;
            market.setPrice(price);
            System.out.println("Price: " + price);
        } else if (priceObj instanceof String) {
            Double price = Double.parseDouble((String) priceObj);
            market.setPrice(price);
            System.out.println("Price (parsed): " + price);
        } else {
            System.out.println("Price type: " + priceObj.getClass().getName());
        }
        market.setItemName("地仙");
        market.setCurrency("矿石");
        MarketVo marketVo = new MarketVo();
        BeanUtils.copyProperties(market, marketVo);
        sendMarketMessage(market);
    }

    //15.真仙
    @Override
    public void getTrueFairy() {
        String requestUrl = "/v11/api/jinnee/trade/sale/list";
        String url = "https://fks-api.lucklyworld.com/v11/api/jinnee/trade/sale/list";
        String paramsPart3 = "page=1&orderByField=3&orderBy=2&level=3";// 入参
        String version = "5.2.2";
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
        Map<String, String> formParams = new LinkedHashMap<>();
        formParams.put("page", "1");
        formParams.put("orderByField", "3");
        formParams.put("orderBy", "2");
        formParams.put("level", "3");
        Map<String, List<Map<String, Object>>> response = apiClient.postFormWithSign(
                url, urlParams, formParams, result, ts, version, Host, Map.class
        );
        // 解析响应（list[0].price，单位矿石）
        List<Map<String, Object>> list = (List<Map<String, Object>>) response.get("list");
        //log.info("price:" + list);
        Map<String, Object> item = list.get(0);
        Object priceObj = item.get("price");
        // 根据实际类型转换
        Market market = new Market();
        if (priceObj instanceof Double) {
            Double price = (Double) priceObj;
            market.setPrice(price);
            System.out.println("Price: " + price);
        } else if (priceObj instanceof String) {
            Double price = Double.parseDouble((String) priceObj);
            market.setPrice(price);
            System.out.println("Price (parsed): " + price);
        } else {
            System.out.println("Price type: " + priceObj.getClass().getName());
        }
        market.setItemName("真仙");
        market.setCurrency("矿石");
        MarketVo marketVo = new MarketVo();
        BeanUtils.copyProperties(market, marketVo);
        sendMarketMessage(market);
    }

    //16.天仙
    @Override
    public void getHeavenlyFairy() {
        String requestUrl = "/v11/api/jinnee/trade/sale/list";
        String url = "https://fks-api.lucklyworld.com/v11/api/jinnee/trade/sale/list";
        String paramsPart3 = "page=1&orderByField=3&orderBy=2&level=4";// 入参
        String version = "5.2.2";
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
        Map<String, String> formParams = new LinkedHashMap<>();
        formParams.put("page", "1");
        formParams.put("orderByField", "3");
        formParams.put("orderBy", "2");
        formParams.put("level", "4");
        Map<String, List<Map<String, Object>>> response = apiClient.postFormWithSign(
                url, urlParams, formParams, result, ts, version, Host, Map.class
        );
        // 解析响应（list[0].price，单位矿石）
        List<Map<String, Object>> list = (List<Map<String, Object>>) response.get("list");
        //log.info("price:" + list);
        Map<String, Object> item = list.get(0);
        Object priceObj = item.get("price");
        // 根据实际类型转换
        Market market = new Market();
        if (priceObj instanceof Double) {
            Double price = (Double) priceObj;
            market.setPrice(price);
            System.out.println("Price: " + price);
        } else if (priceObj instanceof String) {
            Double price = Double.parseDouble((String) priceObj);
            market.setPrice(price);
            System.out.println("Price (parsed): " + price);
        } else {
            System.out.println("Price type: " + priceObj.getClass().getName());
        }
        market.setItemName("天仙");
        market.setCurrency("矿石");
        MarketVo marketVo = new MarketVo();
        BeanUtils.copyProperties(market, marketVo);
        sendMarketMessage(market);
    }

    //17，金仙
    @Override
    public void getGoldenFairy() {
        String requestUrl = "/v11/api/jinnee/trade/sale/list";
        String url = "https://fks-api.lucklyworld.com/v11/api/jinnee/trade/sale/list";
        String paramsPart3 = "page=1&orderByField=3&orderBy=2&level=5";// 入参
        String version = "5.2.2";
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
        Map<String, String> formParams = new LinkedHashMap<>();
        formParams.put("page", "1");
        formParams.put("orderByField", "3");
        formParams.put("orderBy", "2");
        formParams.put("level", "5");
        Map<String, List<Map<String, Object>>> response = apiClient.postFormWithSign(
                url, urlParams, formParams, result, ts, version, Host, Map.class
        );
        // 解析响应（list[0].price，单位矿石）
        List<Map<String, Object>> list = (List<Map<String, Object>>) response.get("list");
        //log.info("price:" + list);
        Map<String, Object> item = list.get(0);
        Object priceObj = item.get("price");
        // 根据实际类型转换
        Market market = new Market();
        if (priceObj instanceof Double) {
            Double price = (Double) priceObj;
            market.setPrice(price);
            System.out.println("Price: " + price);
        } else if (priceObj instanceof String) {
            Double price = Double.parseDouble((String) priceObj);
            market.setPrice(price);
            System.out.println("Price (parsed): " + price);
        } else {
            System.out.println("Price type: " + priceObj.getClass().getName());
        }
        market.setItemName("金仙");
        market.setCurrency("矿石");
        MarketVo marketVo = new MarketVo();
        BeanUtils.copyProperties(market, marketVo);
        sendMarketMessage(market);
    }

    //18.法力
    @Override
    public void getMagicPower() {
        String requestUrl = "/v11/api/fks/energy/trade/sale/list";
        String url = "https://fks-api.lucklyworld.com/v11/api/fks/energy/trade/sale/list";
        String paramsPart3 = "page=1";// 入参
        String version = "5.2.2";
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
        Map<String, String> formParams = new LinkedHashMap<>();
        formParams.put("page", "1");
        Map<String, List<Map<String, Object>>> response = apiClient.postFormWithSign(
                url, urlParams, formParams, result, ts, version, Host, Map.class
        );
        // 解析响应（list[0].price，单位矿石）
        List<Map<String, Object>> list = (List<Map<String, Object>>) response.get("list");
        //log.info("price:" + list);
        Map<String, Object> item = list.get(0);
        Object priceObj = item.get("price");
        // 根据实际类型转换
        Market market = new Market();
        if (priceObj instanceof Double) {
            Double price = (Double) priceObj;
            market.setPrice(price);
            System.out.println("Price: " + price);
        } else if (priceObj instanceof String) {
            Double price = Double.parseDouble((String) priceObj);
            market.setPrice(price);
            System.out.println("Price (parsed): " + price);
        } else {
            System.out.println("Price type: " + priceObj.getClass().getName());
        }
        market.setItemName("法力");
        market.setCurrency("矿石");
        MarketVo marketVo = new MarketVo();
        BeanUtils.copyProperties(market, marketVo);
        sendMarketMessage(market);
    }

    //19.骷髅头
    @Override
    public void getSkull() {
        String requestUrl = "/v11/api/fks/skull/trade/sale/list";
        String url = "https://fks-api.lucklyworld.com/v11/api/fks/skull/trade/sale/list";
        String paramsPart3 = "page=1";// 入参
        String version = "5.2.2";
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
        Map<String, String> formParams = new LinkedHashMap<>();
        formParams.put("page", "1");
        Map<String, List<Map<String, Object>>> response = apiClient.postFormWithSign(
                url, urlParams, formParams, result, ts, version, Host, Map.class
        );
        // 解析响应（list[0].price，单位矿石）
        List<Map<String, Object>> list = (List<Map<String, Object>>) response.get("list");
        //log.info("price:" + list);
        Map<String, Object> item = list.get(0);
        Object priceObj = item.get("price");
        // 根据实际类型转换
        Market market = new Market();
        if (priceObj instanceof Double) {
            Double price = (Double) priceObj;
            market.setPrice(price);
            System.out.println("Price: " + price);
        } else if (priceObj instanceof String) {
            Double price = Double.parseDouble((String) priceObj);
            market.setPrice(price);
            System.out.println("Price (parsed): " + price);
        } else {
            System.out.println("Price type: " + priceObj.getClass().getName());
        }
        market.setItemName("骷髅头");
        market.setCurrency("矿石");
        MarketVo marketVo = new MarketVo();
        BeanUtils.copyProperties(market, marketVo);
        sendMarketMessage(market);
    }

    //20.金石
    @Override
    public void getGoldStone() {
        String requestUrl = "/v11/api/fks/skull/trade/sale/list";
        String url = "https://fks-api.lucklyworld.com/v11/api/fks/skull/trade/sale/list";
        String paramsPart3 = "page=1&productType=2";// 入参
        String version = "5.2.2";
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
        Map<String, String> formParams = new LinkedHashMap<>();
        formParams.put("page", "1");
        formParams.put("productType", "2");
        Map<String, List<Map<String, Object>>> response = apiClient.postFormWithSign(
                url, urlParams, formParams, result, ts, version, Host, Map.class
        );
        // 解析响应（list[0].price，单位矿石）
        List<Map<String, Object>> list = (List<Map<String, Object>>) response.get("list");
        //log.info("price:" + list);
        Map<String, Object> item = list.get(0);
        Object priceObj = item.get("price");
        // 根据实际类型转换
        Market market = new Market();
        if (priceObj instanceof Double) {
            Double price = (Double) priceObj;
            market.setPrice(price);
            System.out.println("Price: " + price);
        } else if (priceObj instanceof String) {
            Double price = Double.parseDouble((String) priceObj);
            market.setPrice(price);
            System.out.println("Price (parsed): " + price);
        } else {
            System.out.println("Price type: " + priceObj.getClass().getName());
        }
        market.setItemName("金石");
        market.setCurrency("矿石");
        MarketVo marketVo = new MarketVo();
        BeanUtils.copyProperties(market, marketVo);
        sendMarketMessage(market);
    }

    //21.灵石
    @Override
    public void getNimbusStone() {
        String requestUrl = "/v8/api/farm/stone/sell/order";
        String url = "https://farm-api.lucklyworld.com/v8/api/farm/stone/sell/order";
        String paramsPart3 = "next=0";// 入参
        String version = "2.1.0";
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
        Map<String, String> formParams = new LinkedHashMap<>();
        formParams.put("next", "0");
        Map<String, List<Map<String, Object>>> response = apiClient.postFormWithSign(
                url, urlParams, formParams, result, ts, version, NimbusStoneHost, Map.class
        );
        // 解析响应（list[0].price，单位矿石）
        List<Map<String, Object>> list = (List<Map<String, Object>>) response.get("items");
        //log.info("price:" + list);
        Map<String, Object> item = list.get(0);
        Object priceObj = item.get("price");
        // 根据实际类型转换
        Market market = new Market();
        if (priceObj instanceof Double) {
            Double price = (Double) priceObj;
            market.setPrice(price);
            System.out.println("Price: " + price);
        } else if (priceObj instanceof String) {
            Double price = Double.parseDouble((String) priceObj);
            market.setPrice(price);
            System.out.println("Price (parsed): " + price);
        } else {
            System.out.println("Price type: " + priceObj.getClass().getName());
        }
        market.setItemName("灵石");
        market.setCurrency("宝石");
        MarketVo marketVo = new MarketVo();
        BeanUtils.copyProperties(market, marketVo);
        sendMarketMessage(market);
    }

//矿石接口
@Override
public void getOre() {
    String requestUrl = "/v11/api/fks/ore/trade/sale/list";
    String url = "https://fks-api.lucklyworld.com/v11/api/fks/ore/trade/sale/list";
    String paramsPart3 = "page=1";// 入参
    String version = "5.2.3";
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
    Map<String, String> formParams = new LinkedHashMap<>();
    formParams.put("page", "1");
    Map<String, List<Map<String, Object>>> response = apiClient.postFormWithSign(
            url, urlParams, formParams, result, ts, version, Host, Map.class
    );
    // 解析响应（list[0].price，单位矿石）
    List<Map<String, Object>> list = (List<Map<String, Object>>) response.get("list");
    //log.info("price:" + list);
    Map<String, Object> item = list.get(0);
    Object priceObj = item.get("price");
    // 根据实际类型转换
    Market market = new Market();
    if (priceObj instanceof Double) {
        Double price = (Double) priceObj;
        market.setPrice(price);
        System.out.println("Price: " + price);
    } else if (priceObj instanceof String) {
        Double price = Double.parseDouble((String) priceObj);
        market.setPrice(price);
        System.out.println("Price (parsed): " + price);
    } else {
        System.out.println("Price type: " + priceObj.getClass().getName());
    }
    market.setItemName("矿石");
    market.setCurrency("宝石");
    MarketVo marketVo = new MarketVo();
    BeanUtils.copyProperties(market, marketVo);
    sendMarketMessage(market);
}
    //23笔画
    @Override
    public void getStroke() {
        String requestUrl = "/v11/api/stroke/trade/sale/list";
        String url = "https://fks-api.lucklyworld.com/v11/api/stroke/trade/sale/list";
        String paramsPart3 = "page=1";// 入参
        String version = "5.2.3";
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
        Map<String, String> formParams = new LinkedHashMap<>();
        formParams.put("page", "1");
        Map<String, List<Map<String, Object>>> response = apiClient.postFormWithSign(
                url, urlParams, formParams, result, ts, version, NimbusStoneHost, Map.class
        );
        log.info("response", response);
        // 解析响应（list[0].price，单位矿石）
        List<Map<String, Object>> list = (List<Map<String, Object>>) response.get("list");
        //log.info("price:" + list);
        Map<String, Object> item = list.get(0);
        Object priceObj = item.get("price");
        // 根据实际类型转换
        Market market = new Market();
        if (priceObj instanceof Double) {
            Double price = (Double) priceObj;
            market.setPrice(price);
            System.out.println("Price: " + price);
        } else if (priceObj instanceof String) {
            Double price = Double.parseDouble((String) priceObj);
            market.setPrice(price);
            System.out.println("Price (parsed): " + price);
        } else {
            System.out.println("Price type: " + priceObj.getClass().getName());
        }
        market.setItemName("笔画");
        market.setCurrency("宝石");
        MarketVo marketVo = new MarketVo();
        BeanUtils.copyProperties(market, marketVo);
        sendMarketMessage(market);
    }

    /**
     * 仙侠宇宙
     */
    //24静心丸
    @Override
    public void getJingxinPills() {
        String requestUrl = "/v8/api/game/auction/sell/order";
        String url = "https://farm-api.lucklyworld.com/v8/api/game/auction/sell/order";
        String paramsPart3 = "next=0&type=7 ";// 入参
        Map<String, String> formParams = new LinkedHashMap<>();
        formParams.put("next", "0");
        formParams.put("type", "7");
        String version = "2.1.3";
        List<Map<String, Object>> list = getMapsRw(requestUrl, version, paramsPart3, url,formParams);
        //log.info("price:" + list);
        Map<String, Object> item = list.get(0);
        Object priceObj = item.get("price");
        // 根据实际类型转换
        Market market = new Market();
        if (priceObj instanceof Double) {
            Double price = (Double) priceObj;
            market.setPrice(price);
            System.out.println("Price: " + price);
        } else if (priceObj instanceof String) {
            Double price = Double.parseDouble((String) priceObj);
            market.setPrice(price);
            System.out.println("Price (parsed): " + price);
        } else {
            System.out.println("Price type: " + priceObj.getClass().getName());
        }
        market.setItemName("静心丸");
        market.setCurrency("灵石");
        MarketVo marketVo = new MarketVo();
        BeanUtils.copyProperties(market, marketVo);
        sendMarketMessage(market);
    }

    //25完璧符
    @Override
    public void getPerfectTalisman() {
        String requestUrl = "/v8/api/game/auction/sell/order";
        String url = "https://farm-api.lucklyworld.com/v8/api/game/auction/sell/order";
        String paramsPart3 = "next=0&type=8&grade=1 ";// 入参
        Map<String, String> formParams = new LinkedHashMap<>();
        formParams.put("next", "0");
        formParams.put("type", "8");
        formParams.put("grade", "1");
        String version = "2.1.3";
        List<Map<String, Object>> list = getMapsRw(requestUrl, version, paramsPart3, url, formParams );
        Map<String, Object> item = list.get(0);
        Object priceObj = item.get("price");
        // 根据实际类型转换
        Market market = new Market();
        if (priceObj instanceof Double) {
            Double price = (Double) priceObj;
            market.setPrice(price);
            System.out.println("Price: " + price);
        } else if (priceObj instanceof String) {
            Double price = Double.parseDouble((String) priceObj);
            market.setPrice(price);
            System.out.println("Price (parsed): " + price);
        } else {
            System.out.println("Price type: " + priceObj.getClass().getName());
        }
        market.setItemName("完璧符");
        market.setCurrency("灵石");
        MarketVo marketVo = new MarketVo();
        BeanUtils.copyProperties(market, marketVo);
        sendMarketMessage(market);
    }

    //26吉星符
    @Override
    public void getAuspiciousStarTalisman() {
        String requestUrl = "/v8/api/game/auction/sell/order";
        String url = "https://farm-api.lucklyworld.com/v8/api/game/auction/sell/order";
        String paramsPart3 = "next=0&type=9&grade=1";// 入参
        Map<String, String> formParams = new LinkedHashMap<>();
        formParams.put("next", "0");
        formParams.put("type", "9");
        formParams.put("grade", "1");
        String version = "2.1.3";
        List<Map<String, Object>> list = getMapsRw(requestUrl, version, paramsPart3, url, formParams );
        Map<String, Object> item = list.get(0);
        Object priceObj = item.get("price");
        // 根据实际类型转换
        Market market = new Market();
        if (priceObj instanceof Double) {
            Double price = (Double) priceObj;
            market.setPrice(price);
            System.out.println("Price: " + price);
        } else if (priceObj instanceof String) {
            Double price = Double.parseDouble((String) priceObj);
            market.setPrice(price);
            System.out.println("Price (parsed): " + price);
        } else {
            System.out.println("Price type: " + priceObj.getClass().getName());
        }
        market.setItemName("完璧符");
        market.setCurrency("灵石");
        MarketVo marketVo = new MarketVo();
        BeanUtils.copyProperties(market, marketVo);
        sendMarketMessage(market);
    }

    //27仙种
    @Override
    public void getImmortalSpecies() {
        String requestUrl = "/v8/api/farm/seed/sell/order";
        String url = "https://farm-api.lucklyworld.com/v8/api/farm/seed/sell/order";
        String paramsPart3 = "next=0";// 入参
        Map<String, String> formParams = new LinkedHashMap<>();
        formParams.put("next", "0");
        String version = "2.1.3";
        List<Map<String, Object>> list = getMapsRw(requestUrl, version, paramsPart3, url, formParams );
        Map<String, Object> item = list.get(0);
        Object priceObj = item.get("price");
        // 根据实际类型转换
        Market market = new Market();
        if (priceObj instanceof Double) {
            Double price = (Double) priceObj;
            market.setPrice(price);
            System.out.println("Price: " + price);
        } else if (priceObj instanceof String) {
            Double price = Double.parseDouble((String) priceObj);
            market.setPrice(price);
            System.out.println("Price (parsed): " + price);
        } else {
            System.out.println("Price type: " + priceObj.getClass().getName());
        }
        market.setItemName("仙种");
        market.setCurrency("宝石");
        MarketVo marketVo = new MarketVo();
        BeanUtils.copyProperties(market, marketVo);
        sendMarketMessage(market);
    }

    //28造化果
    @Override
    public void getCreationFruit() {
        String requestUrl = "/v8/api/game/auction/sell/order";
        String url = "https://farm-api.lucklyworld.com/v8/api/game/auction/sell/order";
        String paramsPart3 = "next=0&type=1";// 入参
        Map<String, String> formParams = new LinkedHashMap<>();
        formParams.put("next", "0");
        formParams.put("type", "1");
        String version = "2.1.3";
        List<Map<String, Object>> list = getMapsRw(requestUrl, version, paramsPart3, url, formParams );
        Map<String, Object> item = list.get(0);
        Object priceObj = item.get("price");
        // 根据实际类型转换
        Market market = new Market();
        if (priceObj instanceof Double) {
            Double price = (Double) priceObj;
            market.setPrice(price);
            System.out.println("Price: " + price);
        } else if (priceObj instanceof String) {
            Double price = Double.parseDouble((String) priceObj);
            market.setPrice(price);
            System.out.println("Price (parsed): " + price);
        } else {
            System.out.println("Price type: " + priceObj.getClass().getName());
        }
        market.setItemName("造化果");
        market.setCurrency("灵石");
        MarketVo marketVo = new MarketVo();
        BeanUtils.copyProperties(market, marketVo);
        sendMarketMessage(market);
    }
    //29功德石包
    @Override
    public void getMeritStonePackage() {
        String requestUrl = "/v8/api/game/auction/sell/order";
        String url = "https://farm-api.lucklyworld.com/v8/api/game/auction/sell/order";
        String paramsPart3 = "next=0&type=6 ";// 入参
        Map<String, String> formParams = new LinkedHashMap<>();
        formParams.put("next", "0");
        formParams.put("type", "6");
        String version = "2.1.3";
        List<Map<String, Object>> list = getMapsRw(requestUrl, version, paramsPart3, url, formParams );
        Map<String, Object> item = list.get(0);
        Object priceObj = item.get("price");
        // 根据实际类型转换
        Market market = new Market();
        if (priceObj instanceof Double) {
            Double price = (Double) priceObj;
            market.setPrice(price);
            System.out.println("Price: " + price);
        } else if (priceObj instanceof String) {
            Double price = Double.parseDouble((String) priceObj);
            market.setPrice(price);
            System.out.println("Price (parsed): " + price);
        } else {
            System.out.println("Price type: " + priceObj.getClass().getName());
        }
        market.setItemName("功德石包");
        market.setCurrency("灵石");
        MarketVo marketVo = new MarketVo();
        BeanUtils.copyProperties(market, marketVo);
        sendMarketMessage(market);
    }
    //30人参
    @Override
    public void getGinseng() {
        String requestUrl = "/v8/api/share/sell/order";
        String url = "https://farm-api.lucklyworld.com/v8/api/share/sell/order";
        String paramsPart3 = "next=0";// 入参
        Map<String, String> formParams = new LinkedHashMap<>();
        formParams.put("next", "0");
        String version = "2.1.3";
        List<Map<String, Object>> list = getMapsRw(requestUrl, version, paramsPart3, url, formParams );
        Map<String, Object> item = list.get(0);
        Object priceObj = item.get("price");
        // 根据实际类型转换
        Market market = new Market();
        if (priceObj instanceof Double) {
            Double price = (Double) priceObj;
            market.setPrice(price);
            System.out.println("Price: " + price);
        } else if (priceObj instanceof String) {
            Double price = Double.parseDouble((String) priceObj);
            market.setPrice(price);
            System.out.println("Price (parsed): " + price);
        } else {
            System.out.println("Price type: " + priceObj.getClass().getName());
        }
        market.setItemName("人参");
        market.setCurrency("灵石");
        MarketVo marketVo = new MarketVo();
        BeanUtils.copyProperties(market, marketVo);
        sendMarketMessage(market);
    }
    //31仙侠宇宙虚拟股
    @Override
    public void getRwVirtualStocks() {
        String requestUrl = "/v8/api/share/sell/order";
        String url = "https://farm-api.lucklyworld.com/v8/api/share/sell/order";
        String paramsPart3 = "next=0";// 入参
        Map<String, String> formParams = new LinkedHashMap<>();
        formParams.put("next", "0");
        String version = "2.1.3";
        List<Map<String, Object>> list = getMapsRw(requestUrl, version, paramsPart3, url, formParams );
        Map<String, Object> item = list.get(0);
        Object priceObj = item.get("price");
        // 根据实际类型转换
        Market market = new Market();
        if (priceObj instanceof Double) {
            Double price = (Double) priceObj;
            market.setPrice(price);
            System.out.println("Price: " + price);
        } else if (priceObj instanceof String) {
            Double price = Double.parseDouble((String) priceObj);
            market.setPrice(price);
            System.out.println("Price (parsed): " + price);
        } else {
            System.out.println("Price type: " + priceObj.getClass().getName());
        }
        market.setItemName("仙侠宇宙虚拟股");
        market.setCurrency("灵石");
        MarketVo marketVo = new MarketVo();
        BeanUtils.copyProperties(market, marketVo);
        sendMarketMessage(market);
    }
    //32每日空投
    @Override
    public void getAirDrop() {
        String requestUrl = "/v9/api/super/game/airdrop/index";
        String url = "https://api.chaojilianjie.cn/v9/api/super/game/airdrop/index";
        String paramsPart3 = "groupId=1";// 入参
        Map<String, String> formParams = new LinkedHashMap<>();
        formParams.put("groupId", "1");
        String version = "4.1.5";
        List<Map<String, Object>> list = getMapsRw(requestUrl, version, paramsPart3, url, formParams );
        Map<String, Object> item = list.get(0);
        Object priceObj = item.get("price");
        // 根据实际类型转换
        Market market = new Market();
        if (priceObj instanceof Double) {
            Double price = (Double) priceObj;
            market.setPrice(price);
            System.out.println("Price: " + price);
        } else if (priceObj instanceof String) {
            Double price = Double.parseDouble((String) priceObj);
            market.setPrice(price);
            System.out.println("Price (parsed): " + price);
        } else {
            System.out.println("Price type: " + priceObj.getClass().getName());
        }
        market.setItemName("每日空投");
        market.setCurrency("灵石");
        MarketVo marketVo = new MarketVo();
        BeanUtils.copyProperties(market, marketVo);
        sendMarketMessage(market);
    }
    private List<Map<String, Object>> getMapsRw(String requestUrl, String version, String paramsPart3, String url,Map<String, String> formParams) {
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
        Map<String, List<Map<String, Object>>> response = apiClient.postFormWithRw(
                url, urlParams, formParams, result, ts, version, NimbusStoneHost, Map.class
        );
        log.info("response", response);
        // 解析响应（list[0].price，单位矿石）
        List<Map<String, Object>> list = (List<Map<String, Object>>) response.get("items");
        return list;
    }

    @Override
    public void autoUpdateDate() {
        Random random = new Random();
        int sleepTime = 800 + random.nextInt(200);
        this.getLooseFairy();//13散仙no
//        this.getSuperCurrency();//1更新超级币no
        this.getBase();//2更新基石yes
        this.getSeashell();//3更新贝壳yes
        this.getPenguins();//4更新胖胖鹅yes
        this.getTurtleEggs();//5更新龟蛋yes
        this.getTurtleBoys();//6更新龟仔yes
        this.getArmor();//7更新护甲yes
        this.getChineseCharacter();//8y
        this.getEarthFairy();//14地仙yes
        this.getSteel();//9y
        this.getWindmill();//10y
        this.getGravel();//11y
        this.getTimePoints();//12y
        try {
            this.getSkull();//19骷髅头yes
            Thread.sleep(sleepTime + random.nextInt(1000)); // 睡眠，反爬虫
        } catch (InterruptedException e) {
            e.printStackTrace();
            // 当线程在睡眠时被中断，会抛出此异常
        }
        sleepTime = 1000 + random.nextInt(200);
        try {
            this.getTrueFairy();//15真仙yes
            Thread.sleep(sleepTime); // 线程暂停 1000毫秒（即 1 秒）
        } catch (InterruptedException e) {
            e.printStackTrace();
            // 当线程在睡眠时被中断，会抛出此异常
        }
        this.getHeavenlyFairy();//16天仙no
        this.getMagicPower();//18法力yes
        this.getGoldStone();//20金石yes
        sleepTime = 1000 + random.nextInt(170);
        try {
            this.getNimbusStone();//21灵石yes
            Thread.sleep(sleepTime); // 线程暂停 1000毫秒（即 1 秒）
        } catch (InterruptedException e) {
            e.printStackTrace();
            // 当线程在睡眠时被中断，会抛出此异常
        }
        this.getOre();//22矿石yes
        try {
            Thread.sleep(sleepTime); // 线程暂停 1000毫秒（即 1 秒）
            this.getStroke();//23笔画
        } catch (InterruptedException e) {
            e.printStackTrace();
            // 当线程在睡眠时被中断，会抛出此异常
        }

    }

    @Scheduled(cron = "0 0/15 * * * ?")
    @Async("marketThreadPool")
    void dojinxian() {
        try {
            Thread.sleep(1000); // 线程暂停 1000毫秒（即 1 秒）
            this.getGoldenFairy();//17金仙yes
        } catch (InterruptedException e) {
            e.printStackTrace();
            // 当线程在睡眠时被中断，会抛出此异常
        }
    }

    @Override
    @Scheduled(cron = "0 0/10 * * * ?")
    @Async("marketThreadPool") // 指定线程池名称
    public void ScheduledUpdateDate() {
        log.info("定时任务开始执行..."); // 添加日志
        autoUpdateDate();
        log.info("定时任务执行完成！");
    }


    @Scheduled(cron = "0 0/10 * * * ?")
    @Override
    @Async("rwThreadPool") // 指定线程池名称
    public void RwScheduledUpdateDate() {
        log.info("定时任务开始执行..."); // 添加日志
        RwUpdateDate();
        log.info("定时任务执行完成！");
    }

    private void RwUpdateDate() {
        try {
            getJingxinPills();
            Thread.sleep(1000+new Random().nextInt(200)); // 线程暂停 1000毫秒（即 1 秒）
        } catch (InterruptedException e) {
            e.printStackTrace();
            // 当线程在睡眠时被中断，会抛出此异常
        }
        try {
           getPerfectTalisman();
            Thread.sleep(1000+new Random().nextInt(200)); // 线程暂停 1000毫秒（即 1 秒）
        } catch (InterruptedException e) {
            e.printStackTrace();
            // 当线程在睡眠时被中断，会抛出此异常
        }
        try {
            getAuspiciousStarTalisman();
            Thread.sleep(1000+new Random().nextInt(200)); // 线程暂停 1000毫秒（即 1 秒）
        } catch (InterruptedException e) {
            e.printStackTrace();
            // 当线程在睡眠时被中断，会抛出此异常
        }
        try {
            getImmortalSpecies();
            Thread.sleep(1000+new Random().nextInt(200)); // 线程暂停 1000毫秒（即 1 秒）
        } catch (InterruptedException e) {
            e.printStackTrace();
            // 当线程在睡眠时被中断，会抛出此异常
        }
        try {
            getCreationFruit();//造化果
            Thread.sleep(1000+new Random().nextInt(200)); // 线程暂停 1000毫秒（即 1 秒）
        } catch (InterruptedException e) {
            e.printStackTrace();
            // 当线程在睡眠时被中断，会抛出此异常
        }
        try {
            getMeritStonePackage() ; //29功德石包
            Thread.sleep(1000+new Random().nextInt(200)); // 线程暂停 1000毫秒（即 1 秒）
        } catch (InterruptedException e) {
            e.printStackTrace();
            // 当线程在睡眠时被中断，会抛出此异常
        }
        try {
            getGinseng();//30人参
            Thread.sleep(1000+new Random().nextInt(200)); // 线程暂停 1000毫秒（即 1 秒）
        } catch (InterruptedException e) {
            e.printStackTrace();
            // 当线程在睡眠时被中断，会抛出此异常
        }
        getRwVirtualStocks();//31仙侠宇宙虚拟股
    }

    /**
     * 消息发送
     *
     * @param market
     */
    @Override
    public void sendMarketMessage(Market market) {
        MarketMessage marketMessage = new MarketMessage(
                market.getItemName(),
                market.getPrice(),
                market.getCurrency()
        );
        rabbitTemplate.convertAndSend(exchangeName, routingKey, marketMessage);
        log.info("Sent market data to queue: {}", marketMessage);
    }


    @Override
    public MarketVo getPriceByItemName(String itemName) {
        return getAllPrices().stream()
                .filter(price -> price.getItemName().equalsIgnoreCase(itemName))
                .findFirst()
                .orElse(null);
    }


}