package com.yt.sportservice.acty;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.location.Criteria;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.yt.sportservice.R;
import com.yt.sportservice.adapter.GPSInfoListAdapter;
import com.yt.sportservice.util.GpsCorrectUtil;
import com.yt.sportservice.util.LogUtils;
import com.yt.sportservice.util.PowerUtils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by B415 on 2018/3/6.
 */

public class GPSTestActivity extends Activity {
    TextView location_text;
    LocationManager locationManager;
    RecyclerView recyclerView;
    GPSInfoListAdapter gpsInfoListAdapter;
    List<String> gpsInfoList = new ArrayList<>();
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().setStatusBarColor(Color.TRANSPARENT);
        setContentView(R.layout.gps_test_activity);
        location_text  =(TextView) findViewById(R.id.text_location);
        recyclerView =(RecyclerView)findViewById(R.id.location_list);
        PowerUtils.instance().acquireWakeLock();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        gpsInfoListAdapter = new GPSInfoListAdapter(this,gpsInfoList);
        recyclerView.setAdapter(gpsInfoListAdapter);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        locationManager =(LocationManager) getSystemService(Context.LOCATION_SERVICE);
        // 为获取地理位置信息时设置查询条件
        String bestProvider = locationManager.getBestProvider(getCriteria(), true);
        // 获取位置信息
        // 如果不设置查询要求，getLastKnownLocation方法传人的参数为LocationManager.GPS_PROVIDER
        Location location = locationManager.getLastKnownLocation(bestProvider);
        updateView(location);
        // 监听状态
        locationManager.addGpsStatusListener(listener);
        // 绑定监听，有4个参数
        // 参数1，设备：有GPS_PROVIDER和NETWORK_PROVIDER两种
        // 参数2，位置信息更新周期，单位毫秒
        // 参数3，位置变化最小距离：当位置距离变化超过此值时，将更新位置信息
        // 参数4，监听
        // 备注：参数2和3，如果参数3不为0，则以参数3为准；参数3为0，则通过时间来定时更新；两者为0，则随时刷新

        // 1秒更新一次，或最小位移变化超过1米更新一次；
        // 注意：此处更新准确度非常低，推荐在service里面启动一个Thread，在run中sleep(10000);然后执行handler.sendMessage(),更新位置
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1, locationListener);
    }



    @Override
    protected void onDestroy() {
        locationManager.removeUpdates(locationListener);
        PowerUtils.instance().releaseWakeLock();
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
    // 位置监听
    private LocationListener locationListener = new LocationListener() {

        /**
         * 位置信息变化时触发
         */
        public void onLocationChanged(Location location) {
            updateView(location);
            LogUtils.e("GPS 时间：" + location.getTime());
            LogUtils.e( "GPS 经度：" + location.getLongitude());
            LogUtils.e( "GPS 纬度：" + location.getLatitude());
            LogUtils.e( "GPS 海拔：" + location.getAltitude());
            String position = GpsCorrectUtil.transform(location.getLatitude(),location.getLongitude());
            LogUtils.e( "GPS position：" + position);
        }

        /**
         * GPS状态变化时触发
         */
        public void onStatusChanged(String provider, int status, Bundle extras) {
            switch (status) {
                // GPS状态为可见时
                case LocationProvider.AVAILABLE:
                    break;
                // GPS状态为服务区外时
                case LocationProvider.OUT_OF_SERVICE:
                    break;
                // GPS状态为暂停服务时
                case LocationProvider.TEMPORARILY_UNAVAILABLE:
                    break;
            }
        }

        /**
         * GPS开启时触发
         */
        public void onProviderEnabled(String provider) {
            Location location = locationManager.getLastKnownLocation(provider);
            updateView(location);
        }

        /**
         * GPS禁用时触发
         */
        public void onProviderDisabled(String provider) {
            updateView(null);
        }

    };
    // 状态监听
    GpsStatus.Listener listener = new GpsStatus.Listener() {
        public void onGpsStatusChanged(int event) {
            switch (event) {
                // 第一次定位
                case GpsStatus.GPS_EVENT_FIRST_FIX:
                    break;
                // 卫星状态改变
                case GpsStatus.GPS_EVENT_SATELLITE_STATUS:
                    // 获取当前状态
                    GpsStatus gpsStatus = locationManager.getGpsStatus(null);
                    // 获取卫星颗数的默认最大值
                    int maxSatellites = gpsStatus.getMaxSatellites();
                    // 创建一个迭代器保存所有卫星
                    Iterator<GpsSatellite> iters = gpsStatus.getSatellites()
                            .iterator();
                    int count = 0;
                    gpsInfoList.clear();
                    while (iters.hasNext() && count <= maxSatellites) {
                        GpsSatellite s = iters.next();
                        String info = "snr=" + s.getSnr() + ",prn=" + s.getPrn();
                        gpsInfoList.add(info);
                        gpsInfoListAdapter.notifyDataSetChanged();
                        count++;
                    }
                    LogUtils.e("GPS count =" + count);
                    break;
                // 定位启动
                case GpsStatus.GPS_EVENT_STARTED:
                    break;
                // 定位结束
                case GpsStatus.GPS_EVENT_STOPPED:
                    break;
            }
        };
    };

    /**
     * 实时更新文本内容
     *
     * @param location
     */
    private void updateView(Location location) {
        if(location != null) {
            StringBuffer sb = new StringBuffer();
            sb.append("location:").append("\n")
                    .append("经度:").append(location.getLongitude()).append("\n")
                    .append("纬度:").append(location.getLatitude()).append("\n");
            location_text.setText(sb.toString());
            String position = GpsCorrectUtil.transform(location.getLatitude(),location.getLongitude());
            LogUtils.e( "GPS position：" + position);
            String position2 =  GpsCorrectUtil.transform(39.981248,116.397268);
            LogUtils.e( "GPS position：" + position2);
        }
    }

    /**
     * 返回查询条件
     *
     * @return
     */
    private Criteria getCriteria() {
        Criteria criteria = new Criteria();
        // 设置定位精确度 Criteria.ACCURACY_COARSE比较粗略，Criteria.ACCURACY_FINE则比较精细
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        // 设置是否要求速度
        criteria.setSpeedRequired(false);
        // 设置是否允许运营商收费
        criteria.setCostAllowed(false);
        // 设置是否需要方位信息
        criteria.setBearingRequired(false);
        // 设置是否需要海拔信息
        criteria.setAltitudeRequired(false);
        // 设置对电源的需求
        criteria.setPowerRequirement(Criteria.POWER_LOW);
        return criteria;
    }
}