package com.netty_book.chapter02_demo;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.CharsetUtil;

@ChannelHandler.Sharable
public class EchoServerHandler  extends ChannelInboundHandlerAdapter {

    public  void channelRead(ChannelHandlerContext ctx,Object msg){
          ByteBuf in= (ByteBuf) msg;
        System.out.println("a");
        System.out.println("Server received:"+in.toString(CharsetUtil.UTF_8));
        ctx.write(in);
    }

    //当读取到该批量读取的最后一条消息出发执行
    public void channelReadComplete(ChannelHandlerContext ctx){
        System.out.println("b");
        ctx.write(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
    }
    public void exceptionCaught(ChannelHandlerContext ctx,Throwable  cause){
        cause.printStackTrace();
        ctx.close();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        ctx.writeAndFlush(Unpooled.copiedBuffer("hello client!",CharsetUtil.UTF_8));
        System.out.println("d");
    }


}
