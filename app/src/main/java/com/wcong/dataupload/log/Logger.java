package com.wcong.dataupload.log;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.wcong.dataupload.util.Bean2MapUtil;
import com.wcong.dataupload.util.DeviceInfoUtil;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * Created by wangcong on 2017/7/5.
 * <p>
 */

public class Logger {
    private static HashMap<BaseDestination, BlockingQueue<Map>> destinations = new HashMap<>();
    private static Context context;
    private static PackageInfo packageInfo;
    private static PackageManager packageManager;

    public static void init(Context context) {
        Logger.context = context;
        checkPermission(context);
//    uploadTimer();
    }

    /**
     * 6.0以上校验用户权限，获取部分用户信息时需要的权限
     *
     * @param context
     */
    private static void checkPermission(Context context) {
        if (ContextCompat.checkSelfPermission(context,
                Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(context,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions((Activity) context, new String[]{
                    Manifest.permission.READ_PHONE_STATE, Manifest.permission.ACCESS_COARSE_LOCATION}, 0);
        }
    }

    public static void addDestination(BaseDestination destination) {
        BlockingQueue<Map> queue = new LinkedBlockingDeque<>();
        destinations.put(destination, queue);
        Logger.Worker worker = new Logger.Worker(destination, queue);
        worker.start();
    }

    public static void info(String message) {
        Logger.sendData(LogLevel.INFO, message, false);
    }

    public static void debug(String message) {
        Logger.sendData(LogLevel.DEBUG, message, false);
    }

    public static void warn(String message) {
        Logger.sendData(LogLevel.WARN, message, false);
    }

    public static void error(String message) {
        Logger.sendData(LogLevel.ERROR, message, false);
    }

    public static void error(String message, Throwable throwable) {
        Logger.sendError(Thread.currentThread(), message, throwable, false);
    }

    public static void error(Thread thread, Throwable message, boolean sync) {
        Logger.sendError(thread, null, message, sync);
    }

    public static void bury(Map map) {
        Logger.sendData(LogLevel.BURY, map, false);
    }

    public static void sendError(Thread thread, String message, Throwable throwable, boolean sync) {
        LogModel model = new LogModel();
        model.setLevel(LogLevel.ERROR);

        StringBuilder sb = new StringBuilder();
        if (message != null) {
            sb.append(message + ": ");
        }
        Writer result = new StringWriter();
        PrintWriter printWriter = new PrintWriter(result);
        throwable.printStackTrace(printWriter);
        sb.append(result.toString());
        model.setMessage(sb.toString());
        model.setThread(thread.getName());
        int i = 0;
        for (StackTraceElement element : thread.getStackTrace()) {
            if (!element.isNativeMethod()) {
                i++;
                if (i == 4) {
                    model.setFileName(element.getClassName());
                    model.setLine(element.getLineNumber());
                    model.setFunction(element.getMethodName());
                    break;
                }
            }
        }
        send(LogLevel.ERROR, Bean2MapUtil.b2M(model), sync);
    }

    public static void sendData(LogLevel level, String message, boolean sync) {
        LogModel model = new LogModel();
        model.setLevel(level);
        model.setMessage(message);
        model.setThread(Thread.currentThread().getName());
        int i = 0;
        for (StackTraceElement element : Thread.currentThread().getStackTrace()) {
            if (!element.isNativeMethod()) {
                i++;
                if (i == 4) {
                    model.setFileName(element.getClassName());
                    model.setLine(element.getLineNumber());
                    model.setFunction(element.getMethodName());
                    break;
                }
            }
        }
        send(level, Bean2MapUtil.b2M(model), sync);
    }

    public static void sendData(LogLevel level, Map map, boolean sync) {
        LogModel model = new LogModel();
        model.setLevel(level);
        model.setThread(Thread.currentThread().getName());
        int i = 0;
        for (StackTraceElement element : Thread.currentThread().getStackTrace()) {
            if (!element.isNativeMethod()) {
                i++;
                if (i == 4) {
                    model.setFileName(element.getClassName());
                    model.setLine(element.getLineNumber());
                    model.setFunction(element.getMethodName());
                    break;
                }
            }
        }
        map.putAll(Bean2MapUtil.b2M(model));
        send(level, map, sync);
    }

    private static void send(LogLevel logLevel, Map data, boolean sync) {
        for (BaseDestination destination : destinations.keySet()) {
            //处理分发事件，判断当前destination是否为支持类型
            if (!destination.accept(logLevel))
                continue;
            data.put(LogField.LEVEL, logLevel);
            if (sync) {
                destination.send(setField(data));
            } else {
                destinations.get(destination).add(data);
            }
        }
    }

    static class Worker extends Thread {
        BlockingQueue<Map> queue;
        private BaseDestination destination;

        public Worker(BaseDestination destination, BlockingQueue<Map> queue) {
            this.destination = destination;
            this.queue = queue;
        }

        public void run() {
            for (; ; ) {
                try {
                    Map data = queue.take();
                    destination.send(setField(data));
                } catch (InterruptedException e) {
                    Log.e("进程异常", e.getMessage());
                    return;
                }
            }
        }
    }

    private static void uploadTimer() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                for (; ; ) {
                    try {
                        Thread.sleep(60 * 5 * 1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    for (BaseDestination destination : destinations.keySet()) {
                        destination.send(setField(new HashMap()));
                    }
                }
            }
        }).start();
    }

    private static Map setField(Map data) {
        LogModel logModel = new LogModel();
        logModel.setLevel(LogLevel.BURY);
        if (data != null && data.size() != 0) {
            logModel.setLevel((LogLevel) data.get(LogField.LEVEL));
            logModel.setMessage((String) data.get(LogField.MESSAGE));
            logModel.setThread((String) data.get(LogField.THREAD));
            logModel.setFileName((String) data.get(LogField.FILE_NAME));
            logModel.setFunction((String) data.get(LogField.FUNCTION));
            logModel.setLine((Integer) data.get(LogField.LINE));
        }
        logModel.setTimestamp(new Date());
        packageManager = context.getPackageManager();
        try {
            packageInfo = packageManager.getPackageInfo(context.getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            packageInfo = new PackageInfo();
        }
        logModel.setAppBuild(packageInfo.versionCode);
        logModel.setAppId(context.getPackageName());

        logModel.setIMEI(DeviceInfoUtil.getImei(context));
        logModel.setIMSI(DeviceInfoUtil.getImsi(context));
        logModel.setPHONE_WIFI_MAC(DeviceInfoUtil.getWifiMac());
        logModel.setROUTER_MAC(DeviceInfoUtil.getRouterMac(context));
        logModel.setCell_id(DeviceInfoUtil.getCellInfo(context));
        logModel.setDevice_id(DeviceInfoUtil.getUDID(context));
        logModel.setSrceen_resolution_desc(DeviceInfoUtil.getScreenResolution(context));
        logModel.setHorizontal_flag(DeviceInfoUtil.getScreenType(context));
        logModel.setOs_version(DeviceInfoUtil.getOsVersion());
        logModel.setMobile_operators_desc(DeviceInfoUtil.getOperators(context));
        logModel.setNetwork_desc(DeviceInfoUtil.getNetworkType(context));
        logModel.setSdk_version(DeviceInfoUtil.getSdkVersion());
        logModel.setDevice_desc(DeviceInfoUtil.getDeviceInfo());
        logModel.setApp_source(DeviceInfoUtil.getDeviceInfo());
        logModel.setApp_version(context.getPackageName());
        logModel.setLanguage(DeviceInfoUtil.getSystemLanguage(context));
        logModel.setIP(DeviceInfoUtil.getDeviceIP());
        logModel.setIsRoot(DeviceInfoUtil.isRoot() + "");
        logModel.setGmtTime(DeviceInfoUtil.getGMT());
        logModel.setMac(DeviceInfoUtil.getMacAddr());
        logModel.setSize(DeviceInfoUtil.getPhoneSize(context));
        data.putAll(Bean2MapUtil.b2M(logModel));
        return data;
    }

}
