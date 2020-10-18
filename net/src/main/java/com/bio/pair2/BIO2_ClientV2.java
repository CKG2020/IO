package com.bio.pair2;


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Inet4Address;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.nio.ByteBuffer;
//为了使得代码结构更有优雅，并且为了更好地处理字符编码问题（demo中保留了各种数据类型的处理方式）。我们将上述版本更新一下。
public class BIO2_ClientV2 {
    // 连接到远程服务器的远程端口
    private static final int PORT = 20000;
    // 本地端口
    private static final int LOCAL_PORT = 20001;

    public static void main(String[] args) throws IOException {
        // 创建Socket的操作，可以选择不同的创建方式
        Socket socket = createSocket();

        // Socket初始化操作
        initSocket(socket);

        // 链接到本地20000端口，超时时间3秒，超过则抛出超时异常
        socket.connect(new InetSocketAddress(Inet4Address.getLocalHost(), PORT), 3000);

        System.out.println("已发起服务器连接，并进入后续流程～");
        System.out.println("客户端信息：" + socket.getLocalAddress() + " P:" + socket.getLocalPort());
        System.out.println("服务器信息：" + socket.getInetAddress() + " P:" + socket.getPort());

        try {
            // 发送接收数据
            todo(socket);
        } catch (Exception e) {
            System.out.println("异常关闭");
        }

        // 释放资源
        socket.close();
        System.out.println("客户端已退出～");

    }

    /**
     * 创建Socket
     * @return
     * @throws IOException
     */
    private static Socket createSocket() throws IOException {
            /*
            // 无代理模式，等效于空构造函数
            Socket socket = new Socket(Proxy.NO_PROXY);

            // 新建一份具有HTTP代理的套接字，传输数据将通过www.baidu.com:8080端口转发
            Proxy proxy = new Proxy(Proxy.Type.HTTP,
                    new InetSocketAddress(Inet4Address.getByName("www.baidu.com"), 8800));
            socket = new Socket(proxy);

            // 新建一个套接字，并且直接链接到本地20000的服务器上
            socket = new Socket("localhost", PORT);

            // 新建一个套接字，并且直接链接到本地20000的服务器上
            socket = new Socket(Inet4Address.getLocalHost(), PORT);

            // 新建一个套接字，并且直接链接到本地20000的服务器上，并且绑定到本地20001端口上
            socket = new Socket("localhost", PORT, Inet4Address.getLocalHost(), LOCAL_PORT);
            socket = new Socket(Inet4Address.getLocalHost(), PORT, Inet4Address.getLocalHost(), LOCAL_PORT);
            */

        // 推荐无参构造，因为其它（上面）的构造方法都是包含构造，设参，以及connect操作。而socket一旦connect后，设置参数的操作就无效了。不便于灵活使用
        Socket socket = new Socket();
        // 绑定到本地20001端口
        socket.bind(new InetSocketAddress(Inet4Address.getLocalHost(), LOCAL_PORT));

        return socket;
    }

    private static void initSocket(Socket socket) throws SocketException {
        // 设置读取超时时间为2秒
        socket.setSoTimeout(2000);

        // 是否复用未完全关闭的Socket地址，对于指定bind操作后的套接字有效（正常Socket关闭后，对应端口在两分钟内将不再复用。而这个设置将可以直接使用对应空置端口）
        socket.setReuseAddress(true);

        // 是否开启Nagle算法（开启后，两点：第一，会对收到的每次数据进行ACK，另一端只有在接收到对应ACK，才会继续发送数据。第二，如果有数据堆积，会一次将所有堆积数据发出去（毕竟这种模式有数据堆积是正常的）
        // 开启后，更为严谨，严格，安全（默认开启）
        socket.setTcpNoDelay(true);

        // 是否需要在长时无数据响应时发送确认数据（类似心跳包），时间大约为2小时
        socket.setKeepAlive(true);

        // 对于close关闭操作行为进行怎样的处理；默认为false，0
        // false、0：默认情况，关闭时立即返回，底层系统接管输出流，将缓冲区内的数据发送完成
        // true、0：关闭时立即返回，缓冲区数据抛弃，直接发送RST结束命令到对方，并无需经过2MSL等待
        // true、200：关闭时最长阻塞200毫秒，随后按第二情况处理
        socket.setSoLinger(true, 20);

        // 是否让紧急数据内敛，默认false；紧急数据通过 socket.sendUrgentData(1);发送
        // 只有设置为true，才会暴露到上层（逻辑层）
        socket.setOOBInline(true);

        // 设置接收发送缓冲器大小
        socket.setReceiveBufferSize(64 * 1024 * 1024);
        socket.setSendBufferSize(64 * 1024 * 1024);

        // 设置性能参数：短链接，延迟，带宽的相对重要性（权重）
        socket.setPerformancePreferences(1, 1, 0);
    }

    private static void todo(Socket client) throws IOException {
        // 得到Socket输出流
        OutputStream outputStream = client.getOutputStream();


        // 得到Socket输入流
        InputStream inputStream = client.getInputStream();
        byte[] buffer = new byte[256];
        ByteBuffer byteBuffer = ByteBuffer.wrap(buffer);
        // 等同于上两行代码（ByteBuffer是NIO提供的一个工具，allocate就是分配内存地址，ByteBuffer处理的是byte）
        // ByteBuffer byteBuffer = ByteBuffer.allocate(256);

        // 尝试各种数据传输，发出
        // byte
        byteBuffer.put((byte) 126);

        // char
        char c = 'a';
        byteBuffer.putChar(c);

        // int
        int i = 2323123;
        byteBuffer.putInt(i);

        // bool
        boolean b = true;
        byteBuffer.put(b ? (byte) 1 : (byte) 0);

        // Long
        long l = 298789739;
        byteBuffer.putLong(l);


        // float
        float f = 12.345f;
        byteBuffer.putFloat(f);


        // double
        double d = 13.31241248782973;
        byteBuffer.putDouble(d);

        // String
        String str = "Hello你好！";
        byteBuffer.put(str.getBytes());

        // 发送到服务器（长度等于index+1）
        outputStream.write(buffer, 0, byteBuffer.position() + 1);

        // 接收服务器返回
        int read = inputStream.read(buffer);
        System.out.println("收到数量：" + read);

        // 资源释放
        outputStream.close();
        inputStream.close();
    }

    /**
     * 扩展-MSL
     * MSL是Maximum Segment Lifetime的英文缩写，可译为“最长报文段寿命”，
     * 它是任何报文在网络上存在的最长的最长时间，超过这个时间报文将被丢弃。
     * 我们都知道IP头部中有个TTL字段，TTL是time to live的缩写，可译为“生存时间”，
     * 这个生存时间是由源主机设置设置初始值但不是但不是存在的具体时间，而是一个IP数据报可以经过的最大路由数，每经过一个路由器，它的值就减1，
     * 当此值为0则数据报被丢弃，同时发送ICMP报文通知源主机。
     * RFC793中规定MSL为2分钟，但这完全是从工程上来考虑，对于现在的网络，MSL=2分钟可能太长了一些。
     * 因此TCP允许不同的实现可根据具体情况使用更小的MSL值。TTL与MSL是有关系的但不是简单的相等关系，MSL要大于TTL。
     */
}
