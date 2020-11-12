package com.netty_book.chapter06_channelHandler;

import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.ReferenceCountUtil;

//Channel，表示一个连接，可以理解为每一个请求，就是一个Channel。
//        ChannelHandler，核心处理业务就在这里，用于处理业务请求。
//        ChannelHandlerContext，用于传输业务数据。
//        ChannelPipeline，用于保存处理过程需要用到的ChannelHandler和ChannelHandlerContext。


//释放消息资源
public class DiscardHandler extends ChannelHandlerAdapter {
    public  void channelRead(ChannelHandlerContext ctx,Object msg){
        ReferenceCountUtil.release(msg);// 需要　丢弃已经接收的消息
    }
}
