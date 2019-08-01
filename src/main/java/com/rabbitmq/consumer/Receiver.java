package com.rabbitmq.consumer;

import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Receiver  class
 *
 * 监听
 * @author : yuxiang
 * @date : 2019-07-31 15:36
 **/
@Component
public class Receiver {
    /**
     * FANOUT广播队列监听一
     * */
    @RabbitListener(queues = {"FANOUT_QUEUE_A"})
    public void on(Message message, Channel channel)throws Exception{
        System.err.println("FANOUT_QUEUE_A "+new String(message.getBody()));
        //采用手动应答模式
        //使用时需要在yml开启手动确认设置
        //消息的标识，false只确认当前一个消息收到，true确认所有将比第一个参数指定的 delivery tag 小的consumer都获得的消息
        channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
    }

    /**
     * FANOUT广播队列监听二
     */
    @RabbitListener(queues = {"FANOUT_QUEUE_B"})
    public void t(Message message, Channel channel) throws IOException {
        System.err.println("FANOUT_QUEUE_B "+new String(message.getBody()));
        //采用手动应答模式
        //使用时需要在yml开启手动确认设置
        channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
    }

    /**
     * DIRECT模式
     */
    @RabbitListener(queues = {"DIRECT_QUEUE"})
    public void message(Message message, Channel channel) throws IOException {
        System.err.println("DIRECT "+new String (message.getBody()));
        //采用手动应答模式
        //使用时需要在yml开启手动确认设置
        channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
    }
}
