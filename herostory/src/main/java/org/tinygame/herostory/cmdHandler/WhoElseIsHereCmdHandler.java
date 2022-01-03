package org.tinygame.herostory.cmdHandler;

import io.netty.channel.ChannelHandlerContext;
import org.tinygame.herostory.model.MoveState;
import org.tinygame.herostory.model.User;
import org.tinygame.herostory.model.UserManager;
import org.tinygame.herostory.msg.GameMsgProtocol;

public class WhoElseIsHereCmdHandler implements ICmdHandler<GameMsgProtocol.WhoElseIsHereCmd>{

    public void handle(ChannelHandlerContext ctx, GameMsgProtocol.WhoElseIsHereCmd msg) {
        // 构建有哪些角色在场结果消息
        GameMsgProtocol.WhoElseIsHereResult.Builder resultBuilder = GameMsgProtocol.WhoElseIsHereResult.newBuilder();

        for (User currUser : UserManager.listUser()) {
            if (currUser == null) {
                continue;
            }

            // 构建每一个用户的信息
            GameMsgProtocol.WhoElseIsHereResult.UserInfo.Builder userInfoBuilder = GameMsgProtocol.WhoElseIsHereResult.UserInfo.newBuilder();
            userInfoBuilder.setUserId(currUser.userId);
            userInfoBuilder.setHeroAvatar(currUser.heroAvatar);

            // 获取移动状态
            MoveState mvState = currUser.moveState;
            // 构建移动状态消息
            GameMsgProtocol.WhoElseIsHereResult.UserInfo.MoveState.Builder mvStateBulder
                    = GameMsgProtocol.WhoElseIsHereResult.UserInfo.MoveState.newBuilder();
            // 起始位置
            mvStateBulder.setFromPosX(mvState.fromPosX);
            mvStateBulder.setFromPosY(mvState.fromPosY);
            // 目标位置
            mvStateBulder.setToPosX(mvState.toPosX);
            mvStateBulder.setToPosY(mvState.toPosY);
            mvStateBulder.setStartTime(mvState.startTime);

            // 将移动状态设置到用户角色上
            userInfoBuilder.setMoveState(mvStateBulder);

            resultBuilder.addUserInfo(userInfoBuilder);

        }

        GameMsgProtocol.WhoElseIsHereResult newResult = resultBuilder.build();
        ctx.writeAndFlush(newResult);
    }
}
