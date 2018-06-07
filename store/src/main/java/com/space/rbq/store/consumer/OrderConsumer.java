package com.space.rbq.store.consumer;

import com.google.gson.Gson;
import com.rabbitmq.client.Channel;
import com.space.rbq.store.bean.Order;
import com.space.rbq.store.service.StoreService;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 负责接收处理订单服务发送的消息
 * @author zhuzhe
 * @date 2018/6/7 10:09
 * @email 1529949535@qq.com
 */
@Component
public class OrderConsumer {

    @Autowired
    private StoreService storeService;

    /*对列名称*/
    public final String QUEUE_NAME1 = "first-queue";

    /**
     * queues  指定从哪个队列（queue）订阅消息
     * @param message
     * @param channel
     */
    @RabbitListener(queues = {QUEUE_NAME1})
    public void handleMessage(Message message,Channel channel) throws IOException {
        try {
            // 处理消息
            System.out.println("OrderConsumer {} handleMessage :"+message);
            // 执行减库存操作
            storeService.update(new Gson().fromJson(new String(message.getBody()),Order.class));

            /*
            * 第一个参数 deliveryTag：就是接受的消息的deliveryTag,可以通过msg.getMessageProperties().getDeliveryTag()获得
              第二个参数 multiple：如果为true，确认之前接受到的消息；如果为false，只确认当前消息。
              如果为true就表示连续取得多条消息才发会确认，和计算机网络的中tcp协议接受分组的累积确认十分相似，
              能够提高效率。

              同样的，如果要nack或者拒绝消息（reject）的时候，
              也是调用channel里面的basicXXX方法就可以了（要指定tagId）。

              注意：如果抛异常或nack（并且requeue为true），消息会重新入队列，
              并且会造成消费者不断从队列中读取同一条消息的假象。
             */
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), true);


        }catch (Exception e){
            channel.basicNack(message.getMessageProperties().getDeliveryTag(), false,false);
        }
    }
}
