package com.yt.sportservice.manager;


import android.content.Context;
import android.telephony.TelephonyManager;

import com.yt.sportservice.KApplication;
import com.yt.sportservice.util.LogUtils;

/**
 * @author mare
 * @Description:TODO
 * @csdnblog http://blog.csdn.net/mare_blue
 * @date 2017/9/4 0004
 * @time 00:55
 */
public class StaticManager {
    private StaticManager() {
    }

    private static class SingletonHolder {
        private static final StaticManager INSTANCE = new StaticManager();
    }

    public static StaticManager instance() {
        return SingletonHolder.INSTANCE;
    }


    public int mHeartRateNum = 0;
    public int mHeartRateCount = 0;
    public int mPreviousValue = 0;
    private int avgHeartValue = 0;
    public int mHeartSBPNum  = 0;
    public int mHeartDBPNum  = 0;
    public int currentBatteryLevel = 0;
    public long currentStep = 0;
    public long previousClassStep = 0;
    public String currentPosition="0.0,0.0";
    public static String IMEI = "";
    public int uploadCount = 0;
    public String mCurrentUserId = "";
    // 获取设备ID
    public void getDeviceId(Context context) {
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        IMEI = telephonyManager.getDeviceId();
    }
    public String getAvgHeartValue(){
        if(mHeartRateCount == 0){
            avgHeartValue = mHeartRateNum;
            mPreviousValue= mHeartRateNum;
        }else {
            avgHeartValue = (mHeartRateNum + mPreviousValue * (mHeartRateCount - 1)) / (mHeartRateCount);
            LogUtils.d("SPORTCLOUD mHeartRateCount =" + mHeartRateCount + ",mPreviousValue =" + mPreviousValue);
            mPreviousValue = avgHeartValue;
        }

        return String.valueOf(avgHeartValue);
    }
}
