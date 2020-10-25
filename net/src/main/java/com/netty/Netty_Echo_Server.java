//package com.netty;
//
//import io.netty.bootstrap.ServerBootstrap;
//import io.netty.channel.ChannelFuture;
//import io.netty.channel.ChannelInitializer;
//import io.netty.channel.ChannelOption;
//import io.netty.channel.ChannelPipeline;
//import io.netty.channel.EventLoopGroup;
//import io.netty.channel.nio.NioEventLoopGroup;
//import io.netty.channel.socket.SocketChannel;
//import io.netty.channel.socket.nio.NioServerSocketChannel;
//import io.netty.handler.logging.LogLevel;
//import io.netty.handler.logging.LoggingHandler;
//
///**
// * Echoes back any received data from a client.
// */
//public final class Netty_Echo_Server {
//    static final int PORT = Integer.parseInt(System.getProperty("port", "8080"));
//
//    public static void main(String[] args) throws Exception {
//        // Configure the server.
//        // 创建EventLoopGroup   accept线程组 NioEventLoop
//        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
//        // 创建EventLoopGroup   I/O线程组
//        EventLoopGroup workerGroup2 = new NioEventLoopGroup(1);
//        try {
//            // 服务端启动引导工具类
//            ServerBootstrap b = new ServerBootstrap();
//            // 配置服务端处理的reactor线程组以及服务端的其他配置
//            b
//                    // 设置两个线程组（Reactor线程模型中的mainReactorThreads与subReactorThreads）。说白了就是两个线程池
//                    .group(bossGroup, workerGroup2)
//                    // 设置采用的channel类型（NioServerSocketChannel对应ServerSocketChannel，其它类似），底层实现用的反射
//                    /**
//                     * ChannelFactory 的 newChannel() 方法什么时候会被调用就可以了。
//                     * 对于 NioSocketChannel，由于它充当客户端的功能，它的创建时机在 connect(…) 的时候；
//                     * 对于 NioServerSocketChannel 来说，它充当服务端功能，它的创建时机在绑定端口 bind(…) 的时候。
//                     */
//                    .channel(NioServerSocketChannel.class)
//                    //TODO 只看到是用于配置，详细还不了解
//                    .option(ChannelOption.SO_BACKLOG, 100)
//                    // 设置handler，这里设置了Netty提供的日志ChannelHandler（并采用了Debug级别）
//                    .handler(new LoggingHandler(LogLevel.DEBUG))
//                    // 设置childHandler 这里可以通过ChannelInitializer实例，来放入多个ChannelHandler（需要重写其中的.initChannel()方法）
//                    .childHandler(new ChannelInitializer&lt;SocketChannel&gt;() {
//                @Override
//                public void initChannel(SocketChannel ch) throws Exception {
//                    // 通过SocketChannel获得ChannelPipeline
//                    ChannelPipeline p = ch.pipeline();
//                    // 在ChannelPipeline后面添加新的ChannelHandler
//                    /**
//                     * 每个 Channel 内部都有一个 pipeline，pipeline 由多个 handler 组成，
//                     * handler 之间的顺序是很重要的，因为 IO 事件将按照顺序顺次经过 pipeline 上的 handler，
//                     * 这样每个 handler 可以专注于做一点点小事，由多个 handler 组合来完成一些复杂的逻辑。
//                     *
//                     * Inbound 和 Outbound。在 Netty 中，IO 事件被分为 Inbound 事件和 Outbound 事件。
//                     * 例如：
//                     * 1. pipeline.addLast(new StringDecoder());
//                     * 2. pipeline.addLast(new StringEncoder());
//                     * 3. pipeline.addLast(new BizHandler());
//                     */
//                    p.addLast(new EchoServerHandler());
//                }
//            });
//            // 通过bind启动服务（Netty的ChannelFuture继承自JDK的Future，只不过多了一些方法
//            ChannelFuture f = b
//                    // 服务端用于绑定端口（其中还涉及Java的channel等操作）
//                    .bind(PORT)
//                    // ChannelFuture新增的.sync()区别于新增的.await()
//                    /**
//                     * sync() 和 await() 用于阻塞等待
//                     * sync() 内部会先调用 await() 方法，等 await() 方法返回后，会检查下这个任务是否失败，如果失败，重新将导致失败的异常抛出来。
//                     * 也就是说，如果使用 await()，任务抛出异常后，await() 方法会返回，但是不会抛出异常，而 sync() 方法返回的同时会抛出异常。
//                     */
//                    .sync();
//
//            // 阻塞主线程，知道网络服务被关闭
//            f
//                    // 用于获得封装在Netty的ChannelFuture内的Channel（Java的），便于进行下一步操作
//                    .channel()
//                    // 当Netty的ChannelFuture被关闭时，返回ChannelFuture
//                    .closeFuture()
//                    // 同上
//                    .sync();
//        } finally {
//            // 关闭线程组
//            bossGroup.shutdownGracefully();
//            workerGroup2.shutdownGracefully();
//        }
//    }
//}
