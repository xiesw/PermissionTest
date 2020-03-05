package com.welab.permissiontest;

/**
 * Created by xieshangwu on 2019/4/8
 */

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Environment;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

public class GetPhoneInfoUtil {




    private static final String KEY_MIUI_VERSION_CODE = "ro.miui.ui.version.code";
    private static final String KEY_MIUI_VERSION_NAME = "ro.miui.ui.version.name";
    private static final String KEY_EMUI_VERSION_CODE = "ro.build.hw_emui_api_level";
    private static final String KEY_FLYME_ID_FALG_KEY = "ro.build.display.id";
    private static final String KEY_FLYME_ICON_FALG = "persist.sys.use.flyme.icon";
    private static final String KEY_FLYME_SETUP_FALG = "ro.meizu.setupwizard.flyme";
    private static final String KEY_FLYME_PUBLISH_FALG = "ro.flyme.published";
    private Properties mProper;

    public static GetPhoneInfoUtil mPhoneInfoUtil;

    public enum ROM_TYPE {

        MIUI_ROM,

        FLYME_ROM,

        EMUI_ROM,

        OTHER_ROM

    }

    private GetPhoneInfoUtil(){
        if(mProper==null){
            mProper=new Properties();
        }
        try {
            mProper.load(new FileInputStream(new File(Environment.getRootDirectory(), "build.prop")));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    };


    public static GetPhoneInfoUtil getInstance() {
        if(mPhoneInfoUtil==null){
            synchronized (GetPhoneInfoUtil.class) {
                if(mPhoneInfoUtil==null){
                    mPhoneInfoUtil=new GetPhoneInfoUtil();
                }
            }
        }
        return mPhoneInfoUtil;
    }



    /**
     * 获取手机厂商
     * @return
     */
    public String getDeviceBrand() {
        return android.os.Build.BRAND;
    }

    /**
     * 获取手机型号
     *
     * @return
     */
    public String getPhoneModel() {
        return android.os.Build.MODEL;
    }


    /**
     * 获取系统版本号
     *
     * @return
     */
    public  String getSystemVersion() {
        return android.os.Build.VERSION.RELEASE;
    }


    /**
     * 获取当前应用版本
     * @return
     */
    public  String getVersion(Context context) {
        try {
            PackageManager manager = context.getPackageManager();
            PackageInfo info = manager.getPackageInfo(context.getPackageName(), 0);
            String version = info.versionName;
            return version;
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    /**
     * 判断ROM是否为MIUI
     * @return
     */
    public boolean isMIUI()  {

        return mProper.containsKey(KEY_MIUI_VERSION_CODE) || mProper.containsKey(KEY_MIUI_VERSION_NAME);
    }

    /**
     * 判断ROM是否为EMUI
     * @param name
     * @return
     */
    public boolean isEMUI() {

        return mProper.containsKey(KEY_EMUI_VERSION_CODE);
    }


    /**
     * 判断ROM是否为Flyme
     * @return
     */
    public boolean isFlyme(){
        return mProper.containsKey(KEY_FLYME_ICON_FALG) || mProper.containsKey(KEY_FLYME_SETUP_FALG) || mProper.containsKey(KEY_FLYME_PUBLISH_FALG);
    }


    /**
     * 获取ROM版本信息
     * @return
     */
    public String getRomInfo(){
        if(isMIUI()){
            return ROM_TYPE.MIUI_ROM+" "+mProper.getProperty(KEY_MIUI_VERSION_NAME) ;
        }else if(isFlyme()){
            return ROM_TYPE.FLYME_ROM+" "+mProper.getProperty(KEY_FLYME_ID_FALG_KEY);
        }else if(isEMUI()){
            return ROM_TYPE.EMUI_ROM+" "+mProper.getProperty(KEY_EMUI_VERSION_CODE);
        }else{
            return ROM_TYPE.OTHER_ROM+"";
        }


    }

}