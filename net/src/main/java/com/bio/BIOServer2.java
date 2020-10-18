package com.bio;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @Description： 直接对原有代码BIOServer进行暴力修改，将其阻塞部分，通过多线程实现异步处理
 * @Author: jarry
 */
public class BIOServer2 {

    private static ExecutorService executorService = Executors.newCachedThreadPool();

    public static void main(String[] args) throws IOException {

        ServerSocket serverSocket = new ServerSocket();
        serverSocket.bind(new InetSocketAddress(8080));

        System.out.println("server has started");

        while (!serverSocket.isClosed()) {
            final Socket requestClient = serverSocket.accept();
            System.out.println("server get a connection: " + requestClient.toString());

            executorService.submit(new Runnable() {
                @Override
                public void run() {
                    InputStream requestInputStream = null;
                    try {
                        requestInputStream = requestClient.getInputStream();
                    }
                    catch (IOException e) {
                        e.printStackTrace();
                    }
                    BufferedReader reader = new BufferedReader(new InputStreamReader(requestInputStream));
                    String msg = null;
                    while (true) {
                        try {
                            if (!((msg = reader.readLine()) != null)) {
                                break;
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        if (msg.length() == 0) {
                            break;
                        }
                        System.out.println(msg);
                    }
                    System.out.println("server has receive a message from: " + requestClient.toString());

                    try {
                        requestInputStream.close();
                        requestClient.close();
                    }
                    catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
        }

        serverSocket.close();
    }

    /**
     * 运行结果分析：
     * server has started
     * server get a connection: Socket[addr=/10.0.75.1,port=64042,localport=8080]
     * server get a connection: Socket[addr=/10.0.75.1,port=64052,localport=8080]
     * server get a connection: Socket[addr=/10.0.75.1,port=64061,localport=8080]
     * 123
     * server has receive a message from: Socket[addr=/10.0.75.1,port=64042,localport=8080]
     * 456
     * server has receive a message from: Socket[addr=/10.0.75.1,port=64052,localport=8080]
     * 789
     * server has receive a message from: Socket[addr=/10.0.75.1,port=64061,localport=8080]
     */
}

