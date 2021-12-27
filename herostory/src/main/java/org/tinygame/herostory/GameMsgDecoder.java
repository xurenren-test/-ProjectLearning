package org.tinygame.herostory;

import com.google.protobuf.GeneratedMessageV3;
import com.google.protobuf.Message;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * 消息解码器
 */
public class GameMsgDecoder extends ChannelInboundHandlerAdapter {

    private static Logger LOGGER = LoggerFactory.getLogger(GameMsgDecoder.class);
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (!(msg instanceof BinaryWebSocketFrame)) {
            return;
        }

        // WebSocket 二进制消息会通过 HttpServerCodec 解码成 BinaryWebSocketFrame 类对象
        BinaryWebSocketFrame frame = (BinaryWebSocketFrame) msg;
        ByteBuf byteBuf = frame.content();

        byteBuf.readShort(); // 读取客户端消息的长度
        int msgCode = byteBuf.readShort(); // 读取客户端消息的编号

        // 获取消息构建者
        Message.Builder msgBuilder = GameMsgRecognizer.getBuilderByMsgCode(msgCode);
        if (msgBuilder == null){
            LOGGER.error("无法识别的消息, msgCode = {}", msgCode);
            return;
        }
        // 拿到消息体
        byte[] msgBody = new byte[byteBuf.readableBytes()];
        byteBuf.readBytes(msgBody);

        msgBuilder.mergeFrom(msgBody);
        msgBuilder.clear();
        // 构建消息
        Message newMsg = msgBuilder.build();

        if (newMsg != null) {
            ctx.fireChannelRead(newMsg);
        }
//        GeneratedMessageV3 cmd = null;
//
//        switch (msgCode) {
//            case GameMsgProtocol.MsgCode.USER_ENTRY_CMD_VALUE:
//                cmd = GameMsgProtocol.UserEntryCmd.parseFrom(msgBody);
//                break;
//            case GameMsgProtocol.MsgCode.WHO_ELSE_IS_HERE_CMD_VALUE:
//                cmd = GameMsgProtocol.WhoElseIsHereCmd.parseFrom(msgBody);
//                break;
//            case GameMsgProtocol.MsgCode.USER_MOVE_TO_CMD_VALUE:
//                cmd = GameMsgProtocol.UserMoveToCmd.parseFrom(msgBody);
//                break;
        }

    }

