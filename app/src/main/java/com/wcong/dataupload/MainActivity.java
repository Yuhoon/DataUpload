package com.wcong.dataupload;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.wcong.dataupload.bury.BuryBaseActivity;
import com.wcong.dataupload.bury.BuryParserResult;
import com.wcong.dataupload.log.CrashHandler;
import com.wcong.dataupload.log.LogField;
import com.wcong.dataupload.log.LogLevel;
import com.wcong.dataupload.log.LogStashDescription;
import com.wcong.dataupload.log.Logger;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends BuryBaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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

        Logger.addDestination(buryDescription);
        Logger.addDestination(infoDescription);

        registerBury(TestBury.class);

        //初始化异常捕获服务
        CrashHandler.getInstance().init(this);

        setBuryResult(new BuryParserResult() {
            @Override
            public void onResult(Map data) {
                for (Object o : data.keySet()) {
                    Log.i("key: ", "" + o);
                    Log.i("value: ", "" + data.get(o));
                }
            }
        });
    }

    public void bury(View view) {
        Map map = new HashMap();
        map.put("bury", "main");
        Logger.bury(map);
    }

    public void err(View view) {
        Logger.error("this is err test");
    }

    public void info(View view) {
        Logger.info("this is info test");
    }

    public void dialog(View view) {
        new DemoDialog(this).show();
    }
}
