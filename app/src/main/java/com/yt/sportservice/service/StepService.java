package com.yt.sportservice.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.annotation.Nullable;


import com.yt.sportservice.entity.StepHistory;
import com.yt.sportservice.manager.StaticManager;
import com.yt.sportservice.step.Constant;
import com.yt.sportservice.step.StepHistoryDaoUtils;
import com.yt.sportservice.step.StepTimeUtil;
import com.yt.sportservice.util.LogUtils;

import java.util.Date;

/**
 * @author mare
 * @Description:TODO 计步服务
 * @csdnblog http://blog.csdn.net/mare_blue
 * @date 2018/1/18
 * @time 17:31
 */
public class StepService extends Service implements SensorEventListener {
    public static final String TAG = "StepService";

    //当前日期
    private static String CURRENT_DATE;
    //当前步数
    private long CURRENT_STEP;
    //3秒进行一次存储
    private static int saveDuration = 3000;
    //传感器
    private SensorManager sensorManager;
    //计步传感器类型 0-counter 1-detector
    private static int stepSensor = -1;
    //广播接收
    private BroadcastReceiver mInfoReceiver;
    //自定义简易计时器
    private TimeCount timeCount;
    //发送消息，用来和Service之间传递步数
    private Messenger messenger = new Messenger(new MessengerHandler());
    //是否有当天的记录
    private boolean hasRecord;
    //未记录之前的步数
    private int hasStepCount;
    //下次记录之前的步数
    private int previousStepCount;

    public static void pull(Context context) {
        context.startService(new Intent(context, StepService.class));
    }
    public static void stopStepService(Context context) {
        context.stopService(new Intent(context, StepService.class));
    }

    @Override
    public void onCreate() {
        super.onCreate();
        initBroadcastReceiver();
        new Thread(new Runnable() {
            public void run() {
                getStepDetector();
            }
        }).start();
        startTimeCount();
        initTodayData();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return messenger.getBinder();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    /**
     * 自定义handler
     */
    private class MessengerHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case Constant.MSG_FROM_CLIENT:
                    try {
                        //这里负责将当前的步数发送出去，可以在界面或者其他地方获取，我这里是在MainActivity中获取来更新界面
                        Messenger messenger = msg.replyTo;
                        Message replyMsg = Message.obtain(null, Constant.MSG_FROM_SERVER);
                        Bundle bundle = new Bundle();
                        bundle.putLong("steps", CURRENT_STEP);
                        replyMsg.setData(bundle);
                        messenger.send(replyMsg);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }


    /**
     * 初始化广播
     */
    private void initBroadcastReceiver() {
        final IntentFilter filter = new IntentFilter();
        // 屏幕灭屏广播
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        //关机广播
        filter.addAction(Intent.ACTION_SHUTDOWN);
        // 屏幕解锁广播
        filter.addAction(Intent.ACTION_USER_PRESENT);
        // 当长按电源键弹出“关机”对话或者锁屏时系统会发出这个广播
        // example：有时候会用到系统对话框，权限可能很高，会覆盖在锁屏界面或者“关机”对话框之上，
        // 所以监听这个广播，当收到时就隐藏自己的对话，如点击pad右下角部分弹出的对话框
        filter.addAction(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
        //监听日期变化
        filter.addAction(Intent.ACTION_DATE_CHANGED);
        filter.addAction(Intent.ACTION_TIME_CHANGED);
        filter.addAction(Intent.ACTION_TIME_TICK);

        mInfoReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                switch (action) {
                    // 屏幕灭屏广播
                    case Intent.ACTION_SCREEN_OFF:
                        //屏幕熄灭改为10秒一存储
                        saveDuration = 10000;
                        break;
                    //关机广播，保存好当前数据
                    case Intent.ACTION_SHUTDOWN:
                        saveStepData();
                        break;
                    // 屏幕解锁广播
                    case Intent.ACTION_USER_PRESENT:
                        saveDuration = 3000;
                        break;
                    // 当长按电源键弹出“关机”对话或者锁屏时系统会发出这个广播
                    // example：有时候会用到系统对话框，权限可能很高，会覆盖在锁屏界面或者“关机”对话框之上，
                    // 所以监听这个广播，当收到时就隐藏自己的对话，如点击pad右下角部分弹出的对话框
                    case Intent.ACTION_CLOSE_SYSTEM_DIALOGS:
                        saveStepData();
                        break;
                    //监听日期变化
                    case Intent.ACTION_DATE_CHANGED:
                    case Intent.ACTION_TIME_CHANGED:
                    case Intent.ACTION_TIME_TICK:
                        saveStepData();
                        isNewDay();
                        break;
                    default:
                        break;
                }
            }
        };
        //注册广播
        registerReceiver(mInfoReceiver, filter);
    }

    /**
     * 初始化当天数据
     */
    private void initTodayData() {
        //获取当前时间
        CURRENT_DATE = StepTimeUtil.getCurrentDate();
        StepHistoryDaoUtils.instance().queryLatestHistory();
        //获取当天的数据，用于展示
        StepHistory entity = StepHistoryDaoUtils.instance().queryLatestHistoryByDate(CURRENT_DATE);
        //为空则说明还没有该天的数据，有则说明已经开始当天的计步了
        if (entity == null) {
            CURRENT_STEP = 0;
        } else {
            CURRENT_STEP = entity.getStep_count();
        }
    }

    /**
     * 监听晚上0点变化初始化数据
     */
    private void isNewDay() {
        String time = "0000";
        if (time.equals(StepTimeUtil.geTimeFormat().format(new Date())) ||
                !CURRENT_DATE.equals(StepTimeUtil.getCurrentDate())) {
            initTodayData();
        }
    }


    /**
     * 获取传感器实例
     */
    private void getStepDetector() {
        if (sensorManager != null) {
            sensorManager = null;
        }
        // 获取传感器管理器的实例
        sensorManager = (SensorManager) this
                .getSystemService(SENSOR_SERVICE);
        //android4.4以后可以使用计步传感器
        int VERSION_CODES = Build.VERSION.SDK_INT;
        if (VERSION_CODES >= 19) {
            addCountStepListener();
        }
    }

    /**
     * 添加传感器监听
     */
    private void addCountStepListener() {
        Sensor countSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        Sensor detectorSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);
        if (countSensor != null) {
            stepSensor = 0;
            sensorManager.registerListener(StepService.this, countSensor, SensorManager.SENSOR_DELAY_NORMAL);
        } else if (detectorSensor != null) {
            stepSensor = 1;
            sensorManager.registerListener(StepService.this, detectorSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    /**
     * 由传感器记录当前用户运动步数，注意：该传感器只在4.4及以后才有，并且该传感器记录的数据是从设备开机以后不断累加，
     * 只有当用户关机以后，该数据才会清空，所以需要做数据保护
     *
     * @param event
     */
    @Override
    public void onSensorChanged(SensorEvent event) {
        if (stepSensor == 0) {
            int tempStep = (int) event.values[0];
            if (!hasRecord) {
                hasRecord = true;
                hasStepCount = tempStep;
            } else {
                int thisStepCount = tempStep - hasStepCount;
                CURRENT_STEP += (thisStepCount - previousStepCount);
                previousStepCount = thisStepCount;
                StaticManager.instance().currentStep = CURRENT_STEP;
            }
        } else if (stepSensor == 1) {
            if (event.values[0] == 1.0) {
                CURRENT_STEP++;
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }


    /**
     * 开始倒计时，去存储步数到数据库中
     */
    private void startTimeCount() {
        timeCount = new TimeCount(saveDuration, 1000);
        timeCount.start();
    }


    private class TimeCount extends CountDownTimer {
        /**
         * @param millisInFuture    The number of millis in the future from the call
         *                          to {@link #start()} until the countdown is done and {@link #onFinish()}
         *                          is called.
         * @param countDownInterval The interval along the way to receive
         *                          {@link #onTick(long)} callbacks.
         */
        public TimeCount(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onTick(long millisUntilFinished) {

        }

        @Override
        public void onFinish() {
            // 如果计时器正常结束，则每隔三秒存储步数到数据库
            timeCount.cancel();
            saveStepData();
            startTimeCount();
        }
    }

    /**
     * 保存当天的数据到数据库中，并去刷新通知栏
     */
    private void saveStepData() {
        //查询数据库中的数据
        StepHistory entity = StepHistoryDaoUtils.instance().queryLatestHistoryByDate(CURRENT_DATE);
        //为空则说明还没有该天的数据，有则说明已经开始当天的计步了
        long previousStep = -1;
        if (entity == null) {
            //没有则新建一条数据
        } else {
            //有则更新当前的数据
            previousStep = entity.getStep_count();
        }
        if (CURRENT_STEP >= previousStep) {
            return;//跳过不断存值
        }else {
           CURRENT_STEP = previousStep + CURRENT_STEP;
        }
        StepHistoryDaoUtils.instance().update(new StepHistory(CURRENT_STEP,System.currentTimeMillis()));
        LogUtils.e("StepService CURRENT_STEP=" + CURRENT_STEP + ",previousStep =" + previousStep);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //主界面中需要手动调用stop方法service才会结束
        stopForeground(true);
        unregisterReceiver(mInfoReceiver);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }
}
