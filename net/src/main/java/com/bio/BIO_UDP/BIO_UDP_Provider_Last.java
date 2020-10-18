package com.bio.BIO_UDP;


    import java.io.IOException;
    import java.net.DatagramPacket;
    import java.net.DatagramSocket;
    import java.util.UUID;

    /**
     * UDP 提供者， 用于提供UDP服务
     */
    public class BIO_UDP_Provider_Last {

        public static void main(String[] args) throws IOException {

            String sn = UUID.randomUUID().toString();
            Provider provider = new Provider(sn);
            provider.start();

            // 读取任意字符，退出
            System.in.read();
            provider.exit();

        }

        private static class Provider extends Thread {
            private final String sn;
            private boolean done = false;
            private DatagramSocket datagramSocket = null;

            public Provider(String sn){
                super();
                this.sn = sn;
            }

            @Override
            public void run() {
                super.run();

                System.out.println("UDPProvider started.");
                try {
                    // 作为一个接收者（接受请求），需要指定一个端口用来接收消息
                    datagramSocket = new DatagramSocket(20000);

                    // 通过一个循环，不断监听，接收数据
                    while (true) {
                        // 接收消息的实体
                        final byte[] buf = new byte[512];
                        DatagramPacket receivePack = new DatagramPacket(buf, buf.length);

                        // 开始接收数据
                        datagramSocket.receive(receivePack);

                        // 打印接收到的信息
                        String ip = receivePack.getAddress().getHostAddress();
                        int port = receivePack.getPort();
                        int dataLength = receivePack.getLength();
                        String data = new String(receivePack.getData(), 0, dataLength);
                        System.out.println("UDPProvider receive form ip:" + ip
                                + "tport:" + port + "tdata:" + data);

                        // 获得目标端口
                        int responsePort = BIO_UDP2_MessageCreator.parsePort(data);
                        if (responsePort != -1){
                            // 构建一份回送数据
                            String responseData = BIO_UDP2_MessageCreator.buildWithSN(sn);
                            byte[] reponseDataBytes = responseData.getBytes();
                            // 直接根据发送者，构建回送数据实体
                            DatagramPacket responsePacket = new DatagramPacket(reponseDataBytes,
                                    reponseDataBytes.length,
                                    receivePack.getAddress(),
                                    // 采用指定的端口，而不是解析获得的来源端口（来源端口不一定就是监听端口，这是有些时候为了简化而已）
                                    responsePort);

                            // 发送构建好的回送消息
                            datagramSocket.send(responsePacket);
                            System.out.println("start send data.");
                        }
                    }
                }catch (Exception ignore){

                }finally {
                    close();
                }

                // 发送结束
                System.out.println("UDPProvider finished.");
            }

            /**
             * 对外提供结束方法
             */
            void exit(){
                done = true;
                close();
            }

            /**
             * 本地关闭DatagramSocket的方法
             */
            private void close(){
                if (datagramSocket != null){
                    datagramSocket.close();
                    datagramSocket = null;
                }
            }

        }
    }
