package org.tinygame.herostory.async;

/**
 * 异步操作接口
 */
public interface IAsyncOperation {
    /**
     * 获取绑定id
     * @return 返回绑定id、默认是0
     */
    default int bindId(){
        return 0;
    }
    /**
     * 执行异步操作
     */
    void doAsunc();

    /**
     * 执行完成逻辑
     * 默认
     */
    default void doFinish() {
    }
}
