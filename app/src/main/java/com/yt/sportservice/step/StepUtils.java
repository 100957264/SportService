package com.yt.sportservice.step;

import android.text.TextUtils;

import com.yt.sportservice.util.LogUtils;
import com.yt.sportservice.util.TimeUtils;

import java.util.Arrays;
import java.util.List;

/**
 * @author mare
 * @Description:TODO
 * @csdnblog http://blog.csdn.net/mare_blue
 * @date 2017/10/24
 * @time 16:51
 */
public class StepUtils {

    private static double sportK = 0.8214;
    private static double sportKg = 30.0;//kg
    private static double sportKm = 0.6/1000;
    public static String getCalorieByStep(long step){
        double calore = sportKg * sportK *sportKm*(double)step;
        return String.valueOf(calore);
    }
    public static String getDistanceByStep(long step){
        double distance = (double) step*sportKm;
        return String.valueOf(distance);
    }
    /**
     * 解析计步时间段
     *
     * @param content
     */
    public static void parseStepWalkingTime(String content) {
        LogUtils.e("parseStepWalkingTime " + content);
        if (TextUtils.isEmpty(content)) {
            return;
        }
        String[] walkingTimes = TextUtils.split(content, ",");
        LogUtils.d("parseStepWalkingTime " + Arrays.toString(walkingTimes));
        if (walkingTimes == null || walkingTimes.length != 3) {
            return;
        }
        List<String> times = Arrays.asList(walkingTimes);
    }

    /**
     * 解析翻转检测时间段设置
     *
     * @param content 收到来自服务器的消息内容
     */
    public static void parseTurnOverTimes(String content) {
        LogUtils.e("parseTurnOverTimes " + content);
        if (TextUtils.isEmpty(content)) {
            return;
        }
        String[] turnOverTimes = TextUtils.split(content, ",");
        LogUtils.d("parseTurnOverTimes " + Arrays.toString(turnOverTimes));
        if (turnOverTimes == null || turnOverTimes.length != 1) {
            return;
        }
        List<String> times = Arrays.asList(turnOverTimes);
    }
    /**
     * 获取当地时间
     *
     * @param time System.currentTimeMillis()
     * @return 比如 120414， 表示 2012 年 4 月 14 日
     */
    public static String long2Date(long time) {
        return TimeUtils.millis2String(time, StepTimeUtil.getDateFormat());
    }

    public static String long2Time(long time) {
        return TimeUtils.millis2String(time, StepTimeUtil.geTimeFormat());
    }


}
