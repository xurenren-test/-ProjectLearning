package org.tinygame.herostory;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinygame.herostory.cmdHandler.CmdHandlerFactory;

/**
 * 服务器入口类
 */
public class ServerMain {

    // 日志对象
    static private final Logger LOGGER = LoggerFactory.getLogger(ServerMain.class);

    public static void main(String[] args) {
        // 初始化
        CmdHandlerFactory.init();
        GameMsgRecognizer.init();
        // NioEventLoopGroup 相当于线程池
        // 负责处理客户端连接
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        // 客户端连接后，负责读写消息（负责业务）
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        // netty 启动
        ServerBootstrap b = new ServerBootstrap();
        b.group(bossGroup, workerGroup);
        // 服务器信道的处理方式
        b.channel(NioServerSocketChannel.class);

        // 客户端信道的处理器方式
        b.childHandler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel socketChannel) throws Exception {
                socketChannel.pipeline().addLast(
                        // http 服务器编解码器，保证了消息完整性
                        new HttpServerCodec(),
                        // 内容限制长度
                        new HttpObjectAggregator(65535),
                        // webSocket 协议处理器，在这里握手、ping、pong等消息
                        new WebSocketServerProtocolHandler("/websocket"),
                        // 自定义消息解码器
                        new GameMsgDecoder(),
                        // 自定义消息编码器
                        new GameMsgEncoder(),
                        // 自定义的消息处理器
                        new GameMsgHandler()
                );
            }
        });

        try {
            // 绑定端口12345，实际项目中会使用argArray中的参数来指定端口
            ChannelFuture f = b.bind(12345).sync();

            if (f.isSuccess()) {
                LOGGER.info("服务器启动成功");
            }
            // 等待服务器信道关闭，也就是不要退出应用程序，让应用程序可以一直提供服务
            f.channel().closeFuture().sync();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
