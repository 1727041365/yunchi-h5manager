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
        }catch (Exception e){
            log.error("Failed to process market data", e);
        }
    }
}
