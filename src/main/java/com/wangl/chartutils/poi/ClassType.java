package com.wangl.chartutils.poi;

/**
  *@Dsecriiption TODO
  *@Author liwang
  *@version 2021/9/29、14:38
**/
public enum  ClassType {


    BYTE("Byte"),
    SHORT("Short"),
    INT("Integer"),
    LONG("Long"),
    DOUBLE("Double"),
    FLOAT("Float"),
    CHAR("Character"),
    BOOLEAN("Boolean"),
    STRING("String"),
    DATE("Date"),
    BIGDECIMAL("BigDecimal");

    private String classType;

    ClassType(String classType){
        this.classType = classType;
    }

    public boolean validateType(Object object){
        return classType.equals(object.getClass().getSimpleName());
    }

}
