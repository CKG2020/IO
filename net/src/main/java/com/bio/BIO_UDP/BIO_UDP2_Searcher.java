package com.bio.BIO_UDP;





    import java.io.IOException;
    import java.net.*;
    import java.util.ArrayList;
    import java.util.List;
    import java.util.concurrent.CountDownLatch;

    /**
     * @Description：
     * @Author: jarry
     */
    public class BIO_UDP2_Searcher {

        // 监听端口号
        private static final int LISTEN_PORT = 30000;


        public static void main(String[] args) throws IOException, InterruptedException {
            System.out.println("UDPSearcher Started");

            Listener listener = listen();
            sendBroadcast();

            // 读取任意键盘信息后退出
            System.in.read();

            List<Device>devices = listener.getDevicesAndClose();
            for (Device device : devices) {
                System.out.println("Device:"+device.toString());
            }

            // 完成
            System.out.println("UDPSearcher Finished");
        }

        private static Listener listen() throws InterruptedException {
            System.out.println("UDPSearcher start listen.");
            CountDownLatch countDownLatch = new CountDownLatch(1);
            Listener listener = new Listener(LISTEN_PORT, countDownLatch);
            listener.start();
            countDownLatch.await();
            return listener;
        }

        /**
         * 用于发送广播消息
         * @throws IOException
         */
        private static void sendBroadcast() throws IOException {

            System.out.println("UDPSearcher sendBroadcast  started.");
            // 作为一个搜索者（发送请求），无需指定一个端口，由系统自动分配
            DatagramSocket datagramSocket = new  DatagramSocket();

            // 构建一份请求数据
            String requestData =BIO_UDP2_MessageCreator.buildWithPort(LISTEN_PORT);
            byte[] requestDataBytes = requestData.getBytes();
            // 构建发送数据实体
            DatagramPacket requestPacket = new DatagramPacket(requestDataBytes, requestDataBytes.length);

            // 设置目标地址（采用广播地址）
            requestPacket.setAddress(Inet4Address.getByName("255.255.255.255"));
            requestPacket.setPort(20000);

            // 发送构建好的消息
            datagramSocket.send(requestPacket);
            System.out.println("start send data.");

            // 发送结束
            System.out.println("UDPSearcher sendBroadcast finished.");
            datagramSocket.close();
        }

        private static class Device {
            final int port;
            final String ip;
            final String sn;

            public Device(int port, String ip, String sn) {
                this.port = port;
                this.ip = ip;
                this.sn = sn;
            }

            @Override
            public String toString() {
                return "Device{" +
                        "port=" + port +
                        ", ip='" + ip + "'" +
                ", sn='" + sn + "'" +
                '}';
            }
        }

        private static class Listener extends Thread{

            private final int listenPort;
            private final CountDownLatch countDownLatch;
            private final List<Device> devices = new ArrayList<Device>();
            private boolean done = false;
            private DatagramSocket ds = null;



            public Listener(int listenPort, CountDownLatch countDownLatch){
                super();
                this.listenPort = listenPort;
                this.countDownLatch = countDownLatch;
            }

            @Override
            public void run() {
                super.run();

                // 通知已启动
                countDownLatch.countDown();

                // 开始实际数据监听部分
                try {
                    // 监听回送端口
                    ds = new DatagramSocket(listenPort);

                    while (!done){
                        // 接收消息的实体
                        final byte[] buf = new byte[512];
                        DatagramPacket receivePack = new DatagramPacket(buf, buf.length);

                        // 开始接收数据
                        ds.receive(receivePack);

                        // 打印接收到的信息
                        String ip = receivePack.getAddress().getHostAddress();
                        int port = receivePack.getPort();
                        int dataLength = receivePack.getLength();
                        String data = new String(receivePack.getData(),0,dataLength);
                        System.out.println("UDPSearcher receive form ip:" + ip
                                + "tport:" + port + "tdata:" + data);

                        String sn =BIO_UDP2_MessageCreator.parseSN(data);
                        if (sn != null){
                            Device device = new Device(port, ip ,sn);
                            devices.add(device);
                        }
                    }
                }catch (Exception e){

                }finally {
                    close();
                }

                System.out.println("UDPSearcher listner finished");
            }


            private void close(){
                if (ds != null){
                    ds.close();
                    ds = null;
                }
            }

            List<Device> getDevicesAndClose(){
                done = true;
                close();
                return devices;
            }

        }
    }
