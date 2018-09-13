package com.yt.sportservice.entity;

/**
 * Created by jianqin on 2018/7/27.
 */

public class PostDataResponse {
    public int code;
    public int interval;
    public String message;
    public int status;
    public Object opration;

    @Override
    public String toString() {
        return "PostDataResponse{" +
                "code=" + code +
                ", interval=" + interval +
                ", message='" + message + '\'' +
                ", status=" + status +
                ", opration=" + opration +
                '}';
    }
}
