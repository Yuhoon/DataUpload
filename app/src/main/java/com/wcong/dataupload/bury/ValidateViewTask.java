package com.wcong.dataupload.bury;

import android.os.AsyncTask;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by wangcong on 2017/6/27.
 * <p>
 * 校验用户的点击区域是否是我们设置的埋点view，遍历复杂布局比较耗时，所以放在子线程中处理
 */

public class ValidateViewTask extends AsyncTask<View, String, View> {

    private View rootView;
    private MotionEvent event;
    private Class buryClass;
    private BuryParserResult result;

    public ValidateViewTask(View rootView, MotionEvent event, Class buryClass) {
        this.rootView = rootView;
        this.event = event;
        this.buryClass = buryClass;
        super.execute();
    }

    public ValidateViewTask(View rootView, MotionEvent event, Class buryClass, BuryParserResult result) {
        this.rootView = rootView;
        this.event = event;
        this.buryClass = buryClass;
        this.result = result;
        super.execute();
    }

    /**
     * 遍历rootView中的所有子viewGroup及view
     *
     * @param view
     * @param event
     * @return
     */
    private View throughView(View view, MotionEvent event) {
        View clickView = null;
        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            for (int i = group.getChildCount() - 1; i >= 0; i--) {
                View chilView = group.getChildAt(i);
                clickView = throughView(chilView, event);
                if (clickView != null) {
                    return clickView;
                }
            }
        } else if (validateView(view, event) && view.getVisibility() == View.VISIBLE) {
            return view;
        }
        return clickView;
    }

    /**
     * 校验点击区域是否存在view
     *
     * @param view
     * @param event
     * @return
     */
    public boolean validateView(View view, MotionEvent event) {
        int[] location = new int[2];
        view.getLocationOnScreen(location);
        int left = location[0];
        int top = location[1];
        int bottom = top + view.getHeight();
        int right = left + view.getWidth();
        if (event.getX() > left && event.getX() < right && event.getY() > top
                && event.getY() < bottom) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    protected View doInBackground(View... params) {
        return throughView(rootView, event);
    }

    @Override
    protected void onPostExecute(View view) {
        if (view == null)
            return;
        if (view.getTag() == null)
            return;
        if (result == null)
            UploadUtil.getInstance().upload(buryClass, (String) view.getTag());
        else
            UploadUtil.getInstance().upload(buryClass, (String) view.getTag(), result);
    }
}
