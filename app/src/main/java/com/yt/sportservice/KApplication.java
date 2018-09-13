package com.yt.sportservice;

import android.app.Application;
import android.content.Context;

/**
 * @author mare
 * @Description:
 * @csdnblog http://blog.csdn.net/mare_blue
 * @date 2017/8/24
 * @time 14:32
 */
public class KApplication extends Application{
    public static Context sContext;


    @Override
    public void onCreate() {
        super.onCreate();
        sContext = this;

    }

}
