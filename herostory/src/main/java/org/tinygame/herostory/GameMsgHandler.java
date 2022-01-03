package org.tinygame.herostory;

import com.google.protobuf.GeneratedMessageV3;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.AttributeKey;
import org.tinygame.herostory.cmdHandler.*;
import org.tinygame.herostory.model.UserManager;
import org.tinygame.herostory.msg.GameMsgProtocol;


/**
 * 自定义消息处理器
 */
public class GameMsgHandler extends SimpleChannelInboundHandler<Object> {

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        // 新用户连接后加入channelGroup
        Broadcaster.addChannel(ctx.channel());
    }

    //用户掉线离场
    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        super.handlerRemoved(ctx);
        Broadcaster.removeChannel(ctx.channel());
        // 获取掉线用户id
        Integer userId = (Integer) ctx.channel().attr(AttributeKey.valueOf("userId")).get();

        if (userId == null) {
            return;
        }

        UserManager.removeUserById(userId);
        //构建客户端用户离场消息
        GameMsgProtocol.UserQuitResult.Builder resultBuilder = GameMsgProtocol.UserQuitResult.newBuilder();
        resultBuilder.setQuitUserId(userId);
        // 客户端用户离场消息群发
        GameMsgProtocol.UserQuitResult newResult = resultBuilder.build();
        Broadcaster.broadcast(newResult);

    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
        System.out.println("收到客户端消息，msgClass = " + msg.getClass().getName() + ",msg = " + msg);

        // ？相当于占位，不知道消息类型，只知道属于GeneratedMessageV3子类（向上转型）
        ICmdHandler<? extends GeneratedMessageV3> cmdHandler = CmdHandlerFactory.cerate(msg.getClass());

        if (cmdHandler != null) {
            cmdHandler.handle(ctx, cast(msg));
        }
    }

    /**
     * 消息对象转型
     * @param msg
     * @param <TCmd>
     * @return
     */
    private static <TCmd extends GeneratedMessageV3> TCmd cast(Object msg) {
        if (msg == null) {
            return null;
        } else {
            return (TCmd) msg;
        }
    }

}
