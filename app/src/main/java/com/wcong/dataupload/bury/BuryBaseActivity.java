package com.wcong.dataupload.bury;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;

import com.wcong.dataupload.log.Logger;

public class BuryBaseActivity extends AppCompatActivity {

    private Class buryClass;

    private boolean isUpload = true;

    private BuryParserResult result;

    private ValidateViewTask validateViewTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Logger.setContext(this);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            if (buryClass != null && isUpload) {
                if (result == null)
                    validateViewTask = new ValidateViewTask(findViewById(android.R.id.content), ev, buryClass);
                else
                    validateViewTask = new ValidateViewTask(findViewById(android.R.id.content), ev, buryClass, result);
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    @Override
    protected void onDestroy() {
        validateViewTask.cancel(true);
        super.onDestroy();
    }

    /**
     * 注册埋点字段
     *
     * @param buryClass
     */
    protected void registerBury(Class buryClass) {
        this.buryClass = buryClass;
    }

    /**
     * 是否上传，默认为true
     *
     * @param isUpload
     */
    protected void setUpload(boolean isUpload) {
        this.isUpload = isUpload;
    }

    protected void setBuryResult(BuryParserResult result) {
        this.result = result;
    }
}
