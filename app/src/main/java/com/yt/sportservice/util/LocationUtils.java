package com.yt.sportservice.util;

import android.content.Context;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.yt.sportservice.entity.LocationEntity;

/**
 * Created by jianqin on 2018/7/27.
 */

public class LocationUtils extends BDAbstractLocationListener {
    private LocationResultListen mLocationResultListen;
    // 定位客户端
    private LocationClient mLocationClient = null;


    public LocationUtils(LocationResultListen lrl){
        mLocationResultListen = lrl;
    }


    public void initLocationClient(Context context) {
        LogUtils.d("SPORTCLOUD initLocationClient()...");
        // 初始化定位客户端
        mLocationClient = new LocationClient(context.getApplicationContext());
        // 配置定位参数
        LocationClientOption option = new LocationClientOption();

        // 设置定位模式
        // LocationClientOption.LocationMode.Hight_Accuracy: 高精度
        // LocationClientOption.LocationMode.Battery_Saving: 低精度
        // LocationClientOption.LocationMode.Device_Sensors: 仅使用设备时
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);

        // 设置返回经纬度坐标类型，默认gcj02
        // gcj02: 国测局坐标
        // bd09ll: 百度经纬度坐标
        // bd09: 百度墨卡托坐标
        // 海外地区定位，无需设置坐标类型，统一返回wgs84类型坐标
        //option.setCoorType("bd09ll");

        // 设置发起定位请求的间隔，int类型，单位ms
        // 如果设置为0，则代表单次定位，即仅定位一次，默认为0
        // 如果设置非0，需要设置1000ms以上才有效
        //option.setScanSpan(2000);

        // 设置是否使用gps，默认false
        // 使用高精度和仅设备两种定位模式的，参数必须设置为true
        option.setOpenGps(true);

        // 设置是否当GPS有效时按照1S/1次频率输出GPS结果，默认false
        //option.setLocationNotify(false);

        // 可选，设置是否在stop的时候杀死这个进程，默认（建议）不杀死
        option.setIgnoreKillProcess(true);

        // 可选，如果设置了该接口，首次启动定位时，会判断当前wif是否超出有效期，若超出有效期，会先重新扫描Wifi，然后定位
//        option.setWifiCacheTimeOut(5 * 60 * 1000);

        // 可选，设置是否需要过滤GPS仿真结果，默认需要，即参数为false
//        option.setEnableSimulateGps(false);

        // 调用LocationClient的start()方法，便可以发起定位请求
        mLocationClient.setLocOption(option);
    }


    // 注册定位监听器
    public void registerLocationListener() {
        mLocationClient.registerLocationListener(this);
    }

    public void restart(){
        mLocationClient.stop();
        mLocationClient.start();
    }





    @Override
    public void onReceiveLocation(BDLocation bdLocation) {
        // 获取经纬度坐标类型， 以LocationClientOption中设置过的坐标类型为准
        String coorType = bdLocation.getCoorType();

        // 获取定位类型，定位错误返回码，
        int errorCode = bdLocation.getLocType();
        if (errorCode == 61 || errorCode == 66 || errorCode == 161) {
            LocationEntity le =new LocationEntity();
            // 获取纬度信息
            le.latitude = bdLocation.getLatitude();
            // 获取经度信息
            le.longitude = bdLocation.getLongitude();
            // 获取定位精度，默认值为0.0f
            le.radius = bdLocation.getRadius();
            mLocationResultListen.locationResult(true,le);
            LogUtils.d("SPORTCLOUD onReceiveLocation=>定位成功,当前位置经度：" + le.longitude + "，纬度：" + le.latitude +",定位精度:"+le.radius);
        } else {
            mLocationResultListen.locationResult(false,null);
            LogUtils.d("SPORTCLOUD onReceiveLocation=>定位失败：" + bdLocation.getLocTypeDescription());
        }
    }

    public interface LocationResultListen{
        void locationResult(boolean result, LocationEntity le);
    }
}
