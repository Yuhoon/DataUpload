package com.wcong.dataupload.util;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by wangcong on 2017/6/22.
 * <p>
 */

public class Bean2MapUtil {
  public static Map<String, Object> b2M(Object object) {
    Map<String, Object> map = new HashMap<String, Object>();

    Class cls = object.getClass();
    Field[] fields = cls.getDeclaredFields();
    for (Field field : fields) {
      field.setAccessible(true);
      try {
        map.put(field.getName(), field.get(object));
      } catch (IllegalAccessException e) {
        e.printStackTrace();
      }
    }
    return map;
  }

}
