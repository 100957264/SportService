package com.yt.sportservice.gps;

/**
 * @author mare
 * @Description:TODO
 * @csdnblog http://blog.csdn.net/mare_blue
 * @date 2017/11/16
 * @time 14:21
 */
public enum GpsErrorType {
    LOCATE_DISABLED,//没有打开GPS
    LOCATE_OUT_OF_SERVICE, //停止运行
    LOCATE_TEMPORARILY_UNAVAILABLE, //暂时获取不到信号(信号偏弱 没达到8个卫星)
    LOCATE_TIMEOUT, //正在定位（一直定位不到结果）
    LOCATE_ERROR       //定位过程中异常(比如硬件不支持...)
}
