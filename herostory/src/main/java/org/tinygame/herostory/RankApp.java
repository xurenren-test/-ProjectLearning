package org.tinygame.herostory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinygame.herostory.mq.MQConsumer;
import org.tinygame.herostory.util.RedisUtil;

/**
 * 排行榜应用程序
 */
public class RankApp {

    static private final Logger LOGGER = LoggerFactory.getLogger(RankApp.class);

    /**
     * 应用入口函数
     *
     * @param args
     */
    public static void main(String[] args) {
        RedisUtil.init();
        MQConsumer.init();
        LOGGER.info("排行榜应用程序启动成功！");
    }
}
