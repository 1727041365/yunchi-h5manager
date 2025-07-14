package com.yupi.springbootinit.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yupi.springbootinit.client.ApiClient;
import com.yupi.springbootinit.mapper.MarketMapper;
import com.yupi.springbootinit.model.entity.Market;
import com.yupi.springbootinit.model.mq.MarketMessage;
import com.yupi.springbootinit.model.vo.MarketVo;
import com.yupi.springbootinit.service.PriceService;
import com.yupi.springbootinit.utils.HmacSha256Utils;
import com.yupi.springbootinit.utils.RequestStringGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
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
    private static final String UID = "12422457"; // 固定用户ID
    private static final String TOKEN = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiIiLCJhdWQiOiIiLCJqdGkiOiIxMjQyMjQ1NyIsImlhdCI6MTc1MjExNTU1MCwibmJmIjoxNzUyMTE1NTUwLCJleHAiOjE3NTM5NTI5MjMsInR5cGUiOiIyMDk3NV92MTFhcHAiLCJhdXRoZW50aWNhdGlvblN0YXRlIjpmYWxzZX0.aVoPWd1th4W27pG1dFcugvFLngZ8zEkyRoy9JldZ-1I";
    private static final String KEY = "BHbE9oCgl58NUz5oJVDUFMLJO9vGQnvdv0Lem3315wQG8laB4dGcxIXFLfDsInHTa";

    public PriceServiceImpl(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<MarketVo> getAllPrices() {
        List<Market> marketList= this.list();
        List<MarketVo> marketVoList = marketList.stream().map(market -> {
            MarketVo marketVo = new MarketVo();
            BeanUtils.copyProperties(market, marketVo);
            return marketVo;
        }).collect(Collectors.toList());
        return marketVoList;
    }


    //    // 1. 超级币
//    private PriceResponse getSuperCoinPrice() {
//        String url = "https://super-link-api.lucklyworld.com/v9/api/trade/purchase/list";
//        // URL参数（uid、version）
//        Map<String, String> urlParams = new HashMap<>();
//        urlParams.put("uid", UID);
//        urlParams.put("version", "4.1.2"); // 注意版本号
//        // 表单参数
//        Map<String, String> formParams = new HashMap<>();
//        formParams.put("page", "1");
//        formParams.put("queryCode", "");
//        // 调用API
//        Map<String, Object> response = apiClient.postFormWithSign(
//                url, urlParams, formParams,result,Map.class
//        );
//        // 解析响应（list[1].price，单位宝石）
//        PriceResponse priceResponse = new PriceResponse();
//        priceResponse.setItemName("超级币");
//        priceResponse.setPrice((Double) response.get("list[1].price"));
//        priceResponse.setCurrency("GEM");
//        priceResponse.setApiSource("super-link-api.lucklyworld.com");
//        return  priceResponse;
//    }
    // 1. 基石
    @Override
//  @Scheduled(cron = "0 0/30 * * * ?")
    @Scheduled(cron = "0 0/10 * * * ?")
    public void getBase() {
        System.out.println("当前线程: " + Thread.currentThread().getName());
        String requestUrl = "/v11/api/foundation/trade/sale/list";
        String url = "https://fks-api.lucklyworld.com/v11/api/foundation/trade/sale/list";
        String paramsPart3 = "page=1";//入参
        RequestStringGenerator requestStringGenerator = new RequestStringGenerator();
        String code = requestStringGenerator.generateRequestString(requestUrl, UID, "5.2.2", TOKEN, paramsPart3);
        String[] split = code.split(",");
        String word = split[0];
        String ts = split[1];
        System.out.println("word" + word);
        HmacSha256Utils hmacSha256Utils = new HmacSha256Utils();
        String result = hmacSha256Utils.hmacSha256Encrypt(word, KEY);
        Map<String, String> urlParams = new HashMap<>();
        urlParams.put("uid", UID);
        urlParams.put("version", "5.2.2");
        // 表单参数
        Map<String, String> formParams = new HashMap<>();
        formParams.put("page", "1");
        Map<String, List<Map<String, Object>>> response = apiClient.postFormWithSign(
                url, urlParams, formParams, result, ts, Map.class
        );
        // 解析响应（list[1].price，单位宝石）
        List<Map<String, Object>> list = (List<Map<String, Object>>) response.get("list");
//        log.info("list"+list.toString());
        Map<String,Object> item = list.get(1);
        Object priceObj= item.get("price");
//        log.info("priceObj"+priceObj);
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
        market.setCurrency("GEM");
        MarketVo marketVo = new MarketVo();
        BeanUtils.copyProperties(market, marketVo);
        sendMarketMessage(market);
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