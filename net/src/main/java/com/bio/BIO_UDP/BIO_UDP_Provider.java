package com.bio.BIO_UDP;



//  代码示例扩展（BIO下UDP）
//        由于实际工作中UDP使用得比较少，所以这里只给出了BIO中UDP的使用
//        。不过也基本满足了UDP的使用入门了，可以实现局域网搜索（起码对我目前的工作来说是够用了）
//        。至于UDP用于音视频数据传输，得读者自己寻找，或者等我了解之后，更新。



import java.io.IOException;
    import java.net.DatagramPacket;
    import java.net.DatagramSocket;
    import java.net.SocketException;

    /**
     * @Description：
     * @Author: jarry
     */
    public class BIO_UDP_Provider{

        public static void main(String[] args) throws IOException {

            System.out.println("UDPProvider started.");

            // 新建DatagramSocekt，并设定在本机20000端口监听，并接收消息
            DatagramSocket datagramSocket = new DatagramSocket(20000);

            // 新建DatagramPacket实体
            byte[] buf = new byte[512];
            DatagramPacket datagramPacket = new DatagramPacket(buf,buf.length);

            // 接收数据
            datagramSocket.receive(datagramPacket);
            // 处理接受到的数据
            String sourceIp = datagramPacket.getAddress().getHostAddress();
            int sourcePort = datagramPacket.getPort();
            String data = new String(datagramPacket.getData(),0,datagramPacket.getLength());
            // 显示接收到的数据
            System.out.println("UDPProvider has received data with source:"+sourceIp+":"+sourcePort+" with length "+data.length()+". data:"+data);

            // 准备发送回送数据
            String responseData = "UDPProvider has received data with length:"+data.length();
            byte[] responseBytes = responseData.getBytes();
            // 构建回送数据实体（别玩了，设置目标ip与port）
            DatagramPacket responsePacket = new DatagramPacket(responseBytes, responseBytes.length
                    ,datagramPacket.getAddress(),datagramPacket.getPort());
            // 发送回送数据
            System.out.println("UDPProvider has sended data.");
            datagramSocket.send(responsePacket);

            // 由于是demo，所以不用循环，就此结束
            System.out.println("UDPProvider finished.");
            datagramSocket.close();
        }
    }
