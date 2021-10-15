package com.wangl.chartutils.poi.config;

import java.lang.annotation.*;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
/**
 * @Author liwang
 * @Description //用于导出数据
 * @Date 9:55 2021/9/28
 * @Param
 * @return
 **/
public @interface ChartCellConfig {
    //导出标题的下标
    int index();

    //是否为图表字段
    boolean isChart() default false;

    //导出字段别称
    String aliasName() default "";

    //用于图表数据类型展示
    boolean axisChar() default false;

    //用于表达式
    String format() default "";

}
