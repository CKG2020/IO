package com.netty_book.zerocopy;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.nio.ByteBuffer;

public class TEst {
    //代码1
    public static void main(String[] args) {
        byte[] byte1 = "he     ".getBytes();
        byte[] byte2 = "llo     ".getBytes();
//
//        ByteBuffer b1 = ByteBuffer.allocate(10);
//        b1.put(byte1);
//        ByteBuffer b2 = ByteBuffer.allocate(10);
//        b2.put(byte2);
//        ByteBuffer b3 = ByteBuffer.allocate(20);
//        ByteBuffer[] b4 = {b1, b2};    // #1
//        b3.put(b1.array());
//        b3.put(b2.array());             //#2
//        //读取内容
//        System.out.println(new String(b3.array()));
//        System.out.println("b1 addr:" + b1.array());
//        System.out.println("b2 addr:" + b2.array());
//        System.out.println("b3 addr:" + b3.array());

        ByteBuf nb1 = Unpooled.buffer(10);
        nb1.writeBytes(byte1);
        ByteBuf nb2 = Unpooled.buffer(10);
        nb2.writeBytes(byte2);
        //      nb2.array();
        ByteBuf nb3 = Unpooled.wrappedBuffer(nb1, nb2);
        nb3.array();                  // #3
        //读取内容                       #4
        byte[] bytes = new byte[20];
        for(int i =0; i< nb3.capacity(); i++) {
            bytes[i] = nb3.getByte(i);
        }
        System.out.println(new String(bytes));

    }

}
