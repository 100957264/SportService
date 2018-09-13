package com.yt.sportservice.entity;


import com.yt.sportservice.manager.StaticManager;
import com.yt.sportservice.step.StepUtils;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Generated;

/**
 * @author mare
 * @Description:TODO 累积步数数据库
 * @csdnblog http://blog.csdn.net/mare_blue
 * @date 2017/8/16
 * @time 17:34
 */
@Entity
public class StepHistory  {
    @Id(autoincrement = true)
    private long id;

    public String imei;//主键

    private String date;//日期格式为(年月日)， 比如 120414， 表示 2012 年 4 月 14 日； （手表所在地日期）

    private String stepTime;

    private long step_count;

    private long limit = 10000;//默认10000

    private long step_energy;//消耗的能量

    private long time;

    public StepHistory(long step_count, long time) {
        this.step_count = step_count;
        this.time = time;
        this.date = StepUtils.long2Date(time);
        this.stepTime = StepUtils.long2Time(time);
        this.imei = StaticManager.IMEI;
    }

    @Generated(hash = 155573448)
    public StepHistory(long id, String imei, String date, String stepTime,
            long step_count, long limit, long step_energy, long time) {
        this.id = id;
        this.imei = imei;
        this.date = date;
        this.stepTime = stepTime;
        this.step_count = step_count;
        this.limit = limit;
        this.step_energy = step_energy;
        this.time = time;
    }


    @Generated(hash = 1435858099)
    public StepHistory() {
    }


    @Override
    public String toString() {
        return "StepHistory{" +
                "id=" + id +
                ", imei='" + imei + '\'' +
                ", date='" + date + '\'' +
                ", stepTime='" + stepTime + '\'' +
                ", step_count=" + step_count +
                ", limit=" + limit +
                ", step_energy=" + step_energy +
                ", time=" + time +
                '}';
    }


    public long getId() {
        return this.id;
    }


    public void setId(long id) {
        this.id = id;
    }


    public String getImei() {
        return this.imei;
    }


    public void setImei(String imei) {
        this.imei = imei;
    }


    public String getDate() {
        return this.date;
    }


    public void setDate(String date) {
        this.date = date;
    }


    public String getStepTime() {
        return this.stepTime;
    }


    public void setStepTime(String stepTime) {
        this.stepTime = stepTime;
    }


    public long getStep_count() {
        return this.step_count;
    }


    public void setStep_count(long step_count) {
        this.step_count = step_count;
    }


    public long getLimit() {
        return this.limit;
    }


    public void setLimit(long limit) {
        this.limit = limit;
    }


    public long getStep_energy() {
        return this.step_energy;
    }


    public void setStep_energy(long step_energy) {
        this.step_energy = step_energy;
    }


    public long getTime() {
        return this.time;
    }


    public void setTime(long time) {
        this.time = time;
    }
}
