package com.welab.permissiontest;

import android.app.DownloadManager;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    public static final String TAG = "pain.xie";
    protected Button mContacts;
    protected Button mLocation;
    protected Button mCamera;
    protected Button mState;
    protected Button mStorage;
    protected Button mSms;
    protected Button mSetting;
    protected Button mSetting1;
    protected Button mSetting2;
    protected Button mSetting3;
    protected Button mclick1;
    protected Button mclick2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setContentView(R.layout.activity_main);
        initView();
    }

    @Override
    public void onClick(View view) {
        if(view.getId() == R.id.contacts) {
            PermisionUtil.checkContacts(this);
        } else if(view.getId() == R.id.location) {
            PermisionUtil.checkLocation(this);
        } else if(view.getId() == R.id.camera) {
            PermisionUtil.checkCamera(this);
        } else if(view.getId() == R.id.state) {
            PermisionUtil.checkState(this);
        } else if(view.getId() == R.id.storage) {
            PermisionUtil.checkStorage(this);
        } else if(view.getId() == R.id.sms) {
            PermisionUtil.checkSms(this);
        } else if(view.getId() == R.id.setting) {
            SettingUtil.goSetting(this);
        } else if(view.getId() == R.id.setting1) {
            SettingUtil.goSetting1(this);
        } else if(view.getId() == R.id.setting2) {
            SettingUtil.goSetting2(this);
        } else if(view.getId() == R.id.setting3) {
            SettingUtil.goSetting3(this);
        } else if(view.getId() == R.id.click1) {
            FileDownloadsUtil util = new FileDownloadsUtil(this);
            util.downloadApkFile("https://welabcdn.oss-cn-shenzhen.aliyuncs.com/wolaidai.a_promotion1000.apk", "test");
        } else if(view.getId() == R.id.click2) {
            FileDownloadsUtil.startDownload(this);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(new InstallReceiver(), new IntentFilter(DownloadManager
                .ACTION_DOWNLOAD_COMPLETE));
    }

    private void initView() {
        mContacts = (Button) findViewById(R.id.contacts);
        mContacts.setOnClickListener(MainActivity.this);
        mLocation = (Button) findViewById(R.id.location);
        mLocation.setOnClickListener(MainActivity.this);
        mCamera = (Button) findViewById(R.id.camera);
        mCamera.setOnClickListener(MainActivity.this);
        mState = (Button) findViewById(R.id.state);
        mState.setOnClickListener(MainActivity.this);
        mStorage = (Button) findViewById(R.id.storage);
        mStorage.setOnClickListener(MainActivity.this);
        mSms = (Button) findViewById(R.id.sms);
        mSms.setOnClickListener(MainActivity.this);
        mSetting = (Button) findViewById(R.id.setting);
        mSetting.setOnClickListener(MainActivity.this);
        mSetting1 = (Button) findViewById(R.id.setting1);
        mSetting1.setOnClickListener(MainActivity.this);
        mSetting2 = (Button) findViewById(R.id.setting2);
        mSetting2.setOnClickListener(MainActivity.this);
        mSetting3 = (Button) findViewById(R.id.setting3);
        mSetting3.setOnClickListener(MainActivity.this);
        mclick1 = (Button) findViewById(R.id.click1);
        mclick1.setOnClickListener(MainActivity.this);
        mclick2 = (Button) findViewById(R.id.click2);
        mclick2.setOnClickListener(MainActivity.this);

    }
}
