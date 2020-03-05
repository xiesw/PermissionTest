/*
 * Copyright © 2017 LiZhimin All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.welab.permissiontest;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AppOpsManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

/**
 * Desc:Android 6.0运行时权限处理工具类
 * 适配类型：
 * 1、6.0以上大部分手机会走正常流程，如果运行权限没有授权就会系统弹出dialog让用户选择授权，如果用户拒绝授权且没有勾选不再提示，下一次询问的还会弹出dialog，
 * 如果用户拒绝授权并勾选不再提示，那么下一次询问权限是否授权，如果发现权限未得到授权就不会弹dialog了，这时我们要自定义弹出dialog，让用户去设置界面手动对权限授权
 * 2、6.0以上如：小米、360等手机上，一个权限在第一次弹dialog让用户选择是否授权，如果用户拒绝授权，那么下一次弹出dialog，不管用户是否选择授权，系统都会默认授权
 * 回调授权成功的回调方法
 * 3、4.3~6.0的手机：默认对清单文件权限授予权限，手机是有权限管理，有的手机是可以通过权限管理手动更改权限，有的手机无论如何在权限管理中改动了权限状态，系统都是默认授权
 * 4、4.3以下   默认授权  无权限管理
 * <p>
 * 解决方法，6.0以上手机对应部分机型只要检测权限不是允许的就让用户手动去设置中心授予权限，其他手机默认走6.0运行时权限申请流程
 * 4.3~6.0手机只要是检测权限管理中的某个权限状态不是允许就跳转到权限管理界面让用户手动修改
 */
public class PermissionUtils {

    private static final String PERMISSION_SP = "permission";
    private static boolean needCheckPermission = true;
    private static int mRequestCode = -1;
    private static OnPermissionListener mOnPermissionListener;
    public static HashMap<String, Integer> opStringIndex = new HashMap<>();
    public static HashMap<String, String> permissionOpstr = new HashMap<>();
    //单个权限
    public static final String[] SMS_PERMISSION_GROUP = new String[]{Manifest.permission.SEND_SMS, Manifest.permission.READ_SMS, Manifest.permission.RECEIVE_SMS};
    public static final String[] LOCATION_PERMISSION_GROUP = new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
    public static final String[] RECORD_PERMISSION_GROUP = new String[]{Manifest.permission.RECORD_AUDIO};
    public static final String[] CAMERA_PERMISSION_GROUP = new String[]{Manifest.permission.CAMERA};
    public static final String[] STORAGE_PERMISSION_GROUP = new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};
    public static  AlertDialog alertDialog;
    static {
        //19~23必须通过权限对应的int并通过反射才能查询权限状态
        opStringIndex.put(AppOpsManager.OPSTR_COARSE_LOCATION, 0);
        opStringIndex.put(AppOpsManager.OPSTR_FINE_LOCATION, 1);
        opStringIndex.put(AppOpsManager.OPSTR_READ_CONTACTS, 4);
        opStringIndex.put(AppOpsManager.OPSTR_WRITE_CONTACTS, 5);
        opStringIndex.put(AppOpsManager.OPSTR_READ_CALENDAR, 8);
        opStringIndex.put(AppOpsManager.OPSTR_WRITE_CALENDAR, 9);
        opStringIndex.put(AppOpsManager.OPSTR_CALL_PHONE, 13);
        opStringIndex.put(AppOpsManager.OPSTR_READ_PHONE_STATE, 51);
        opStringIndex.put(AppOpsManager.OPSTR_READ_SMS, 14);
        opStringIndex.put(AppOpsManager.OPSTR_RECEIVE_SMS, 16);
        opStringIndex.put(AppOpsManager.OPSTR_SEND_SMS, 20);
        opStringIndex.put(AppOpsManager.OPSTR_CAMERA, 26);
        opStringIndex.put(AppOpsManager.OPSTR_RECORD_AUDIO, 27);
        opStringIndex.put(AppOpsManager.OPSTR_READ_EXTERNAL_STORAGE, 59);
        opStringIndex.put(AppOpsManager.OPSTR_WRITE_EXTERNAL_STORAGE, 60);

        permissionOpstr.put(Manifest.permission.SEND_SMS, AppOpsManager.OPSTR_SEND_SMS);
        permissionOpstr.put(Manifest.permission.READ_SMS, AppOpsManager.OPSTR_READ_SMS);
        permissionOpstr.put(Manifest.permission.RECEIVE_SMS, AppOpsManager.OPSTR_RECEIVE_SMS);
        permissionOpstr.put(Manifest.permission.CAMERA, AppOpsManager.OPSTR_CAMERA);
        permissionOpstr.put(Manifest.permission.RECORD_AUDIO, AppOpsManager.OPSTR_RECORD_AUDIO);
        permissionOpstr.put(Manifest.permission.ACCESS_COARSE_LOCATION, AppOpsManager.OPSTR_COARSE_LOCATION);
        permissionOpstr.put(Manifest.permission.ACCESS_FINE_LOCATION, AppOpsManager.OPSTR_FINE_LOCATION);
        permissionOpstr.put(Manifest.permission.READ_CALENDAR, AppOpsManager.OPSTR_READ_CALENDAR);
        permissionOpstr.put(Manifest.permission.WRITE_CALENDAR, AppOpsManager.OPSTR_WRITE_CALENDAR);
        permissionOpstr.put(Manifest.permission.CALL_PHONE, AppOpsManager.OPSTR_CALL_PHONE);
        permissionOpstr.put(Manifest.permission.READ_PHONE_STATE, AppOpsManager.OPSTR_READ_PHONE_STATE);
        permissionOpstr.put(Manifest.permission.READ_CONTACTS, AppOpsManager.OPSTR_READ_CONTACTS);
        permissionOpstr.put(Manifest.permission.WRITE_CONTACTS, AppOpsManager.OPSTR_WRITE_CONTACTS);
        permissionOpstr.put(Manifest.permission.READ_EXTERNAL_STORAGE, AppOpsManager.OPSTR_READ_EXTERNAL_STORAGE);
        permissionOpstr.put(Manifest.permission.WRITE_EXTERNAL_STORAGE, AppOpsManager.OPSTR_WRITE_EXTERNAL_STORAGE);

    }

    public interface OnPermissionListener {

        void onPermissionGranted();

        void onPermissionDenied(String[] deniedPermissions, boolean alwaysDenied);
    }

    static public class PermissionListenerAdapter implements OnPermissionListener {

        @Override
        public void onPermissionGranted() {

        }

        @Override
        public void onPermissionDenied(String[] deniedPermissions, boolean alwaysDenied) {

        }
    }

    public enum RequestCode {

        PHONE(0x00, "电话", "我们需要访问电话权限才能正常使用该功能"),
        LOCATION(0x01, "定位", "我们需要定位权限才能正常使用该功能"),
        CAMERA(0x02, "照相机", "我们需要访问相机权限才能正常使用该功能"),
        RECORD(0x03, "录音或麦克风", "我们需要麦克风权限才能正常使用该功能"),
        AUDIO(0x04, "语音", "我们需要访问语音权限才能正常使用该功能"),
        SMS(0x05, "短信", "我们需要访问短信权限才能正常使用该功能"),
        EXTERNAL(0x06, "存储", "我们需要访问存储权限才能正常使用该功能"),
        CONTACTS(0x07, "通讯录", "我们需要访问通讯录权限才能正常使用该功能"),
        CALENDAR(0x10,"日历提醒","我们需要访问日历提醒权限才能正常使用该功能"),
        MORE(0x08, "多个权限", "多个权限"),
        NONE(0x09, "无", "无");
        public int code;
        public String name;
        public String message;

        RequestCode(int code, String name, String message) {
            this.code = code;
            this.name = name;
            this.message = message;
        }

        public RequestCode setMessage(String message) {
            this.message = message;
            return this;
        }

        public RequestCode setName(String name) {
            this.name = name;
            return this;
        }

        public static RequestCode getRequestType(int code) {
            switch (code) {
                case 0x00:
                    return PHONE;
                case 0x01:
                    return LOCATION;
                case 0x02:
                    return CAMERA;
                case 0x03:
                    return RECORD;
                case 0x04:
                    return AUDIO;
                case 0x05:
                    return SMS;
                case 0x06:
                    return EXTERNAL;
                case 0x07:
                    return CONTACTS;
                case 0x08:
                    return MORE;
                case 0x10:
                    return CALENDAR;
                default:
                    return NONE;
            }

        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    public static void requestPermissionsAgain(@NonNull Context context, @NonNull String[] permissions,
                                               @NonNull RequestCode requestCode) {
        if (context instanceof Activity) {
            ((Activity) context).requestPermissions(permissions, requestCode.code);
        } else {
            throw new IllegalArgumentException("Context must be an Activity");
        }
    }


    @TargetApi(Build.VERSION_CODES.M)
    public static void requestPermissions(@NonNull Context context, @NonNull RequestCode requestCode,
                                          @NonNull String[] permissions, OnPermissionListener listener) {
        if (needCheckPermission) {
            mRequestCode = requestCode.code;
            mOnPermissionListener = listener;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                //>=6.0的手机，对部分360、小米处理设置中禁止却又默认授权的情况
                if (needMySelfAdapter(context) && !checkAppOpsList(context, permissions)) {
                    showPermissionManagerDialog(context, requestCode.name);
                    return;
                }
                //权限申请的正常流程，以上都是针对特定机型和特定版本的预处理
                String[] deniedPermissions = getDeniedPermissions(context, permissions);
                if (deniedPermissions.length > 0) {
                    requestPermissionsAgain(context, deniedPermissions, requestCode);
                } else {
                    if (mOnPermissionListener != null) {
                        mOnPermissionListener.onPermissionGranted();
                        mOnPermissionListener = null;
                    }
                }
            } else if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2){
                //6.0以下4.3或以上(安装就授予所有权限但是又有授权管理界面)
                if (!judgePermissionIsGranted(context, permissions)) {
                    showPermissionManagerDialog(context, requestCode.name);
                    return;
                }
                if (mOnPermissionListener != null) {
                    mOnPermissionListener.onPermissionGranted();
                    mOnPermissionListener = null;
                }
            }else{
                if (mOnPermissionListener != null) {
                    mOnPermissionListener.onPermissionGranted();
                    mOnPermissionListener = null;
                }
            }

        } else {
            if (mOnPermissionListener != null){
                mOnPermissionListener.onPermissionGranted();
                mOnPermissionListener = null;
            }
        }
    }
    /**
     * 请求权限结果，对应Activity中onRequestPermissionsResult()方法。
     */
    public static void onRequestPermissionsResult(@NonNull final Activity context, final int requestCode,
                                                  @NonNull String[] permissions, int[] grantResults) {
        final RequestCode requestCodeEnum = RequestCode.getRequestType(requestCode);
        if (mRequestCode != -1 && requestCode == mRequestCode) {
            if (mOnPermissionListener != null) {
                final String[] deniedPermissions = getDeniedPermissions(context, permissions);
                if (deniedPermissions.length > 0) {
                    boolean alwaysDenied = hasAlwaysDeniedPermission(context, permissions);
                    if (alwaysDenied) {
                        showPermissionManagerDialog(context, requestCodeEnum.name);
                    } else {
                        String content;
                        if(requestCodeEnum.code ==RequestCode.MORE.code){//more,多个权限
                            content = "我需要获取"+requestCodeEnum.name+"权限才能正常使用该功能";
                        }else{
                            content = requestCodeEnum.message;
                        }
                        alertDialog= new AlertDialog.Builder(context,R.style.Theme_dialog_self).setTitle("温馨提示")
                                .setMessage(content)
                                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        mOnPermissionListener = null;
                                    }
                                })
                                .setPositiveButton("验证权限", new DialogInterface.OnClickListener() {
                                    @RequiresApi(api = Build.VERSION_CODES.M)
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        requestPermissionsAgain(context, deniedPermissions,
                                                requestCodeEnum);
                                    }
                                }).setOnCancelListener(new DialogInterface.OnCancelListener() {
                            @Override
                            public void onCancel(DialogInterface dialog) {
                                mOnPermissionListener = null;
                            }
                        }).show();
                    }
                    mOnPermissionListener.onPermissionDenied(deniedPermissions, alwaysDenied);
                } else {
                    mOnPermissionListener.onPermissionGranted();
                    mOnPermissionListener = null;
                }
            }
        }
    }


    /**
     * 获取请求权限中需要授权的权限
     */
    private static String[] getDeniedPermissions(@NonNull Context context, @NonNull String[] permissions) {
        List<String> deniedPermissions = new ArrayList();
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_DENIED) {
                deniedPermissions.add(permission);
            }
        }
        return deniedPermissions.toArray(new String[deniedPermissions.size()]);
    }

    /**
     * 是否彻底拒绝了某项权限
     */
    private static boolean hasAlwaysDeniedPermission(@NonNull Context context, @NonNull String... deniedPermissions) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return false;
        }
        boolean rationale;
        for (String permission : deniedPermissions) {
            rationale = ActivityCompat.shouldShowRequestPermissionRationale((Activity) context, permission);
            if (!rationale) {
                return true;
            }
        }
        return false;
    }
    public static void disMissDialog(){
        if (alertDialog!=null&&alertDialog.isShowing()){
            alertDialog.dismiss();
        }
    }

    private static void showPermissionManagerDialog(final Context context, String str) {
        new androidx.appcompat.app.AlertDialog.Builder(context, R.style.Theme_dialog_self).setTitle("获取" + str + "权限被禁用")
                .setMessage("请在 设置-应用管理-" + context.getString(R.string.app_name) + "-权限管理 (将" + str + "权限打开)")
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mOnPermissionListener = null;
                    }
                }).setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        mOnPermissionListener = null;
                    }
                }).setPositiveButton("去设置", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        intent.setData(Uri.parse("package:" + context.getPackageName()));
                        context.startActivity(intent);
                        mOnPermissionListener = null;
                    }
                }).show();
    }


    /**
     * 查看原生态的权限是否有授权 注：version>=23可以通过该方法直接判断某个权限在权限管理中是否被授予权限
     *
     * @param context
     * @param op      如定位权限AppOpsManager.OPSTR_FINE_LOCATION
     * @return
     */
    public static boolean checkAppops(Context context, String op) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            int checkOp = getAppOpsState(context, op);
            if (checkOp == AppOpsManager.MODE_IGNORED || checkOp == AppOpsManager.MODE_DEFAULT) {
                return false;
            }
        }
        return true;
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public static int getAppOpsState(Context context, String op) {
        AppOpsManager appOpsManager = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
        int checkOp = appOpsManager.checkOpNoThrow(op, Binder.getCallingUid(), context.getPackageName());
        return checkOp;
    }

    /**
     * 查看原生态的权限是否有授权 这里适配的版本是   19<=version<23
     *
     * @param context
     * @param permissions
     * @return
     */
    private static boolean judgePermissionIsGranted(Context context, String[] permissions) {
        if (permissions == null && permissions.length <= 0) {
            return false;
        }
        for (String permission : permissions) {
            try {
                Object object = context.getSystemService(Context.APP_OPS_SERVICE);
                if (object == null) {
                    return false;
                }
                Class localClass = object.getClass();
                Class[] arrayOfClass = new Class[3];
                arrayOfClass[0] = Integer.TYPE;
                arrayOfClass[1] = Integer.TYPE;
                arrayOfClass[2] = String.class;
                Method method = localClass.getMethod("checkOp", arrayOfClass);

                if (method == null) {
                    return false;
                }
                Object[] arrayOfObject = new Object[3];
                arrayOfObject[0] = opStringIndex.get(permissionOpstr.get(permission));
                arrayOfObject[1] = Integer.valueOf(Binder.getCallingUid());
                arrayOfObject[2] = context.getPackageName();
                int m = ((Integer) method.invoke(object, arrayOfObject)).intValue();
                if (m == AppOpsManager.MODE_IGNORED || m == 4) {
                    return false;
                }
            } catch (Exception e) {
                //e.printStackTrace();
                Log.e("PermissionUtils", "judgePermissionIsGranted: Exception="+e);
            }
        }

        return true;
    }


    public static boolean checkAppOpsList(Context context, String[] permissions) {
        if (permissions == null || permissions.length <= 0) {
            return false;
        }
        for (String permission : permissions) {
            if (!checkAppops(context, permissionOpstr.get(permission))) {
                return false;
            }
        }
        return true;
    }


    // 需要适配的系统
    private static final String KEY_MIUI_VERSION_CODE = "ro.miui.ui.version.code";
    private static final String KEY_MIUI_VERSION_NAME = "ro.miui.ui.version.name";
    private static final String KEY_MIUI_INTERNAL_STORAGE = "ro.miui.internal.storage";
    private static final String KEY_EMUI_API_LEVEL = "ro.build.hw_emui_api_level";
    private static final String KEY_EMUI_VERSION = "ro.build.version.emui";
    private static final String KEY_EMUI_CONFIG_HW_SYS_VERSION = "ro.confg.hw_systemversion";
    private static final String KEY_QIKU_MODEL = "ro.qiku.product.ifaa.model";
    private static final String KEY_QIKU_NAME = "ro.qiku.product.devicename";
    private static final String KEY_QIKU_CPU_TYPE = "ro.qiku.product.cpu";


    public static void savePropState(Context context, boolean isMIUI) {
        SharedPreferences sp = context.getSharedPreferences(PERMISSION_SP, Context.MODE_PRIVATE);
        sp.edit().putBoolean("OPS_STATE", isMIUI).commit();
    }

    public static boolean getProptate(Context context) {
        SharedPreferences sp = context.getSharedPreferences(PERMISSION_SP, Context.MODE_PRIVATE);
        return sp.getBoolean("OPS_STATE", false);
    }

    public static boolean needMySelfAdapter(Context context) {
        FileInputStream propFileStream = null;
        try {
            boolean adapterState = getProptate(context);
            if (adapterState) {
                return true;
            }
            Properties prop = new Properties();
            propFileStream = new FileInputStream(new File(Environment.getRootDirectory(), "build.prop"));
            prop.load(propFileStream);
            if (prop.getProperty(KEY_MIUI_VERSION_CODE, null) != null
                    || prop.getProperty(KEY_MIUI_VERSION_NAME, null) != null
                    || prop.getProperty(KEY_MIUI_INTERNAL_STORAGE, null) != null) {
                savePropState(context, true);
                return true;//小米
            } else if (prop.getProperty(KEY_EMUI_API_LEVEL, null) != null
                    || prop.getProperty(KEY_EMUI_VERSION, null) != null
                    || prop.getProperty(KEY_EMUI_CONFIG_HW_SYS_VERSION, null) != null) {
                savePropState(context, true);
                return false;//华为
            } else if (getSystemProperty("ro.build.display.id", "").toLowerCase().contains("flyme")) {
                savePropState(context, false);
                return false;//魅族
            } else if (prop.getProperty(KEY_QIKU_MODEL, null) != null
                    || prop.getProperty(KEY_QIKU_NAME, null) != null
                    || prop.getProperty(KEY_QIKU_CPU_TYPE, null) != null) {
                savePropState(context, true);
                return true;//奇酷360
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (propFileStream != null) {
                try {
                    propFileStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return false;
    }

    private static String getSystemProperty(String key, String defaultValue) {
        try {
            Class<?> clz = Class.forName("android.os.SystemProperties");
            Method get = clz.getMethod("get", String.class, String.class);
            return (String) get.invoke(clz, key, defaultValue);
        } catch (Exception e) {
        }
        return defaultValue;
    }
}