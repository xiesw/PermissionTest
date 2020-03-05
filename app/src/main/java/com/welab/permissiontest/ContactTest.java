package com.welab.permissiontest;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by xieshangwu on 2019/3/7
 * 联系人测试
 */
public class ContactTest {

    public static void readContact(Context context) {

        Cursor cursor = null;
        try {
            //获取内容提供器
            ContentResolver resolver = context.getContentResolver();
            //查询联系人数据
            cursor = resolver.query(
                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                    null, null, null, null, null);
            //遍历联系人列表
            Log.e("pain", "cursor");
            Toast.makeText(context,  " cursor", Toast.LENGTH_SHORT).show();
            while (cursor.moveToNext()) {
                //获取联系人姓名
                String name = cursor.getString(cursor.getColumnIndex(
                        ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                //获取联系人手机号
                String number = cursor.getString(cursor.getColumnIndex(
                        ContactsContract.CommonDataKinds.Phone.NUMBER));
                Log.e("pain", "Name:" + name + "\tPhone:" + number);
            }
        } catch (Exception e) {
            Log.e("pain", "read error");
            Toast.makeText(context,  " read error", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

    }
}
