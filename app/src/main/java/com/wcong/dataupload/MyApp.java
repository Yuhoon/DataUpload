package com.wcong.dataupload;

import android.app.Application;

import com.wcong.dataupload.log.CrashHandler;
import com.wcong.dataupload.log.LogField;
import com.wcong.dataupload.log.LogLevel;
import com.wcong.dataupload.log.LogStashDescription;
import com.wcong.dataupload.log.Logger;
import com.wcong.dataupload.log.UploadPolicy;

/**
 * Created by wangcong on 2017/7/6.
 * <p>
 */

public class MyApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        Logger.init(this);
        //TODO 每一种上传数据类型对应一个description
        LogStashDescription buryDescription = new LogStashDescription(this, LogLevel.BURY);
        //TODO 本地缓存的json文件名
        buryDescription.setFileName("bury");
        //TODO 设置TOKEN，具体实现按需求调整
        buryDescription.setToken(LogField.TOKEN);
        //TODO 配置日志上传地址，具体实现按需求调整
        buryDescription.setServerUrl(LogField.URL);


        LogStashDescription infoDescription = new LogStashDescription(this, LogLevel.ERROR,
                LogLevel.INFO);
        infoDescription.setFileName("error");
        infoDescription.setToken(LogField.TOKEN);
        infoDescription.setServerUrl(LogField.URL);
        //TODO 设置上传策略
        infoDescription.setPolicy(UploadPolicy.UPLOAD_POLICY_DEBUG, UploadPolicy.UPLOAD_POLICY_REALTIME, UploadPolicy.UPLOAD_POLICY_WIFI);

        Logger.addDestination(buryDescription);
        Logger.addDestination(infoDescription);

        //初始化异常捕获服务
        CrashHandler.getInstance().init(this);
    }
}
