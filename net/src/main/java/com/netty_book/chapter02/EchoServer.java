package com.netty_book.chapter02;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.net.InetSocketAddress;

public class EchoServer {
    private final  int port;

    public EchoServer(int port) {
        this.port = port;
    }

    public static void main(String[] args) throws InterruptedException {
//        if (args.length==1){
//            System.err.println("Usage:"+EchoServer.class.getSimpleName()+"<port>");
//            return;
//        }
//        int i = Integer.parseInt(args[0]);
//        System.out.println("server:"+":::"+i);

        new EchoServer(8888).start();
        System.out.println(args.length);
        System.out.println(args[0]);
    }

    private void start() throws InterruptedException {
        final EchoServerHandler serverHandler=new EchoServerHandler();
        NioEventLoopGroup group = new NioEventLoopGroup();
        ServerBootstrap b = new ServerBootstrap();
      b.group(group)
              .channel(NioServerSocketChannel.class)
              .localAddress(new InetSocketAddress(port))
              .childHandler(new ChannelInitializer<SocketChannel>() {
                  @Override
                  protected void initChannel(SocketChannel ch) throws Exception {
                      ch.pipeline().addLast(serverHandler);

                  }
              });
        ChannelFuture f = b.bind().sync();
        f.channel().closeFuture().sync();
        group.shutdownGracefully().sync();

    }


}
