package com.welab.permissiontest;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;

import com.hjq.permissions.XXPermissions;

/**
 * Created by xieshangwu on 2018/8/6
 */
public class SettingUtil {

    /**
     *
     * @param context
     */
    public static void goSetting(Context context) {
        try {
            Intent localIntent = new Intent();
            localIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            localIntent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
            localIntent.setData(Uri.fromParts("package", context.getPackageName(), null));
            context.startActivity(localIntent);
        } catch(Exception ee) {
            ee.printStackTrace();
        }
    }

    /**
     * XXPermissions ok
     * @param context
     */
    public static void goSetting1(Context context) {
        XXPermissions.gotoPermissionSettings(context);
    }

    /**
     * JumpPermissionManagement
     */
    public static void goSetting2(Activity activity) {
        try {
            JumpPermissionManagement.GoToSetting(activity);
        } catch(Exception e) {
            Toast.makeText(activity, "setting", Toast.LENGTH_SHORT).show();
            JumpPermissionManagement.ApplicationInfo(activity);
        }
    }

    /**
     * PermissionPageUtils ok
     * @param context
     */
    public static void goSetting3(Context context) {
        PermissionPageUtils permissionPageUtils = new PermissionPageUtils(context);
        permissionPageUtils.jumpPermissionPage();
    }
}
