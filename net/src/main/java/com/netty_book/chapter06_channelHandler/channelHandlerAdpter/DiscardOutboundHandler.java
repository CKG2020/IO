package com.netty_book.chapter06_channelHandler.channelHandlerAdpter;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import io.netty.util.ReferenceCountUtil;

public class DiscardOutboundHandler  extends ChannelOutboundHandlerAdapter {
    public  void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise){
        ReferenceCountUtil.release(msg);  //释放资源
        promise.setSuccess();//通知ＣｈａｎｎｅｌＰｒｏｍｉｓｅ数据已经被处理
    }
}
