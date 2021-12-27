package org.tinygame.herostory;

import io.netty.channel.Channel;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;

/**
 * 消息群发
 */
public final class Broadcaster {
    // 客户端信道数组，消息群发（一定要是static，否则无法实现群发）
    static private final ChannelGroup channelGroup = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    // 私有化默认构造器
    private Broadcaster() {
    }

    // 添加信道
    public static void addChannel(Channel channel){
        channelGroup.add(channel);
    }

    // 移除信道
    public static void removeChannel(Channel channel){
        channelGroup.remove(channel);
    }

    // 广播消息
    public static void broadcast(Object msg){
        if (msg == null){
            return;
        }
        channelGroup.writeAndFlush(msg);
    }
}
