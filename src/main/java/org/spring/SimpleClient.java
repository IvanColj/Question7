package org.spring;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.util.CharsetUtil;
import io.netty.buffer.Unpooled;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;

public class SimpleClient {
    public void start() throws Exception {
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group)
                    .channel(NioDatagramChannel.class)
                    .handler(new SimpleUdpClientHandler());

            Channel channel = bootstrap.bind(0).sync().channel();
            BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

            while (true) {
                String input = in.readLine();
                if ("exit".equalsIgnoreCase(input)) {
                    break;
                }
                channel.writeAndFlush(new DatagramPacket(
                        Unpooled.copiedBuffer(input, CharsetUtil.UTF_8),
                        new InetSocketAddress("localhost", 1234)));
            }
        } finally {
            group.shutdownGracefully();
        }
    }

    private static class SimpleUdpClientHandler extends SimpleChannelInboundHandler<DatagramPacket> {
        @Override
        protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket packet) {
            String response = packet.content().toString(CharsetUtil.UTF_8);
            System.out.println("Echoed: " + response);
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            cause.printStackTrace();
            ctx.close();
        }
    }
}
