package com.yupi.springbootinit.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
    @Value("${app.rabbitmq.queue}")
    private String queueName;
    @Value("${app.rabbitmq.exchange}")
    private String exchangeName;
    @Value("${app.rabbitmq.routing-key}")
    private String routingKey;
    @Value("${app.rabbitmq.detailQueue}")
    private String ifmDetailQueueName;
    @Value("${app.rabbitmq.detailExchange}")
    private String ifmDetailExchangeName;
    @Value("${app.rabbitmq.detailRouting-key}")
    private String ifmDetailRoutingKey;
    // 声明队列
    @Bean
    public Queue marketDataQueue() {
        return new Queue(queueName, true);
    }
    // 声明交换器
    @Bean
    public DirectExchange marketDataExchange() {
        return new DirectExchange(exchangeName);
    }

    // 绑定队列和交换器
    @Bean
    public Binding binding() {
        return BindingBuilder.bind(marketDataQueue())
                .to(marketDataExchange())
                .with(routingKey);
    }

    @Bean
    public Queue detailQueue() {
        return new Queue(ifmDetailQueueName, true);
    }
    // 声明交换器
    @Bean
    public DirectExchange detailExchange() {
        return new DirectExchange(ifmDetailExchangeName);
    }

    // 绑定队列和交换器
    @Bean
    public Binding detailBinding() {
        return BindingBuilder.bind(detailQueue())
                .to(detailExchange())
                .with(ifmDetailRoutingKey);
    }
}
