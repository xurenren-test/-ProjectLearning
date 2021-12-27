package org.tinygame.herostory;


import com.google.protobuf.GeneratedMessageV3;
import com.google.protobuf.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinygame.herostory.msg.GameMsgProtocol;

import java.util.HashMap;
import java.util.Map;


/**
 * 消息识别器
 */
public final class GameMsgRecognizer {

    private static final Logger LOGGER = LoggerFactory.getLogger(GameMsgRecognizer.class);
    // 消息代码和消息体字典
    private static final Map<Integer, GeneratedMessageV3> msgCodeAndMsgBodyMap = new HashMap<>();
    // 消息类型和消息编号字典
    private static final Map<Class<?>, Integer> msgClazzAndMsgCodeMap = new HashMap<>();

    // 私有化默认构造器
    private GameMsgRecognizer() {
    }

    // 初始化
    public static void init() {
        // 获取GameMsgProtocol 下定义的内部类
        Class<?>[] innerClassArray = GameMsgProtocol.class.getDeclaredClasses();

        for (Class<?> innerClazz : innerClassArray) {
            if (!GeneratedMessageV3.class.isAssignableFrom(innerClazz)) {
                continue;
            }
            // 获取到类名
            String className = innerClazz.getSimpleName();
            className = className.toLowerCase();

            // 遍历消息代号，如移动
            for (GameMsgProtocol.MsgCode msgCode : GameMsgProtocol.MsgCode.values()) {
                String strMsgCode = msgCode.name();
                // 取消代号中下划线
                strMsgCode.replaceAll("_", "");
                strMsgCode.toLowerCase();
                // 判断是否以该类名开头
                if (!strMsgCode.startsWith(className)) {
                    continue;
                }
                try {
                    // 通过GameMsgProtocol内部getDefaultInstance 返回消息代码实列
                    Object returnObj = innerClazz.getDeclaredMethod("getDefaultInstance").invoke(innerClazz);

                    // 消息与消息编号一一映射
                    LOGGER.info("{} <==> {}", innerClazz.getName(), msgCode.getNumber());

                    msgCodeAndMsgBodyMap.put(msgCode.getNumber(), (GeneratedMessageV3) returnObj);

                    msgClazzAndMsgCodeMap.put(innerClazz, msgCode.getNumber());
                } catch (Exception e) {
                    LOGGER.error(e.getMessage(), e);
                }

            }
        }
//        msgCodeAndMsgBodyMap.put(GameMsgProtocol.MsgCode.USER_ENTRY_CMD_VALUE, GameMsgProtocol.UserEntryCmd.getDefaultInstance());
//        msgCodeAndMsgBodyMap.put(GameMsgProtocol.MsgCode.WHO_ELSE_IS_HERE_CMD_VALUE, GameMsgProtocol.WhoElseIsHereCmd.getDefaultInstance());
//        msgCodeAndMsgBodyMap.put(GameMsgProtocol.MsgCode.USER_MOVE_TO_CMD_VALUE, GameMsgProtocol.UserMoveToCmd.getDefaultInstance());

//        msgClazzAndMsgCodeMap.put(GameMsgProtocol.UserEntryResult.class, GameMsgProtocol.MsgCode.USER_ENTRY_RESULT_VALUE);
//        msgClazzAndMsgCodeMap.put(GameMsgProtocol.WhoElseIsHereResult.class, GameMsgProtocol.MsgCode.WHO_ELSE_IS_HERE_RESULT_VALUE);
//        msgClazzAndMsgCodeMap.put(GameMsgProtocol.UserMoveToResult.class, GameMsgProtocol.MsgCode.USER_MOVE_TO_RESULT_VALUE);
//        msgClazzAndMsgCodeMap.put(GameMsgProtocol.UserQuitResult.class, GameMsgProtocol.MsgCode.USER_QUIT_RESULT_VALUE);

    }

    /**
     * 根据消息编号获取构建者
     */
    public static Message.Builder getBuilderByMsgCode(int msgCode) {
        if (msgCode < 0) {
            return null;
        }
        GeneratedMessageV3 msg = msgCodeAndMsgBodyMap.get(msgCode);
        if (msg == null) {
            return null;
        }
        return msg.newBuilderForType();
    }

    /**
     * 根据消息类获取消息编号
     */
    public static int getMsgCodeByClazz(Class<?> msgClass) {
        if (msgClass == null) {
            return -1;
        }
        Integer msgCode = msgClazzAndMsgCodeMap.get(msgClass);
        if (msgCode != null) {
            return msgCode.intValue();
        } else {
            return -1;
        }
    }

}
