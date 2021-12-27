package org.tinygame.herostory.cmdHandler;

import com.google.protobuf.GeneratedMessageV3;
import com.sun.org.apache.bcel.internal.generic.NEW;
import org.tinygame.herostory.msg.GameMsgProtocol;

import java.util.HashMap;
import java.util.Map;

/**
 * 指令处理器工厂
 */
public final class CmdHandlerFactory {
    // 处理器字典
    private static Map<Class<?>, ICmdHandler<? extends GeneratedMessageV3>> handlerMap = new HashMap<>();

    // 私有化默认构造器
    private CmdHandlerFactory() {
    }

    public static void init() {
        handlerMap.put(GameMsgProtocol.UserEntryCmd.class, new UserEntryCmdHandler());
        handlerMap.put(GameMsgProtocol.WhoElseIsHereCmd.class, new WhoElseIsHereCmdHandler());
        handlerMap.put(GameMsgProtocol.UserMoveToCmd.class, new UserMoveToCmdHandler());
    }

    public static ICmdHandler<? extends GeneratedMessageV3> cerate(Class<?> msgclzz) {
        if (msgclzz == null) {
            return null;
        }
        return handlerMap.get(msgclzz);
    }
}
