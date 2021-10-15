package com.wangl.chartutils.poi.utils;
/**
  *@Dsecriiption TODO
  *@Author liwang
  *@version 2021/10/9、17:36
**/
public enum ChartType {

    BARCHART("barChart","柱形图"),
    PIECHART("pieChart","饼状图"),
    TIMEXYCHAR("timeXYChar","折线图"),
    AREACHART("areaChart","面积图");

    private String code;
    private String describe;
    ChartType(String code, String describe){
        this.code = code;
        this.describe = describe;
    }

    public String code(){
        return code;
    }
}
