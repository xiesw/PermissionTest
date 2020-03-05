package com.welab.permissiontest;

import android.app.Application;
import android.os.Build;
import android.util.Log;

import com.yanzhenjie.permission.checker.StandardChecker;

import java.lang.reflect.Field;


/**
 * Created by pain.xie on 2019-09-04
 */
public class MyApplication extends Application {

    private static final String TAG = "pain.xie";
    // 数据接收的 URL
    final String SA_SERVER_URL = "YOUR_SERVER_URL";


    @Override
    public void onCreate() {
        super.onCreate();
        hookAndPermissionChecker();
    }

    /****
     * 由于AndPermission库未适配Android Q，在权限检查时候，替换为标准方式以解决严格模式下面不兼容Android Q的问题
     * Android Q
     * fieldObj = class com.yanzhenjie.permission.checker.DoubleChecker
     * newFieldObj = class com.yanzhenjie.permission.checker.StandardChecker
     */
    private void hookAndPermissionChecker() {
        try {
            Class<?> andPermission = Class.forName("com.yanzhenjie.permission.AndPermission");
            Field permissionChecker = andPermission.getDeclaredField("PERMISSION_CHECKER");
            permissionChecker.setAccessible(true);
            Object fieldObj = permissionChecker.get(null);
            Log.d(TAG, "hookAndPermissionChecker fieldObj = " + fieldObj.getClass());

            permissionChecker.set(fieldObj, new StandardChecker());
            Object newFieldObj = permissionChecker.get(null);
            Log.d(TAG, "hookAndPermissionChecker newFieldObj = " + newFieldObj.getClass());

            Log.d(TAG, "hookAndPermissionChecker success!");
        } catch(Exception e) {
            Log.e("pain.xie", "hookAndPermissionChecker: " + "error");
            e.printStackTrace();
        }
    }


    //copy from Android Q sdk .
    private boolean isAndroidQ() {
        return Build.VERSION.SDK_INT > 28;
    }


}
