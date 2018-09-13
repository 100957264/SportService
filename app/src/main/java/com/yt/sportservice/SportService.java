package com.yt.sportservice;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import com.yt.sportservice.entity.LocationEntity;
import com.yt.sportservice.entity.LoginResponse;
import com.yt.sportservice.entity.PostDataResponse;
import com.yt.sportservice.entity.UploadDataEntity;
import com.yt.sportservice.event.StatusEvent;
import com.yt.sportservice.manager.StaticManager;
import com.yt.sportservice.presenter.UploadDataEntityDaoUtils;
import com.yt.sportservice.util.JsonUtils;
import com.yt.sportservice.util.LocationUtils;
import com.yt.sportservice.util.LogUtils;
import com.yt.sportservice.util.ResponseUtils;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONException;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class SportService extends Service {

    private static final String TAG = "SportService";

    // 网络请求数据类型
    private static final MediaType MEDIA_TYPE_JSON = MediaType.parse("application/json; charset=utf-8");
    // 注册用户URL
    //private static final String REGISTER_URL = "http://upload.sportsxdata.com/login";//Testing URL
    private static final String REGISTER_URL = "http://up.sportsxdata.com/login";
    // 上传数据URL
   // private static final String UPLOAD_URL = "http://upload.sportsxdata.com/uploadDatas"; //Testing URL
    private static final String UPLOAD_URL = "http://up.sportsxdata.com/uploadDatas";
//    private static final String UPLOAD_URL = "http://rap2api.taobao.org/app/mock/12709/status/2";
    // 上传数据广播Action
    private static final String UPLOAD_ACTION = "com.yuntian.service.upload_data";
    private static final String REGISTRER_ACTION = "com.yuntian.service.register";
    // 上传数据定时器请求代码
    private static final int UPLOAD_REQUEST_CODE = 20;
    // 1秒的毫秒数
    private static final int SECOND = 1000;
    // 网络连接超时时间，单位：秒
    private static final int CONNECT_TIMEOUT = 30;
    //链接失败消息
    private static final int MSG_CONNECT_FAILED = 9;
    // 读取网络数据超时时间，单位：秒
    private static final int READ_TIMEOUT = 30;
    // 注册用户成功消息
    private static final int MSG_REGISTER_SUCCESS = 1;
    // 上传数据消息
    private static final int MSG_UPLOAD_DATA = 2;
    // 重新尝试上传数据消息
    private static final int MSG_RETRY_UPLOAD_DATA = 3;
    // 定位成功消息
    private static final int MSG_LOCATION_SUCCESS = 4;
    // 上传成功消息
    private static final int MSG_UPLOAD_SUCCESS = 6;
    // 上传失败消息
    private static final int MSG_UPLOAD_FAIL = 7;
    // 注册用户失败消息
    private static final int MSG_REGISTER_FAIL =8;
    private int connectCount = 0;
    private int uploadFailedCount = 0;

    // 时间格式化对象
    private SimpleDateFormat mDateFormat;
    // 网络请求客户端
    private OkHttpClient mOkHttpClient;
    // 异步线程
    private HandlerThread mHttpThread;
    // 异步处理Handler
    private Handler mSyncHandler;
    private Messenger mStepService;

    // 用户名称
    private String mUserName;
    // 用户密码
    private String mPassword;
    // 设备ID
    private String mDeviceId;
    //定位结果
    private LocationEntity mLocationEntity;
    private LocationUtils mLocationUtils;

    //注册，上传数据响应
    private ResponseUtils mResponseUtils;

    // 下次上传数据时间
    private int mUploadInterval = 120;
    private int currentStatus = 0;
    private long mStepCounter = 0;
    // 是否注册成功
    private boolean mIsRegister;
    // 当前网络是否可用
    private boolean mIsNetworkActive;
    // 是否因为网络不可用导致需要等待网络可用再上传数据



    private boolean mIsWaitToNetworkUpload;
    private Messenger clientMessenger;


    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            LogUtils.d("SPORTCLOUD handleMessage=>what: " + msg.what);
            switch (msg.what) {
                case 1000:
                    clientMessenger = msg.replyTo;
                    startRegisterUser();
                    break;
                case MSG_REGISTER_SUCCESS:
                    LogUtils.e("SPORTCLOUD MSG_UPLOAD_SUCCESS ....mUploadInterval="+ mUploadInterval);
                    mIsRegister = true;
                    connectCount = 0;
                    if (mUploadInterval >= 0 && mUploadInterval < 8) {
                        mHandler.sendEmptyMessageDelayed(MSG_UPLOAD_DATA, mUploadInterval * SECOND);
                    } else {
                        createUploadTimer(mUploadInterval,UPLOAD_ACTION);
                    }
                    mHandler.removeMessages(MSG_REGISTER_SUCCESS);
                    break;

                case MSG_RETRY_UPLOAD_DATA:
                    mHandler.removeMessages(MSG_RETRY_UPLOAD_DATA);
                    LogUtils.e("SPORTCLOUD 重新上传数据。。。");
                    prepUploadData();
                    break;
                case MSG_UPLOAD_DATA:
                    LogUtils.e("SPORTCLOUD prepUploadData();" +
                            "");
                    prepUploadData();
                    mHandler.removeMessages(MSG_UPLOAD_DATA);
                    break;

                case MSG_LOCATION_SUCCESS:
                    mHandler.removeMessages(MSG_LOCATION_SUCCESS);
                    break;
                case MSG_UPLOAD_FAIL:
                    mHandler.removeMessages(MSG_UPLOAD_FAIL);
                    LogUtils.e("SPORTCLOUD 上传失败，10秒后继续上传...");
                    uploadFailedCount +=1;
                    if(uploadFailedCount <= 10){
                        createUploadTimer(30,UPLOAD_ACTION);
                    }else  if(uploadFailedCount > 10 && uploadFailedCount <= 20){
                        createUploadTimer(120,UPLOAD_ACTION);
                    }else if(uploadFailedCount>20 && uploadFailedCount<= 30){
                        createUploadTimer(300,UPLOAD_ACTION);
                    }else {
                        createUploadTimer(600,UPLOAD_ACTION);
                    }
                    break;
                case MSG_UPLOAD_SUCCESS:
                    mHandler.removeMessages(MSG_UPLOAD_SUCCESS);
                    uploadFailedCount = 0;
                    LogUtils.e("MSG_UPLOAD_SUCCESS ....mUploadInterval="+ mUploadInterval);
                    if (mUploadInterval >= 0 && mUploadInterval < 15) {
                        mHandler.sendEmptyMessageDelayed(MSG_UPLOAD_DATA, 10 * SECOND);
                    } else {
                        createUploadTimer(mUploadInterval,UPLOAD_ACTION);
                    }
                    break;
                case MSG_REGISTER_FAIL:
                    connectCount = connectCount + 1;
                    if(uploadFailedCount <= 10){
                        createUploadTimer(30,REGISTRER_ACTION);
                    }else  if(uploadFailedCount > 10 && uploadFailedCount <= 20){
                        createUploadTimer(120,REGISTRER_ACTION);
                    }else if(uploadFailedCount>20 && uploadFailedCount<= 30){
                        createUploadTimer(300,REGISTRER_ACTION);
                    }else {
                        createUploadTimer(600,REGISTRER_ACTION);
                    }
                    break;
                case MSG_CONNECT_FAILED:


                    break;
            }
        }
    };


    //创建和界面通信
    private Messenger serviceMessenger = new Messenger(mHandler);

    private void sendMessageToAty(int what){
        try {
            Message msg = Message.obtain();
            msg.what = what;
            clientMessenger.send(msg);
        } catch (RemoteException e) {
            LogUtils.d("SPORTCLOUD sendMessageToAty...");
            e.printStackTrace();
        }
    }


    @Override
    public IBinder onBind(Intent intent) {
        return serviceMessenger.getBinder();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        LogUtils.d("SPORTCLOUD onCreate...");
        // 初始化定位客户端
        // 注册定位监听器
        mLocationUtils = new LocationUtils(new LocationListen());
        mLocationUtils.initLocationClient(SportService.this);
        mLocationUtils.registerLocationListener();
        //上传数据响应类
        mResponseUtils =new ResponseUtils(new ResponseListen());
        // 时间格式化对象初始化
        mDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        // 网络连接对象初始化
        mOkHttpClient = new OkHttpClient().newBuilder()
                .connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)
                .readTimeout(READ_TIMEOUT, TimeUnit.SECONDS).build();
        // 异步线程初始化
        mHttpThread = new HandlerThread("http-requst");
        mHttpThread.start();
        mSyncHandler = new Handler(mHttpThread.getLooper());
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(UPLOAD_ACTION);
        intentFilter.addAction(REGISTRER_ACTION);
        registerReceiver(mUploadReceiver,intentFilter);
        updateNetworkActiveState();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        LogUtils.d("SPORTCLOUD onStartCommand...");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        LogUtils.d("SPORTCLOUD onDestroy...");
        if(mUploadReceiver != null) {
            unregisterReceiver(mUploadReceiver);
        }
        mHttpThread.quit();
        super.onDestroy();
    }

    // 注册用户
    private void startRegisterUser() {
        LogUtils.e("SPORTCLOUD 注册用户----开始获取设备ID...");
        if (mDeviceId == null) {
            mDeviceId = StaticManager.IMEI;
            if (mDeviceId == null) {
                StaticManager.instance().getDeviceId(this);
                mHandler.sendEmptyMessage(MSG_REGISTER_FAIL);
                return;
            }
        }
        LogUtils.e("SPORTCLOUD 注册用户----当前设备ID: " + mDeviceId);

        try {
            final String data = JsonUtils.instance().createRegisterJsonData("device_yt","device_yt");
            LogUtils.e("SPORTCLOUD 注册用户----=>jsonData: \n" + data);
            mSyncHandler.post(new Runnable() {
                @Override
                public void run() {
                    try {
                        mResponseUtils.setResponseType(ResponseUtils.TYPE_REG);
                        httpPostRequest(REGISTER_URL, data, mResponseUtils);
                    } catch (UnsupportedEncodingException e) {
                    }
                }
            });
        } catch (JSONException e) {
            LogUtils.e("SPORTCLOUD 注册用户----=>error: ", e);
        } catch (UnsupportedEncodingException | NoSuchAlgorithmException e) {
            LogUtils.e("SPORTCLOUD 注册用户----=>error: ", e);
        }
    }

    // 准备上传数据
    private void prepUploadData() {
        LogUtils.e("SPORTCLOUD prepUploadData=>准备上传数据...");
        if (mIsNetworkActive) {
            mIsWaitToNetworkUpload = false;
            LogUtils.e("SPORTCLOUD prepUploadData=>开始获取设备ID...");
            if (mDeviceId == null) {
                mDeviceId = StaticManager.IMEI;
                if (mDeviceId == null) {
                    LogUtils.e("SPORTCLOUD prepUploadData=>获取设备ID失败");
                    mHandler.sendEmptyMessage(MSG_UPLOAD_FAIL);
                    return;
                }
            }
              LogUtils.e("SPORTCLOUD prepUploadData=>当前设备ID: " + mDeviceId);
              LogUtils.e("SPORTCLOUD prepUploadData=>开始更新计步信息...");
              if(mIsRegister) {
                  startUploadData();
              }else{
                  startRegisterUser();
              }
        } else {
            LogUtils.e("SPORTCLOUD prepUploadData=>网络不可用");
            mIsWaitToNetworkUpload = true;
            updateNetworkActiveState();
            //mHandler.sendEmptyMessage(MSG_UPLOAD_FAIL);
        }
    }




    // 开始上传数据
    private void startUploadData() {
        LogUtils.e("SPORTCLOUD startUploadData=>开始上传数据....");
        try {
            final String jsonData = JsonUtils.instance().createClassUploadDataJsonData();
            mSyncHandler.post(new Runnable() {
                @Override
                public void run() {
                    try {
                        LogUtils.e("SPORTCLOUD startUploadData=>jsonData: " + jsonData);
                        mResponseUtils.setResponseType(ResponseUtils.TYPE_POST_DATA);
                        httpPostRequest(UPLOAD_URL, jsonData, mResponseUtils);
                    } catch (UnsupportedEncodingException e) {
                        LogUtils.e("SPORTCLOUD startUploadData=>error: ", e);
                        mHandler.sendEmptyMessage(MSG_UPLOAD_FAIL);
                    }
                }
            });
        } catch (JSONException e) {
            LogUtils.e("SPORTCLOUD startUploadData=>error: ", e);
            mHandler.sendEmptyMessage(MSG_UPLOAD_FAIL);
        }
    }

    // 获取当前时间字符串
    private String getCurrentDateString() {
        Calendar c = Calendar.getInstance();
        return mDateFormat.format(c.getTime());
    }

    // 更新网络状态
    private void updateNetworkActiveState() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = cm.getActiveNetworkInfo();
        if (activeNetworkInfo != null && activeNetworkInfo.isConnected() && activeNetworkInfo.isAvailable()) {
            LogUtils.d("SPORTCLOUD 网络已经可用");
            mIsNetworkActive = true;
            if (mIsWaitToNetworkUpload) {
                prepUploadData();
            }
        } else {
            LogUtils.d("SPORTCLOUD 网络还是不可用");
            mIsNetworkActive = false;
        }
    }

    // 创建上传数据定时器
    private void createUploadTimer(int interval,String action) {
        AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
        Intent upload = new Intent(action);
        PendingIntent uploadIntent = PendingIntent.getBroadcast(this, UPLOAD_REQUEST_CODE, upload, PendingIntent.FLAG_UPDATE_CURRENT);
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.SECOND, interval);
        am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), uploadIntent);
        LogUtils.e("SPORTCLOUD send UPLOAD_ACTION  end interval=" + interval);
    }

    // 异步post网络请求
    public void httpPostRequest(String url, String jsonData, final RequestCallback callback) throws UnsupportedEncodingException {
        RequestBody body = RequestBody.create(MEDIA_TYPE_JSON, jsonData);
        final Request request = new Request.Builder().url(url).post(body).build();
        final Call call = mOkHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(final Call call, final IOException e) {
                LogUtils.d("SPORTCLOUD onFailure=>error: ", e);
                mHandler.sendEmptyMessage(MSG_CONNECT_FAILED);
                callback.onFailure(call, e);
            }

            @Override
            public void onResponse(final Call call, final Response response) throws IOException {
                LogUtils.d("SPORTCLOUD onResponse=>success: " + response.isSuccessful());
                callback.onResponseStr(call, response);
            }
        });
    }




    private BroadcastReceiver mUploadReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            LogUtils.d("SPORTCLOUD onReceive=>action: " + action);
            if (UPLOAD_ACTION.equals(action)) {
                prepUploadData();
            } else if(REGISTRER_ACTION.equals(action)){
                startRegisterUser();
            }else if (ConnectivityManager.CONNECTIVITY_ACTION.equals(action)) {
                updateNetworkActiveState();
                LogUtils.e("SPORTCLOUD ConnectivityManager.CONNECTIVITY_ACTION.equals(action)");
            }
        }
    };


    //定位结果
    private class LocationListen implements LocationUtils.LocationResultListen{
        @Override
        public void locationResult(boolean result, LocationEntity le) {
           if(result){
               mLocationEntity = le;
           }
        }
    }

   //上传数据结果
    private class ResponseListen implements ResponseUtils.ResponseResultListen{

        @Override
        public void responseSuccess(int status,Object object) {
            LogUtils.e("SPORTCLOUD responseSuccess(int status,Object object)");
            if(status == 0){
                LogUtils.e("SPORTCLOUD 注册用户成功");
                LoginResponse loginResponse = (LoginResponse)object;
                mUploadInterval = loginResponse.interval;
                mIsRegister = true;
                mHandler.sendEmptyMessage(MSG_REGISTER_SUCCESS);
            }else{
                mHandler.sendEmptyMessage(MSG_UPLOAD_SUCCESS);
                PostDataResponse postDataResponse =(PostDataResponse) object;
                mUploadInterval = postDataResponse.interval;
                currentStatus =status;
                LogUtils.e("SPORTCLOUD mUploadInterval =" + mUploadInterval);
                List<UploadDataEntity> list =UploadDataEntityDaoUtils.instance().selectAll();
                if( list!= null && list.size()>0) {
                    UploadDataEntityDaoUtils.instance().deleteAll();//上传成功删除传完的数据
                }
            }
        }

        @Override
        public void responseFailure(int status,String error) {
           if(mResponseUtils.getesponseType() == ResponseUtils.TYPE_REG){
               LogUtils.e("SPORTCLOUD 注册用户失败");
               mIsRegister = false;
               mHandler.sendEmptyMessageDelayed(MSG_REGISTER_FAIL,10000);
           }else{
               mHandler.sendEmptyMessage(MSG_UPLOAD_FAIL);
               LogUtils.e("SPORTCLOUD responseFailure。。。。上传失败消息");
               StaticManager.instance().uploadCount+=1;
               if(StaticManager.instance().uploadCount == 10){
                   StaticManager.instance().uploadCount = 0;
                   StatusEvent statusEvent = new StatusEvent(5);
                   EventBus.getDefault().post(statusEvent);
               }
           }
        }
    }



    public interface RequestCallback {
        void onFailure(Call call, IOException e);

        void onResponseStr(Call call, Response response);
    }

}
