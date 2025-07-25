package com.yupi.springbootinit.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yupi.springbootinit.client.ApiClient;
import com.yupi.springbootinit.common.ErrorCode;
import com.yupi.springbootinit.exception.BusinessException;
import com.yupi.springbootinit.mapper.InformationDetailMapper;
import com.yupi.springbootinit.model.entity.Information;
import com.yupi.springbootinit.model.entity.InformationDetail;
import com.yupi.springbootinit.model.entity.Market;
import com.yupi.springbootinit.model.enums.MarketConfigEnum;
import com.yupi.springbootinit.service.InformationDetailService;
import com.yupi.springbootinit.service.InformationService;
import com.yupi.springbootinit.service.PriceService;
import com.yupi.springbootinit.utils.HmacSha256Utils;
import com.yupi.springbootinit.utils.RequestStringGenerator;
import lombok.extern.log4j.Log4j2;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Log4j2
public class InformationDetailServiceImpl extends ServiceImpl<InformationDetailMapper, InformationDetail> implements InformationDetailService {
    @Resource
    private RabbitTemplate rabbitTemplate;

    @Value("${app.rabbitmq.exchange}")
    private String exchangeName;

    @Value("${app.rabbitmq.routing-key}")
    private String routingKey;

    @Resource
    private InformationService informationSrervice;
    @Resource
    private PriceService priceSrervice;

    public InformationDetailServiceImpl(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    private final ApiClient apiClient;
    private static final String Host = "fks-api.lucklyworld.com";

    @Override
    public List<InformationDetail> getDetail(Long informationId) {
        List<InformationDetail> list = this.list(Wrappers.<InformationDetail>lambdaQuery().eq(InformationDetail::getInformationId, informationId));
        return list;
    }
    public InformationDetail updateDetail(Long informationId) {
        Boolean immortal =updateImmortal();
        if (!immortal) {
            Boolean immortal1 = updateImmortal();//添加重试机制
            if (!immortal1) {
                updateImmortal();
            }
        }
        return null;
    }
    /**
     * 定时任务每日更新
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @Scheduled(cron = "0 0/10 * * * ?")
    public Boolean updateImmortal() {
        Long informationId = informationSrervice.getOne(Wrappers.<Information>lambdaQuery().eq(Information::getDescription, "仙")).getId();
        if (informationId == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "没有仙人板块");
        }
        List<InformationDetail> list = this.list(Wrappers.lambdaQuery(InformationDetail.class).eq(InformationDetail::getInformationId, informationId));
        String requestUrl = "/v11/api/jinnee/monkey/combine/data";
        String url = "https://fks-api.lucklyworld.com/v11/api/jinnee/monkey/combine/data";
        String paramsPart3 = null;// 入参
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
        Map<String, List<Map<String, Object>>> response = apiClient.postFormWithSign(
                url, urlParams, formParams, result, ts, version, Host, Map.class
        );
        List<Map<String, Object>> levelList = response.get("levelList");
        Map<String, InformationDetail> nameToInfoMap = list.stream()
                .collect(Collectors.toMap(InformationDetail::getName, info -> info));
        double fl = priceSrervice.getOne(Wrappers.lambdaQuery(Market.class).eq(Market::getItemName, "法力")).getPrice();
        levelList.forEach(level -> {
            String name = level.get("name").toString();
            if ("散仙".equals(name) || "地仙".equals(name) || "真仙".equals(name) || "天仙".equals(name) || "金仙".equals(name) || "仙尊".equals(name)) {
                log.info("Processing immortal level: {}", name);
                // 直接通过名称查找对应的InformationDetail对象
                InformationDetail info = nameToInfoMap.get(name);
                info.setInformationId(informationId);
                info.setSuccessRate(level.get("successRate").toString());
                info.setEvaluateCombatPower(level.get("avgPower").toString());
                info.setDailyRewardOre(level.get("dailyRewardOre").toString());
                if ("仙尊".equals(name)){
                    Market market = priceSrervice.getOne(Wrappers.lambdaQuery(Market.class).eq(Market::getItemName, "金仙"));
                    Double xprice = market.getRemark()*market.getPrice();
                    double dailyRewardOre = Double.parseDouble((String) level.get("dailyRewardOre"));
                    info.setMonthlyProfit( String.format("%.4f", ((dailyRewardOre-(fl*dailyRewardOre))*30)/xprice));
                }
                else {
                    double xprice = priceSrervice.getOne(Wrappers.lambdaQuery(Market.class).eq(Market::getItemName, name)).getPrice();
                    double dailyRewardOre = Double.parseDouble((String) level.get("dailyRewardOre"));
                    info.setMonthlyProfit( String.format("%.4f", ((dailyRewardOre-(fl*dailyRewardOre))*30)/xprice));
                }
            }
        });
        log.info("informationDetailList:" + list);
        return this.updateBatchById(list);
    }

    //13. 获取仙的平均战力,日采矿数量,合成成功率
    @Override
    public Boolean addImmortal() {
        Long informationId = informationSrervice.getOne(Wrappers.<Information>lambdaQuery().eq(Information::getDescription, "仙")).getId();
        if (informationId == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "没有仙人板块");
        }
            String requestUrl = "/v11/api/jinnee/monkey/combine/data";
            String url = "https://fks-api.lucklyworld.com/v11/api/jinnee/monkey/combine/data";
            String paramsPart3 = null;// 入参
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
            Map<String, List<Map<String, Object>>> response = apiClient.postFormWithSign(
                    url, urlParams, formParams, result, ts, version, Host, Map.class
            );
            List<Map<String, Object>> levelList = response.get("levelList");
            List<InformationDetail> informationDetailList = new ArrayList<>();
        double fl = priceSrervice.getOne(Wrappers.lambdaQuery(Market.class).eq(Market::getItemName, "法力")).getPrice();
        levelList.forEach(level -> {
                String name = level.get("name").toString();
                if ("散仙".equals(name) || "地仙".equals(name) || "真仙".equals(name) || "天仙".equals(name) || "金仙".equals(name) || "仙尊".equals(name)) {
                    InformationDetail informationDetail = new InformationDetail();
                    log.info("name:" + name);
                    if ("仙尊".equals(name)){
                        Market market = priceSrervice.getOne(Wrappers.lambdaQuery(Market.class).eq(Market::getItemName, "金仙"));
                        Double xprice = market.getRemark()*market.getPrice();
                        double dailyRewardOre = Double.parseDouble((String) level.get("dailyRewardOre"));
                        informationDetail.setMonthlyProfit( String.format("%.4f", ((dailyRewardOre-(fl*dailyRewardOre))*30)/xprice));
                    }
                    else {
                        double xprice = priceSrervice.getOne(Wrappers.lambdaQuery(Market.class).eq(Market::getItemName, name)).getPrice();
                        double dailyRewardOre = Double.parseDouble((String) level.get("dailyRewardOre"));
                        informationDetail.setMonthlyProfit( String.format("%.4f", ((dailyRewardOre-(fl*dailyRewardOre))*30)/xprice));
                    }
                    informationDetail.setName(name);
                    informationDetail.setInformationId(informationId);
                    informationDetail.setPhotoPath(level.get("icon").toString());
                    informationDetail.setSuccessRate(level.get("successRate").toString());//合成成功率
                    informationDetail.setEvaluateCombatPower(level.get("avgPower").toString());//平均战力
                    informationDetail.setDailyRewardOre(level.get("dailyRewardOre").toString());//日采矿数量
                    informationDetailList.add(informationDetail);
                }
            });
            log.info("informationDetailList:::" + informationDetailList);
            return this.saveBatch(informationDetailList);

    }


    /**
     * 消息发送
     *
     */
//    public void sendMarketMessage(InformationDetail informationDetail) {
//        MarketMessage marketMessage = new MarketMessage(
//                market.getItemName(),
//                market.getPrice(),
//                market.getCurrency()
//        );
//        rabbitTemplate.convertAndSend(exchangeName, routingKey, marketMessage);
//        log.info("Sent market data to queue: {}", marketMessage);
//    }
}
