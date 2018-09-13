// ISportServiceCallback.aidl
package com.yt.sportservice;

// Declare any non-default types here with import statements

interface ISportServiceCallback {

    void onRegisterUserResult(boolean success);
    void onError(String errorMsg);

}
