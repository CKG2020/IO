package com.nio;



import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

/**
 * @Description： 直接根据BIOServer进行转变的。所以整体的逻辑与BIOServer类似
 * @Author: jarry
 */
public class NIO_Server1 {
    public static void main(String[] args) throws IOException {
        // 创建网络服务端
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.configureBlocking(false);
        //TODO .socket().bind()与.bind()的区别不清楚
        serverSocketChannel.socket().bind(new InetSocketAddress(8080));
        System.out.println("server has started");

        // 通过循环，不断获取监听不同客户端发来的连接请求
        while (true){
            // 由于NIO是非阻塞，故返回值是完全可能是null的
            SocketChannel socketChannel = serverSocketChannel.accept();
            if (socketChannel != null){
                System.out.println("server has connect a new client: "+socketChannel.getRemoteAddress().toString());
                socketChannel.configureBlocking(false);

                ByteBuffer requestBuffer = ByteBuffer.allocate(1024);
                while (socketChannel.isOpen() && socketChannel.read(requestBuffer) != -1){
                    if (requestBuffer.position() > 0){
                        break;
                    }
                }
                if (requestBuffer.position() == 0){
                    // 如果没有数据，就不再进行后续处理，而是进入下一个循环
                    continue;
                }
                requestBuffer.flip();
                System.out.println("server receive a message: "+new String(requestBuffer.array()));
                System.out.println("server receive a message from: "+socketChannel.getRemoteAddress());

                // 响应结果 200
                String response = "HTTP/1.1 200 OKrn" +
                        "Content-Length: 12rnrn" +
                        "Hello World!";
                ByteBuffer responseBuffer = ByteBuffer.wrap(response.getBytes());
                while (responseBuffer.hasRemaining()){
                    socketChannel.write(responseBuffer);
                }
            }

        }
    }
}
