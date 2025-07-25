package com.yupi.springbootinit.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yupi.springbootinit.client.ApiClient;
import com.yupi.springbootinit.mapper.SpiritStoneUserDetailMapper;
import com.yupi.springbootinit.mapper.StoneStatisticsMapper;
import com.yupi.springbootinit.model.entity.Information;
import com.yupi.springbootinit.model.entity.SpiritStoneUserDetail;
import com.yupi.springbootinit.model.entity.StoneStatistics;
import com.yupi.springbootinit.model.entity.StoneUserDetail;
import com.yupi.springbootinit.model.enums.MarketConfigEnum;
import com.yupi.springbootinit.model.vo.StoneQuantityDetailVo;
import com.yupi.springbootinit.service.InformationService;
import com.yupi.springbootinit.service.SpiritStoneUserDetailService;
import com.yupi.springbootinit.service.StoneUserDetailService;
import com.yupi.springbootinit.utils.SpiritStoneLevelUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
@Transactional(rollbackFor = Exception.class)
public class SpiritStoneUserDetailServiceImpl extends ServiceImpl<SpiritStoneUserDetailMapper, SpiritStoneUserDetail> implements SpiritStoneUserDetailService {
    @Resource
    private StoneUserDetailService stoneUserDetailService;
    @Resource
    private InformationService informationService;
    @Resource
    private ApiClient apiClient;
    @Resource
    private StoneStatisticsMapper stoneStatisticsMapper;

    @Override
    public StoneQuantityDetailVo getDate() {
        List<StoneStatistics> list = stoneStatisticsMapper.selectList(Wrappers.lambdaQuery(StoneStatistics.class));
        if (list != null && list.size() > 0) {
            StoneStatistics stoneStatistics = list.get(0);
            StoneQuantityDetailVo stoneQuantityDetailVo = new StoneQuantityDetailVo();
            BeanUtils.copyProperties(stoneStatistics, stoneQuantityDetailVo);
            return stoneQuantityDetailVo;
        }
        return null;
    }
    @Override
    @Async
    @Scheduled(cron = "0 0/30 * * * ?")
    public CompletableFuture<Boolean> addOrUpdateDeta() {
        try {
            Long id = informationService.getOne(Wrappers.lambdaQuery(Information.class).eq(Information::getTitle, "仙侠宇宙灵石板块")).getId();
            Map<String, String> urlParam = new HashMap<>();
            urlParam.put("uid", "");
            urlParam.put("version", "2.1.3");
            Map<String, String> formParam = new HashMap<>();
            // 获取全网用户
            String[] stringArray = new String[10];
            for (int i = 0; i < 10; i++) {
                stringArray[i] = String.valueOf(i + 1);
            }
            List<StoneUserDetail> stoneUserDetails = new ArrayList<>();
            Arrays.stream(stringArray).forEach(size -> {
                formParam.put("next", size);
                Map<String, List<Map<String, Object>>> response = apiClient.postFormWithStoneDetail("https://farm-api.lucklyworld.com/v8/api/game/room/suggest", urlParam, formParam, "farm-api.lucklyworld.com", Map.class);
                log.info("response={}", response);
                List<Map<String, Object>> items = response.get("items");
                items.stream().forEach(item -> {
                    String roomId = item.get("roomId").toString();
                    StoneUserDetail stoneUserDetail = new StoneUserDetail();
                    stoneUserDetail.setName((String) item.get("name"));
                    stoneUserDetail.setId(Long.valueOf(roomId));
                    stoneUserDetail.setUpdateTime(new Date());
                    stoneUserDetails.add(stoneUserDetail);
                });
                try {
                    Thread.sleep(new Random().nextInt(200));
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            });
            // 处理StoneUserDetail数据：批量存在则更新，不存在则保存
            boolean b1 = batchUpsertStoneUserDetails(stoneUserDetails);
            List<SpiritStoneUserDetail> spiritStoneUserDetails = new ArrayList<>();//所有矿主列表
            stoneUserDetails.stream().forEach(item -> {
                Long roomId = item.getId();
                formParam.put("next", "");
                formParam.put("roomId", String.valueOf(roomId));
                formParam.put("sortRule", "2");
                formParam.put("sortColumn", "2");
                urlParam.put("uid", MarketConfigEnum.StoenUID.getValue());
                urlParam.put("version", "2.1.2");
                Map<String, List<Map<String, Object>>> response = apiClient.postJsonWithCaveHouseDetail("https://farm-api.lucklyworld.com/v8/api/game/room/union/member", urlParam, formParam, "farm-api.lucklyworld.com", String.valueOf(roomId), Map.class);
                log.info("response={}", response);
                List<Map<String, Object>> items = response.get("items");
                items.forEach(itemStone -> {
                    BigInteger ore = new BigInteger(String.valueOf(itemStone.get("ore")));
                    if (ore.compareTo(BigInteger.ZERO) > 0) {
                        SpiritStoneUserDetail spiritStoneUserDetail = new SpiritStoneUserDetail();
                        spiritStoneUserDetail.setStoneTotal(ore);
                        spiritStoneUserDetail.setStoneLevel(String.valueOf(SpiritStoneLevelUtils.getStoneLevel(ore)));
                        spiritStoneUserDetail.setName((String) itemStone.get("nickname"));
                        spiritStoneUserDetail.setId(Long.valueOf(String.valueOf(itemStone.get("roleId"))));
                        spiritStoneUserDetail.setRoomId(roomId);
                        spiritStoneUserDetails.add(spiritStoneUserDetail);
                    }
                });
                try {
                    Thread.sleep( new Random().nextInt(200));
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            });
            // 处理SpiritStoneUserDetail数据：批量存在则更新，不存在则保存
            boolean b = batchUpsertSpiritStoneUserDetails(spiritStoneUserDetails);
            getTotalStone(spiritStoneUserDetails);
            return CompletableFuture.completedFuture(b1 && b);
        } catch (Exception e) {
            return CompletableFuture.failedFuture(e);
        }
    }
    /**
     * 批量处理SpiritStoneUserDetail数据：存在则更新，不存在则保存
     */
    @Transactional
    public boolean batchUpsertSpiritStoneUserDetails(List<SpiritStoneUserDetail> details) {
        if (details.isEmpty()) {
            return true;
        }
        // 分批处理，每批1000条
        int batchSize = 1000;
        for (int i = 0; i < details.size(); i += batchSize) {
            List<SpiritStoneUserDetail> batch = details.subList(i, Math.min(i + batchSize, details.size()));
            // 使用自定义SQL实现批量upsert
            this.saveOrUpdateBatch(batch);
        }
        return true;
    }

    /**
     * 获取所有阶级灵石
     */
   void getTotalStone(List<SpiritStoneUserDetail> details){
        // 定义所有区间的临界值（使用 BigInteger 避免类型转换问题）
        BigInteger tenThousand = new BigInteger("100000");      // 10万
        BigInteger thirtyThousand = new BigInteger("300000");   // 30万
        BigInteger sixtyThousand = new BigInteger("600000");    // 60万
        BigInteger oneHundredTen = new BigInteger("1100000");   // 110万
        BigInteger oneHundredNinety = new BigInteger("1900000");// 190万
        BigInteger twoHundredNinety = new BigInteger("2900000");// 290万
        BigInteger fourHundredNinety = new BigInteger("4900000");// 490万
        BigInteger sevenHundredNinety = new BigInteger("7900000");// 790万
        BigInteger oneThousandTwoHundredNinety = new BigInteger("12900000");// 1290万
        BigInteger twoThousandTwoHundredNinety = new BigInteger("22900000");// 2290万
// 1. 所有 ore 的总和（原逻辑）
        BigInteger totalAll = details.stream()
                .map(SpiritStoneUserDetail::getStoneTotal)
                .filter(stoneTotal -> stoneTotal != null)
                .reduce(BigInteger.ZERO, BigInteger::add);
// 2. 10万 < ore < 30万 的总和
        BigInteger total10To30 = details.stream()
                .map(SpiritStoneUserDetail::getStoneTotal)
                .filter(stoneTotal -> stoneTotal != null
                        && stoneTotal.compareTo(tenThousand) >= 0    // >10万
                        && stoneTotal.compareTo(thirtyThousand) < 0) // <30万
                .reduce(BigInteger.ZERO, BigInteger::add);
// 3. 30万 < ore < 60万 的总和
        BigInteger total30To60 = details.stream()
                .map(SpiritStoneUserDetail::getStoneTotal)
                .filter(stoneTotal -> stoneTotal != null
                        && stoneTotal.compareTo(thirtyThousand) >= 0   // >30万
                        && stoneTotal.compareTo(sixtyThousand) < 0)   // <60万
                .reduce(BigInteger.ZERO, BigInteger::add);
// 4. 60万 < ore < 110万 的总和
        BigInteger total60To110 = details.stream()
                .map(SpiritStoneUserDetail::getStoneTotal)
                .filter(stoneTotal -> stoneTotal != null
                        && stoneTotal.compareTo(sixtyThousand) >= 0    // >60万
                        && stoneTotal.compareTo(oneHundredTen) < 0)   // <110万
                .reduce(BigInteger.ZERO, BigInteger::add);
// 5. 110万 < ore < 190万 的总和
        BigInteger total110To190 = details.stream()
                .map(SpiritStoneUserDetail::getStoneTotal)
                .filter(stoneTotal -> stoneTotal != null
                        && stoneTotal.compareTo(oneHundredTen) >= 0    // >110万
                        && stoneTotal.compareTo(oneHundredNinety) < 0)// <190万
                .reduce(BigInteger.ZERO, BigInteger::add);
// 6. 190万 < ore < 290万 的总和
        BigInteger total190To290 = details.stream()
                .map(SpiritStoneUserDetail::getStoneTotal)
                .filter(stoneTotal -> stoneTotal != null
                        && stoneTotal.compareTo(oneHundredNinety) >= 0 // >190万
                        && stoneTotal.compareTo(twoHundredNinety) < 0)// <290万
                .reduce(BigInteger.ZERO, BigInteger::add);
// 7. 290万 < ore < 490万 的总和
        BigInteger total290To490 = details.stream()
                .map(SpiritStoneUserDetail::getStoneTotal)
                .filter(stoneTotal -> stoneTotal != null
                        && stoneTotal.compareTo(twoHundredNinety) >=0 // >290万
                        && stoneTotal.compareTo(fourHundredNinety) < 0)// <490万
                .reduce(BigInteger.ZERO, BigInteger::add);
// 8. 490万 < ore < 790万 的总和
        BigInteger total490To790 = details.stream()
                .map(SpiritStoneUserDetail::getStoneTotal)
                .filter(stoneTotal -> stoneTotal != null
                        && stoneTotal.compareTo(fourHundredNinety) >= 0 // >490万
                        && stoneTotal.compareTo(sevenHundredNinety) < 0)// <790万
                .reduce(BigInteger.ZERO, BigInteger::add);
// 9. 790万 < ore < 1290万 的总和
        BigInteger total790To1290 = details.stream()
                .map(SpiritStoneUserDetail::getStoneTotal)
                .filter(stoneTotal -> stoneTotal != null
                        && stoneTotal.compareTo(sevenHundredNinety) >= 0 // >790万
                        && stoneTotal.compareTo(oneThousandTwoHundredNinety) < 0)// <1290万
                .reduce(BigInteger.ZERO, BigInteger::add);
// 10. ore > 2290万 的总和
        BigInteger totalOver2290 = details.stream()
                .map(SpiritStoneUserDetail::getStoneTotal)
                .filter(stoneTotal -> stoneTotal != null
                        && stoneTotal.compareTo(oneThousandTwoHundredNinety) >= 0 // >1290万
                        && stoneTotal.compareTo(twoThousandTwoHundredNinety) < 0)// <2290万
                .reduce(BigInteger.ZERO, BigInteger::add);
       BigInteger totalmax2290 = details.stream()
               .map(SpiritStoneUserDetail::getStoneTotal)
               .filter(stoneTotal -> stoneTotal != null
                       && stoneTotal.compareTo(twoThousandTwoHundredNinety) >= 0)// >=2290万
               .reduce(BigInteger.ZERO, BigInteger::add);
       List<StoneStatistics> list = stoneStatisticsMapper.selectList(Wrappers.lambdaQuery(StoneStatistics.class));
       String size = String.valueOf(details.size());
       if (list != null && list.size() > 0) {
            StoneStatistics stoneStatistics = list.get(0);
            stoneStatistics.setTotal(totalAll);
            stoneStatistics.setOneTotal(total10To30);
            stoneStatistics.setTwoTotal(total30To60);
            stoneStatistics.setThreeTotal(total60To110);
            stoneStatistics.setFourTotal(total110To190);
            stoneStatistics.setFiveTotal(total190To290 );
            stoneStatistics.setSixTotal(total290To490);
            stoneStatistics.setSevenTotal(total490To790);
            stoneStatistics.setEightTotal(total790To1290);
            stoneStatistics.setNineTotal(totalOver2290);
            stoneStatistics.setTenTotal(totalmax2290);
            stoneStatistics.setUserQuantity(size);
            stoneStatistics.setUpdateTime(new Date());
            stoneStatisticsMapper.updateById(stoneStatistics);
        }else {
            StoneStatistics stoneStatistics = new StoneStatistics();
            stoneStatistics.setTotal(totalAll);
            stoneStatistics.setOneTotal(total10To30);
            stoneStatistics.setTwoTotal(total30To60);
            stoneStatistics.setThreeTotal(total60To110);
            stoneStatistics.setFourTotal(total110To190);
            stoneStatistics.setFiveTotal(total190To290 );
            stoneStatistics.setSixTotal(total290To490);
            stoneStatistics.setSevenTotal(total490To790);
            stoneStatistics.setEightTotal(total790To1290);
            stoneStatistics.setNineTotal(totalOver2290);
            stoneStatistics.setTenTotal(totalmax2290);
            stoneStatistics.setUserQuantity(size);
            stoneStatistics.setUpdateTime(new Date());
            stoneStatisticsMapper.insert(stoneStatistics);
        }
    }
    /**
     * 批量处理StoneUserDetail数据：存在则更新，不存在则保存
     */
    @Transactional
    public boolean batchUpsertStoneUserDetails(List<StoneUserDetail> details) {
        if (details.isEmpty()) {
            return true;
        }
        // 分批处理，每批1000条，避免SQL语句过长
        int batchSize = 1000;
        for (int i = 0; i < details.size(); i += batchSize) {
            List<StoneUserDetail> batch = details.subList(i, Math.min(i + batchSize, details.size()));
            stoneUserDetailService.saveOrUpdateBatch(batch); // 使用MyBatis-Plus的批量upsert
        }
        return true;
    }
}
