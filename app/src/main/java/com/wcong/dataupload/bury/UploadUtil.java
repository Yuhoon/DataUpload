package com.wcong.dataupload.bury;

import android.text.TextUtils;
import android.util.Log;

import com.wcong.dataupload.log.Logger;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by wangcong on 2017/6/27.
 * <p>
 * 上传工具类
 */

public class UploadUtil {
    private static UploadUtil util;

    public static UploadUtil getInstance() {
        if (util == null)
            util = new UploadUtil();
        return util;
    }

    public void upload(Class buryClass, String tag) {
        Map map = parse(buryClass, tag);
        this.upload(map);
    }

    public void upload(Class buryClass, String tag, BuryParserResult result) {
        Map map = parse(buryClass, tag);
        if (result != null)
            result.onResult(map);
    }

    public void upload(Map map) {
        Logger.bury(map);
    }

    private Map parse(Class buryClass, String tag) {
        Map map = new HashMap();
        try {
            Field field = buryClass.getDeclaredField(tag);
            Bury bury = field.getAnnotation(Bury.class);
            Method[] methods = bury.getClass().getDeclaredMethods();
            for (int i = 3; i < methods.length - 1; i++) {
                if (TextUtils.isEmpty((CharSequence) methods[i].invoke(bury, (Object[]) null)))
                    continue;
                map.put(methods[i].getName(), methods[i].invoke(bury, (Object[]) null));
            }
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return map;
    }

}
