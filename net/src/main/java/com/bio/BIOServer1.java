package com.bio;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * @Description： BIO模型中Server端的简单实现
 * @Author: jarry
 */
public class BIOServer1 {
    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket();
        serverSocket.bind(new InetSocketAddress(8080));

        System.out.println("server has started");

        while (!serverSocket.isClosed()) {
            Socket requestClient = serverSocket.accept();
            System.out.println("server get a connection: " + requestClient.toString());

            InputStream requestInputStream = requestClient.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(requestInputStream));
            String msg;
            while ((msg = reader.readLine()) != null) {
                if (msg.length() == 0) {
                    break;
                }
                System.out.println(msg);
            }
            System.out.println("server has receive a message from: " + requestClient.toString());

            requestInputStream.close();
            requestClient.close();
        }

        serverSocket.close();
    }
}
