package com.bio;




import java.io.*;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * @Description： 直接对原有代码BIOServer进行暴力修改，增加了其http格式的返回，确保浏览器可以正常访问
 * @Author: jarry
 */
public class BIOServer3 {
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

            // 返回数据，并确保可以被http协议理解
            OutputStream outputStream = requestClient.getOutputStream();
            outputStream.write("HTTP/1.1 200 OKrr".getBytes("utf-8"));
            outputStream.write("Content-Length: 11rnrn".getBytes("utf-8"));
            outputStream.write("Hello World".getBytes("utf-8"));
            outputStream.flush();

            requestInputStream.close();
            outputStream.close();
            requestClient.close();
        }

        serverSocket.close();
    }

    /**
     * 运行结果分析：
     */
    //  server has started
    //  server get a connection: Socket[addr=/0:0:0:0:0:0:0:1,port=63008,localport=8080]
    //  GET / HTTP/1.1
    //  Host: localhost:8080
    //  Connection: keep-alive
    //  Cache-Control: max-age=0
    //  Upgrade-Insecure-Requests: 1
    //  User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/77.0.3865.90 Safari/537.36
    //  Sec-Fetch-Mode: navigate
    //  Sec-Fetch-User: ?1
    //  Accept: text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3
    //  Sec-Fetch-Site: none
    //  Accept-Encoding: gzip, deflate, br
    //  Accept-Language: en-US,en;q=0.9,zh-CN;q=0.8,zh;q=0.7
    //  Cookie: Webstorm-c7a2b5a2=b5e53f87-54cc-41d5-a21f-c7be3056dfe8; centcontrol_login_token=09E8A6B6888CB0B7A9F89AB3DB5FAFE4
    //  server has receive a message from: Socket[addr=/0:0:0:0:0:0:0:1,port=63008,localport=8080]
    //  server get a connection: Socket[addr=/0:0:0:0:0:0:0:1,port=63009,localport=8080]
    //  GET /favicon.ico HTTP/1.1
    //  Host: localhost:8080
    //  Connection: keep-alive
    //  Pragma: no-cache
    //  Cache-Control: no-cache
    //  Sec-Fetch-Mode: no-cors
    //  User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/77.0.3865.90 Safari/537.36
    //  Accept: image/webp,image/apng,image/*,*/*;q=0.8
    //  Sec-Fetch-Site: same-origin
    //  Referer: http://localhost:8080/
    //  Accept-Encoding: gzip, deflate, br
    //  Accept-Language: en-US,en;q=0.9,zh-CN;q=0.8,zh;q=0.7
    //  Cookie: Webstorm-c7a2b5a2=b5e53f87-54cc-41d5-a21f-c7be3056dfe8; centcontrol_login_token=09E8A6B6888CB0B7A9F89AB3DB5FAFE4
    //  server has receive a message from: Socket[addr=/0:0:0:0:0:0:0:1,port=63009,localport=8080]

}
