package com.yt.sportservice.manager;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.telephony.TelephonyManager;


import com.yt.sportservice.gps.GpsErrorType;
import com.yt.sportservice.gps.GpsLocationInfo;
import com.yt.sportservice.gps.GpsManager;
import com.yt.sportservice.gps.GpsRespondListener;
import com.yt.sportservice.service.FunctionLocService;
import com.yt.sportservice.util.GpsCorrectUtil;
import com.yt.sportservice.util.LogUtils;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

/**
 * @author mare
 * @Description TODO: 负责开启结束定位
 * @csdnblog http://blog.csdn.net/mare_blue
 * @date 2017/9/2
 * @time 17:03
 */
public class FunctionLocManager implements GpsRespondListener {


    private FunctionLocManager() {
    }

    private static class SingletonHolder {
        private static final FunctionLocManager INSTANCE = new FunctionLocManager();
    }

    public static FunctionLocManager instance() {
        return SingletonHolder.INSTANCE;
    }


    public void start() {
        //TODO GPS Listener
        GpsManager.instance().requestLocation(this);
    }
    public void stop() {
        //TODO GPS Listener
        GpsManager.instance().stop();
//        FunctionLocService.pull();
    }
    @Override
    public void toLocateFailure(GpsErrorType errorType) {
        //TODO 发送WG指令让服务器获取经纬度
        GpsLocationInfo result = null;
        boolean shouldUpload = false;
        switch (errorType) {
            case LOCATE_TIMEOUT:
                shouldUpload = true;//上传wifi 基站
                result = new GpsLocationInfo();
                break;
            case LOCATE_TEMPORARILY_UNAVAILABLE://暂时获取不到信号(信号偏弱 没达到8个卫星)
                shouldUpload = false;//暂时不上传 继续搜星
                break;
            case LOCATE_DISABLED:
            case LOCATE_OUT_OF_SERVICE:
            case LOCATE_ERROR:
            default:
                break;
        }
        if (shouldUpload) {
            onLocateSuccess(result);
        }
    }

    @Override
    public void onLocateSuccess(GpsLocationInfo result) {
          StringBuffer sb = new StringBuffer();
          sb.append(String.valueOf(result.getLongitude()))
            .append(",").append(String.valueOf(result.getLatitude()));
          String position = GpsCorrectUtil.transform(result.getLongitude(),result.getLatitude());
          StaticManager.instance().currentPosition = position;
          LogUtils.e("mare onLocateSuccess original= "+ sb.toString() +", Correct=" + position);
    }
}