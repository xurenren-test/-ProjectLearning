package org.tinygame.herostory.mq;

import com.alibaba.fastjson.JSONObject;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.common.message.MessageExt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinygame.herostory.rank.RankService;

import java.util.List;

/**
 * 消息队列消费者
 */
public final class MQConsumer {

    static private final Logger LOGGER = LoggerFactory.getLogger(MQConsumer.class);

    private MQConsumer() {
    }

    /**
     * 初始化
     */
    public static void init() {

        DefaultMQPushConsumer dfConsumer = new DefaultMQPushConsumer("herostory");
        dfConsumer.setNamesrvAddr("127.0.0.1:9876");


        try {
            // 订阅消息
            dfConsumer.subscribe("Victor", "*");

            // 注册回调
            dfConsumer.registerMessageListener(new MessageListenerConcurrently() {
                @Override
                public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgExtKList, ConsumeConcurrentlyContext ctx) {
                    for (MessageExt msgExt : msgExtKList){
                        // 解析战斗结果消息
                        VictorMsg mqMsg = JSONObject.parseObject(msgExt.getBody(),VictorMsg.class);

                        LOGGER.info("从消息队列中收到战斗结果, winnerId = {}, loserId = {}",mqMsg.winnerId,mqMsg.loserId);

                        // 刷新排行榜
                        RankService .getInstance().refreshRank(mqMsg.winnerId,mqMsg.loserId);
                    }

                    return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
                }
            });

        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }

    }
}
