package org.tinygame.herostory;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.GlobalEventExecutor;
import org.tinygame.herostory.msg.GameMsgProtocol;

import java.util.HashMap;
import java.util.Map;

/**
 * 自定义消息处理器
 */
public class GameMsgHandler extends SimpleChannelInboundHandler<Object> {
    // 客户端信道数组，消息群发（一定要是static，否则无法实现群发）
    static private final ChannelGroup channelGroup = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    //用户字典，根据用户id与用户建立一对一的关系
    static private final Map<Integer, User> userMap = new HashMap<>();

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        // 新用户连接后加入channelGroup
        channelGroup.add(ctx.channel());
    }

    //用户掉线离场
    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        super.handlerRemoved(ctx);
        channelGroup.remove(ctx.channel());
        // 获取掉线用户id
        Integer userId = (Integer) ctx.channel().attr(AttributeKey.valueOf("userId")).get();

        if (userId == null) {
            return;
        }

        userMap.remove(userId);
        //构建客户端用户离场消息
        GameMsgProtocol.UserQuitResult.Builder resultBuilder = GameMsgProtocol.UserQuitResult.newBuilder();
        resultBuilder.setQuitUserId(userId);
        // 客户端用户离场消息群发
        GameMsgProtocol.UserQuitResult newResult = resultBuilder.build();
        channelGroup.writeAndFlush(newResult);

    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
        System.out.println("收到客户端消息，msgClass = " + msg.getClass().getName() + ",msg = " + msg);
        if (msg instanceof GameMsgProtocol.UserEntryCmd) {
            // 从指令对象中获取英雄id和英雄形象
            GameMsgProtocol.UserEntryCmd cmd = (GameMsgProtocol.UserEntryCmd) msg;
            int userId = cmd.getUserId();
            String heroAvatar = cmd.getHeroAvatar();

            // 构建一个user的result返回
            GameMsgProtocol.UserEntryResult.Builder resultBuilder = GameMsgProtocol.UserEntryResult.newBuilder();
            resultBuilder.setUserId(userId);
            resultBuilder.setHeroAvatar(heroAvatar);

            //将用户加入字典，在场角色加入
            User newUser = new User();
            newUser.userId = userId;
            newUser.heroAvatar = heroAvatar;
            userMap.put(newUser.userId, newUser);

            // 将用户 Id 附着到 Channel
            ctx.channel().attr(AttributeKey.valueOf("userId")).set(userId);

            // 构建结果并群发
            GameMsgProtocol.UserEntryResult newResult = resultBuilder.build();
            channelGroup.writeAndFlush(newResult);

        } else if (msg instanceof GameMsgProtocol.WhoElseIsHereCmd) {
            // 构建有哪些角色在场结果消息
            GameMsgProtocol.WhoElseIsHereResult.Builder resultBuilder = GameMsgProtocol.WhoElseIsHereResult.newBuilder();

            for (User currUser : userMap.values()) {
                if (currUser == null) {
                    continue;
                }

                GameMsgProtocol.WhoElseIsHereResult.UserInfo.Builder userInfoBuilder = GameMsgProtocol.WhoElseIsHereResult.UserInfo.newBuilder();
                userInfoBuilder.setUserId(currUser.userId);
                userInfoBuilder.setHeroAvatar(currUser.heroAvatar);
                resultBuilder.addUserInfo(userInfoBuilder);

            }

            GameMsgProtocol.WhoElseIsHereResult newResult = resultBuilder.build();
            ctx.writeAndFlush(newResult);
        } else if (msg instanceof GameMsgProtocol.UserMoveToCmd) {
            // 从channel中获取用户id
            Integer userId = (Integer) ctx.channel().attr(AttributeKey.valueOf("userId")).get();

            if (userId == null) {
                return;
            }

            GameMsgProtocol.UserMoveToCmd cmd = (GameMsgProtocol.UserMoveToCmd) msg;

            GameMsgProtocol.UserMoveToResult.Builder resultBuilder = GameMsgProtocol.UserMoveToResult.newBuilder();
            resultBuilder.setMoveUserId(userId);
            resultBuilder.setMoveToPosX(cmd.getMoveToPosX());
            resultBuilder.setMoveToPosY(cmd.getMoveToPosY());

            GameMsgProtocol.UserMoveToResult newResult = resultBuilder.build();
            // 移动的消息群发
            channelGroup.writeAndFlush(newResult);

        }
    }
}
