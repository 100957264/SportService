package com.yt.sportservice;

import android.Manifest;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;

public class SportClient extends Activity implements View.OnClickListener {

    private static final String TAG = "SportClient";

    private static final int PERMISSIONS_REQUEST_CODE = 10;

    private EditText mUserNameEt;
    private EditText mPasswordEt;
    private Button mRegisterBt;
    private Button mUnRegisterBt;
    private ISportService mSportService;
    private ArrayList<String> mPermissions;
    private ArrayList<String> mRequestPermissions;

    private String mUserName;
    private String mPassword;
    private boolean mNeedRegisterOnBindService;
    private boolean mIsBindService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sport_client);

        mUserNameEt = (EditText) findViewById(R.id.user_name);
        mPasswordEt = (EditText) findViewById(R.id.password);
        mRegisterBt = (Button) findViewById(R.id.register);
        mUnRegisterBt = (Button) findViewById(R.id.unregister);

        mRegisterBt.setOnClickListener(this);
        mUnRegisterBt.setOnClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        initPermissions();
        mUserNameEt.setText("dev1");
        mPasswordEt.setText("dev1-0423");
        if (mSportService == null) {
            bindSportService();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        updateUIs();
        if (mSportService != null) {
            unbindSportService();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        Log.d(TAG, "onRequestPermissionsResult=>requestCode: " + requestCode);
        if (permissions == null || permissions.length == 0 || grantResults == null || grantResults.length == 0) {
            if (mRequestPermissions.size() > 0) {
                Toast.makeText(this, "权限拒绝", Toast.LENGTH_LONG).show();
                finish();
                return;
            }
        }
        switch (requestCode) {
            case PERMISSIONS_REQUEST_CODE:
                int grantCount = 0;

                for (int i = 0; i < grantResults.length; i++) {
                    Log.d(TAG, "onRequestPermissionsResult=>permission: " + permissions[i] + " grant: " + grantResults[i]);
                    if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                        grantCount++;
                    } else {
                        if (!shouldShowRequestPermissionRationale(permissions[i])) {
                            Log.d(TAG, "onRequestPermissionsResult=>" + mRequestPermissions.get(i) + " denied.");
                        }
                    }
                }
                if (grantCount != grantResults.length) {
                    Toast.makeText(this, "权限拒绝", Toast.LENGTH_LONG).show();
                    finish();
                    return;
                }
                break;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.register:
                String name = mUserNameEt.getText().toString();
                String password = mPasswordEt.getText().toString();
                if (!TextUtils.isEmpty(name) && !TextUtils.isEmpty(password)) {
                    updateRegisterUIState(false);
                    mUserName = name;
                    mPassword = password;
                    if (mSportService != null) {
                        mNeedRegisterOnBindService = false;
                        try {
                            mSportService.registerUser(mUserName, mPassword);
                        } catch (RemoteException e) {
                            Log.e(TAG, "onClick=>register error: ", e);
                            Toast.makeText(SportClient.this, "注册失败", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        mNeedRegisterOnBindService = true;
                        bindSportService();
                    }
                } else {
                    Toast.makeText(this, "用户名或密码不能为空", Toast.LENGTH_SHORT).show();
                }
                break;

            case R.id.unregister:
                if (mSportService != null) {
                    try {
                        mSportService.setRegister(false);
                        mSportService.unregisterSportServiceCallback(mSportServiceCallback);
                    } catch (RemoteException e) {
                        Log.e(TAG, "onClick=>error: ", e);
                    }
                    unbindSportService();
                    mSportService = null;
                    Intent service = new Intent(SportClient.this, SportService.class);
                    stopService(service);
                    mUnRegisterBt.setEnabled(false);
                    updateRegisterUIState(true);
                }
                break;
        }
    }

    private void updateRegisterUIState(boolean enabled) {
        mUserNameEt.setEnabled(enabled);
        mPasswordEt.setEnabled(enabled);
        mRegisterBt.setEnabled(enabled);
    }

    private void updateUIs() {
        if (mSportService != null) {
            try {
                boolean isRegister = mSportService.isRegister();
                Log.d(TAG, "updateUIs=>isRegister: " + isRegister);
                if (isRegister || mNeedRegisterOnBindService) {
                    String userName = mSportService.getUserName();
                    String password = mSportService.getPassword();
                    if (!TextUtils.isEmpty(userName) && !TextUtils.isEmpty(password)) {
                        mUserNameEt.setText(userName);
                        mPasswordEt.setText(password);
                        mRegisterBt.setEnabled(false);
                        mUnRegisterBt.setEnabled(true);
                    }
                } else {
                    mRegisterBt.setEnabled(true);
                    mUnRegisterBt.setEnabled(false);
                }
            } catch (RemoteException e) {
                Log.e(TAG, "updateUIs=>error: ", e);
                Toast.makeText(this, "获取当前用户信息失败.", Toast.LENGTH_SHORT).show();
            }
        } else {
            mRegisterBt.setEnabled(true);
            mUnRegisterBt.setEnabled(false);
        }
    }

    private void bindSportService() {
        Intent service = new Intent(this, SportService.class);
        getApplicationContext().startService(service);
        mIsBindService = bindService(service, serviceConnection, BIND_AUTO_CREATE);
        Log.d(TAG, "bindSportService=>bind: " + mIsBindService);
    }

    private  void unbindSportService() {
        Log.d(TAG, "unbindSportService=>bind: " + mIsBindService);
        if (mIsBindService) {
            unbindService(serviceConnection);
            mIsBindService = false;
        }
    }

    private void initPermissions() {
        mPermissions = new ArrayList<String>();
        mPermissions.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        mPermissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
        mPermissions.add(Manifest.permission.ACCESS_WIFI_STATE);
        mPermissions.add(Manifest.permission.ACCESS_NETWORK_STATE);
        mPermissions.add(Manifest.permission.CHANGE_WIFI_STATE);
        mPermissions.add(Manifest.permission.READ_PHONE_STATE);
        mPermissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        mPermissions.add(Manifest.permission.INTERNET);
        mPermissions.add(Manifest.permission.VIBRATE);
        mPermissions.add(Manifest.permission.WAKE_LOCK);

        mRequestPermissions = new ArrayList<String>();

        for (int i = 0; i < mPermissions.size(); i++) {
            if (!hasPermission(mPermissions.get(i))) {
                Log.d(TAG, "initPermissions=>need permission: " + mPermissions.get(i));
                mRequestPermissions.add(mPermissions.get(i));
            }
        }

        Log.d(TAG, "initPermissions=>need permission size: " + mRequestPermissions.size());
        if (mRequestPermissions.size() > 0) {
            requestPermission(mRequestPermissions.toArray(new String[mRequestPermissions.size()]), PERMISSIONS_REQUEST_CODE);
        }
    }


    private boolean hasPermission(String permission) {
        boolean granted = (checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED);
        Log.d(TAG, "hasPermission=>granted: " + granted + " permission: " + permission);
        return granted;
    }

    private void requestPermission(String[] permissions, int requestCode) {
        Log.d(TAG, "requestPermission=>requestCode: " + requestCode + " permissions: " + Arrays.toString(permissions));
        requestPermissions(permissions, requestCode);
    }

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(TAG, "onServiceConnected=>name: " + name + ", service: " + service);
            mSportService = ISportService.Stub.asInterface(service);
            try {
                mSportService.registerSportServiceCallback(mSportServiceCallback);
                updateUIs();
                if (mNeedRegisterOnBindService) {
                    mSportService.registerUser(mUserName, mPassword);
                }
            } catch (RemoteException e) {
                Log.e(TAG, "onServiceConnected=>error：", e);
                mSportService = null;
                Toast.makeText(SportClient.this, "启动服务失败", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d(TAG, "onServiceDisconnected=>name: " + name);
            try {
                mSportService.unregisterSportServiceCallback(mSportServiceCallback);
            } catch (RemoteException e) {
                Log.e(TAG, "onServiceDisconnected=>error: ", e);
            }
        }
    };

    private ISportServiceCallback.Stub mSportServiceCallback = new ISportServiceCallback.Stub() {
        @Override
        public void onRegisterUserResult(boolean success) throws RemoteException {
            Log.d(TAG, "onRegisterUserResult=>success: " + success);
            if (success) {
                Toast.makeText(SportClient.this, "注册成功", Toast.LENGTH_SHORT).show();
                mUnRegisterBt.setEnabled(true);
            } else {
                Toast.makeText(SportClient.this, "注册失败", Toast.LENGTH_SHORT).show();
                mUnRegisterBt.setEnabled(false);
                updateRegisterUIState(true);
            }

        }

        @Override
        public void onError(String errorMsg) throws RemoteException {
            Log.d(TAG, "onError=>errorMsg: " + errorMsg);
            if (!TextUtils.isEmpty(errorMsg)) {
                Toast.makeText(SportClient.this, errorMsg, Toast.LENGTH_SHORT).show();
            }
        }
    };
}
