package com.welab.permissiontest;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.yanzhenjie.permission.Action;
import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.Permission;

import java.util.List;

/**
 * Created by xieshangwu on 2018/8/6
 */
public class PermisionUtil {

    private static final String TAG = "pain.xie";

    public static void checkContacts(Context context) {
        check(context, Permission.WRITE_CONTACTS);
    }

    public static void checkLocation(Context context) {
        check(context, Permission.ACCESS_FINE_LOCATION);
    }

    public static void checkCamera(Context context) {
        check(context, Permission.CAMERA);
    }

    public static void checkState(Context context) {
        check(context, Permission.READ_PHONE_STATE);
    }

    public static void checkStorage(Context context) {
        check(context, Permission.READ_EXTERNAL_STORAGE);
    }

    public static void checkSms(Context context) {
        check(context, Permission.READ_SMS);
    }

    public static void check(final Context context, final String permision) {
        AndPermission.with(context)
                .runtime()
                .permission(permision)
                .onGranted(new Action<List<String>>() {
                    @Override
                    public void onAction(List<String> data) {
                        for(String datum : data) {
                            Log.d(TAG, "onAction: " + datum);
                        }
                        Toast.makeText(context, "onGranted", Toast.LENGTH_SHORT).show();
                    }
                })
                .onDenied(new Action<List<String>>() {
                    @Override
                    public void onAction(List<String> data) {
                        for(String datum : data) {
                            Log.d(TAG, "onAction: " + datum);
                        }
                        Toast.makeText(context, "onDenied", Toast.LENGTH_SHORT).show();

                        if(AndPermission.hasAlwaysDeniedPermission(context, permision)) {
                            Toast.makeText(context, "hasAlwaysDeniedPermission", Toast.LENGTH_SHORT)
                                    .show();
                        }
                    }

                })
                .start();
    }

}
