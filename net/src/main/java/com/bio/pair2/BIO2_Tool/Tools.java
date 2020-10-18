package com.bio.pair2.BIO2_Tool;
//这里的tool，表明了两点：如何实现int与byte之间的转换，可以自定义实现数据的转换


//
/**
 * 过渡一下，简述int与byte之间的转换。
 * 进而明确各种数据类型与byte之间的转化。
 * 最终引申出NIO包下的ByteBuffer工具，实现不同数据类型与byte类型的相互转换
 */
//public class Tools {
//    public static int byteArrayToInt(byte[] b) {
//        return b[3] &amp; 0xFF |
//                (b[2] &amp; 0xFF) &lt;&lt; 8 |
//                (b[1] &amp; 0xFF) &lt;&lt; 16 |
//                (b[0] &&0xFF) &lt;&lt; 24;
//    }
//
//    public static byte[] intToByteArray(int a) {
//        return new byte[]{
//                (byte) ((a &gt;&gt; 24) &amp; 0xFF),
//        (byte) ((a &gt;&gt; 16) &amp; 0xFF),
//        (byte) ((a &gt;&gt; 8) &amp; 0xFF),
//        (byte) (a &amp; 0xFF)
//            };
//    }
//}

