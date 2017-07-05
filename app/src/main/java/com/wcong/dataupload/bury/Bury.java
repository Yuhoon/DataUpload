package com.wcong.dataupload.bury;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by wangcong on 2017/6/27.
 * <p>
 */

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Bury {
  String id();

  String remark() default "";

  String remark1() default "";

  String remark2() default "";

  String remark3() default "";
}
