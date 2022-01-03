package org.tinygame.herostory.cmdHandler;

import com.google.protobuf.GeneratedMessageV3;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinygame.herostory.util.PackageUtil;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * 指令处理器工厂
 */
public final class CmdHandlerFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(CmdHandlerFactory.class);
    // 处理器字典
    private static final Map<Class<?>, ICmdHandler<? extends GeneratedMessageV3>> handlerMap = new HashMap<>();

    // 私有化默认构造器
    private CmdHandlerFactory() {
    }

    public static void init() {
//        handlerMap.put(GameMsgProtocol.UserEntryCmd.class, new UserEntryCmdHandler());
//        handlerMap.put(GameMsgProtocol.WhoElseIsHereCmd.class, new WhoElseIsHereCmdHandler());
//        handlerMap.put(GameMsgProtocol.UserMoveToCmd.class, new UserMoveToCmdHandler());
        LOGGER.info("==== 完成 Cmd 和 Handler 的关联 ====");

        // 获取实现ICmdHandler的所有实现类列表
        Set<Class<?>> clazzSet = PackageUtil.listSubClazz(CmdHandlerFactory.class.getPackage().getName(), true, ICmdHandler.class);

        for (Class<?> clazz : clazzSet) {
            // getModifiers获取修饰符，做 & 位运算，判断是否属于抽象类
            if ((clazz.getModifiers() & Modifier.ABSTRACT) != 0) {
                continue; // 如果属于抽象类，直接退出
            }
            // 获取方法数组
            Method[] methodArray = clazz.getDeclaredMethods();
            // 消息类型
            Class<?> msgType = null;

            for (Method currMethod : methodArray) {
                if (!currMethod.getName().equals("handle")) {
                    // 如果不是handle方法
                    continue;
                }

                // 获取方法中参数类型
                Class<?>[] paramTypeArray = currMethod.getParameterTypes();
                // 参数小于2，或者第二个参数消息类型不属于 GeneratedMessageV3 ，则退出
                if (paramTypeArray.length < 2 ||
                        !GeneratedMessageV3.class.isAssignableFrom(paramTypeArray[1])) {
                    continue;
                }

                msgType = paramTypeArray[1];
                break;
            }
            if (msgType == null) {
                continue;
            }

            try {
                ICmdHandler<?> newHandler = (ICmdHandler<?>) clazz.newInstance();

                LOGGER.info("{} <===> {}",msgType.getName(),clazz.getName());

                handlerMap.put(msgType,newHandler);
            } catch (Exception e) {
                LOGGER.error(e.getMessage(),e);
            }
        }
    }

    public static ICmdHandler<? extends GeneratedMessageV3> cerate(Class<?> msgclzz) {
        if (msgclzz == null) {
            return null;
        }
        return handlerMap.get(msgclzz);
    }
}
