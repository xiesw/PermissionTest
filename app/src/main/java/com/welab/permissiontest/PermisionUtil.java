package com.welab.permissiontest;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import com.yanzhenjie.permission.Action;
import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.Permission;

import java.util.List;

import static android.content.Context.LOCATION_SERVICE;

/**
 * Created by xieshangwu on 2018/8/6
 */
public class PermisionUtil {

    private static final String TAG = "pain.xie";

    public static void checkContacts(Context context) {
        check(context, Permission.ACCESS_COARSE_LOCATION);
    }

    public void openAppDetailPage(Context context) {
        try {
            Intent localIntent = new Intent();
            localIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            if(Build.VERSION.SDK_INT >= 9) {
                localIntent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
                localIntent.setData(Uri.fromParts("package", context.getPackageName(), null));
            } else if(Build.VERSION.SDK_INT <= 8) {
                localIntent.setAction(Intent.ACTION_VIEW);
                localIntent.setClassName("com.android.settings", "com.android.settings" +
                        ".InstalledAppDetails");
                localIntent.putExtra("com.android.settings.ApplicationPkgName",
                        context.getPackageName());
            }
            context.startActivity(localIntent);
        } catch(Exception ee) {
            ee.printStackTrace();
        }
    }

    public static void checkLocation(Context context) {
        // check(context, Permission.ACCESS_FINE_LOCATION);
        LocationManager locationManager =
                (LocationManager) context.getSystemService(LOCATION_SERVICE);
        try {
            if(null != locationManager && locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 0,
                        new LocationListener() {
                    @Override
                    public void onLocationChanged(Location location) {

                    }

                    @Override
                    public void onStatusChanged(String provider, int status, Bundle extras) {

                    }

                    @Override
                    public void onProviderEnabled(String provider) {

                    }

                    @Override
                    public void onProviderDisabled(String provider) {

                    }
                });
            }
        } catch(Exception e) {
            e.printStackTrace();

        }

    }

    public static void checkCamera(Context context) {
        check(context, Permission.CAMERA);
    }

    public static void checkState(Context context) {
        // check(context, Permission.READ_PHONE_STATE);
        try {
            TelephonyManager tm =
                    (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            String uuid = tm.getDeviceId();
            Log.e("pain.xie", "checkState: " + uuid);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public static void checkStorage(Context context) {
        check(context, Permission.READ_EXTERNAL_STORAGE);
    }

    public static void checkSms(Context context) {
        check(context, new String[]{Permission.CAMERA, Permission.WRITE_EXTERNAL_STORAGE,
                Permission.READ_PHONE_STATE, Permission.ACCESS_FINE_LOCATION});
    }

    public static void check(final Context context, final String permision) {
        AndPermission.with(context)
                .runtime()
                .permission(permision)
                .onGranted(new Action<List<String>>() {
                    @Override
                    public void onAction(List<String> data) {
                        Log.e(TAG, "pain. onGranted: ");
                        Toast.makeText(context, "onGranted", Toast.LENGTH_SHORT).show();
                    }
                })
                .onDenied(new Action<List<String>>() {
                    @Override
                    public void onAction(List<String> data) {
                        Log.e(TAG, "pain onDenied: ");
                        Toast.makeText(context, "onDenied", Toast.LENGTH_SHORT).show();

                        if(AndPermission.hasAlwaysDeniedPermission(context, permision)) {
                            Toast.makeText(context, "hasAlwaysDeniedPermission", Toast.LENGTH_SHORT)
                                    .show();
                            Log.e(TAG, "pain hasAlwaysDeniedPermission");
                        }
                    }

                })
                .start();
    }

    public static void check(final Context context, final String[] permision) {
        AndPermission.with(context)
                .runtime()
                .permission(permision)
                .onGranted(new Action<List<String>>() {
                    @Override
                    public void onAction(List<String> data) {
                        for(String datum : data) {
                            Log.e(TAG, "onGranted: " + datum);
                        }
                    }
                })
                .onDenied(new Action<List<String>>() {
                    @Override
                    public void onAction(List<String> data) {
                        for(String datum : data) {
                            Log.e(TAG, "onDenied: " + datum);
                            if(AndPermission.hasAlwaysDeniedPermission(context, datum)) {
                                Log.e(TAG, "hasAlwaysDeniedPermission: " + datum);
                            }
                        }

                    }

                })
                .start();
    }
}
