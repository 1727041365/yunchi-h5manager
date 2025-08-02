package com.yupi.springbootinit.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.yupi.springbootinit.client.ApiClient;
import com.yupi.springbootinit.common.BaseResponse;
import com.yupi.springbootinit.common.ErrorCode;
import com.yupi.springbootinit.common.ResultUtils;
import com.yupi.springbootinit.exception.BusinessException;
import com.yupi.springbootinit.mapper.InformationDetailMapper;
import com.yupi.springbootinit.model.entity.*;
import com.yupi.springbootinit.model.enums.MarketConfigEnum;
import com.yupi.springbootinit.service.*;
import com.yupi.springbootinit.utils.HmacSha256Utils;
import com.yupi.springbootinit.utils.RequestStringGenerator;
import lombok.extern.log4j.Log4j2;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.ZoneId;
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
    OrePledgeService orePledgeService;
    @Resource
    OrePledgeBreakDownService orePledgeBreakdownService;
    @Resource
    private InformationService informationSrervice;
    @Resource
    private PriceService priceSrervice;
    @Autowired
    private OrePledgeBreakDownService orePledgeBreakDownService;
    @Resource
    ArmorWorkShopService armorWorkShopService;

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
        Boolean immortal = updateImmortal();
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
    @Scheduled(cron = "0 0/15 * * * ?")
    public Boolean updateImmortal() {
        Long informationId = informationSrervice.getOne(Wrappers.<Information>lambdaQuery().eq(Information::getDescription, "仙")).getId();
        if (informationId == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "没有仙人板块");
        }
        List<InformationDetail> list = this.list(Wrappers.lambdaQuery(InformationDetail.class).eq(InformationDetail::getInformationId, informationId));
        // 获取所需价格变量
        Market sanXianMarket = priceSrervice.getOne(Wrappers.lambdaQuery(Market.class)
                .eq(Market::getItemName, "散仙"));
        Market jinShiMarket = priceSrervice.getOne(Wrappers.lambdaQuery(Market.class)
                .eq(Market::getItemName, "金石"));
        Market kuLouTouMarket = priceSrervice.getOne(Wrappers.lambdaQuery(Market.class)
                .eq(Market::getItemName, "骷髅头"));

        if (sanXianMarket == null || jinShiMarket == null || kuLouTouMarket == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "缺少必要的价格数据");
        }
        double X = sanXianMarket.getPrice(); // 散仙价格
        double Z = jinShiMarket.getPrice();  // 金石价格
        double Y = kuLouTouMarket.getPrice();// 骷髅头价格
        String requestUrl = "/v11/api/jinnee/monkey/combine/data";
        String url = "https://fks-api.lucklyworld.com/v11/api/jinnee/monkey/combine/data";
        String paramsPart3 = null;// 入参
        String version = "5.2.3";
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
        Map<String, List<Map<String, Object>>> response = apiClient.postFormWithSign(
                url, urlParams, formParams, result, ts, version, Host, Map.class
        );
        List<Map<String, Object>> levelList = response.get("levelList");
        Map<String, InformationDetail> nameToInfoMap = list.stream()
                .collect(Collectors.toMap(InformationDetail::getName, info -> info));
        double fl = priceSrervice.getOne(Wrappers.lambdaQuery(Market.class).eq(Market::getItemName, "法力")).getPrice();
        List<InformationDetail> resultInfo = new ArrayList<>();
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
                info.setUpdateTime(new Date());
                // 计算合成预期 ConsolidatedExpectations
                String consolidatedExpectations = calculateConsolidatedExpectations(name, X, Z, Y);
                info.setConsolidatedExpectations(consolidatedExpectations);
                if ("仙尊".equals(name)) {
                    Market market = priceSrervice.getOne(Wrappers.lambdaQuery(Market.class).eq(Market::getItemName, "金仙"));
                    Double xprice = market.getRemark() * market.getPrice();
                    double dailyRewardOre = Double.parseDouble((String) level.get("dailyRewardOre"));
                    info.setMonthlyProfit(String.format("%.4f", ((dailyRewardOre - (fl * dailyRewardOre)) * 30) / xprice));
                } else {
                    double xprice = priceSrervice.getOne(Wrappers.lambdaQuery(Market.class).eq(Market::getItemName, name)).getPrice();
                    double dailyRewardOre = Double.parseDouble((String) level.get("dailyRewardOre"));
                    info.setMonthlyProfit(String.format("%.4f", ((dailyRewardOre - (fl * dailyRewardOre)) * 30) / xprice));
                }
                log.info("更新的数据:::",info);
                resultInfo.add(info);
            }
        });
        log.info("informationDetailList:" + list);
        return this.updateBatchById(resultInfo);
    }

    private String calculateConsolidatedExpectations(String name, double X, double Z, double Y) {
        double cost = 0.0;
        switch (name) {
            case "地仙":
                // 地仙期望成本 = (3X + Y + Z) * 0.9 + (2X + Y + Z) * 0.1 = 2.9X + Y + Z
                cost = 2.9 * X + Y + Z;
                break;
            case "真仙":
                cost = (9 * X + 2 * Z + 2 * Y) * 0.85 + (6 * X + 2 * Z + 2 * Y) * 0.15;
                break;
            case "天仙":
                cost = (27 * X + 5 * Z + 5 * Y) * 0.8 + (18 * X + 5 * Y + 5 * Z) * 0.2;
                break;
            case "金仙":
                cost = (81 * X + 12 * Z + 12 * Y) * 0.75 + (54 * X + 12 * Z + 12 * Y) * 0.25;
                break;
            case "仙尊":
                cost = (243 * X + 20 * Z + 20 * Y) * 0.7 + (162 * X + 20 * Z + 20 * Y) * 0.3;
                break;
            case "散仙":
                cost = X;
                break;
            default:
                cost = 0.0;
    }
        return String.format("%.4f", cost);
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
                if ("仙尊".equals(name)) {
                    Market market = priceSrervice.getOne(Wrappers.lambdaQuery(Market.class).eq(Market::getItemName, "金仙"));
                    Double xprice = market.getRemark() * market.getPrice();
                    double dailyRewardOre = Double.parseDouble((String) level.get("dailyRewardOre"));
                    informationDetail.setMonthlyProfit(String.format("%.4f", ((dailyRewardOre - (fl * dailyRewardOre)) * 30) / xprice));
                } else {
                    double xprice = priceSrervice.getOne(Wrappers.lambdaQuery(Market.class).eq(Market::getItemName, name)).getPrice();
                    double dailyRewardOre = Double.parseDouble((String) level.get("dailyRewardOre"));
                    informationDetail.setMonthlyProfit(String.format("%.4f", ((dailyRewardOre - (fl * dailyRewardOre)) * 30) / xprice));
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

    //13. 获取矿石山的平均战力,日采矿数量,合成成功率
    @Scheduled(cron = "0 0/15 * * * ?")
    @Async("rwThreadPool")
//    @Scheduled(fixedRate = 1000 * 60 )
    @Transactional(rollbackFor = Exception.class)
    public void getOreMountain() throws JsonProcessingException, InterruptedException {
        String requestUrl = "/v11/api/block/beast/ore/pledge/data";
        String url = "https://fks-api.lucklyworld.com/v11/api/block/beast/ore/pledge/data";
        String paramsPart3 = null;// 入参
        String version = "5.2.2";
        RequestStringGenerator requestStringGenerator = new RequestStringGenerator();
        String code = requestStringGenerator.generateRequestString(requestUrl, MarketConfigEnum.UID.getValue(), version, MarketConfigEnum.TOKEN.getValue(), paramsPart3);
        log.info("sign入参:" + code);
        String[] split = code.split(",");
        String word = split[0];
        String ts = split[1];
        System.out.println("word" + word);
        HmacSha256Utils hmacSha256Utils = new HmacSha256Utils();
        String result = hmacSha256Utils.hmacSha256Encrypt(word, MarketConfigEnum.KEY.getValue());
        log.info("sign:" + result);
        Map<String, String> urlParams = new HashMap<>();
        urlParams.put("uid", MarketConfigEnum.UID.getValue());
        urlParams.put("version", version);
        // 表单参数
        Map<String, String> formParams = new LinkedHashMap<>();
        OrePledge orePledge = apiClient.postFormKuang(
                url, urlParams, formParams, result, ts, version, Host, OrePledge.class
        );
        Thread.sleep(2000+new Random().nextInt(1000));
        QueryWrapper<OrePledge> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByDesc("create_time");
        queryWrapper.last("limit 1");
        queryWrapper.eq("is_delete", 0);
        OrePledge one = orePledgeService.getOne(queryWrapper, true);
        OrePledgeBreakDown orePledgeBreakDown = new OrePledgeBreakDown();
        QueryWrapper<OrePledgeBreakDown> queryWrapper1 = new QueryWrapper<>();
        queryWrapper1.orderByDesc("create_time");
        queryWrapper1.last("limit 1");
        queryWrapper1.eq("is_delete", 0);
        OrePledgeBreakDown one1 = orePledgeBreakdownService.getOne(queryWrapper1, true);
        if ( one1==null) {
            orePledgeBreakDown.setUpdateTime(new Date());
            orePledgeBreakDown.setTotalOre(orePledge.getTodayTotalOre());
            orePledgeBreakDown.setDayProfit(orePledge.getMyTodayOre());
            orePledgeBreakDown.setCumulativeProfit(orePledge.getMyTodayOre());
        }else {
            orePledgeBreakDown.setUpdateTime(new Date());
            orePledgeBreakDown.setTotalOre(orePledge.getTodayTotalOre());
            orePledgeBreakDown.setDayProfit(orePledge.getMyTodayOre());
            orePledgeBreakDown.setCumulativeProfit(orePledge.getMyTodayOre()+one1.getCumulativeProfit());
        }
        if (one!= null && isSameDay(one.getCreateTime(), new Date())){
            one.setTodayTotalOre(orePledge.getTodayTotalOre());
            one.setEverydayOre(orePledge.getEverydayOre());
            one.setYesterdayTotalOre(orePledge.getYesterdayTotalOre());
            one.setWeekdayTotalOre(orePledge.getWeekdayTotalOre());
            one.setMonthdayTotalOre(orePledge.getMonthdayTotalOre());
            one.setUserUseOre(orePledge.getUserUseOre());
            one.setUserYesterdayOre(orePledge.getUserYesterdayOre());
            one.setImmortalHallOre(orePledge.getImmortalHallOre());
            one.setImmortalYesterdayOre(orePledge.getImmortalYesterdayOre());
            one.setSalaryPoolOre(orePledge.getSalaryPoolOre());
            one.setSalaryPoolYesterdayOre(orePledge.getSalaryPoolYesterdayOre());
            one.setOtherOre(orePledge.getOtherOre());
            one.setOtherYesterdayOre(orePledge.getOtherYesterdayOre());
            one.setMyTodayOre(orePledge.getMyTodayOre());
            one.setUpdateTime(new Date());
            one1.setTotalOre(orePledgeBreakDown.getTotalOre());
            one1.setDayProfit(orePledgeBreakDown.getDayProfit());
            one1.setCumulativeProfit(orePledgeBreakDown.getCumulativeProfit());
            one1.setUpdateTime(new Date());
         orePledgeService.updateById(one);
         orePledgeBreakdownService.updateById(one1);
        }else {
            orePledgeService.save(orePledge);
            orePledgeBreakDownService.save(orePledgeBreakDown);
        }
    }
    @Override
    public void getOreDetail() throws JsonProcessingException {
    String requestUrl = "/v11/api/world/armor/trade/sale/list";
    String url ="https://fks-api.lucklyworld.com/v11/api/world/armor/trade/sale/list";
    String paramsPart3 ="page=1";// 入参
    String version = "5.2.2";
    RequestStringGenerator requestStringGenerator = new RequestStringGenerator();
    String code = requestStringGenerator.generateRequestString(requestUrl, MarketConfigEnum.UID.getValue(), version, MarketConfigEnum.TOKEN.getValue(), paramsPart3);
    log.info("sign入参:" + code);
    String[] split = code.split(",");
    String word = split[0];
    String ts = split[1];
    System.out.println("word" + word);
    HmacSha256Utils hmacSha256Utils = new HmacSha256Utils();
    String result = hmacSha256Utils.hmacSha256Encrypt(word, MarketConfigEnum.KEY.getValue());
    log.info("sign:" + result);
    Map<String, String> urlParams = new HashMap<>();
    urlParams.put("uid", MarketConfigEnum.UID.getValue());
    urlParams.put("version", version);
    // 表单参数
    Map<String, String> formParams = new LinkedHashMap<>();
    formParams.put("page","1");
    apiClient.postFormKuangDetail(
            url, urlParams, formParams, result, ts, version, Host, Map.class
    );
}

    private boolean isSameDay(Date date1, Date date2) {
        LocalDate localDate1 = date1.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate localDate2 = date2.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        return localDate1.isEqual(localDate2);
    }

// 获取护甲坊的平均战力,日采矿数量,合成成功率
//    @Scheduled(fixedRate = 1000 * 60 )
    @Scheduled(cron = "0 0/15 * * * ?")
    @Async("rwThreadPool")
    @Transactional(rollbackFor = Exception.class)
    public BaseResponse<String> getArmorWorkshop() throws JsonProcessingException, InterruptedException {
        String requestUrl = "/v11/api/armor/rand/forge/index";
        String url = "https://fks-api.lucklyworld.com/v11/api/armor/rand/forge/index";
        String paramsPart3 ="";// 入参
        String version = "5.2.2";
        Map<String, String> formParams = new LinkedHashMap<>();
        Map<String, Object> getArmor = getStringObjectMap(requestUrl, version, paramsPart3, url,formParams);
        ArmorWorkShop armorWorkShop = new ArmorWorkShop();
        Double output =Double.parseDouble(getArmor .get("output").toString()) ;
        armorWorkShop.setDailyTotalr(output);
        Double unitPrice =Double.parseDouble(getArmor .get("unitPrice").toString()) ;
        armorWorkShop.setCostPerShare(unitPrice);
        ArmorWorkShop one = armorWorkShopService.getOne(Wrappers.<ArmorWorkShop>lambdaQuery().orderByDesc(ArmorWorkShop::getCreateTime), false);
        if (one !=null&&isSameDay(one.getCreateTime(), new Date())) {
            one.setUpdateTime(new Date());
            one.setDailyTotalr(output);
            one.setCostPerShare(unitPrice);
            armorWorkShopService.updateById(one);
        }else {
            armorWorkShopService.save(armorWorkShop);
        }
        return ResultUtils.success("成功更新");
    }

    private Map<String, Object> getStringObjectMap(String requestUrl, String version, String paramsPart3, String url,Map<String, String> formParams) throws JsonProcessingException {
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
        Map<String, Object> stringObjectMap = apiClient.postFormKuangDetail(
                url, urlParams, formParams, result, ts, version, Host, Map.class
        );
        return stringObjectMap;
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
