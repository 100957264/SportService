package com.yt.sportservice.entity;


import com.yt.sportservice.manager.StaticManager;
import com.yt.sportservice.step.StepUtils;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;

/**
 * @author mare
 * @Description:TODO 累积步数数据库
 * @csdnblog http://blog.csdn.net/mare_blue
 * @date 2017/8/16
 * @time 17:34
 */
@Entity
public class UploadDataEntity {
    @Id(autoincrement = true)
    private Long id;

    private String time;//日期格式为(年月日)， 比如 120414， 表示 2012 年 4 月 14 日； （手表所在地日期）
    private String avgHeartrate;
    private String heartrate;
    private String stepCounter;
    private String sleep;
    private String distance;
    private String calorie;
    private String position;


    public UploadDataEntity(String time, String avgHeartrate, String heartrate, String stepCounter, String sleep, String distance, String calorie, String position) {
        this.time = time;
        this.avgHeartrate = avgHeartrate;
        this.heartrate = heartrate;
        this.stepCounter = stepCounter;
        this.sleep = sleep;
        this.distance = distance;
        this.calorie = calorie;
        this.position = position;
    }

    @Generated(hash = 1313506487)
    public UploadDataEntity(Long id, String time, String avgHeartrate, String heartrate, String stepCounter, String sleep, String distance, String calorie, String position) {
        this.id = id;
        this.time = time;
        this.avgHeartrate = avgHeartrate;
        this.heartrate = heartrate;
        this.stepCounter = stepCounter;
        this.sleep = sleep;
        this.distance = distance;
        this.calorie = calorie;
        this.position = position;
    }

    @Generated(hash = 1106204936)
    public UploadDataEntity() {
    }


    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getAvgHeartrate() {
        return avgHeartrate;
    }

    public void setAvgHeartrate(String avgHeartrate) {
        this.avgHeartrate = avgHeartrate;
    }

    public String getHeartrate() {
        return heartrate;
    }

    public void setHeartrate(String heartrate) {
        this.heartrate = heartrate;
    }

    public String getStepCounter() {
        return stepCounter;
    }

    public void setStepCounter(String stepCounter) {
        this.stepCounter = stepCounter;
    }

    public String getSleep() {
        return sleep;
    }

    public void setSleep(String sleep) {
        this.sleep = sleep;
    }

    public String getDistance() {
        return distance;
    }

    public void setDistance(String distance) {
        this.distance = distance;
    }

    public String getCalorie() {
        return calorie;
    }

    public void setCalorie(String calorie) {
        this.calorie = calorie;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
