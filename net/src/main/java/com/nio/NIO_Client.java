package com.nio;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Scanner;
/**
 * @Description： NIO模型下的TCP客户端实现
 * @Author: jarry
 */
public class NIO_Client {
    public static void main(String[] args) throws IOException {
        // 获得一个SocektChannel
        SocketChannel socketChannel = SocketChannel.open();
        // 设置SocketChannel为非阻塞模式
        socketChannel.configureBlocking(false);
        // 设置SocketChannel的连接配置
        socketChannel.connect(new InetSocketAddress(Inet4Address.getLocalHost(), 8080));

        // 通过循环，不断连接。跳出循环，表示连接建立成功
        while (!socketChannel.finishConnect()){
            // 如果没有成功建立连接，就一直阻塞当前线程（.yield()会令当前线程“谦让”出CPU资源）
            Thread.yield();
        }

        // 发送外部输入的数据
        Scanner scanner = new Scanner(System.in);
        System.out.println("please input:");
        String msg = scanner.nextLine();
        // ByteBuffer.wrap()会直接调用HeapByteBuffer。故一方面其会自己完成内存分配。另一方面，其分配的内存是非直接内存（非heap堆）
        ByteBuffer byteBuffer = ByteBuffer.wrap(msg.getBytes());
        // ByteBuffer.hasRemaining()用于判断对应ByteBuffer是否还有剩余数据（实现：return position &lt; limit;）
        while (byteBuffer.hasRemaining()){
            socketChannel.write(byteBuffer);
        }

        // 读取响应
        System.out.println("receive echoResponse from server");
        // 设置缓冲区大小为1024
        ByteBuffer requestBuffer = ByteBuffer.allocate(1024);
        // 判断条件：是否开启，是否读取到数据
        //TODO 我认为这里的实现十分粗糙，是不可以置于生产环境的，具体还需要后面再看看（即使是过渡demo，也可以思考一下嘛）
        while (socketChannel.isOpen() && socketChannel.read(requestBuffer) != -1){
            // 长连接情况下,需要手动判断数据有没有读取结束 (此处做一个简单的判断: 超过0字节就认为请求结束了)
            if (requestBuffer.position() > 0) {
                break;
            }
        }
        requestBuffer.flip();
        //      byte[] content = new byte[requestBuffer.limit()];
        //      // .get()方法只会返回byte类型（猜测是当前标记位的数据）
        //      requestBuffer.get(content);
        //      System.out.println(new String(content));
        // ByteBuffer提供了大量的基本类型转换的方法，可以直接拿来使用
        System.out.println(new String(requestBuffer.array()));

        scanner.close();
        socketChannel.close();
    }
}
