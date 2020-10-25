package com.reactor;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @Description： 为了更好地学习与理解Netty，结合Reactor线程模型的NIO再过一遍，并做出一定的结构调整
 * @Author: jarry
 */
public class Reactor2 {

    // 工作线程池，其中工作线程用于完成实际工作（如计算，编解码等工作）
    private static ExecutorService workerPool = Executors.newCachedThreadPool();
    // 全局变量ServerSocketChannel，记录服务端的Channel
    private ServerSocketChannel serverSocketChannel;
    // 创建mainReactors线程组
    private MainReactorThread[] mainReactorThreads = new MainReactorThread[1];
    // 创建subReactors线程组
    private SubReactorThread[] subReactorThreads = new SubReactorThread[8];

    private abstract class AbstractReactorThread extends Thread {
        // 创建Selector，用于建立Channel事件监听
        protected Selector selector;
        // 用于标记线程运行状态
        private volatile boolean running = false;
        // 创建任务队列，用于多线程处理工作
        private LinkedBlockingQueue<Runnable> taskQueue = new LinkedBlockingQueue<>();

        /**
         * 通过懒加载方式，实例化Selector
         */
        public AbstractReactorThread() throws IOException {
            selector = Selector.open();
        }

        @Override
        /**
         * 重写run方法，完成ReactorThread的公共代码逻辑
         */
        public void run() {
            while (running){
                // 1.通过一个巧妙的方式，遍历处理taskQueue中的所有task
                Runnable task;
                while ((task = taskQueue.poll()) != null){
                    task.run();
                }

                // 2.通过.select()阻塞当前线程，直到有注册的selectionKey触发（之所以等待1000ms，应该是为了令上面的task执行完成）
                try {
                    selector.select(1000L);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                // 3.接下来的操作类似，遍历处理各种监听到的事件
                Set<SelectionKey> selectionKeySet = selector.selectedKeys();
                Iterator<SelectionKey> iterator = selectionKeySet.iterator();
                while (iterator.hasNext()){
                    SelectionKey selectedKey = iterator.next();
                    iterator.remove();

                    // 获得事件类型的编号
                    int readyOps = selectedKey.readyOps();
                    // 通过位运算等方式，快速判断readyOps是否与对应事件类型编号符合（这里作为demo只关注accept与read事件）
                    if ((readyOps & (SelectionKey.OP_READ | SelectionKey.OP_ACCEPT)) != 0 || readyOps == 0) {
                        // 根据V2的编程了解，.attachment返回的极可能是服务端的ServerSocketChannel，也可能是客户端的SocketChannel，故采用他们共同的父类SelectableChannel
                        SelectableChannel channel = (SelectableChannel)selectedKey.attachment();
                        try {
                            // 老规矩，将channel设置为非阻塞式的
                            channel.configureBlocking(false);
                            // 将channel交给抽象方法reactorHandler解决，（具体实现由各自子类去实现）
                            //TODO_FINISH 话说，如何确定哪个子类解决哪个问题
                            // 答案：抽象类不会实例化成对象
                            // 这里的reactorHandler都是由对应子类调用的。MainReactorThread只有在注册时调用，并且是直接置入taskQueue，第二次不会到这里
                            reactorHandler(channel);
                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        } catch (ExecutionException e) {
                            e.printStackTrace();
                        }
                        // 判断channel是否关闭
                        if (!channel.isOpen()){
                            // 如果channel已经关闭，那么其上的SelectionKey就可以取消订阅了
                            selectedKey.cancel();
                        }
                    }

                }
                //TODO 这个还是看不懂
                try {
                    selector.selectNow();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        /**
         * 根据提交的channel，进行注册处理（毕竟调用这个方法的，也只有此类，与衍生类了）
         * @param channel
         * @return
         */
        protected SelectionKey register(SelectableChannel channel) throws ExecutionException, InterruptedException {
            // 为什么register要以任务提交的形式，让reactor线程去处理？
            // 因为线程在执行channel注册到selector的过程中，会和调用selector.select()方法的线程争用同一把锁
            // 而select()方法是在eventLoop中通过while循环调用的，争抢的可能性很高，为了让register能更快的执行，就放到同一个线程来处理

            // 这里无论是解决方案，还是register与select通用一把synchronized锁，都蛮令人惊叹的（虽然我不大理解为什么register要与select公用一边锁）
            // select -&gt; SelectorImpl.lockAndDoSelect 该方法的执行内容采用了synchronized(this)锁
            // register -&gt; SelectorImpl.register 该方法的执行内容采用了synchronized(this.publicKeys)锁 （果然这个比较复杂，主要synchronized锁太多了）
            FutureTask<SelectionKey> futureTask = new FutureTask<>(() ->channel.register(selector, 0, channel)
                );

            taskQueue.add(futureTask);
            return futureTask.get();
        }

        /**
         * 执行启动操作（其实外部可以判断线程状态的，但是这里running表示的线程状态，与规定的线程状态不同）
         */
        protected void doStart(){
            if (!running){
                running = true;
                start();
            }
        }

        /**
         * mainReactor与subReactor的handler处理逻辑是不同的，交由子类实现
         */
        protected abstract void reactorHandler(SelectableChannel channel) throws IOException, ExecutionException, InterruptedException;
    }

    /**
     * mainReactor的实现类，实现了父类的reactorHandler方法。主要完成accept的监听与处理，并进行事件分发操作
     */
    public class MainReactorThread extends AbstractReactorThread {

        AtomicInteger atomicInteger = new AtomicInteger(0);

        /**
         * 通过懒加载方式，实例化Selector
         */
        public MainReactorThread() throws IOException {
        }

        @Override
        protected void reactorHandler(SelectableChannel channel) throws IOException, ExecutionException, InterruptedException {
            // 获得服务端ServerSocketChannel
            ServerSocketChannel server = (ServerSocketChannel) channel;
            // 获得客户端SocketChannel
            SocketChannel client = server.accept();
            // 设置客户端SocketChannel为非阻塞模式
            client.configureBlocking(false);

            //          // 设置新的事件监听
            //          client.register(selector, SelectionKey.OP_READ, client);
            // 不再由当前线程完成read事件的注册，毕竟当前线程只完成accept事件处理，与事件分发
            // 故调用专门写的一个私有方法，进行注册
            doRegister(client);

            // 打印日志
            System.out.println("server has connect a new client: "+client.getRemoteAddress());

        }

        /**
         * Reactor线程模型下，MainReactor将read事件的注册下放到SubReactor
         * @param client 需要进行事件（这里只处理read事件）注册的client
         */
        private void doRegister(SocketChannel client) throws ExecutionException, InterruptedException {
            // 通过轮询的方式（也可以自定义，或扩展开），将事件（非Accept事件，如read事件）交给subReactor线程池中的线程处理
            int index = atomicInteger.getAndIncrement() % subReactorThreads.length;
            // 获取subReactorThread对象，又称workEventLoop对象（为了更好地对接Netty中的EventLoop
            SubReactorThread workEventLoop = subReactorThreads[index];

            // 调用workEventLoop的doStart()方法，启动工作线程（如果之前已有事件启动了，就不会再启动了）
            workEventLoop.doStart();
            // 完成事件的注册工作（AbstractReactorThread中的注册，默认监听事件编码为0。
            SelectionKey selectionKey = workEventLoop.register(client);
            // 设置监听事件的编码（这样的分离，有助于不同子类的实现，更加友好）
            selectionKey.interestOps(SelectionKey.OP_READ);
        }

    }

    /**
     * subReactor的实现类，实现了父类的reactorHandler方法。主要完成非accept事件（这里demo特指read）的监听与处理，包括打印，计算，响应等
     */
    public class SubReactorThread extends AbstractReactorThread {

        /**
         * 通过懒加载方式，实例化Selector
         */
        public SubReactorThread() throws IOException {
        }

        @Override
        /**
         * 完成非accept事件（这里特指read）事件的处理（打印与响应）
         */
        protected void reactorHandler(SelectableChannel channel) throws IOException {
            // 获得客户端SocketChannel
            SocketChannel client = (SocketChannel) channel;
            // 创建ByteBuffer作为缓冲区
            ByteBuffer requestBuffer = ByteBuffer.allocate(1024);
            // 尝试读取数据
            while (client.isOpen() && (client.read(requestBuffer)) != -1){
                // 这里进行简单判断与处理
                if (requestBuffer.position() > 0){
                    break;
                }
            }
            // 判断requestBuffer大小
            if (requestBuffer.position() == 0){
                // 如果没有数据，就不需要进行接下来的处理了
                return;
            }

            // 将requestBuffer由写模式转为读模式
            requestBuffer.flip();
            // TODO 业务操作 数据库、接口...
            workerPool.submit(() -> {
                // 如：打印请求数据
                System.out.println("server get a message: "+new String(requestBuffer.array()));
            });

            // 打印日志
            System.out.println("server get a mesage from client: "+client.getRemoteAddress());

            // 发送响应
            String response = "HTTP/1.1 200 OKrn" +
                    "Content-Length: 11rnrn" +
                    "Hello World";
            ByteBuffer responseBuffer = ByteBuffer.wrap(response.getBytes());
            while (responseBuffer.hasRemaining()){
                client.write(responseBuffer);
            }
        }
    }

    /**
     * Reactor线程模型的初始化
     * @throws IOException
     * @throws ExecutionException
     * @throws InterruptedException
     */
    public void init() throws IOException, ExecutionException, InterruptedException {
        initGroup();
        initMain();
    }

    /**
     * 进行服务端，端口绑定
     * @param port
     * @throws IOException
     */
    public void bind(int port) throws IOException {
        serverSocketChannel.socket().bind(new InetSocketAddress(8080));
        System.out.println("server bind success");
        System.out.println("server start");
    }

    /**
     * 实例化两个Reactor线程组
     * @throws IOException
     */
    private void initGroup() throws IOException {
        for (int i = 0; i < mainReactorThreads.length; i++) {
            mainReactorThreads[i] = new MainReactorThread();
        }
        for (int i = 0; i < subReactorThreads.length; i++) {
            subReactorThreads[i] = new SubReactorThread();
        }
    }

    /**
     * 初始化一个MainReactorThread，来进行工作
     * @throws IOException
     * @throws ExecutionException
     * @throws InterruptedException
     */
    private void initMain() throws IOException, ExecutionException, InterruptedException {
        //TODO_FINISHED 话说，这里的mainReactorThreads只有一个线程，MainReactorThread可能多个线程嘛？还是说一个端口-》一个ServerSocketChannel-》一个MainReactorThread?
        // 参照Netty的bossGroup的NioEventLoopGroup

        // 初始化并配置serverSocketChannel
        serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.configureBlocking(false);

        // 从mainReactorThreads中挑选一个MainReactorThread
        int index = new Random().nextInt(mainReactorThreads.length);
        // 启动挑选出来的mainReactorThread
        mainReactorThreads[index].doStart();

        // 通过挑选出来的mainReactorThread线程对服务端serverSocketChannel进行注册
        SelectionKey selectionKey = mainReactorThreads[index].register(serverSocketChannel);
        // 设定监听的事件编码（Accept事件监听）
        selectionKey.interestOps(SelectionKey.OP_ACCEPT);
    }


    public static void main(String[] args) throws IOException, ExecutionException, InterruptedException {
        Reactor2 nioServerV3 = new Reactor2();
        nioServerV3.init();
        nioServerV3.bind(8080);
    }
}

