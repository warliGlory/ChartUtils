package com.wangl.chartutils.poi.utils;/*
 * ----------------------------------------------------------------------
 * Copyright  2019 China Mobile (SuZhou) Software Technology Co.,Ltd.
 *
 * The programs can not be copied and/or distributed without the express
 * permission of China Mobile (SuZhou) Software Technology Co.,Ltd.
 *
 * @description:
 *
 * @author: zongtao.li@zznode.com
 * @create: 2019/11/13 11:06
 *
 * ----------------------------------------------------------------------
 */


import java.lang.reflect.Array;
import java.util.Collection;



public class CheckUtil {

  public static boolean isEmpty(Collection<?> collection) {
    return (collection == null || collection.isEmpty()) || collection.size()==0;
  }

  public static boolean isEmpty(String str) {
    return (str==null||str.isEmpty());
  }


  public static boolean isEmpty(Object obj) {
    if(obj == null){
      return true;
    }
    if(obj.getClass().isArray()){
      return Array.getLength(obj) == 0;
    }
    return false;
  }

  public static boolean isNotEmpty(Object obj) {
    return !isEmpty(obj);
  }




}
