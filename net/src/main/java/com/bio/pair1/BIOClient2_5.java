package com.bio.pair1;



import java.io.*;
import java.net.*;

/**
 * @Description：
 * @Author: jarry
 */
public class BIOClient2_5 {
    public static void main(String[] args) throws IOException {
        Socket socket = new Socket();
        socket.setSoTimeout(2000);
        socket.connect(new InetSocketAddress(Inet4Address.getLocalHost(),2000),2000);
        System.out.println("client startup");

        dealMsg(socket);
        socket.close();
    }

    private static void dealMsg(Socket clientSocket) throws IOException {
        // 1.获取键盘输入流
        InputStream systemInputStream = System.in;
        // 2.将systemInputStream转化为具有缓存功能的字符输入流BufferedReader
        BufferedReader systemBufferedReader = new BufferedReader(new InputStreamReader(systemInputStream));

        // 3.获取Socket输入流
        InputStream socketInputStream = clientSocket.getInputStream();
        // 4.将socketInputStream转换为具有缓存能力的字符输入流
        BufferedReader socketBufferedReader = new BufferedReader(new InputStreamReader(socketInputStream));

        // 5.获取Socket输出流
        OutputStream socketOutputStream = clientSocket.getOutputStream();
        // 6.将socketOutputStream转换为打印流（用于发送String)
        PrintStream socketPrintStream = new PrintStream(socketOutputStream);

        // 用于确立连接状态的标识符
        boolean flag = true;

        // 7.利用循环，client与server进行交互
        do {
            // 从键盘等系统输入流获取输入字符串
            String str = systemBufferedReader.readLine();
            // 将str写入到socketClient的打印流（本质是输出流）。socketClient的输出流是连接Server的，用于向Server发送数据的
            socketPrintStream.println(str);

            // 从Server获得回写（Server的回写，一定会发送到socketClient的输入流中（输入的“入”是指入socketClient）
            String echo = socketBufferedReader.readLine();

            // 建立一个用于关闭的方式
            if ("bye".equalsIgnoreCase(echo)){
                flag = false;
            }else{
                // 在控制台打印server的echo
                System.out.println("server echo:"+echo);
            }
        }while (flag);

        // 8.退出交互，需要关闭与Server连接的两个资源（输入与输出）     考虑一下lombok的@Cleanup
        socketBufferedReader.close();
        socketPrintStream.close();

    }
}
