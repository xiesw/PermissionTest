package com.welab.permissiontest;

import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.os.Message;
import android.support.annotation.NonNull;
import android.util.Log;

import com.yanzhenjie.permission.Action;
import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.Permission;

import java.io.File;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


/**
 * TODO: 文件下载工具类
 * terry
 * 08/05/2017
 */
public class FileDownloadsUtil {

    private static final String TAG = FileDownloadsUtil.class.getSimpleName();
    private static final String showProgressEvent = "showDownloadProgress";
    private Context mReactContext;
    private DownloadManager downloadManager;
    private long lastDownloadId;
    private ScheduledExecutorService scheduledExecutorService;
    private String localFilePath;
    private String updateProgressEvent;

    public FileDownloadsUtil(Context reactContext) {
        this.mReactContext = reactContext;
    }


    public void downloadApkFile(final String apkUrl, final String eventName) {
        AndPermission.with(mReactContext)
                .runtime()
                .permission(Permission.READ_EXTERNAL_STORAGE)
                .onGranted(new Action<List<String>>() {
                    @Override
                    public void onAction(List<String> permissions) {
                        // 允许 存储 权限后下载
                        download(apkUrl, eventName);
                    }
                })
                .onDenied(new Action<List<String>>() {
                    @Override
                    public void onAction(@NonNull List<String> permissions) {
                        if(AndPermission.hasAlwaysDeniedPermission(mReactContext, permissions)) {
                        }
                    }
                })
                .start();
    }

    private void download(String apkUrl, String eventName) {
        updateProgressEvent = eventName;
        String fileName = getFileName(apkUrl);
        downloadManager = (DownloadManager) mReactContext.getSystemService(Context.DOWNLOAD_SERVICE);
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(apkUrl));
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE);
        request.setMimeType("application/vnd.android.package-archive");
        if(!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            return;
        }
        localFilePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/" + fileName;
        File file = new File(localFilePath);
        if(file.exists()) {
            file.delete();
        }
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName);
        lastDownloadId = downloadManager.enqueue(request);
        Log.e(TAG, "download: " + lastDownloadId);
        if(null != scheduledExecutorService && !scheduledExecutorService.isShutdown()) {
            scheduledExecutorService.shutdownNow();
        }
        scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        scheduledExecutorService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                queryTaskByIdandUpdateView(lastDownloadId);
            }
        }, 0, 500, TimeUnit.MILLISECONDS);
    }

    private String getFileName(String apkUrl) {
        return apkUrl.substring(apkUrl.lastIndexOf('/') + 1, apkUrl.length());
    }

    public void cancleDownloads() {
        try {
            downloadManager.remove(lastDownloadId);
            if(null != scheduledExecutorService && !scheduledExecutorService.isShutdown()) {
                scheduledExecutorService.shutdownNow();
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    private void queryTaskByIdandUpdateView(long id) {
        DownloadManager.Query query = new DownloadManager.Query().setFilterById(id);
        Cursor cursor = null;
        String size = "0";
        String sizeTotal = "0";
        try {
            cursor = downloadManager.query(query);
            if(null != cursor && cursor.moveToNext()) {
                size = cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));
                sizeTotal = cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES));
                int status = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS));
                Message msg = Message.obtain();
                switch(status) {
                    case DownloadManager.STATUS_PAUSED:
                    case DownloadManager.STATUS_PENDING:
                    case DownloadManager.STATUS_RUNNING:
                        //正在下载
                        break;
                    case DownloadManager.STATUS_SUCCESSFUL:
                        //完成
                        if(null!=scheduledExecutorService && !scheduledExecutorService.isShutdown())
                            scheduledExecutorService.shutdownNow();
                        installAPK();
                        break;
                    case DownloadManager.STATUS_FAILED:
                        //清除已下载的内容，重新下载
                        break;
                }
            }
        } finally {
            if(cursor != null) {
                cursor.close();
            }
        }
    }

     private void installAPK(){
         DownloadManager dm = (DownloadManager) mReactContext.getSystemService(Context.DOWNLOAD_SERVICE);
         DownloadManager.Query query = new DownloadManager.Query().setFilterById(lastDownloadId);
         Cursor c = dm.query(query);
         if (c != null) {
             if (c.moveToFirst()) {
                 String fileUri = c.getString(c.getColumnIndexOrThrow(DownloadManager.COLUMN_LOCAL_URI));
                 Log.e("pain", fileUri);
                 Intent i = new Intent(Intent.ACTION_VIEW);
                 i.setDataAndType(Uri.parse(fileUri), "application/vnd.android.package-archive");
                 i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                 mReactContext.startActivity(i);
             }
             c.close();
         }
     }

}
