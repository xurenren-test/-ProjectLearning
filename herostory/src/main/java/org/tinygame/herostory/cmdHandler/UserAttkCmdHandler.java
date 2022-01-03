package org.tinygame.herostory.cmdHandler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.util.AttributeKey;
import org.tinygame.herostory.Broadcaster;
import org.tinygame.herostory.msg.GameMsgProtocol;

/**
 * 用户攻击指令处理器
 */
public class UserAttkCmdHandler implements ICmdHandler<GameMsgProtocol.UserAttkCmd> {
    @Override
    public void handle(ChannelHandlerContext ctx, GameMsgProtocol.UserAttkCmd cmd) {
        if (ctx == null || cmd == null){
            return;
        }
        // 获取发动攻击的用户id
        Integer attkUserId = (Integer) ctx.channel().attr(AttributeKey.valueOf("userId")).get();
        if (attkUserId == null){
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

        // 构建被攻击者减血消息
        GameMsgProtocol.UserSubtractHpResult.Builder resultBuilder2 = GameMsgProtocol.UserSubtractHpResult.newBuilder();
        resultBuilder2.setTargetUserId(targetUserId);
        resultBuilder2.setSubtractHp(10);

        // 减血消息广播
        GameMsgProtocol.UserSubtractHpResult newResult2 = resultBuilder2.build();
        Broadcaster.broadcast(newResult2);
    }
}
