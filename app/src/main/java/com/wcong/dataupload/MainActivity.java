package com.wcong.dataupload;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.wcong.dataupload.bury.BuryBaseActivity;
import com.wcong.dataupload.bury.BuryParserResult;
import com.wcong.dataupload.log.Logger;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends BuryBaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        注册埋点字段
        registerBury(TestBury.class);

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
