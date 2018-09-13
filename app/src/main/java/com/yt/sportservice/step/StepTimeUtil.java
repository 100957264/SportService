package com.yt.sportservice.step;


import android.content.Context;


import com.yt.sportservice.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * 时间工具类
 */

public class StepTimeUtil {
    private static SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyMMdd HHmm", Locale.getDefault());
    private static SimpleDateFormat dateFormat = new SimpleDateFormat("yyMMdd", Locale.getDefault());
    private static SimpleDateFormat timeFormat = new SimpleDateFormat("HHmm", Locale.getDefault());
    private static int[] weekStrings = new int[]{R.string.sun, R.string.one, R.string.two, R.string.three, R.string.four, R.string.five, R.string.six};
    private static int[] rWeekStrings = new int[]{R.string.sunday, R.string.monday, R.string.tuesday, R.string.wednesday, R.string.thursday, R.string.friday, R.string.saturday};

    public static SimpleDateFormat getDateTimeFormat() {
        return dateTimeFormat;
    }

    public static SimpleDateFormat getDateFormat() {
        return dateFormat;
    }

    public static SimpleDateFormat geTimeFormat() {
        return timeFormat;
    }

    /**
     * @return
     */
    public static String formatDateTime(String date, String time) {
        return date + " " + time;
    }

    /**
     * 返回当前的时间
     *
     * @return 今天 0948
     */
    public static String getCurTime(Context context) {
        String time = context.getString(R.string.today) + timeFormat.format(System.currentTimeMillis());
        return time;
    }

    /**
     * 获取运动记录是周几，今天则返回具体时间，其他则返回具体周几
     */
    public static String getWeekStr(Context context, String dateStr) {

        String todayStr = dateFormat.format(Calendar.getInstance().getTime());

        if (todayStr.equals(dateStr)) {
            return getCurTime(context);
        }

        Calendar preCalendar = Calendar.getInstance();
        preCalendar.add(Calendar.DATE, -1);
        String yesterdayStr = dateFormat.format(preCalendar.getTime());
        if (yesterdayStr.equals(dateStr)) {
            return context.getString(R.string.yesterday);
        }

        int w = 0;
        try {
            Date date = dateFormat.parse(dateStr);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            w = calendar.get(Calendar.DAY_OF_WEEK) - 1;
            if (w < 0) {
                w = 0;
            }

        } catch (ParseException e) {
            e.printStackTrace();
        }
        return context.getString(rWeekStrings[w]);
    }


    /**
     * 获取是几号
     *
     * @return dd
     */
    public static int getCurrentDay() {
        return Calendar.getInstance().get(Calendar.DATE);
    }

    /**
     * 获取当前的日期
     *
     * @return yyMMdd
     */
    public static String getCurrentDate() {
        String currentDateStr = dateFormat.format(Calendar.getInstance().getTime());
        return currentDateStr;
    }


    /**
     * 根据date列表获取day列表
     *
     * @param dateList
     * @return
     */
    public static List<Integer> dateListToDayList(List<String> dateList) {
        Calendar calendar = Calendar.getInstance();
        List<Integer> dayList = new ArrayList<>();
        for (String date : dateList) {
            try {
                calendar.setTime(dateFormat.parse(date));
                int day = calendar.get(Calendar.DATE);
                dayList.add(day);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        return dayList;
    }


    /**
     * 根据当前日期获取以含当天的前一周日期
     *
     * @return [2017年02月21日, 2017年02月22日, 2017年02月23日, 2017年02月24日, 2017年02月25日, 2017年02月26日, 2017年02月27日]
     */
    public static List<String> getBeforeDateListByNow() {
        List<String> weekList = new ArrayList<>();

        for (int i = -6; i <= 0; i++) {
            //以周日为一周的第一天
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.DATE, i);
            String date = dateFormat.format(calendar.getTime());
            weekList.add(date);
        }
        return weekList;
    }

    /**
     * 判断当前日期是周几
     *
     * @param curDate
     * @return
     */
    public static String getCurWeekDay(Context context, String curDate) {
        int w = 0;
        try {
            Date date = dateFormat.parse(curDate);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            w = calendar.get(Calendar.DAY_OF_WEEK) - 1;
            if (w < 0) {
                w = 0;
            }

        } catch (ParseException e) {
            e.printStackTrace();
        }
        return context.getString(weekStrings[w]);
    }
}
