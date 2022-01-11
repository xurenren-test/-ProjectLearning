package org.tinygame.herostory.async;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinygame.herostory.MainThreadProcessor;
import org.tinygame.herostory.cmdHandler.UserAttkCmdHandler;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

/**
 * 异步操作处理器 （相当于厨师）
 */
public final class AsyncOperationProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserAttkCmdHandler.class);
    /**
     * 单例对象
     */
    private static final AsyncOperationProcessor instance = new AsyncOperationProcessor();

    // 创建一个单线程数组
    private final ExecutorService esArray[] = new ExecutorService[8];

    /**
     * 私有化构造器
     */
    private AsyncOperationProcessor() {
        for (int i = 0; i < esArray.length; i++) {
            // 线程名称
            final String threadName = "AsyncOperationProcessor_" + i;
            // 创建一个单线程
            esArray[i] = Executors.newSingleThreadExecutor(new ThreadFactory() {
                @Override
                public Thread newThread(Runnable r) {
                    Thread newThread = new Thread(r);
                    // 重置线程名
                    newThread.setName(threadName);
                    return newThread;
                }
            });
        }
    }


    /**
     * 获取单例对象
     *
     * @return 异步操作处理器
     */
    public static AsyncOperationProcessor getInstance() {
        return instance;
    }

    /**
     * 处理异步操作
     *
     * @param asyncOp 异步操作
     */
    public void process(IAsyncOperation asyncOp) {
        if (asyncOp == null) {
            return;
        }

        // 根据bindId获取线程索引
        // 避免连续点击登录出现多个线程执行登录操作
        int bindId = Math.abs(asyncOp.bindId());
        int esIndex = bindId % esArray.length;

        esArray[esIndex].submit(() -> {
            try {
                // IAsyncOperation 先由自身es线程去处理doAsunc方法，完成异步调用过程
                asyncOp.doAsunc();

                // 异步调用完成后，返回一个runnable回主线程继续执行
                MainThreadProcessor.getInstance().process(() -> {
                    asyncOp.doFinish();
                });
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
            }
        });
    }
}
