package com.wangl.chartutils.poi.utils;

import org.apache.commons.beanutils.BeanComparator;
import org.apache.commons.collections.comparators.ComparableComparator;
import org.apache.commons.collections4.ComparatorUtils;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
  *@Dsecriiption TODO
  *@Author liwang
  *@version 2021/7/2、16:57
**/
public class ListUtil {
    //集合List<Bean>按字段排序
    public static  <T> void sort(List<T> list, String fieldName, boolean asc) {

        Comparator<?> mycmp = ComparableComparator.getInstance();
        mycmp = ComparatorUtils.nullLowComparator(mycmp); // 允许null
        if (!asc) {
            mycmp = ComparatorUtils.reversedComparator(mycmp); // 逆序
        }
        Collections.sort(list, new BeanComparator(fieldName, mycmp));
    }

}
