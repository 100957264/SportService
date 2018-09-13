// ISportService.aidl
package com.yt.sportservice;

import com.yt.sportservice.ISportServiceCallback;

// Declare any non-default types here with import statements

interface ISportService {

    String getUserName();
    String getPassword();
    boolean isRegister();
    void setRegister(boolean register);
    void registerUser(String name, String password);
    void registerSportServiceCallback(ISportServiceCallback cb);
    void unregisterSportServiceCallback(ISportServiceCallback cb);

}
