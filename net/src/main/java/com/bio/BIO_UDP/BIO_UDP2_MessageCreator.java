package com.bio.BIO_UDP;

//为了网络监听的clear，以及权限问题，需要对上述代码进行一次升级。
    /**
     * @Description： 自定义通信数据格式（这可能是最简单的应用层协议了）
     * @Author: jarry
     */
    public class BIO_UDP2_MessageCreator {

        private static final String SN_HEADER = "收到暗号，我是（SN）：";
        private static final String PORT_HEADER = "发送暗号，请回电端口（PORT):";

        public static String buildWithPort(int port){
            return PORT_HEADER + port;
        }

        public static int parsePort(String data){
            if (data.startsWith(PORT_HEADER)){
                return Integer.parseInt(data.substring(PORT_HEADER.length()));
            }
            return -1;
        }

        public static String buildWithSN(String sn){
            return SN_HEADER + sn;
        }

        public static String parseSN(String data){
            if (data.startsWith(SN_HEADER)){
                return data.substring(SN_HEADER.length());
            }
            return null;
        }
    }
