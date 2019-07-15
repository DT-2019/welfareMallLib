package com.fifthera.ecmall.utils;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.util.Pair;

import java.util.concurrent.CountDownLatch;

public class RunUtil {

    private static Handler sHandler;
    private static final int MESSAGE_RUN_ON_UITHREAD = 0x1;

    private static Handler getHandler() {
        synchronized (RunUtil.class) {
            if (sHandler == null) {
                sHandler = new InternalHandler();
            }
            return sHandler;
        }
    }

    public static void runOnUiThread(Runnable runnable) {
        runOnUiThread(runnable, false);
    }

    public static void runOnUiThread(Runnable runnable, boolean waitUtilDone) {
        if (Thread.currentThread() == Looper.getMainLooper().getThread()) {
            runnable.run();
            return;
        }

        CountDownLatch countDownLatch = null;
        if (waitUtilDone) {
            countDownLatch = new CountDownLatch(1);
        }
        Pair<Runnable, CountDownLatch> pair = new Pair<>(runnable, countDownLatch);
        getHandler().obtainMessage(MESSAGE_RUN_ON_UITHREAD, pair).sendToTarget();
        if (waitUtilDone) {
            try {
                countDownLatch.await();
            } catch (InterruptedException e) {
                Log.w("ec", e);
            }
        }
    }

    private static class InternalHandler extends Handler {
        public InternalHandler() {
            super(Looper.getMainLooper());
        }

        @Override
        public void handleMessage(Message msg) {
            if (msg.what == MESSAGE_RUN_ON_UITHREAD) {
                Pair<Runnable, CountDownLatch> pair = (Pair<Runnable, CountDownLatch>) msg.obj;
                try {
                    Runnable runnable = pair.first;
                    runnable.run();

                } finally {
                    if (pair.second != null) {
                        pair.second.countDown();
                    }
                }
            }
        }
    }
}
