package com.yongda.site.wallet.app.util.test;

/**
 * <br>
 * 作者： zhangweijie <br>
 * 日期： 2017/2/3-11:17<br>
 */
public class MyTask implements Runnable{
    private int taskNum;

    public MyTask(int num) {
        this.taskNum = num;
    }

    @Override
    public void run() {
        System.out.println("正在执行task "+taskNum);
        try {
            Thread.currentThread().sleep(4000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("task "+taskNum+"执行完毕");
    }
}
