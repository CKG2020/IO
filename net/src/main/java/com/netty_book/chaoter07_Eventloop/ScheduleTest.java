package com.netty_book.chaoter07_Eventloop;

import java.util.concurrent.*;

public class ScheduleTest {
    public static void main(String[] args) {
        ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(10);
   ScheduledFuture<?>  future= scheduledExecutorService.schedule(new Runnable() {
            @Override
            public void run() {
                System.out.println("60------------");
            }
        },60, TimeUnit.SECONDS); //从现在开始６０秒以后执行

   //.................
        scheduledExecutorService.shutdown();//调度任务结束后就关闭线程池　释放资源　
    }}
