package com.bio;

import com.sun.org.glassfish.gmbal.Description;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Inet4Address;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.Charset;
import java.util.Scanner;


public class BIOClient1 {
    private static final Charset charset = Charset.forName("utf-8");

    public static void main(String[] args) throws IOException {

//        /1我还以为可以的。但是貌似上面的8080表示目标端口，而下面的8080表示
//        socket.bind(new InetSocketAddress("localhost", 8080));
//        //后来才去确定，.bind是用于绑定源信息，而.connect是用于绑定目标信点
//        socket.connect(new InetSocketAddress(Inet4Address.getLocalHost(), 8080));
       //第一种
//        Socket socket = new Socket("localhost", 8080);
      // 第二种
        Socket socket = new Socket();
//        socket.bind(new InetSocketAddress("localhost", 10000)); //指定我客户端的socket在的端口号
        socket.connect(new InetSocketAddress(Inet4Address.getLocalHost(), 8080));

        OutputStream outputStream = socket.getOutputStream();

        Scanner scanner = new Scanner(System.in);
        System.out.println("please input;");
        String msg = scanner.nextLine();
        outputStream.write(msg.getBytes(charset));
        scanner.close();
        outputStream.close();
        socket.close();

    }
}
