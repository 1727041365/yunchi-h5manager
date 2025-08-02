package com.yupi.springbootinit.mq.consumer;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.yupi.springbootinit.model.entity.Market;
import com.yupi.springbootinit.model.mq.MarketMessage;
import com.yupi.springbootinit.model.vo.MarketVo;
import com.yupi.springbootinit.service.PriceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.text.DecimalFormat;
import java.util.Date;

@Slf4j
@Component
public class MarketDataConsumer {
    @Resource
    private PriceService priceService;
    @RabbitListener(queues = "${app.rabbitmq.queue}")
    public void processMarketData(MarketMessage  marketMessage) {
        try {
            log.info("Received market data: {}", marketMessage);
            Market market = new Market();
            BeanUtils.copyProperties(marketMessage, market);
            LambdaQueryWrapper<Market> queryWrapper = Wrappers.lambdaQuery(Market.class).eq(Market::getItemName, market.getItemName());
            Market one = priceService.getOne(queryWrapper);
            if (one == null) {
                market.setCreateTime(new Date());
                priceService.save(market);
            }else {
                market.setId(one.getId());
                market.setUpdateTime(new Date());
                priceService.updateById(market);
            }
            if ("金仙".equals(market.getItemName())){
                log.info("-----正在添加仙尊数据-----");
                Market market2 = new Market();
                market2.setItemName("仙尊");
                market2.setCurrency("矿石");
                Double result = marketMessage.getPrice();
                DecimalFormat df = new DecimalFormat("#.00");
                String formattedPrice = df.format(result);
                Double price = Double.valueOf(formattedPrice);
                market2.setPrice(price*4.6);
                LambdaQueryWrapper<Market> queryWrapper2 = Wrappers.lambdaQuery(Market.class).eq(Market::getItemName,"仙尊" );
                Market one2 = priceService.getOne(queryWrapper2);
                if (one2 == null) {
                    market2.setCreateTime(new Date());
                    priceService.save(market2);
                }else {
                    market2.setId(one2.getId());
                    market2.setUpdateTime(new Date());
                    priceService.updateById(market2);
                }
                log.info("-----添加仙尊数据完毕-----::market2:",market2);
            }
        }catch (Exception e){
            log.error("Failed to process market data", e);
        }
    }
}
