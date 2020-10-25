package com.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

/**
 * @Description： 这个版本，充分利用了NIO的第三个支柱-Selector，完成事件驱动的转型
 * 注意，上个版本使用循环，就类似自旋（自旋相对比较底层，小），虽然解决了BIO的每个client占据一个线程的资源消耗（主要是内存），但是加大了CPU的消耗（CPU要不断进行循环，判断，即使是无效的操作）
 * NIO通过Selector，建立事件驱动模型，来解决这一问题。即只有当特定的事件（如连接建立完成）发生，才会进行对应的事件处理（从而避免了CPU的无效消耗，提高效率）
 * 私语：很多Javaer一直停留在初级层次（网络编程只能百度，使用BIO），就是无法突破事件驱动模型这种抽象层次更高的高层思想
 * @Description： 为了更好地学习与理解Netty，基础的NIO再过一遍
 * @Author: jarry
 */
public class NIO_Server3 {
    public static void main(String[] args) throws IOException {
        // 1.创建并配置ServerSocketChannel
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.configureBlocking(false);
        serverSocketChannel.socket().bind(new InetSocketAddress(8080));

        // 2.创建Selector，并完成SelectionKey的注册，并完成初始化监听
        // Selector在非阻塞的基础上，实现了一个线程管理多个Channel（也就常说的“多路复用”）
        // 那可不可以理解为一个selector管理多个channel，监听多个channel（后续代码中，除了server外，还有client们都注册到了这个selector中）
        Selector selector = Selector.open();
        SelectionKey selectionKey = serverSocketChannel.register(selector, 0, serverSocketChannel);
        selectionKey.interestOps(SelectionKey.OP_ACCEPT);

        System.out.println("server start success ");

        // 3.开始循环处理各个事件
        while (true) {
            // 1.通过.select()阻塞当前线程，直到有注册的selectionKey触发（触发是，会将对应的selectionKey复制到selected set中）
            selector.select();
            // 2.获取触发的selectionKey集合
            Set<
                    SelectionKey>
                    selectionKeySet = selector.selectedKeys();
            // 3.遍历处理触发的selectionKey集合
            Iterator <
            SelectionKey >
            iterator = selectionKeySet.iterator();
            while (iterator.hasNext()) {
                // 1.获得触发的selectionKey
                SelectionKey selectedKey = iterator.next();
                // 2.从集合中移除正在处理的selectionKey（单线程也可以在处理完后移除，但多线程中就可能出现同一selectionKey被多个线程处理）
                iterator.remove();
                // 3.根据iteration触发的事件类型，进行对应处理（这里demo为了简单一些，就只处理accept与read事件类型）
                if (selectedKey.isAcceptable()) {
                    // 如果selectedKey触发的是accept事件类型，即serverSocketChannel通过accept获得了一个客户端连接
                    // 1.获得服务端ServerSocketChannel（即之前监听accept事件时，放入attachment的可选对象，便于后续处理)
                    ServerSocketChannel server = (ServerSocketChannel) selectedKey.attachment();
                    // 2.获得客户端SocketChannel（利用刚刚获得的server，与触发的.accept()方法），便于后续操作
                    SocketChannel client = server.accept();
                    // 3.配置客户端SocketChannel（毕竟SocketChannel也是默认配置阻塞的）
                    client.configureBlocking(false);
                    // 4.注册新的事件（既然已经连接成功，那么开始注册如read等新事件，便于后续监听）
                    // 也可以采取类似初始化阶段那样两句代码完成，但是这里不需要（也可以说时表现一个新的处理方法）
                    client.register(selector, SelectionKey.OP_READ, client);
                    // 5.日志打印
                    System.out.println("server has connect a new client: " + client.getRemoteAddress());
                }
                if (selectedKey.isReadable()) {
                    // 如果selectedKey触发的是可读事件类型，即当前selectionKey对应的channel可以进行读操作（但不代表就一定有数据可以读）
                    // 1.获得客户端SocketChannel（即之前监听事件处理时，注册read事件时置入的attachment对象)
                    SocketChannel client = (SocketChannel) selectedKey.attachment();
                    // 2.新建一个ByteBuffer用于缓冲数据（或者说，用来盛放数据）
                    ByteBuffer requestBuffer = ByteBuffer.allocate(1024);
                    // 3.判断对应client是否处于open状态，对应channel内是否有可读数据（如果不满足就跳过该循环）
                    // 原本我在想我都已经移除了对应的key，这里又没有处理数据，那下一次不就没有对应key了。
                    // 但实际是我移除的是.selectedKeys()选出来的key（是复制体），下次触发read事件，还会有对应key被selectedKeys选出来的。
                    while ( client.isOpen() &&
                    client.read(requestBuffer) != -1){
                        // 达到这里，说明对应channel中是有对应数据的
                        // 开始读取数据
                        if ( requestBuffer.position() >
                        0){
                            // 这里为了简化处理，就设定为一旦读取了数据就算读取完毕
                            // 注意：读取的操作在loop的判断条件中，client.read(requestBuffer)
                            //TODO_FINISH 疑问：既然这里设置的是&gt;0就break，那为什么实际操作中，数据（字符串）是读完了呢
                            // 答案：while循环条件的read就是完成了当前缓冲区数据的读取。
                            //而循环体中的if在生产环境可能更多是进行（编解码的沾包拆包处理等）。
                            break;
                        }
                    }
                    // 4.如果requestBuffer为空，即没有读取到数据，那就跳出本次selectionKey的处理
                    if (requestBuffer.position() == 0) {
                        continue;
                    }
                    // 5.到达这里说明requestBuffer.position()不为0，即bytebBuffer不为空，即读取到了数据，那么就处理数据
                    // 5.1 将requestBuffer从写模式转为读模式
                    requestBuffer.flip();
                    // 5.2 业务处理：将brequestBuffer中的数据打印出来（切记，只有.allocate()分配的非直接内存的ByteBuffer才可以.array()）
                    System.out.println(new String(requestBuffer.array()));
                    System.out.println("server has receive a message from: " + client.getRemoteAddress());
                    // 6.返回响应
                    // 6.1 模拟一下http协议的响应，便于浏览器解析(响应结果 200)
                    String response = "HTTP/1.1 200 OKrn" +
                            "Content-Length: 11rnrn" +
                            "Hello World";
                    // 6.2 通过ByteBuffer.wrap()将数据置入响应的ByteBuffer
                    ByteBuffer responseBuffer = ByteBuffer.wrap(response.getBytes());
                    // 6.2 将响应的ByteBuffer写入到客户端Socket中（底层会自动将该数据发送过去，额，好吧。实际是交由操作系统底层处理）
                    while (responseBuffer.hasRemaining()) {
                        client.write(responseBuffer);
                    }
                }

            }
            //TODO_FINISHED 不理解这句的目的是什么，这是一个类似.select()的非阻塞式方法。
            // epoll空论的一种解决方案，但是无法根本解决问题，最好还是如Netty那样refresh解决
            selector.selectNow();
        }
    }
}

