package com.wangl.chartutils.poi.config;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
  *@Dsecriiption TODO
  *@Author liwang
  *@version 2021/9/28、10:14
**/
//用于存储自定义注解的值
@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class CellConfigBean {
    private int index;

    private boolean isChart;

    private String aliasName;

    private String fieldName;

    private boolean axisChar;

    private String format;

    private String fieldType;

}
