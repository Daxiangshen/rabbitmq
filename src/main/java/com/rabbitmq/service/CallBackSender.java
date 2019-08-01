package com.rabbitmq.service;

import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

/**
 * SendController  class
 *
 * 发送
 * @author : yuxiang
 * @date : 2019-07-31 15:40
 **/
@Component
public class CallBackSender implements RabbitTemplate.ConfirmCallback{

    @Resource
    private RabbitTemplate rabbitTemplate;

    /**
     *  测试广播模式
     */
    public void send() {
        rabbitTemplate.setConfirmCallback(this);
        String msg = "callbackSender : i am callback sender";
        System.err.println(msg);
        CorrelationData correlationData = new CorrelationData(UUID.randomUUID().toString());
        System.err.println("callbackSender UUID: " + correlationData.getId());
        this.rabbitTemplate.convertAndSend("FANOUT_EXCHANGE", "", msg, correlationData);
    }

    /**
     *  测试Direct模式
     */
    public void direct() {
        rabbitTemplate.setConfirmCallback(this);
        String msg = "callbackSender : i am callback sender";
        System.err.println(msg);
        CorrelationData correlationData = new CorrelationData(UUID.randomUUID().toString());
        System.err.println("callbackSender UUID: " + correlationData.getId());
        rabbitTemplate.convertAndSend("DIRECT_EXCHANGE", "DIRECT_ROUTING_KEY", msg, correlationData);
    }

    /**
     * 测试延迟消息插件
     * */
    public void delay(){
        SimpleDateFormat sf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        System.err.println("发送时间"+sf.format(new Date()));
        rabbitTemplate.convertAndSend("DELAY_EXCHANGE", "DELAY_ROUTING_KEY", "测试延迟消息插件", (message) ->{
            message.getMessageProperties().setHeader("x-delay", 9000);
            return message;
        });
    }

    @Override
    public void confirm(CorrelationData correlationData, boolean b, String s) {
        //这里的ack是Broker对发布者消息到达服务端的确认
        System.err.println("callback confirm  "+correlationData.getId()+"  ACK:  "+b+"  cause: "+s);
    }
}
