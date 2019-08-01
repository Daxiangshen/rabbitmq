package com.rabbitmq.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import java.util.HashMap;
import java.util.Map;

/**
 * RabbitConfig  class
 *
 * @author : yuxiang
 * @date : 2019-07-31 15:08
 **/
@Configuration
public class RabbitConfig {

    @Autowired
    private RabbitTemplate rabbitTemplate;
    /**
     * 此处为模板类定义 Jackson消息转换器
     *
     * ConfirmCallback接口用于实现消息发送到RabbitMQ交换器后接收ack回调   即消息发送到exchange  ack
     *
     * ReturnCallback接口用于实现消息发送到RabbitMQ 交换器，但无相应队列与交换器绑定时的回调  即消息发送不到任何一个队列中  ack
     *
     * */

    /**
     * 因为要设置回调类，所以应是prototype类型，如果是singleton类型，则回调类为最后一次设置
     * */
    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public AmqpTemplate amqpTemplate(){
        Logger log= LoggerFactory.getLogger(RabbitTemplate.class);
        //使用jackson消息转换器
        rabbitTemplate.setMessageConverter(new Jackson2JsonMessageConverter());
        rabbitTemplate.setEncoding("UTF-8");
        //开启returnCallback yml需要配置 publisher-returns: true
        rabbitTemplate.setMandatory(true);
        rabbitTemplate.setReturnCallback((message,replyCode,replyText,exchange,routingKey)->{
            String correlationId=message.getMessageProperties().getCorrelationId();
            log.debug("消息：{} 发送失败, 应答码：{} 原因：{} 交换机: {}  路由键: {}", correlationId, replyCode, replyText, exchange, routingKey);
        });
        //消息确认 yml需要配置  publisher-returns: true
        rabbitTemplate.setConfirmCallback((correlationData, ack, cause)->{
            if (ack){
                log.debug("消息发送到exchange成功,id: {}", correlationData.getId());
            }else {
                log.debug("消息发送到exchange失败,原因: {}", cause);
            }
        });
        return rabbitTemplate;
    }


    /* ----------------------------------------------------------------------------Direct exchange test--------------------------------------------------------------------------- */
    /**
     * 声明Direct交换机 支持持久化  ps:durable属性设置为true,支持持久化
     *
     * */
    @Bean("directExchange")
    public Exchange directExchange(){
        return ExchangeBuilder.directExchange("DIRECT_EXCHANGE").durable(true).build();
    }

    /**
     * 声明一个队列,支持持久化
     *
     * Queue可以有四个参数
     *          1.队列名
     *          2.durable 是否持久化 持久化代表rabbit重启时不需要创建新的队列 默认是true
     *          3.auto-delete  消息队列在没有使用时是否自动删除 默认是false
     *          4.exclusive 表示该消息队列是否只在但花钱connection生效,默认为false
     * */
    @Bean("directQueue")
    public Queue directQueue(){
        return QueueBuilder.durable("DIRECT_QUEUE").build();
    }

    /**
     * 指定路由键routingKey,将指定队列绑定到指定的交换机
     * */
    @Bean
    public Binding directBinding(@Qualifier("directQueue") Queue queue,@Qualifier("directExchange") Exchange exchange){
        return BindingBuilder.bind(queue).to(exchange).with("DIRECT_ROUTING_KEY").noargs();
    }

    /* ----------------------------------------------------------------------------Fanout exchange test--------------------------------------------------------------------------- */
    /**
     * 声明fanout交换机
     * */
    @Bean("fanoutExchange")
    public FanoutExchange fanoutExchange(){
        return (FanoutExchange) ExchangeBuilder.fanoutExchange("FANOUT_EXCHANGE").durable(true).build();
    }

    /**
     * fanout queue A
     * */
    @Bean("fanoutQueueA")
    public Queue fanoutQueueA(){
        return QueueBuilder.durable("FANOUT_QUEUE_A").build();
    }

    /**
     * fanout queue B
     * */
    @Bean("fanoutQueueB")
    public Queue fanoutQueueB() {
        return QueueBuilder.durable("FANOUT_QUEUE_B").build();
    }

    /**
     * 绑定队列A到Fanout交换机
     * */
    @Bean
    public Binding bindingA(@Qualifier("fanoutQueueA") Queue queue, @Qualifier("fanoutExchange") FanoutExchange fanoutExchange){
        return BindingBuilder.bind(queue).to(fanoutExchange);
    }

    /**
     * 绑定队列B到Fanout交换机
     * */
    @Bean
    public Binding bindingB(@Qualifier("fanoutQueueB") Queue queue, @Qualifier("fanoutExchange") FanoutExchange fanoutExchange) {
        return BindingBuilder.bind(queue).to(fanoutExchange);
    }

    /* ----------------------------------------------------------------------------延迟队列插件测试（包含ACK）--------------------------------------------------------------------------- */
    /**
     * 创建延迟队列交换机
     * 注意：类型必须是x-delayed-message
     *
     * args代表携带的参数，noArgs表示没有额外参数
     * */
    @Bean("delayExchange")
    public CustomExchange customExchange(){
        //设置交换机类型
        Map<String,Object> args=new HashMap<>();
        args.put("x-delayed-type", "direct");
        return new CustomExchange("DELAY_EXCHANGE","x-delayed-message",true,false,args);
    }

    /**
     * 创建队列
     * */
    @Bean("delayQueue")
    public Queue delayQueue(){
        return QueueBuilder.durable("DELAY_QUEUE").build();
    }

    /**
     * 将DELAY_QUEUE队列绑定到DELAY_EXCHANGE交换机
     * */
    @Bean
    public Binding bindingDelay(@Qualifier("delayQueue") Queue queue, @Qualifier("delayExchange") CustomExchange exchange){
        return BindingBuilder.bind(queue).to(exchange).with("DELAY_ROUTING_KEY").noargs();
    }
}
