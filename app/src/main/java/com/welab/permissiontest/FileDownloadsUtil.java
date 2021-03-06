package com.welab.permissiontest;

import android.accounts.NetworkErrorException;
import android.app.DownloadManager;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.os.Message;
import androidx.annotation.NonNull;
import android.util.Log;

import com.yanzhenjie.permission.Action;
import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.Permission;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
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
        downloadManager = (DownloadManager) mReactContext.getSystemService(Context
                .DOWNLOAD_SERVICE);
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(apkUrl));
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE);
        request.setMimeType("application/vnd.android.package-archive");
        if(!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            return;
        }
        localFilePath = Environment.getDownloadCacheDirectory() + "/" + fileName;
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
                size = cursor.getString(cursor.getColumnIndex(DownloadManager
                        .COLUMN_BYTES_DOWNLOADED_SO_FAR));
                sizeTotal = cursor.getString(cursor.getColumnIndex(DownloadManager
                        .COLUMN_TOTAL_SIZE_BYTES));
                int status = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS));
                Message msg = Message.obtain();
                Log.e("pain.xie", "queryTaskByIdandUpdateView: " + size);
                switch(status) {
                    case DownloadManager.STATUS_PAUSED:
                    case DownloadManager.STATUS_PENDING:
                    case DownloadManager.STATUS_RUNNING:
                        //正在下载
                        break;
                    case DownloadManager.STATUS_SUCCESSFUL:
                        //完成
                        if(null != scheduledExecutorService && !scheduledExecutorService
                                .isShutdown())
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

    private void installAPK() {
        DownloadManager dm = (DownloadManager) mReactContext.getSystemService(Context
                .DOWNLOAD_SERVICE);
        DownloadManager.Query query = new DownloadManager.Query().setFilterById(lastDownloadId);
        Cursor c = dm.query(query);
        if(c != null) {
            if(c.moveToFirst()) {
                String filePath = c.getString(c.getColumnIndexOrThrow(DownloadManager
                        .COLUMN_LOCAL_URI));
                Log.e("pain.xie", "installAPK: " + filePath);
                AndPermission.with(mReactContext)
                        .install()
                        .file(new File(filePath.replace("file://", "")))
                        .start();
            }
            c.close();
        }
//        DownloadManager dm = (DownloadManager) mReactContext.getSystemService(Context
//                .DOWNLOAD_SERVICE);
//        DownloadManager.Query query = new DownloadManager.Query().setFilterById(lastDownloadId);
//        Cursor c = dm.query(query);
//        if(c != null) {
//            if(c.moveToFirst()) {
//                String fileUri = c.getString(c.getColumnIndexOrThrow(DownloadManager
//                        .COLUMN_LOCAL_URI));
//                Log.e("pain", fileUri);
//                Intent i = new Intent(Intent.ACTION_VIEW);
//                i.setDataAndType(Uri.parse(fileUri), "application/vnd.android.package-archive");
//                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                mReactContext.startActivity(i);
//            }
//            c.close();
//        }
    }


    public static void startDownload(Context context) {
        String downloadUrl = "http://dl.yzcdn.cn/koudaitong.apk";
        DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context
                .DOWNLOAD_SERVICE);
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(downloadUrl));
        request.setTitle("app-release.apk");
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI);
        request.setAllowedOverRoaming(false);
        request.setMimeType("application/vnd.android.package-archive");
        request.setNotificationVisibility(DownloadManager.Request
                .VISIBILITY_VISIBLE_NOTIFY_COMPLETED);

        //设置文件存放路径
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "app-release" +
                ".apk");
        downloadManager.enqueue(request);
    }

    public static String post(String url, String content) {
        HttpURLConnection conn = null;
        try {
            // 创建一个URL对象
            URL mURL = new URL(url);
            // 调用URL的openConnection()方法,获取HttpURLConnection对象
            conn = (HttpURLConnection) mURL.openConnection();

            conn.setRequestMethod("POST");// 设置请求方法为post
            conn.setReadTimeout(5000);// 设置读取超时为5秒
            conn.setConnectTimeout(10000);// 设置连接网络超时为10秒
            conn.setDoOutput(true);// 设置此方法,允许向服务器输出内容

            // post请求的参数
            String data = content;
            // 获得一个输出流,向服务器写数据,默认情况下,系统不允许向服务器输出内容
            OutputStream out = conn.getOutputStream();// 获得一个输出流,向服务器写数据
            out.write(data.getBytes());
            out.flush();
            out.close();

            int responseCode = conn.getResponseCode();// 调用此方法就不必再使用conn.connect()方法
            if (responseCode == 200) {

                InputStream is = conn.getInputStream();
                String response = getStringFromInputStream(is);
                return response;
            } else {
                throw new NetworkErrorException("response status is "+responseCode);
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (conn != null) {
                conn.disconnect();// 关闭连接
            }
        }

        return null;
    }

    public static String get(String url) {
        HttpURLConnection conn = null;
        try {
            // 利用string url构建URL对象
            URL mURL = new URL(url);
            conn = (HttpURLConnection) mURL.openConnection();

            conn.setRequestMethod("GET");
            conn.setReadTimeout(5000);
            conn.setConnectTimeout(10000);

            int responseCode = conn.getResponseCode();
            if (responseCode == 200) {

                InputStream is = conn.getInputStream();
                String response = getStringFromInputStream(is);
                return response;
            } else {
                throw new NetworkErrorException("response status is "+responseCode);
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {

            if (conn != null) {
                conn.disconnect();
            }
        }

        return null;
    }

    private static String getStringFromInputStream(InputStream is)
            throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        // 模板代码 必须熟练
        byte[] buffer = new byte[1024];
        int len = -1;
        while ((len = is.read(buffer)) != -1) {
            os.write(buffer, 0, len);
        }
        is.close();
        String state = os.toString();// 把流中的数据转换成字符串,采用的编码是utf-8(模拟器默认编码)
        os.close();
        return state;
    }

}
