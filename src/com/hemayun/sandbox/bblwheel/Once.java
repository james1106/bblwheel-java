package com.hemayun.sandbox.bblwheel;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by apple on 16/11/18.
 * 实现“只执行一次”的功能
 */
public class Once {

    private AtomicInteger done = new AtomicInteger(0);

    public void run(Runnable r) {
        if (done.get() == 1) return;
        if (done.compareAndSet(0, 1)) {
            r.run();
        }
    }
}
