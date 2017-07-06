package com.wcong.dataupload.log;

/**
 * Created by wangcong on 2017/7/6.
 * <p>
 */

public enum UploadPolicy {
    /**
     * 只在WIFI环境下上传
     */
    UPLOAD_POLICY_WIFI,
    /**
     * 在Debug模式下上传，默认false
     */
    UPLOAD_POLICY_DEBUG,
    /**
     * 是否实时上传，默认在达到临界值时上传
     */
    UPLOAD_POLICY_REALTIME
}
