package org.tinygame.herostory.cmdHandler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.util.AttributeKey;
import org.tinygame.herostory.Broadcaster;
import org.tinygame.herostory.msg.GameMsgProtocol;

public class UserMoveToCmdHandler implements ICmdHandler<GameMsgProtocol.UserMoveToCmd>{

    @Override
    public void handle(ChannelHandlerContext ctx, GameMsgProtocol.UserMoveToCmd msg) {
        // 从channel中获取用户id
        Integer userId = (Integer) ctx.channel().attr(AttributeKey.valueOf("userId")).get();

        if (userId == null) {
            return;
        }

        GameMsgProtocol.UserMoveToCmd cmd = msg;

        GameMsgProtocol.UserMoveToResult.Builder resultBuilder = GameMsgProtocol.UserMoveToResult.newBuilder();
        resultBuilder.setMoveUserId(userId);
        resultBuilder.setMoveToPosX(cmd.getMoveToPosX());
        resultBuilder.setMoveToPosY(cmd.getMoveToPosY());

        GameMsgProtocol.UserMoveToResult newResult = resultBuilder.build();
        // 移动的消息群发
        Broadcaster.broadcast(newResult);
    }
}
