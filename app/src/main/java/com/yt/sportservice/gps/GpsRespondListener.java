package com.yt.sportservice.gps;

/**
 * 类描述：供外部实现的接口（抽象观察者）
 */
public interface GpsRespondListener {
    /**
     * 方法描述：位置信息发生改变时被调用
     *
     * @param locationInfo 更新位置后的locationInfo
     */
    void onLocateSuccess(GpsLocationInfo locationInfo);

    /**
     * 方法描述：定位失败时被调用
     *
     * @param failureCode 错误信息代码
     */
    void toLocateFailure(GpsErrorType failureCode);
}
