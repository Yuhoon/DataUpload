package com.wcong.dataupload.log;

import java.util.Map;

/**
 * Created by wangcong on 2017/7/5.
 * <p>
 */

public interface BaseDestination {
    void send(Map data);

    void setFileName(String fileName);

    void setToken(String token);

    void setServerUrl(String url);

    void setThreshold(int threshold);

    void setnterval(int interval);

    boolean accept(LogLevel level);
}
