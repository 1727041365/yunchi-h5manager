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
}
