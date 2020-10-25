package com.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @Description： 与BIOServer同样的，NIOServer也无法同时连接多个客户端
 * V1版本这里，依旧如BIOServer1那样，通过轮询实现多个客户端处理（不过BIO由于是阻塞的，所以采用多线程。而NIO是非阻塞的，所以采用一个全局列表来进行处理）
 * @Author: jarry
 */
public class NIO_Server2 {
    private static List<SocketChannel>socketChannelList = new ArrayList<>();

    public static void main(String[] args) throws IOException {
        // 创建网络服务端
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.configureBlocking(false);
        //TODO .socket().bind()与.bind()的区别不清楚
        serverSocketChannel.socket().bind(new InetSocketAddress(8080));
        System.out.println("server has started");

        // 通过循环，不断获取监听不同客户端发来的连接请求
        while (true) {
            // 由于NIO是非阻塞，故返回值是完全可能是null的
            SocketChannel socketChannel = serverSocketChannel.accept();
            if (socketChannel != null) {
                // 如果有新的连接接入，就打印日志，并将对应的SocektChannel置入全局队列中
                System.out.println("server has connect a new client: " + socketChannel.getRemoteAddress().toString());
                socketChannel.configureBlocking(false);
                socketChannelList.add(socketChannel);

            } else {
                // 如果没有新的连接接入，就对现有连接的数据进行处理，如果处理完了就从列表中删除对应SocketChannel
                Iterator<SocketChannel> socketChannelIterator = socketChannelList.iterator();
                while (socketChannelIterator.hasNext()){
                    SocketChannel clientSocketChannel = socketChannelIterator.next();
                    ByteBuffer requestBuffer = ByteBuffer.allocate(1024);

                    // 新增：如果当前channel的数据长度为0，表示这个通道没有数据需要处理，那就过会儿处理
                    if (clientSocketChannel.read(requestBuffer) == 0){
                        // 进入下一个循环，即处理下一个channel
                        continue;
                    }

                    while (clientSocketChannel.isOpen() &&clientSocketChannel.read(requestBuffer) != -1) {
                        if (requestBuffer.position() > 0) {
                            break;
                        }
                    }
                    if (requestBuffer.position() == 0) {
                        // 如果没有数据，就不再进行后续处理，而是进入下一个循环
                        continue;
                    }
                    requestBuffer.flip();
                    System.out.println("server receive a message: " + new String(requestBuffer.array()));
                    System.out.println("server receive a message from: " + clientSocketChannel.getRemoteAddress());

                    // 响应结果 200
                    String response = "HTTP/1.1 200 OKrn" +
                            "Content-Length: 12rnrn" +
                            "Hello World!";
                    ByteBuffer responseBuffer = ByteBuffer.wrap(response.getBytes());
                    while (responseBuffer.hasRemaining()) {
                        clientSocketChannel.write(responseBuffer);
                    }

                    // 新增：如果运行到这里，说明返回的数据已经返回了
                    // 我认为，如果是长连接的话，这里的处理应当更加严密（当然这只是一个过渡demo版本）
                    socketChannelIterator.remove();
                    // 我认为，应当进行close等资源释放操作。并且应该先remove()，再close
                    clientSocketChannel.close();
                }
            }
        }
    }
}
