package com.welab.permissiontest;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

/**
 * 安装下载接收器
 */

public class InstallReceiver extends BroadcastReceiver {

    private static final String TAG = "InstallReceiver";


    // 安装下载接收器
    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent.getAction().equals(DownloadManager.ACTION_DOWNLOAD_COMPLETE)) {
            // installApk(context);
            Log.e(TAG, "onReceive: " + intent.toString());
            long downloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
            Log.e(TAG, "onReceive: " + downloadId);
            DownloadManager dm = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
            DownloadManager.Query query = new DownloadManager.Query().setFilterById(downloadId);
            Cursor c = dm.query(query);
            if (c != null) {
                if (c.moveToFirst()) {
                    String fileUri = c.getString(c.getColumnIndexOrThrow(DownloadManager.COLUMN_LOCAL_URI));
                    Log.e(TAG, "onReceive: " + fileUri );
                    // TODO 你可以在这里处理你的文件
                    installApk(context, fileUri);
                }
                c.close();
            }
        }
    }

    // 安装Apk
    private void installApk(Context context, String fileUri) {

        try {
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setDataAndType(Uri.parse(fileUri+"1"), "application/vnd.android.package-archive");
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(i);
        } catch(Exception e) {
            Log.e(TAG, "安装失败");
            e.printStackTrace();
        }

    }
}