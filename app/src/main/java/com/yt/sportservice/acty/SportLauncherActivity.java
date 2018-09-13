package com.yt.sportservice.acty;

import android.Manifest;
import android.app.Activity;
import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.graphics.drawable.ClipDrawable;
import android.graphics.drawable.LayerDrawable;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.tbruyelle.rxpermissions.RxPermissions;
import com.yt.sportservice.ISportService;
import com.yt.sportservice.ISportServiceCallback;
import com.yt.sportservice.KApplication;
import com.yt.sportservice.R;
import com.yt.sportservice.SportClient;
import com.yt.sportservice.SportService;
import com.yt.sportservice.constant.MsgContent;
import com.yt.sportservice.entity.StudentInfoEvent;
import com.yt.sportservice.entity.UploadDataEntity;
import com.yt.sportservice.event.BatteryStatus;
import com.yt.sportservice.event.StatusEvent;
import com.yt.sportservice.manager.DBManager;
import com.yt.sportservice.manager.FunctionLocManager;
import com.yt.sportservice.manager.StaticManager;
import com.yt.sportservice.presenter.UploadDataEntityDaoUtils;
import com.yt.sportservice.receiver.BatteryStatusReceiver;
import com.yt.sportservice.service.HeartrateService;
import com.yt.sportservice.service.StepService;
import com.yt.sportservice.step.StepUtils;
import com.yt.sportservice.util.LogUtils;
import com.yt.sportservice.util.MediaManager;
import com.yt.sportservice.util.TimeUtils;
import com.yt.sportservice.util.Utils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Timer;
import java.util.TimerTask;

import rx.functions.Action1;

public class SportLauncherActivity extends Activity  implements View.OnClickListener{

    private static final String TAG = "SportClient";
    ClipDrawable mClipDrawable;
    LayerDrawable mLayerDrawable;
    ImageView mBatteryView;
    BatteryStatusReceiver batteryStatusReceiver;
    TextView mTVState;
    TextView mTVStudentName;
    TextView mTVStudentSN;
    TextView mTVStudentNumber;
    Messenger activityMessenger;
    Messenger serviceMessenger;
    boolean serviceConnected = false;
    RelativeLayout mainLayout;
    boolean isHasPlayWarning = false;
    ImageView imgRingtone;

    private static final long MIN_CLICK_INTERVAL = 600L;
    private long mLastClickTime;
    private int mSecretNumber = 0;
    Timer mTimer ;
    private boolean isTimerRunning = false;
    boolean isDataServiceRunning = false;

    private static final String[] PERMISSIONS = {
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.INTERNET,
            Manifest.permission.CHANGE_WIFI_STATE
    };
    private void getPermission(){
            RxPermissions.getInstance(this)
                    .request(PERMISSIONS).subscribe(new Action1<Boolean>() {
                @Override
                public void call(Boolean granted) {
                    if (granted) {// 已经获取权限
                      LogUtils.e("SPORTCLOUD permission is granted");
                      StaticManager.instance().getDeviceId(SportLauncherActivity.this);
                      DBManager.instance().initDao();
                    } else {
                        // 未获取权限
                    }
                }
            });
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().setStatusBarColor(Color.TRANSPARENT);
        setContentView(R.layout.activity_fise_launcher);
        initBatteryView();
        initTextView();
        EventBus.getDefault().register(this);
        getPermission();
        activityMessenger = new Messenger(mHandler);
    }

    private void initBatteryView() {
        mBatteryView = (ImageView) findViewById(R.id.img_battery);
        batteryStatusReceiver = new BatteryStatusReceiver();
        IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        registerReceiver(batteryStatusReceiver, filter);
    }
    private void initService(){
        isDataServiceRunning = true;
        HeartrateService.startHeartrateService(SportLauncherActivity.this);
        StepService.pull(SportLauncherActivity.this);
    }
    private void stopService(){
        isDataServiceRunning = false;
        HeartrateService.stopHeartrateService(this);
        StepService.stopStepService(this);
        StaticManager.instance().previousClassStep = StaticManager.instance().currentStep;
        LogUtils.e("SPORTCLOUD previousClassStep=" + StaticManager.instance().previousClassStep);
    }
    private void initTextView(){
        mTVState =(TextView) findViewById(R.id.tv_state);
        mTVStudentName =(TextView) findViewById(R.id.tv_student_name);
        mTVStudentNumber =(TextView) findViewById(R.id.tv_student_number);
        mTVStudentSN =(TextView) findViewById(R.id.tv_student_sn);
        imgRingtone = findViewById(R.id.img_ringtone);
        mainLayout = findViewById(R.id.main_layout);
        mainLayout.setOnClickListener(this);
    }
    @Override
    protected void onResume() {
        super.onResume();
        String str = serviceConnected ? "成功":"失败";
        LogUtils.e("SPORTCLOUD 绑定服务 " + str);
        if(!serviceConnected){
            bindSportService();
        }

    }
    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public void onBackPressed() {

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onBatteryChangeEvent(BatteryStatus event) {
        updateBattery(event);
    }
    public void updateBattery(BatteryStatus event) {
        if (null == event) {
            return;
        }
        if(event.plugged == 2){
            mBatteryView.setImageResource(R.drawable.battery_charging);
        }else {
            if (event.level <= 15) {
                mBatteryView.setImageResource(R.drawable.battery_low_15);
            } else {
                mBatteryView.setImageResource(R.drawable.battery);
            }
        }
        StaticManager.instance().currentBatteryLevel = event.level;
        mLayerDrawable = (LayerDrawable) mBatteryView.getDrawable();
        mClipDrawable = (ClipDrawable) mLayerDrawable.findDrawableByLayerId(R.id.clip_drawable);
        mClipDrawable.setLevel((event.level * 100 / event.scale) * 100);
    }

    @Subscribe(threadMode = ThreadMode.MAIN,sticky = true)
    public void onStudentInfoEvent(StudentInfoEvent event)
    {
        EventBus.getDefault().removeStickyEvent(event);
        LogUtils.e("SPORTCLOUD onStudentInfoEvent " + event.toString());
        mTVStudentName .setText(event.studentName);
        mTVStudentName.setVisibility(View.VISIBLE);
        String str = event.studentNumber;
        mTVStudentNumber .setText(str.substring(str.length()-4,str.length()));
        mTVStudentNumber.setVisibility(View.VISIBLE);
        mTVStudentSN .setText(event.studentGroup);
        mTVStudentSN.setVisibility(View.VISIBLE);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onStatusEvent(StatusEvent event)
    {
        LogUtils.e("SPORTCLOUD status =" + event.status);
      updateStatu(event.status);
    }

    private void updateStatu(int status){
        switch (status){
            case 1:
                mainLayout.setBackgroundResource(R.drawable.background);
                mTVState.setText(R.string.text_state_pair);
                if(isDataServiceRunning){
                    stopService();
                }
                FunctionLocManager.instance().stop();
                updateIdleStatus();
                StaticManager.instance().mCurrentUserId="";
                stopCallTimer();
                break;
            case 2:
                mTVState.setText(R.string.text_state_class);
                if(!isDataServiceRunning) {
                    initService();
                }
                FunctionLocManager.instance().start();
                updateUIshowNormal();
                if(!isTimerRunning) {
                    startTimer();
                }
                break;
            case 3:
                mTVState.setText(R.string.text_state_ready);
                updateUIshowNormal();
                if(isDataServiceRunning){
                    stopService();
                }
                FunctionLocManager.instance().stop();
                stopCallTimer();
                break;
            case 4:
                mainLayout.setBackgroundResource(R.drawable.overload);
                updateUIbyWarn();
                if(!isDataServiceRunning) {
                    initService();
                }
                FunctionLocManager.instance().start();
                if(!isTimerRunning) {
                    startTimer();
                }
                mHandler.sendEmptyMessage(MsgContent.MSG_PLAY_WARING_SOUND);
                break;
            case 5:
                mTVState.setText(R.string.text_state_pair_fail);
                updateUIshowDisconnect();
                stopCallTimer();
                break;

        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(batteryStatusReceiver != null) {
            unregisterReceiver(batteryStatusReceiver);
        }
        if(serviceConnected){
            unbindService(connection);
            serviceConnected = false;
        }
        EventBus.getDefault().unregister(this);
    }


    private void bindSportService() {
        Intent serviceIntent = new Intent(this, SportService.class);
        serviceConnected = bindService(serviceIntent,connection, Service.BIND_AUTO_CREATE);
        LogUtils.e("SPORTCLOUD bindSportService=>start  serviceConnected: "+serviceConnected );
    }

    private  ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            serviceMessenger = new Messenger(service);
            Message message = Message.obtain();
            message.what = 1000;
            message.replyTo = activityMessenger;
            try {
                serviceMessenger.send(message);
                LogUtils.e("SPORTCLOUD start initService");
            } catch (RemoteException e) {
                LogUtils.e("SPORTCLOUD bindSportService=>界面和服务建立通信失败: " );
                e.printStackTrace();
            }

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            serviceMessenger = null;
            serviceConnected = false;
        }
    };


     Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case MsgContent.MSG_PLAY_WARING_SOUND:
                    if(!isHasPlayWarning) {
                        MediaManager.findDevice(R.raw.perseus, false);
                        isHasPlayWarning = true;
                    }
                    break;
                case MsgContent.MSG_START_SAVING_DATA:
                    LogUtils.e("SPORTCLOUD 保持数据");
                    SaveUploadData();
                       break;
            }
        }
    };


    private void SaveUploadData(){
        UploadDataEntity uploadDataEntity = new UploadDataEntity();
        uploadDataEntity.setAvgHeartrate(StaticManager.instance().getAvgHeartValue());
        uploadDataEntity.setCalorie(StepUtils.getCalorieByStep(StaticManager.instance().currentStep));
        uploadDataEntity.setDistance(StepUtils.getDistanceByStep(StaticManager.instance().currentStep));
        uploadDataEntity.setHeartrate(String.valueOf(StaticManager.instance().mHeartRateNum));
        uploadDataEntity.setPosition(StaticManager.instance().currentPosition);
        long stepCount = StaticManager.instance().currentStep-StaticManager.instance().previousClassStep;
        if(stepCount>0){
            uploadDataEntity.setStepCounter(String.valueOf(stepCount));
        }else {
            uploadDataEntity.setStepCounter(String.valueOf(StaticManager.instance().currentStep));
        }
        uploadDataEntity.setTime(TimeUtils.millis2String(System.currentTimeMillis()));
        uploadDataEntity.setSleep("12");
        UploadDataEntityDaoUtils.instance().insert(uploadDataEntity);
    }

    private void updateUIbyWarn(){
        mTVState.setVisibility(View.INVISIBLE);
        mTVStudentName .setText(R.string.text_state_pair_warn);
        mTVStudentNumber.setVisibility(View.INVISIBLE);
        imgRingtone.setVisibility(View.INVISIBLE);
        mTVStudentSN.setVisibility(View.INVISIBLE);
    }
    private void updateIdleStatus(){
        mTVStudentName.setVisibility(View.INVISIBLE);
        mTVStudentNumber.setVisibility(View.INVISIBLE);
        mTVStudentSN.setVisibility(View.INVISIBLE);
        imgRingtone.setVisibility(View.INVISIBLE);
    }
    private void updateUIshowNormal(){
        mTVState.setVisibility(View.VISIBLE);
        mTVStudentNumber.setVisibility(View.VISIBLE);
        mTVStudentSN.setVisibility(View.VISIBLE);
        imgRingtone.setVisibility(View.INVISIBLE);
    }
    private void updateUIshowDisconnect(){
        mTVState.setVisibility(View.VISIBLE);
        mTVStudentNumber.setVisibility(View.INVISIBLE);
        mTVStudentSN.setVisibility(View.INVISIBLE);
        imgRingtone.setVisibility(View.VISIBLE);
    }

    @Override
    public void onClick(View v) {
        long now = SystemClock.uptimeMillis();
        if (now - mLastClickTime <= MIN_CLICK_INTERVAL) {
            mSecretNumber += 1;
            if (mSecretNumber > 12) {
                startActivity(new Intent().setClassName("com.android.settings", "com.android.settings.Settings$OtherSettingsActivity"));
            }
        } else {
            mSecretNumber = 0;
        }
        mLastClickTime = now;
    }
      private void startTimer(){
          stopCallTimer();
          isTimerRunning = true;
        if(mTimer == null){
            mTimer = new Timer();
        }
        mTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                mHandler.sendEmptyMessage(MsgContent.MSG_START_SAVING_DATA);
            }
        },0,1000);
      }
    public void stopCallTimer() {
        isTimerRunning= false;
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }
    }

}
