package com.wcong.dataupload.log;

/**
 * Created by wangcong on 2017/7/5.
 * <p>
 */

public enum LogLevel {
    DEBUG(1), INFO(5), WARN(8), ERROR(10), BURY(10);

    public int point;

    private LogLevel(int point) {
        this.point = point;
    }
}
