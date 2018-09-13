package com.yt.sportservice.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.support.annotation.Nullable;


import com.yt.sportservice.constant.HeartrateConstant;
import com.yt.sportservice.manager.StaticManager;
import com.yt.sportservice.util.LogUtils;


/**
 * @author mare
 * @Description:TODO 心率服务
 * @csdnblog http://blog.csdn.net/mare_blue
 * @date 2018/1/18
 * @time 17:31
 */
public class HeartrateService extends Service implements SensorEventListener {
    public static final String TAG = "HeartrateService";

    //传感器
    public SensorManager mSensorManager;
    public Sensor mSensor;
    //    public  LinkedList<Integer>  mTemplinkedList;
    int mValueType = 0;
    public boolean isHasHeartValue = false;
    public boolean isHasSBPValue = false;
    public boolean isHasDBPValue = false;


    public static void startHeartrateService(Context context) {
        context.startService(new Intent(context, HeartrateService.class));
    }

    public static void stopHeartrateService(Context context) {
        context.stopService(new Intent(context, HeartrateService.class));
    }

    @Override
    public void onCreate() {
        super.onCreate();
        initData();
        registerSensorListener();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    /**
     * 心率数据监听
     *
     * @param event
     */
    @Override
    public void onSensorChanged(SensorEvent event) {
        float[] values = event.values;
        checkValueTypeAndSetResult(values[0]);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    private void initData() {
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
    }

    private void registerSensorListener() {
        if (null != mSensor) {
            mSensorManager.registerListener(this, mSensor,
                    SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    private void unRegisterSensorListener() {
        if (null != mSensor) {
            mSensorManager.unregisterListener(this, mSensor);
        }
    }
    //  因为同用一个接口，所以心率、血压乘以不同倍数（心率10倍、舒张压100倍、收缩压1000倍）以不同区间来区分心率和血压数据，应用层需除去这些倍数
    private void checkValueTypeAndSetResult(float value) {
        if (200f < value) {
            if (value < 2000f) {
                mValueType = HeartrateConstant.TYPE_HEARTRATE;
                isHasHeartValue = true;
                int currentHeart = (int) value / 10;
                StaticManager.instance().mHeartRateNum = currentHeart;
                String avgHeart =StaticManager.instance().getAvgHeartValue();
                StaticManager.instance().mHeartRateCount+=1;
                LogUtils.d("SPORTCLOUD HEART mHeartRateNum=" + currentHeart + ",avgHeart=" + avgHeart + " ,mHeartRateCount=" +  StaticManager.instance().mHeartRateCount);
            } else if (value < 15000f) {
                mValueType = HeartrateConstant.TYPE_DBP;
                isHasDBPValue = true;
                StaticManager.instance().mHeartDBPNum = (int) value / 100;
            } else if (value < 60000f) {
                return;//空区间
            } else if (value < 360000f) {//  血压的最高值目前还没有一个确切的记录，但是超过舒张压超过300mmHg的，确有不少，尤其是在手术室中，326mmHg也遇到过，所以立式水银柱式血压计的最高计数可以做到360mmHg，这个数值倒是罕有人测到。
                mValueType = HeartrateConstant.TYPE_SBP;
                isHasSBPValue = true;
                StaticManager.instance().mHeartSBPNum = (int) value / 1000;
            } else if (value == 1000000f) {
                mValueType = HeartrateConstant.TYPE_LTH;
            } else {
                return;
            }
            LogUtils.d("value =" + value + ",mValueType ==" + mValueType);

        } else {
            return;
        }
    }





    @Override
    public void onDestroy() {
        super.onDestroy();
        unRegisterSensorListener();
    }
}
