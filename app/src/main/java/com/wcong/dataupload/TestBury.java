package com.wcong.dataupload;


import com.wcong.dataupload.bury.Bury;

/**
 * Created by wangcong on 2017/6/27.
 * <p>
 */

public class TestBury {

  @Bury(id = "BURY_TEST_ORDER",remark = "this is order")
  String order;

  @Bury(id = "BURY_TEST_DEMO",remark = "this is demo",remark1 = "测试字段",remark2 = "仅此而已")
  String demo;

  @Bury(id = "commit",remark = "点击了确定")
  static String commit="commit";

  @Bury(id = "cancel",remark = "用户选择了取消",remark1 = "用户取消了我能怎么办")
  static String cancel="cancel";
}
