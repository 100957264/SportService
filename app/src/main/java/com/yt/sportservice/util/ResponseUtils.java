package com.yt.sportservice.util;

import com.yt.sportservice.SportService;
import com.yt.sportservice.constant.MsgContent;
import com.yt.sportservice.dao.UploadDataEntityDao;
import com.yt.sportservice.entity.LoginResponse;
import com.yt.sportservice.entity.PostDataResponse;
import com.yt.sportservice.entity.PostDataResponseOpration;
import com.yt.sportservice.entity.StudentInfoEvent;
import com.yt.sportservice.event.StatusEvent;
import com.yt.sportservice.manager.StaticManager;
import com.yt.sportservice.presenter.UploadDataEntityDaoUtils;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Response;

/**
 * Created by jianqin on 2018/7/27.
 */

public class ResponseUtils implements SportService.RequestCallback {
    private ResponseResultListen mResponseResultListen;

    public static int TYPE_REG = 1;
    public static int TYPE_POST_DATA = 2;

    private int mType ;

    public int STATUS_SLEEP = 1;
    public int STATUS_WORK = 2;
    public int STATUS_IDLE = 3;
    public int STATUS_WARN = 4;

    public ResponseUtils(ResponseResultListen rrl){
        mResponseResultListen = rrl;
    }
    @Override
    public void onFailure(Call call, IOException e) {
        if(this.mType == TYPE_REG) {
            LogUtils.e("SPORTCLOUD GEG onFailure e=" + e);
            mResponseResultListen.responseFailure(MsgContent.MSG_REGISTER_FAIL,"注册网络请求失败");
        }else{
            LogUtils.e("SPORTCLOUD POST DATA onFailure=>uploadData error. call: " + call + " error: ", e);
            mResponseResultListen.responseFailure(MsgContent.UPLOAD_DATA_FAIL,"上传数据网络请求失败");
        }
    }

    @Override
    public void onResponseStr(Call call, Response response) {
        if(this.mType == TYPE_REG){
            handleRegResponse(response);
        }else if(this.mType == TYPE_POST_DATA){
            handlePoseDataResponse(response);
        }
    }

    private void handlePoseDataResponse(Response response) {
        try {
            String responseStr = response.body().string();
            LogUtils.e("SPORTCLOUD onResponse responseStr=" + responseStr);
            if (response.isSuccessful()) {
                JSONObject oriData = new JSONObject(responseStr.trim());
                int code = oriData.getInt("code");
                if (code == 1) {
                    LogUtils.e("SPORTCLOUD onResponseStr=>上传成功");
                    PostDataResponse pdr = new PostDataResponse();
                    pdr.code = code;
                    pdr.interval = oriData.getInt("interval");
                    pdr.message = null;
                    pdr.status = oriData.getInt("status");
                    StatusEvent statusEvent  = new StatusEvent(pdr.status);
                    EventBus.getDefault().post(statusEvent);
                    if(pdr.status == STATUS_IDLE){
                        JSONObject opration = oriData.getJSONObject("operation");
                        PostDataResponseOpration pdro =new PostDataResponseOpration();
                        pdro.userName = opration.getString("username");
                        pdro.userCode = opration.getString("userCode");
                        pdro.groupCode = opration.getString("groupCode");
                        pdr.opration  = pdro;
                        StaticManager.instance().mCurrentUserId = pdro.userCode;
                        StudentInfoEvent studentInfoEvent = new StudentInfoEvent(pdro.userName,pdro.userCode,pdro.groupCode);
                        EventBus.getDefault().postSticky(studentInfoEvent);
                    }else {
                        pdr.opration = "";
                    }
                    LogUtils.e("SPORTCLOUD onResponseStr=>下次上传数据间隔时间：" + pdr.toString());
                    mResponseResultListen.responseSuccess(pdr.status,pdr);
                } else {
                    LogUtils.e("SPORTCLOUD onResponseStr=>上传失败: " + oriData.getString("msg"));
                    mResponseResultListen.responseFailure(MsgContent.UPLOAD_DATA_FAIL,"上传失败");
                }
            } else {
                LogUtils.e("SPORTCLOUD onResponseStr=>上传失败");
                mResponseResultListen.responseFailure(MsgContent.UPLOAD_DATA_FAIL,"上传失败");
            }
        } catch (Exception e) {
            LogUtils.e("SPORTCLOUD onResponseStr=>error: ", e);
            mResponseResultListen.responseFailure(MsgContent.UPLOAD_DATA_FAIL,"上传数据服务器返回内容格式错误");
        }
    }

    public void setResponseType(int type){
        this.mType = type;
    }

    public int getesponseType() {
        return mType;
    }


    public interface ResponseResultListen{
        void responseSuccess(int status,Object object);
        void responseFailure(int status,String error);
    }



    private void handleRegResponse(Response response){
        try {
            String responseStr = response.body().string();
            LogUtils.e("SPORTCLOUD onResponse regStr=" + responseStr);
            JSONObject oriData = new JSONObject(responseStr.trim());
            if (response.isSuccessful()) {
                int code = oriData.getInt("code");
                if (code == 1) {
                    LogUtils.e("SPORTCLOUD 注册成功");
                    LoginResponse lr =new LoginResponse();
                    lr.code = code;
                    lr.interval = oriData.getInt("interval");
                    lr.message = null;
                    LogUtils.e("SPORTCLOUD onResponseStr=>下次上传数据间隔时间：" + lr.interval);
                    mResponseResultListen.responseSuccess(MsgContent.MSG_REGISTER,lr);
                } else {
                    String msg = oriData.getString("msg");
                    mResponseResultListen.responseFailure(MsgContent.MSG_REGISTER_FAIL,"注册失败:"+msg);
                }
            } else {
                mResponseResultListen.responseFailure(MsgContent.MSG_REGISTER_FAIL,"注册失败");
            }
        } catch (Exception e) {
            LogUtils.e("SPORTCLOUD onResponseStr=>error: ", e);
            mResponseResultListen.responseFailure(MsgContent.MSG_REGISTER_FAIL,"注册服务器返回内容格式错误");
        }
    }
}
