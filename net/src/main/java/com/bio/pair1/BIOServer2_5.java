package com.bio.pair1;



import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * @Description：
 * @Author: jarry
 */
public class BIOServer2_5 {

    public static void main(String[] args) throws IOException {
        // 建立Server的Socket，服务端不需要设置IP，以及Port
        // IP采用本地IP
        ServerSocket serverSocket = new ServerSocket(2000);
        System.out.println("server startup");

        // 通过循环，对client的请求进行监听
        while (true){
            // 获得client的请求
            Socket clientRequest = serverSocket.accept();
            // 异步处理client的请求
            ClientHandler clientHandler = new ClientHandler(clientRequest);
            clientHandler.start();
        }

    }

    private static class ClientHandler extends Thread {
        Socket socketClient = null;
        boolean flag = true;

        ClientHandler(Socket socketClient){
            this.socketClient = socketClient;
        }

        @Override
        public void run() {
            super.run();
            // 构建系统输入流
            InputStream systemInputStream = System.in;
            // 将系统输入流转换为字符输入流
            BufferedReader systemBufferedReader = new BufferedReader(new InputStreamReader(systemInputStream));

            try {
                // 构建socketClient的输入流（即客户端中，写入client输出流的数据）
                InputStream clientInputStream = socketClient.getInputStream();
                // 将client的输入流转为具有存储能力的BufferedReader
                BufferedReader clientBufferedReader = new BufferedReader(new InputStreamReader(clientInputStream));

                // 构建socketClient的输出流（用于发送数据，即客户端中，从client输入流读取的数据）
                OutputStream clientOutputStream = socketClient.getOutputStream();
                // 将client的输出流转换为打印流，便于输出数据
                PrintStream clientPrintStream = new PrintStream(clientOutputStream);

                // 通过循环，与客户端进行交互
                do {
                    // 读取从客户端发送来的数据，即读取socketClient的输入流转化的BufferedReader
                    String str = clientBufferedReader.readLine();
                    if ("bye".equalsIgnoreCase(str)){
                        flag = false;
                        clientPrintStream.println("connect interrupt");
                    }else{
                        System.out.println(str);

                        // 发送回写数据，即将回写数据写入socketClient的输出流（客户端的输入流会获取相关数据）
                        clientPrintStream.println(str.length());
                    }

                    // 从系统输入中获取想要发送的数据
                    String servStr = systemBufferedReader.readLine();
                    // 发送到客户端
                    clientPrintStream.println(servStr);
                }while (flag);

                // 同样的，关闭连接资源
                clientBufferedReader.close();
                clientPrintStream.close();

            } catch (IOException e) {
                e.printStackTrace();
            }finally {
                // 无论发生什么，最后都要关闭socket连接
                try {
                    socketClient.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }
    }

}
