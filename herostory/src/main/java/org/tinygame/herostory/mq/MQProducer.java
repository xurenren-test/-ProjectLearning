package org.tinygame.herostory.mq;

import com.alibaba.fastjson.JSONObject;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.common.message.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 消息队列生产者
 */
public final class MQProducer {

    static private final Logger LOGGER = LoggerFactory.getLogger(MQProducer.class);

    /**
     * 生产者
     */
    private static DefaultMQProducer producer = null;

    private MQProducer() {
    }

    /**
     * 初始化
     */
    public static void init() {
        try {
            // 创建生产者
            DefaultMQProducer dfproducer = new DefaultMQProducer("herostory");
            // 指定 nameServer 地址
            dfproducer.setNamesrvAddr("127.0.0.1:9876");
            dfproducer.start();
            // 失败后重发
            dfproducer.setRetryTimesWhenSendFailed(3);

            producer = dfproducer;
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    /**
     * 发送消息
     *
     * @param topic 主题
     * @param msg   消息对象
     */
    public static void sendMsg(String topic, Object msg) {
        if (topic == null || msg == null) {
            return;
        }
        if (producer == null) {
            throw new RuntimeException("producter 尚未初始化");
        }

        Message mqMsg = new Message();
        mqMsg.setTopic(topic);
        mqMsg.setBody(JSONObject.toJSONBytes(msg));

        try {
            producer.send(mqMsg);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }

    }
}
