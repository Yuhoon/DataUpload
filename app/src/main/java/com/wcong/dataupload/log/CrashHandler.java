package com.wcong.dataupload.log;

import android.content.Context;
import android.os.Process;
import android.util.Log;

/**
 * Created by wangcong on 2017/7/5.
 * <p>
 */
public final class CrashHandler implements Thread.UncaughtExceptionHandler {
    private static CrashHandler instance = new CrashHandler();

    private CrashHandler() {
    }

    public static CrashHandler getInstance() {
        return instance;
    }

    public void init(Context context) {
        Thread.setDefaultUncaughtExceptionHandler(this);
    }

    /**
     * . 当程序中有未被捕获的异常， 系统会自动调用 uncaughtException 方法
     *
     * @param thread    出现未捕获异常的线程
     * @param throwable 未捕获的异常
     */
    @Override
    public void uncaughtException(Thread thread, Throwable throwable) {
        throwable.printStackTrace();
        Logger.error(thread, throwable, true);
        Log.e(thread.getName(), "crash", throwable);
        Process.killProcess(Process.myPid());
    }
}