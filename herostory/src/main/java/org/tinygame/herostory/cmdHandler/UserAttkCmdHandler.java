package org.tinygame.herostory.cmdHandler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.util.AttributeKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinygame.herostory.Broadcaster;
import org.tinygame.herostory.model.User;
import org.tinygame.herostory.model.UserManager;
import org.tinygame.herostory.mq.MQProducer;
import org.tinygame.herostory.mq.VictorMsg;
import org.tinygame.herostory.msg.GameMsgProtocol;

/**
 * 用户攻击指令处理器
 */
public class UserAttkCmdHandler implements ICmdHandler<GameMsgProtocol.UserAttkCmd> {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserAttkCmdHandler.class);

    @Override
    public void handle(ChannelHandlerContext ctx, GameMsgProtocol.UserAttkCmd cmd) {
        if (ctx == null || cmd == null) {
            return;
        }
        // 获取发动攻击的用户id
        Integer attkUserId = (Integer) ctx.channel().attr(AttributeKey.valueOf("userId")).get();
        if (attkUserId == null) {
            return;
        }
        // 获取被攻击者id
        int targetUserId = cmd.getTargetUserId();
        // 构建攻击消息
        GameMsgProtocol.UserAttkResult.Builder resultBuilder = GameMsgProtocol.UserAttkResult.newBuilder();
        resultBuilder.setAttkUserId(attkUserId);
        resultBuilder.setTargetUserId(targetUserId);

        // 攻击消息广播
        GameMsgProtocol.UserAttkResult newResult = resultBuilder.build();
        Broadcaster.broadcast(newResult);

        // 获取被攻击用户
        User targetUser = UserManager.getUserById(targetUserId);
        if (targetUser == null) {
            return;
        }

        LOGGER.info("当前线程 = {}", Thread.currentThread().getName());

        int subtractHp = 10; // 攻击减血量，可根据业务变更
        targetUser.currHp = targetUser.currHp - subtractHp;

        // 广播减血消息
        broadcastSubtractHp(targetUserId, subtractHp);

        if (targetUser.currHp <= 0) {
            broadcastDie(targetUserId);

            if (!targetUser.died) {
                targetUser.died = true;

                // 发送消息到 MQ
                VictorMsg mqMsg = new VictorMsg();
                mqMsg.winnerId = attkUserId;
                mqMsg.loserId = targetUserId;
                MQProducer.sendMsg("Victor", mqMsg);
            }
        }

    }

    /**
     * 广播减血消息
     *
     * @param targetUserId 目标用户id
     * @param subtractHp   减血量
     */
    private static void broadcastSubtractHp(int targetUserId, int subtractHp) {
        if (targetUserId <= 0 || subtractHp <= 0) {
            return;
        }
        // 构建被攻击者减血消息
        GameMsgProtocol.UserSubtractHpResult.Builder resultBuilder = GameMsgProtocol.UserSubtractHpResult.newBuilder();
        resultBuilder.setTargetUserId(targetUserId);
        resultBuilder.setSubtractHp(subtractHp);

        // 减血消息广播
        GameMsgProtocol.UserSubtractHpResult newResult = resultBuilder.build();
        Broadcaster.broadcast(newResult);
    }

    /**
     * 广播死亡消息
     *
     * @param targetUserId
     */
    private static void broadcastDie(int targetUserId) {
        if (targetUserId <= 0) {
            return;
        }

        GameMsgProtocol.UserDieResult.Builder resultBuilder = GameMsgProtocol.UserDieResult.newBuilder();
        resultBuilder.setTargetUserId(targetUserId);

        GameMsgProtocol.UserDieResult newResult = resultBuilder.build();
        Broadcaster.broadcast(newResult);
    }
}