package com.wangl.chartutils.poi.utils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Pattern;

public class DateUtil {

  private static  String format_milliSecond = "yyyy-MM-dd HH:mm:ss:SSS";
  private static  String format_second = "yyyy-MM-dd HH:mm:ss";
  /*
   * datetime格式的时间支持所有的转化 但是date类型的显然是不能
   * */
  public static String DateToString(Date date, String patten) {
    return OldDateUtil.format(date, patten);
  }

  public static Date StringToDate(String dateStr) {
    Pattern ymdNumber = Pattern.compile("\\d{8}");
    Pattern ymdhNumber = Pattern.compile("\\d{10}");
    Pattern ymdhmNumber = Pattern.compile("\\d{12}");
    Pattern ymdhmsNumber = Pattern.compile("\\d{14}");
    Pattern y_m_d = Pattern.compile("\\d{4}-\\d{1,2}-\\d{1,2}");
    Pattern y_m_d_h_m_s = Pattern.compile("\\d{4}-\\d{1,2}-\\d{1,2} \\d{1,2}:\\d{1,2}:\\d{1,3}.*");
    Pattern y_m_d_h = Pattern.compile("\\d{4}-\\d{1,2}-\\d{1,2} \\d{1,2}");

    try {
      if (dateStr.contains("-")) {
        //yyyy-MM-dd
        if (y_m_d.matcher(dateStr).matches()) {
          return OldDateUtil.parse(dateStr, "yyyy-MM-dd");
        }
        //  yyyy-MM-dd HH:mm:ss
        if (y_m_d_h_m_s.matcher(dateStr).matches()) {
          return OldDateUtil.parse(dateStr, "yyyy-MM-dd HH:mm:ss");
        }
        //  yyyy-MM-dd HH
        if (y_m_d_h.matcher(dateStr).matches()) {
          return OldDateUtil.parse(dateStr, "yyyy-MM-dd HH");
        }
      } else {
        //yyyyMMdd
        if (ymdNumber.matcher(dateStr).matches()) {
          return OldDateUtil.parse(dateStr, "yyyyMMdd");
        }
        //yyyyMMddHH
        if (ymdhNumber.matcher(dateStr).matches()) {
          return OldDateUtil.parse(dateStr, "yyyyMMddHH");
        }
        //yyyyMMddHHmm
        if (ymdhmNumber.matcher(dateStr).matches()) {
          return OldDateUtil.parse(dateStr, "yyyyMMddHHmm");
        }
        //yyyyMMddHHmmss
        if (ymdhmsNumber.matcher(dateStr).matches()) {
          return OldDateUtil.parse(dateStr, "yyyyMMddHHmmss");
        }
      }
    } catch (Exception e) {
      System.out.println("转化异常：" + dateStr);
    }
    return null;
  }


  public static Date LongToDate(String longtime) {
    if (longtime.length() > 10) {
      longtime = longtime.substring(0, 10);
    }
    Long timestamp = Long.valueOf(longtime);
    if (timestamp == 0) {
      return null;
    }
    Calendar c = Calendar.getInstance();
    c.setTimeInMillis(timestamp);
    return c.getTime();
  }

  /**
   * 日期累加
   *
   * @param format 返回的日期格式
   * @param year 加多少年
   * @param month 加多少个月
   * @param day 加多少天
   */
  public static String GetSysDate(String format, Date date, int year,
      int month, int day) {
    Calendar cal = Calendar.getInstance();
    cal.setTime(date);

    if (day != 0) {
      cal.add(cal.DATE, day);
    }
    if (month != 0) {
      cal.add(cal.MONTH, month);
    }
    if (year != 0) {
      cal.add(cal.YEAR, year);
    }
    return OldDateUtil.format(cal.getTime(), format);
  }


  /**
   * 日期累加
   *
   * @param format 返回的日期格式
   * @param hour 加多少小时
   * @param min 加多少分钟
   * @param second 加多少秒
   */
  public static Long GetDateLong(String format, String date, int hour,
      int min, int second) {

    Calendar cal = Calendar.getInstance();
    cal.setTime(DateUtil.StringToDate(date));

    if (hour != 0) {
      cal.add(cal.HOUR, hour);
    }
    if (min != 0) {
      cal.add(cal.MINUTE, min);
    }
    if (second != 0) {
      cal.add(cal.SECOND, second);
    }
    return cal.getTime().getTime();
  }


  public static Date getDatePlus(Date date, int year,
      int month, int day) {
    Calendar cal = Calendar.getInstance();
    cal.setTime(date);

    if (day != 0) {
      cal.add(cal.DATE, day);
    }
    if (month != 0) {
      cal.add(cal.MONTH, month);
    }
    if (year != 0) {
      cal.add(cal.YEAR, year);
    }
    return cal.getTime();
  }

  public static Date getDatePlus2(Date date, int hour,
      int min, int second) {
    Calendar cal = Calendar.getInstance();
    cal.setTime(date);

    if (hour != 0) {
      cal.add(cal.HOUR, hour);
    }
    if (min != 0) {
      cal.add(cal.MINUTE, min);
    }
    if (second != 0) {
      cal.add(cal.SECOND, second);
    }
    return cal.getTime();
  }

  public static Long DateToLong(Date time) {
    return time.getTime();
  }

  public static long stringToLong(String str) {
    long time = 0L;
    Date date = StringToDate(str);
    return date.getTime();
  }

  public static String changeEnd(String end) {
    StringBuilder sb = new StringBuilder(end);
    String[] str = new String[]{"2", "3", "5", "9", "5", "9"};
    if (end.length() < 14) {
      int flag = 14 - end.length();
      for (int i = 6 - flag; i < str.length; i++) {
        sb.append(str[i]);
      }
    }
    return sb.toString();
  }


  /**
   * 功能描述 获取当前时间是周几(星期几)
   */
  public static Integer getCurrentWeekNum() {
    Calendar c = Calendar.getInstance();
    int dayForWeek = 0;
    if (c.get(Calendar.DAY_OF_WEEK) == 1) {
      dayForWeek = 7;
    } else {
      dayForWeek = c.get(Calendar.DAY_OF_WEEK) - 1;
    }
    return dayForWeek;
  }

  /**
   * 获取 某分钟之后的时间
   *
   * @param minute 分钟
   */
  public static Date getTime(int minute) {
    Calendar nextTime = Calendar.getInstance();
    nextTime.setTime(new Date());
    nextTime.add(Calendar.MINUTE, minute);
    return nextTime.getTime();
  }


  /**
   * 获取当天时间 【按传入参数格式返回】
   */
  public static String getNowTime(String dateformat) {
    Date now = new Date();
    SimpleDateFormat dateFormat = null;
    String hehe = null;
    try {
      dateFormat = new SimpleDateFormat(dateformat);// 可以方便地修改日期格式
      hehe = dateFormat.format(now);
    } catch (Exception e) {
      dateFormat = new SimpleDateFormat("yyyy-MM-dd");// 可以方便地修改日期格式
      hehe = dateFormat.format(now);
    }
    return hehe;
  }

  /**
   * 获取七天之前(以当前时间为参照)的时间 【按传入参数格式返回】
   */
  public static String getBefore7DaysTime(String dateformat) {
    Calendar cd = Calendar.getInstance();
    cd.add(Calendar.DATE, -6);
    return new SimpleDateFormat(dateformat).format(cd.getTime());
  }


  /**
   * 获取指定时间指定日期 【按传入参数格式返回】
   */
  public static String getDaysTime(String dateformat, int day) {
    Calendar cd = Calendar.getInstance();
    cd.add(Calendar.DATE, day);
    return new SimpleDateFormat(dateformat).format(cd.getTime());
  }

  public static String getBeforeNDaysTime(int num, String date, String dateformat) {
    Calendar dayc1 = new GregorianCalendar();
    SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
    Date daystart;
    try {
      daystart = format.parse(date);
      dayc1.setTime(daystart);
    } catch (ParseException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    dayc1.add(Calendar.DATE, -num);
    return new SimpleDateFormat(dateformat).format(dayc1.getTime());
  }

  /**
   * 获取N天之前(以当前时间为参照)的时间
   */
  public static String getBeforeNDaysTime(int num, String dateformat) {
    Calendar cd = Calendar.getInstance();
    cd.add(Calendar.DATE, -num);
    return new SimpleDateFormat(dateformat).format(cd.getTime());
  }

  /**
   * 获取本月第一天的日期
   */
  public static String getFirstDayToCurMonth() {
    Date date = new Date();
    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM");
    String currentDate = format.format(date);
    StringBuffer sb = new StringBuffer(currentDate);
    sb.append("-01");
    return sb.toString();
  }

  /**
   * 获取本月第一天的日期(给到秒)
   */
  public static String getFirstDayForTime() {
    String firTime = getFirstDayToCurMonth();
    StringBuffer str = new StringBuffer(firTime);
    str.append(" 00:00:00");
    return str.toString();
  }

  /**
   * 获取昨天的日期
   */
  public static String getYesterDayDate() {
    Calendar cd = Calendar.getInstance();
    cd.add(Calendar.DATE, -1);
    return new SimpleDateFormat("yyyy-MM-dd").format(cd.getTime());
  }

  /**
   * 获取昨天的日期
   */
  public static String getYesterDayDate(String dateformat) {
    if (CheckUtil.isEmpty(dateformat)) {
      dateformat = "yyyy-MM-dd";
    }
    Calendar cd = Calendar.getInstance();
    cd.add(Calendar.DATE, -1);
    return new SimpleDateFormat(dateformat).format(cd.getTime());
  }

  /**
   * 获取指定日期前一天的日期
   */
  public static String getYesterDayDate(String strTime, String dateformat) {
    Calendar cd = Calendar.getInstance();
    cd.setTime(getStrByDataTime(strTime, dateformat));
    cd.add(Calendar.DATE, -1);
    return new SimpleDateFormat(dateformat).format(cd.getTime());
  }

  /**
   * 获取指定日期前23小时
   */
  public static String getYesterHourDate(String strTime, String dateformat) {
    Calendar cd = Calendar.getInstance();
    cd.setTime(getStrByDataTime(strTime, dateformat));
    cd.add(Calendar.HOUR_OF_DAY, -23);
    return new SimpleDateFormat(dateformat).format(cd.getTime());
  }

  /**
   * 获取昨天的日期(给到秒)
   */
  public static String getYesterDayTime() {
    String yesTime = getYesterDayDate();
    StringBuffer str = new StringBuffer(yesTime);
    str.append(" 23:59:59");
    return str.toString();
  }

  /**
   * 返回指定日期的前一天日期
   */
  public static Date getYesterByDate(Date dateParam) {
    Calendar c = Calendar.getInstance();
    c.setTime(dateParam);
    int day = c.get(Calendar.DATE);
    c.set(Calendar.DATE, day - 1);
    return c.getTime();
  }

  /**
   * 返回指定日期的前一天日期
   */
  public static String getYesterByDate(String dateParam) {
    SimpleDateFormat sd = new SimpleDateFormat("yyyy-MM-dd");
    Date date = null;
    try {
      date = getYesterByDate(sd.parse(dateParam));
    } catch (Exception e) {
      e.printStackTrace();
    }
    return new SimpleDateFormat("yyyy-MM-dd").format(date);
  }

  /**
   * 指定日期添加天数
   */
  public static Date getDateByDay(Date data, int day) {
    Calendar cal = Calendar.getInstance();
    cal.setTime(data);
    cal.add(Calendar.DATE, day);
    return cal.getTime();
  }

  /**
   * 指定日期添加小时
   */
  public static Date getDateByHour(Date data, int hour) {
    Calendar cal = Calendar.getInstance();
    cal.setTime(data);
    cal.add(Calendar.HOUR_OF_DAY, hour);
    return cal.getTime();
  }


  /**
   * 返回指定日期的前N天日期
   */
  public static String getDateBefore(String dateParam, int num) {
    SimpleDateFormat sd = new SimpleDateFormat("yyyy-MM-dd");
    Date date = null;
    try {
      //date = getYesterByDate(sd.parse(dateParam));

      Calendar c = Calendar.getInstance();
      c.setTime(sd.parse(dateParam));
      int day = c.get(Calendar.DATE);
      c.set(Calendar.DATE, day - (num - 1));
      date = c.getTime();

    } catch (Exception e) {
      e.printStackTrace();
    }
    return new SimpleDateFormat("yyyy-MM-dd").format(date);
  }

  /**
   * 返回时间戳
   *
   * @param user_time 字符串格式yyyy-MM-dd 年月日
   */
  public static int getTime(String user_time) {
    String re_time = null;
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    Date d;
    try {
      d = sdf.parse(user_time);
      long l = d.getTime();
      String str = String.valueOf(l);
      re_time = str.substring(0, 10);
    } catch (ParseException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return Integer.parseInt(re_time);
  }


  /**
   * 返回时间戳
   *
   * @param time 字符串格式yyyy-MM-dd 年月日
   */
  public static long getLongTime(String time) {
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    try {
      Date d = sdf.parse(time);
      return d.getTime();
    } catch (ParseException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
      return 0;
    }
  }

  /**
   * <h1>获取时间到日期</h1>
   * 说明：如果参数为null，则返回当天日期
   */
  public static Date getDateByDay(Date date) {
    if (date == null) {
      date = new Date();
    }
    String time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date);
    return getStrByDataTime(time, "yyyy-MM-dd HH");
  }


  /**
   * <h1>获取时间到日期</h1>
   * 说明：如果参数为null，则返回当天日期
   *
   * @return 格式：2012-01-05 11:12:13
   */
  public static String getDateByDays(Date date) {
    if (date == null) {
      date = new Date();
    }
    return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date);
  }

  /**
   * 字符串日期转换
   *
   * @param dateStr 要转换的日期
   * @param formatFrom 转前格式
   * @param formatTo 转后格式
   */
  public static String dateStrConvert(String dateStr, String formatFrom, String formatTo) {
    if (CheckUtil.isEmpty(dateStr)) {
      return "";
    }
    DateFormat sdf = new SimpleDateFormat(formatFrom);
    Date date = null;
    try {
      date = sdf.parse(dateStr);
    } catch (ParseException e) {
      e.printStackTrace();
    }
    return new SimpleDateFormat(formatTo).format(date);
  }

  /**
   * 获取当前时间
   *
   * @param type 格式,例如：yyyyMMddHHmmss，不传参数默认为：yyyy-MM-dd HH:mm:ss
   */
  public static String getDateTimeStr(String... type) {
    String ss = "yyyy-MM-dd HH:mm:ss";
    if (type.length > 0) {
      ss = type[0];
    }
    return new SimpleDateFormat(ss).format(new Date());
  }

  /**
   * 获取昨天时间
   *
   * @param type 格式,例如：yyyyMMddHHmmss，不传参数默认为：yyyy-MM-dd HH:mm:ss
   */
  public static String getDateTimeYesterDayStr(String... type) {
    String ss = "yyyy-MM-dd HH:mm:ss";
    if (type.length > 0) {
      ss = type[0];
    }
    Calendar cal = Calendar.getInstance();
    cal.add(Calendar.DATE, -1);
    return new SimpleDateFormat(ss).format(cal.getTime());
  }


  /**
   * 字符串转换到日期时间格式
   *
   * @param dateStr 需要转换的字符串
   * @param formatStr 需要格式的目标字符串  举例 yyyy-MM-dd
   * @return Date 返回转换后的时间
   * @throws ParseException 转换异常
   */
  public static Date strToDate(String dateStr, String formatStr) {
    DateFormat sdf = new SimpleDateFormat(formatStr);
    Date date = null;
    try {
      date = sdf.parse(dateStr);
    } catch (ParseException e) {
      e.printStackTrace();
    }
    return date;
  }


  /**
   * 比较两个时间的相差值（d1与d2）
   *
   * @param d1 时间一
   * @param type 类型【d/h/m/s】
   * @return d1 - d2
   */
  public static long compareDate(Date d1, Date d2, char type) {
    long num = d1.getTime() - d2.getTime();
    num /= 1000;
    if ('m' == type) {
      num /= 60;
    } else if ('h' == type) {
      num /= 3600;
    } else if ('d' == type) {
      num /= 3600;
      num /= 24;
    }
    return num;
  }


  /**
   * 根据参照时间获取相差天、小时数的新时间
   *
   * @param date 参照时间
   * @param type 天或小时或分钟[d/h/m]
   * @param num 差值，例如：2表示之后2天或小时，-2表示之前2天或小时
   */
  public static Date getNextDay(Date date, char type, int num) {
    Calendar calendar = Calendar.getInstance();
    calendar.setTime(date);
    switch (type) {
      case 'd':
        calendar.add(Calendar.DAY_OF_MONTH, num);
        break;
      case 'h':
        calendar.add(Calendar.HOUR_OF_DAY, num);
        break;
      case 'm':
        calendar.add(Calendar.MINUTE, num);
        break;
      default:
        break;
    }
    date = calendar.getTime();
    return date;
  }


  /**
   * 获取当前时间戳
   **/
  public static String getTimeStamp() {
    String timeStamp = new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date());
    return timeStamp;
  }


  /**
   * 获得本周第一天时间(yyyy-MM-dd)
   **/
  public static String getMonOfWeek() {
//		Calendar c = new GregorianCalendar();
//		c.setFirstDayOfWeek(Calendar.MONDAY);
//		c.setTime(new Date());
//		c.set(Calendar.DAY_OF_WEEK, c.getFirstDayOfWeek());

    Calendar c = Calendar.getInstance();
    c.set(Calendar.DAY_OF_WEEK, c.getActualMinimum(Calendar.DAY_OF_WEEK));

    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    String str = sdf.format(c.getTime());
    return str;
  }


  /**
   * 获得本周的最后一天(yyyy-MM-dd)
   **/
  public static String getSunOfWeek() {
//		Calendar c = new GregorianCalendar();
//		c.setFirstDayOfWeek(Calendar.MONDAY);
//		c.setTime(new Date());
//		c.set(Calendar.DAY_OF_WEEK, c.getFirstDayOfWeek() + 6);

    Calendar c = Calendar.getInstance();
    c.set(Calendar.DAY_OF_WEEK, c.getActualMaximum(Calendar.DAY_OF_WEEK));

    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    String str = sdf.format(c.getTime());
    return str;
  }

  /**
   * 获得当月的第一天 默认(yyyy-MM-dd)
   *
   * @param formatStr 需要格式的目标字符串  举例 yyyy-MM-dd
   */
  public static String getFirstDayOfMonth(String formatStr) {
    if (CheckUtil.isEmpty(formatStr)) {
      formatStr = "yyyy-MM-dd";
    }
    Calendar cal = Calendar.getInstance();
    cal.set(Calendar.DAY_OF_MONTH, cal.getActualMinimum(Calendar.DAY_OF_MONTH));
    SimpleDateFormat sdf = new SimpleDateFormat(formatStr);
    String str = sdf.format(cal.getTime());
    return str;
  }


  /**
   * 获得当月的第一天(yyyy-MM-dd)
   **/
  public static String getFirstDayOfMonth() {
    Calendar cal = Calendar.getInstance();
    cal.set(Calendar.DAY_OF_MONTH, cal.getActualMinimum(Calendar.DAY_OF_MONTH));
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    String str = sdf.format(cal.getTime());
    return str;
  }

  /**
   * 输入一个月份，获得该月份的第一天
   */
  public static String getParamFirstDayOfMonth(String date) {
    return getDateTimeByStr(getStrByDataTime(date, "yyyy-MM"), "yyyy-MM-dd");
  }

  /**
   * 输入一个月份，获得该月份的最后一天
   */
  public static String getParamLastDayOfMonth(String date) {
    Calendar cal = Calendar.getInstance();
    cal.set(Calendar.YEAR,
        Integer.parseInt(getDateTimeByStr(getStrByDataTime(date, "yyyy-MM-dd"), "yyyy")));
    cal.set(Calendar.MONTH,
        Integer.parseInt(getDateTimeByStr(getStrByDataTime(date, "yyyy-MM-dd"), "MM")) - 1);
    cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
    SimpleDateFormat sdfs = new SimpleDateFormat("yyyy-MM-dd");
    String str = sdfs.format(cal.getTime());
    return str;
  }


  /**
   * 获得当月的最后一天(yyyy-MM-dd)
   **/
  public static String getLastDayOfMonth() {
    Calendar cal = Calendar.getInstance();
    cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
    SimpleDateFormat sdfs = new SimpleDateFormat("yyyy-MM-dd");
    String str = sdfs.format(cal.getTime());
    return str;
  }


  /**
   * 格式化时间，时间转字符串
   *
   * @param date null则为当前系统时间
   * @param format 格式，null则默认为：'yyyy-MM-dd HH:mm:ss'
   * @return 字符串格式的日期
   */
  public static String getDateTimeByStr(Date date, String format) {
    if (date == null) {
      date = new Date();
    }
    if (format == null) {
      format = "yyyy-MM-dd HH:mm:ss";
    }
    return new SimpleDateFormat(format).format(date);
  }


  /**
   * 格式化时间，字符串转时间
   *
   * @param dataStr 需要转换的字符串
   * @return 转换的Date
   */
  public static Date getDateByStr(String dataStr) {

    Date date = null;
    System.out.println(dataStr.length());
    if (!CheckUtil.isEmpty(dataStr)) {
      String format = "yyyy-MM-dd hh:mm:ss";
      if (dataStr.length() <= 10) {
        format = "yyyy-MM-dd";
      } else if (dataStr.length() <= 16) {
        format = "yyyy-MM-dd hh";
      } else if (dataStr.length() <= 18) {
        format = "yyyy-MM-dd hh:mm";
      }
      DateFormat sdf = new SimpleDateFormat(format);
      try {
        date = sdf.parse(dataStr);
        if (date == null) {
          date = new Date();
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
    } else {
      date = new Date();
    }

    return date;
  }


  /**
   * 格式化时间，字符串转时间
   *
   * @param dataStr 需要转换的字符串
   * @param format 格式，null则默认为：'yyyy-MM-dd HH:mm:ss'
   * @return 转换的Date
   */
  public static Date getStrByDataTime(String dataStr, String format) {
    if (dataStr == null) {
      return new Date();
    }
    if (format == null) {
      format = "yyyy-MM-dd HH:mm:ss";
    }
    DateFormat sdf = new SimpleDateFormat(format);
    Date date = null;
    try {
      date = sdf.parse(dataStr);
    } catch (Exception e) {
      e.printStackTrace();
    }
    return date;
  }

  /**
   * 格式化时间，字符串转时间
   *
   * @param dataStr 需要转换的字符串
   * @param format 格式，null则默认为：'yyyy-MM-dd HH:mm:ss'
   * @param tomat 格式，null则默认为：'yyyy-MM-dd HH:mm:ss'
   * @return 转换的Date
   */
  public static String getStrDataTime(String dataStr, String format, String tomat) {
    return getDateTimeByStr(getStrByDataTime(dataStr, format), tomat);
  }

  /**
   * 输入一个日期参数，获得年月 月格式:yyyyMM年
   */
  public static String getYearMonthStr(String time) {
    return getStrDataTime(time, "yyyy-MM-dd", "yyyyMM");
  }


  /******************
   * 获取当前日期前后的日期
   * @param difference  天数
   * @param flag      前后 true.前 false.后
   * @param format    格式，null则默认为：'yyyy-MM-dd HH:mm:ss'
   */
  public static List<String> getAroundDayList(int difference, boolean flag, String format)
      throws Exception {
    List<String> dateList = new ArrayList<>();
    if (flag) {
      for (int i = 0; i < difference; i++) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, -(difference - i));
        String date = DateUtil.getDateTimeByStr(calendar.getTime(), format);
        System.out.println(date);
        dateList.add(date);
      }
    } else {
      Calendar calendar = Calendar.getInstance();
      for (int i = 0; i < difference; i++) {
        calendar.add(Calendar.DAY_OF_MONTH, +1);
        String date = DateUtil.getDateTimeByStr(calendar.getTime(), format);
        System.out.println(date);
        dateList.add(date);
      }
    }
    return dateList;
  }


  /**
   * 两时间相隔月份
   * @param startDate
   * @param endDate
   * @return
   */
  public static Integer getDiffMonth(Date startDate, Date endDate){
    Calendar start = Calendar.getInstance();
    Calendar end = Calendar.getInstance();
    start.setTime(startDate);
    end.setTime(endDate);
    int result = end.get(Calendar.MONTH) - start.get(Calendar.MONTH);
    int month = (end.get(Calendar.YEAR) - start.get(Calendar.YEAR)) * 12;
    return Math.abs(month + result);
  }

//  public static String getDiffHour(Date endDate, Date nowDate) {
//
//    long nd = 1000 * 24 * 60 * 60;
//    long nh = 1000 * 60 * 60;
//    long nm = 1000 * 60;
//    // long ns = 1000;
//    // 获得两个时间的毫秒时间差异
//    long diff = endDate.getTime() - nowDate.getTime();
//    // 计算差多少天
//    long day = diff / nd;
//    // 计算差多少小时
//    long hour = diff % nd / nh;
//    // 计算差多少分钟
//    long min = diff % nd % nh / nm;
//    // 计算差多少秒//输出结果
//    // long sec = diff % nd % nh % nm / ns;
//    return day + "天" + hour + "小时" + min + "分钟";
//  }



  public static long getDiffHour(Date startDate, Date endDate) {

    long nd = 1000 * 24 * 60 * 60;
    long nh = 1000 * 60 * 60;
    long nm = 1000 * 60;
    // long ns = 1000;
    // 获得两个时间的毫秒时间差异
    long diff = endDate.getTime() - startDate.getTime();

    // 计算差多少小时
    long hour = diff  / nh;

    return  hour;
  }

  public static long getDiffHour(String start, String end) {

    Date startDate = getDateByStr(start);
    Date endDate = getDateByStr(end);

    long nd = 1000 * 24 * 60 * 60;
    long nh = 1000 * 60 * 60;
    long nm = 1000 * 60;
    // long ns = 1000;
    // 获得两个时间的毫秒时间差异
    long diff = endDate.getTime() - startDate.getTime();

    // 计算差多少小时
    long hour = diff  / nh;
    if (hour<0){
      hour = -hour;
    }
    return  hour;
  }




//	public static void main(String[] args) {
//		System.out.println("七天前的时间："+getBefore7DaysTime("yyyy-MM-dd"));
//		System.out.println("本月第一天:"+getFirstDayToCurMonth());
//		System.out.println("昨天:"+getYesterDayDate());
//		Date date = new Date();
//		System.out.println("给定日期的前一天:"+getYesterByDate(date));
//		System.out.println("本月第一天(精确到秒):"+getFirstDayForTime());
//		System.out.println("昨天(精确到秒)"+getYesterDayTime());
//
//
//		System.out.println("昨天(精确到秒)"+getBeforeNDaysTime(10,"20130808","yyyy-MM-dd"));
//
//
//		System.out.println(getTime("2013-05-01"));
//
//
//		for(int i=1;i<=15;i++){
//			String value=getDateBefore("2013-09-15",i);
//			value=value.substring(5,7)+"."+value.substring(8,10);
//
//		}
//		System.out.println(getDaysTime("yyyyMMddHHmm",0));
//
//		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//		System.out.println(sdf.format(DateUtil.getDatePlus(new Date(),0,-1,0)));

//    long diffHour = getDiffHour("2020-10-22 1:00:00", "2020-10-23 14:00:00");
//    System.out.println(diffHour);
//
//  }
  public static Date DateMilliSecondToSecond(Date milliSecondDate){
    String millisecond = OldDateUtil.format(milliSecondDate, format_second);
    return strToDate(millisecond,format_second);
  }

  public static Date timeStamp2Date(String time) {
    Long timeLong = Long.parseLong(time);
    SimpleDateFormat sdf = new SimpleDateFormat(format_second);//要转换的时间格式
    Date date;
    try {
      date = sdf.parse(sdf.format(timeLong));
      return date;
    } catch (ParseException e) {
      e.printStackTrace();
      return null;
    }
  }

}
