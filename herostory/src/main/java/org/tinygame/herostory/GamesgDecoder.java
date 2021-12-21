package org.tinygame.herostory;

import com.google.protobuf.GeneratedMessageV3;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import javafx.css.CssMetaData;
import org.tinygame.herostory.msg.GameMsgProtocol;

/**
 * 消息解码器
 */
public class GamesgDecoder extends ChannelInboundHandlerAdapter {
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (!(msg instanceof BinaryWebSocketFrame)) {
            return;
        }
        // WebSocket 二进制消息会通过 HttpServerCodec 解码成 BinaryWebSocketFrame 类对象
        BinaryWebSocketFrame frame = (BinaryWebSocketFrame) msg;
        ByteBuf byteBuf = frame.content();

        byteBuf.readShort(); // 读取消息的长度，例如打印的二进制字节码：0,2,0,2, 前两个字节是占位，第二个字节表示除前两个字节外的字节长度
        int msgCode = byteBuf.readShort();// 读取消息的编号，例如：0,2,0,2 从第三个字节开始，0对应GameMsgProtocol.proto文件对应的值

        // 获取到消息体
        byte[] msgbody = new byte[byteBuf.readableBytes()];
        byteBuf.readBytes(msgbody);

        GeneratedMessageV3 cmd = null;

        switch (msgCode) {
            // 如果读取的消息体是USER_ENTRY_CMD_VALUE类型，则转换为GameMsgProtocol.UserEntryCmd的对象
            case GameMsgProtocol.MsgCode.USER_ENTRY_CMD_VALUE:
                cmd = GameMsgProtocol.UserEntryCmd.parseFrom(msgbody);
                break;

            case GameMsgProtocol.MsgCode.WHO_ELSE_IS_HERE_CMD_VALUE:
                cmd = GameMsgProtocol.WhoElseIsHereCmd.parseFrom(msgbody);
                break;

            case GameMsgProtocol.MsgCode.USER_MOVE_TO_CMD_VALUE:
                cmd = GameMsgProtocol.UserMoveToCmd.parseFrom(msgbody);
                break;
        }
        if (cmd != null){
            ctx.fireChannelRead(cmd);
        }
    }
}
