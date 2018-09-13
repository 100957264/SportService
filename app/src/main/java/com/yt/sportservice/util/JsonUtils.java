package com.yt.sportservice.util;

import com.yt.sportservice.entity.UploadDataEntity;
import com.yt.sportservice.manager.StaticManager;
import com.yt.sportservice.presenter.UploadDataEntityDaoUtils;
import com.yt.sportservice.step.StepUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

/**
 * Created by jianqin on 2018/7/27.
 */

public class JsonUtils {
    private static JsonUtils mJsonUtils;
    public static JsonUtils instance(){
        if(mJsonUtils == null){
            mJsonUtils = new JsonUtils();
        }
        return mJsonUtils;
    }

    // 创建注册用户的json格式字符串
    public String createRegisterJsonData(String userName,String password) throws JSONException, UnsupportedEncodingException, NoSuchAlgorithmException {
        String pwd = password + userName  + StaticManager.IMEI;
        String securityPassword = encryptionPassWord(pwd);
        LogUtils.e("SPORTCLOUD createRegisterJsonData=>pwd ="+ pwd+ ",密码加密后的值：" + securityPassword);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("factoryName","云天智能");
        jsonObject.put("factoryId",2);
        jsonObject.put("deviceId", StaticManager.IMEI);
        jsonObject.put("username", userName);
        jsonObject.put("password", securityPassword);
        return jsonObject.toString();
    }

    // 创建上传数据的json格式字符串
    public String createUploadDataJsonData() throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("factoryName","云天智能");
        jsonObject.put("factoryId","2");
        jsonObject.put("deviceType","band");
        jsonObject.put("deviceId", StaticManager.IMEI);
        jsonObject.put("userId",StaticManager.instance().mCurrentUserId);
        jsonObject.put("battery", String.valueOf(StaticManager.instance().currentBatteryLevel));
        JSONArray datas = new JSONArray();
        JSONObject data = new JSONObject();
        data.put("avgHeartRate",StaticManager.instance().getAvgHeartValue());
        data.put("calorie", StepUtils.getCalorieByStep(StaticManager.instance().currentStep));
        data.put("distance",StepUtils.getDistanceByStep(StaticManager.instance().currentStep));
        data.put("heartRate",String.valueOf(StaticManager.instance().mHeartRateNum));
        data.put("position",StaticManager.instance().currentPosition);
        data.put("sleep","12");
        data.put("stepCounter", String.valueOf(StaticManager.instance().currentStep));
        data.put("timeStamp", TimeUtils.millis2String(System.currentTimeMillis()));
        datas.put(data);
        jsonObject.put("datas", datas);
        return jsonObject.toString();
    }

    // 创建上传数据的json格式字符串
    public String createClassUploadDataJsonData() throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("factoryName","云天智能");
        jsonObject.put("factoryId","2");
        jsonObject.put("deviceType","band");
        jsonObject.put("deviceId", StaticManager.IMEI);
        jsonObject.put("userId",StaticManager.instance().mCurrentUserId);
        jsonObject.put("battery", String.valueOf(StaticManager.instance().currentBatteryLevel));
        List<UploadDataEntity> list = UploadDataEntityDaoUtils.instance().queryAllClassUploadData();
        if(list != null && list.size()>0){
            LogUtils.e("SPORTCLOUD list=" + list.size());
            JSONArray datas = new JSONArray();
            for (UploadDataEntity uploadDataEntity:list){
                JSONObject data = new JSONObject();
                data.put("avgHeartRate",uploadDataEntity.getAvgHeartrate());
                data.put("calorie", uploadDataEntity.getCalorie());
                data.put("distance",uploadDataEntity.getDistance());
                data.put("heartRate",uploadDataEntity.getHeartrate());
                data.put("position",uploadDataEntity.getPosition());
                data.put("sleep",uploadDataEntity.getSleep());
                data.put("stepCounter", uploadDataEntity.getStepCounter());
                data.put("timeStamp",uploadDataEntity.getTime());
                datas.put(data);
            }
            jsonObject.put("datas", datas);
        }else {
            jsonObject.put("datas", "");
        }

        return jsonObject.toString();
    }


    // 加密密码
    private String encryptionPassWord(String pwd) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        String securityPwd = getStringMd5(pwd);
        int i = 0;
        do {
            securityPwd = getStringMd5(securityPwd);
            i++;
        } while (i < 2);

        return securityPwd;
    }



    // 获取字符MD5值
    private String getStringMd5(String value) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        byte[] hash = null;

        hash = MessageDigest.getInstance("MD5").digest(value.getBytes("UTF-8"));

        StringBuilder hex = new StringBuilder(hash.length * 2);

        for (byte b : hash) {
            if ((b & 0xFF) < 0X10) {
                hex.append("0");
            }
            hex.append(Integer.toHexString(b & 0xFF));
        }

        return hex.toString();
    }
}
