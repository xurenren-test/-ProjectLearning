package org.tinygame.herostory.cmdHandler;

import com.google.protobuf.GeneratedMessageV3;
import io.netty.channel.ChannelHandlerContext;

/**
 * 指令处理器接口
 * GeneratedMessageV3 是各类消息的父类，实现时不用在做处理
 * @param <TCmd>
 */
public interface ICmdHandler<TCmd extends GeneratedMessageV3> {

    // 处理指令
    void handle(ChannelHandlerContext ctx,TCmd cmd);
}
