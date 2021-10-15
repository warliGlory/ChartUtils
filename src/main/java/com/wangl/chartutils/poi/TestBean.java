package com.wangl.chartutils.poi;

import com.wangl.chartutils.poi.config.ChartCellConfig;
import com.wangl.chartutils.poi.config.ExcelName;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
  *@Dsecriiption TODO
  *@Author liwang
  *@version 2021/9/27、15:49
**/
@Data
@ExcelName(name="TestBean")
public class TestBean {

    @ChartCellConfig(index = 1, isChart = true,aliasName = "资源池名称", axisChar = true)
    private String poolId;
    @ChartCellConfig(index = 0, isChart = true,aliasName = "cpu利用率")
    private double cpu;
    @ChartCellConfig(index = 3, isChart = true,aliasName = "men利用率")
    private double men;
    @ChartCellConfig(index = 2, isChart = true,aliasName = "disk利用率")
    private double disk;
    @ChartCellConfig(index = 4, isChart = true,aliasName = "统计")
    private double count;
    @ChartCellConfig(index = 5, isChart = false,aliasName = "intD")
    private int intD;
    @ChartCellConfig(index = 6, isChart = false,aliasName = "longD")
    private Long longD;
    @ChartCellConfig(index = 7, isChart = false,aliasName = "dateD")
    private Date dateD;
    @ChartCellConfig(index = 8, isChart = false,aliasName = "bigDecimalD")
    private BigDecimal bigDecimalD;
    @ChartCellConfig(index = 9, isChart = false,aliasName = "floatD")
    private float floatD;
    @ChartCellConfig(index = 10, isChart = false,aliasName = "dateTime",format = "yyyy/MM/dd HH:mm:ss")
    private Date dateTime;
    @ChartCellConfig(index = 11, isChart = false,aliasName = "bigDecimalE",format = "#,0.00")
    private BigDecimal bigDecimalE;
}
