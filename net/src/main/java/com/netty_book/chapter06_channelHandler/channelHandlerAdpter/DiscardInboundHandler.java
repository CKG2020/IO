package com.netty_book.chapter06_channelHandler.channelHandlerAdpter;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;

public class DiscardInboundHandler  extends ChannelInboundHandlerAdapter {
    public  void channelRead(ChannelHandlerContext ctx,Object msg){
        ReferenceCountUtil.release(msg);
    }
}
