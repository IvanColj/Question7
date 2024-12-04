package org.spring;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.util.CharsetUtil;

public class SimpleServer {
    public void start() throws Exception {
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group)
                    .channel(NioDatagramChannel.class)
                    .option(ChannelOption.SO_BROADCAST, true)
                    .handler(new SimpleUdpServerHandler());

            bootstrap.bind(1234).sync().channel().closeFuture().await();
        } finally {
            group.shutdownGracefully();
        }
    }

    private static class SimpleUdpServerHandler extends SimpleChannelInboundHandler<DatagramPacket> {
        @Override
        protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket packet) {
            String received = packet.content().toString(CharsetUtil.UTF_8);
            System.out.println("Received: " + received);
            ctx.writeAndFlush(new DatagramPacket(
                    packet.content().retain(),
                    packet.sender())); // Отправляем обратно клиенту
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            cause.printStackTrace();
            ctx.close();
        }
    }
}
