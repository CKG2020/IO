package com.netty_book.chapter06_channelHandler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

// 继承这个类会自动释放资源
public class SimpleHandler extends SimpleChannelInboundHandler {
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, Object o) throws Exception {
 // 不需要显示的释放任何资源
    }
}
