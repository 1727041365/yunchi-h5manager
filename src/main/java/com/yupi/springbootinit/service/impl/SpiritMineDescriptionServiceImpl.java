package com.yupi.springbootinit.service.impl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yupi.springbootinit.client.ApiClient;
import com.yupi.springbootinit.mapper.SpiritMineDescriptionMapper;
import com.yupi.springbootinit.model.entity.SpiritMineDescription;
import com.yupi.springbootinit.model.enums.MarketConfigEnum;
import com.yupi.springbootinit.service.SpiritMineDescriptionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.text.DecimalFormat;
import java.util.*;

@Service
@Slf4j
public class SpiritMineDescriptionServiceImpl extends ServiceImpl<SpiritMineDescriptionMapper, SpiritMineDescription> implements SpiritMineDescriptionService {
    @Resource
    ApiClient apiClient;
    private static final String Host = "fks-api.lucklyworld.com";

    @Override
    @Transactional(rollbackFor = Exception.class)
    @Async
    @Scheduled(cron = "0 0/30 * * * ?")
    public void saveOrUpdateDetail() {
        String url1 = "https://farm-api.lucklyworld.com/v8/api/user/home";
        String url2 = "https://farm-api.lucklyworld.com/v8/api/game/ruins/ore";
        String version = "2.1.3";
        Map<String, String> urlParams = new HashMap<>();
        urlParams.put("uid", "");
        urlParams.put("version", version);
        // 表单参数
        Map<String, String> jsonParams = new LinkedHashMap<>();
        Map<String, Object> response1 = apiClient.postFormWithStoneDetail(
                url1, urlParams, jsonParams, "farm-api.lucklyworld.com", Map.class
        );
        log.info("response", response1);
        // 从根对象中获取 roleId
        Long roleId = null;
        Object roleIdObj = response1.get("roleId");
        if (roleIdObj instanceof Number) {
            roleId = ((Number) roleIdObj).longValue();
        } else if (roleIdObj instanceof String) {
            roleId = Long.parseLong((String) roleIdObj);
        }
        jsonParams.put("roleId", String.valueOf(roleId));
        urlParams.put("uid", MarketConfigEnum.StoenUID.getValue());
        Map<String, Object> response2 = apiClient.postFormWithStoneDetail(
                url2, urlParams, jsonParams, "farm-api.lucklyworld.com", Map.class
        );
        log.info("response", response2);
        List<Map<String, Object>> allConfList = (List<Map<String, Object>>) response2.get("allConf");
        log.info("allConf", allConfList );
        List<SpiritMineDescription> spiritMineDescriptionsList = new ArrayList<>();
        if (allConfList != null) {
            for (Map<String, Object> conf : allConfList) {
                SpiritMineDescription spiritMineDescription = new SpiritMineDescription();
                String name = (String) conf.get("name");
                spiritMineDescription.setName(name);
                List<Map<String, Object>> introList = (List<Map<String, Object>>) conf.get("intro");
                String openCost = null;
                if (introList.size() > 0) {
                    List<String> valueList = (List<String>) introList.get(0).get("value");
                    if (valueList != null && valueList.size() > 0) {
                        openCost = valueList.get(0);
                        spiritMineDescription.setInputStone(openCost);
                    }
                }
                // 提取每日发放灵石（intro 第二个元素的 value 列表的第一个值）
                String dailyReward = null;
                if (introList.size() > 1) {
                    List<String> valueList = (List<String>) introList.get(1).get("value");
                    if (valueList != null && valueList.size() > 0) {
                        dailyReward = valueList.get(0);
                        spiritMineDescription.setOutputStone(dailyReward);
                        String[] ws = openCost.split("w");
                        double v = Double.parseDouble(ws[0]);
                        Double i = Double.parseDouble(dailyReward);
                        double result = (i * 30.0) / (v * 10000.0);
                        // 格式化保留 6 位小数
                        DecimalFormat df = new DecimalFormat("0.0000%");
                        String formattedResult = df.format(result); // 结果为字符串类型
                        spiritMineDescription.setMonthlyProfit(formattedResult);
                    }
                }
                spiritMineDescriptionsList.add(spiritMineDescription);
            }
        }
        List<SpiritMineDescription> list = this.list();
        if (list != null&&list.size()>0) {
            list.forEach(item1 -> {
                spiritMineDescriptionsList.forEach(item2 -> {
                   if (item1.getName().equals(item2.getName())){
                       item1.setInputStone(item2.getInputStone());
                       item1.setOutputStone(item2.getOutputStone());
                       item1.setMonthlyProfit(item2.getMonthlyProfit());
                       item1.setUpdateTime(new Date());
                   }
                });
            });
            this.updateBatchById(list);
       }else {
            this.saveBatch(spiritMineDescriptionsList);
        }
    }
}
