package com.wangl.chartutils.poi.utils;

/**
 * UUID Generator
 */
public class UUID {
  public static String random() {
    return java.util.UUID.randomUUID().toString().replaceAll("-", "");
  }
}
