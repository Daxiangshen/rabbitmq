package com.rabbitmq.consumer;

import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

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
        /**
         * 下面这行代码就是手动确认。如果删除，则还是会消费消息，但是队列中不会删除这条消息，也不会重复推送。但是如果consumer重启
         * 则会重新推送这条消息。直到进行手动确认为止。
         * 没有手动确认的话。在mq的管理界面中的UnAckEd下可以看到消息存在。当关闭consumer时,会从UnAckEd下回到Ready下面,等待consumer启动后发出
         * */
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

    /**
     * 延迟消息插件队列监听
     * */
    @RabbitListener(queues = {"DELAY_QUEUE"})
    public void delay(Message message,Channel channel) throws IOException {
        SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        System.err.println("接收时间:" + sf.format(new Date()));
        System.err.println("消息内容：" + new String(message.getBody()));
        channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
    }
}
