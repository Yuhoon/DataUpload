package com.wcong.dataupload;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import com.wcong.dataupload.bury.UploadUtil;


/**
 * Created by wangcong on 2017/6/28.
 * <p>
 */

public class DemoDialog extends Dialog implements View.OnClickListener {
    private String msg;

    public DemoDialog(Context context) {
        super(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        View rootView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_demo, null);
        setContentView(rootView);

        rootView.findViewById(R.id.commit).setOnClickListener(this);
        rootView.findViewById(R.id.cancel).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.commit:
                UploadUtil.getInstance().upload(TestBury.class, TestBury.commit);
                break;
            case R.id.cancel:
                UploadUtil.getInstance().upload(TestBury.class, TestBury.cancel);
                break;
        }
    }
}
