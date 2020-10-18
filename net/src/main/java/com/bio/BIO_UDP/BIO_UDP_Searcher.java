package com.bio.BIO_UDP;


import java.io.IOException;
import java.net.*;

/**
 * @Description：
 * @Author: jarry
 */
public class BIO_UDP_Searcher {

    public static void main(String[] args) throws IOException {

        System.out.println("UDPSearcher started.");

        // 构建UDP的Socket（由于是searcher，即数据的率先发送者，所以可以不用指定port，用于监听）
        DatagramSocket datagramSocket = new DatagramSocket();

        // 构建请求消息的实体（包含目标ip及port）
        String requestMsg = "just a joke.";
        byte[] requestBytes = requestMsg.getBytes();
        DatagramPacket requestPacket = new DatagramPacket(requestBytes, requestBytes.length);
        requestPacket.setAddress(Inet4Address.getLocalHost());
        requestPacket.setPort(20000);

        // 发送请求数据
        System.out.println("UDPSearcher has send msg.");
        datagramSocket.send(requestPacket);

        // 接收回送数据
        byte[] buf = new byte[512];
        DatagramPacket receivePacket = new DatagramPacket(buf,buf.length);
        datagramSocket.receive(receivePacket);
        String sourceIp = receivePacket.getAddress().getHostAddress();
        int sourcePort = receivePacket.getPort();
        int dataLength = receivePacket.getLength();
        String receiveData = new String(receivePacket.getData(),0,receivePacket.getData().length);
        // 显示接收到的数据
        System.out.println("UDPSearcher has received data with source:"+sourceIp+":"+sourcePort+" with length "+dataLength+". data:"+receiveData);

        // 由于是demo，所以不用循环，就此结束
        System.out.println("UDPSearcher finished.");
        datagramSocket.close();

    }
}
