package org.tinygame.herostory;

import com.google.protobuf.GeneratedMessageV3;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinygame.herostory.cmdHandler.CmdHandlerFactory;
import org.tinygame.herostory.cmdHandler.ICmdHandler;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

/**
 * 主线程处理器 （相当于上菜员）
 */
public final class MainThreadProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(CmdHandlerFactory.class);


    private MainThreadProcessor() {
    }

    // 单列对象
    private static final MainThreadProcessor instance = new MainThreadProcessor();

    // 创建一个单线程
    // 不使用多线程的原因是：多线程的并发导致的数据读写，会造成死锁
    private final ExecutorService es = Executors.newSingleThreadExecutor(new ThreadFactory() {
        @Override
        public Thread newThread(Runnable r) {
            Thread newThread = new Thread(r);
            // 重置主线程名
            newThread.setName("MainThreadProcessor");
            return newThread;
        }
    });

    /**
     * 获取单例对象
     *
     * @return 返回主险处理器
     */
    public static MainThreadProcessor getInstance() {
        return instance;
    }

    /**
     * 处理客户端消息
     *
     * @param ctx 客户端信道上下文
     * @param msg 消息对象
     */
    public void process(ChannelHandlerContext ctx, GeneratedMessageV3 msg) {
        if (ctx == null || msg == null) {
            return;
        }

        this.es.submit(() -> {
            // 获取消息类
            Class<?> msgClazz = msg.getClass();

            LOGGER.info("收到客户端消息，msgClass = " + msg.getClass().getName() + ",msg = " + msg);

            // 获取指令处理器
            // ？相当于占位，不知道消息类型，只知道属于GeneratedMessageV3子类（向上转型）
            ICmdHandler<? extends GeneratedMessageV3> cmdHandler = CmdHandlerFactory.cerate(msg.getClass());

            if (cmdHandler == null) {
                LOGGER.info("未找到相对应的指令处理器, msgClazz = {}" + msgClazz.getName());
                return;
            }

            try {
                cmdHandler.handle(ctx, cast(msg));
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
            }
        });

    }

    /**
     * 处理消息对象
     * @param r Runnable实例
     */
    public void process(Runnable r) {
        if (r != null) {
            es.submit(r);
        }
    }

    /**
     * 消息对象转型
     *
     * @param msg
     * @param <TCmd>
     * @return
     */
    private static <TCmd extends GeneratedMessageV3> TCmd cast(Object msg) {
        if (msg == null) {
            return null;
        } else {
            return (TCmd) msg;
        }
    }
}
