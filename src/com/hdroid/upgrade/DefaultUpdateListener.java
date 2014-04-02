package com.hdroid.upgrade;

import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

import com.hdroid.downloader.DownloadTask;
import com.hdroid.log.snow.Log;

/**
 * Created by snowdream on 1/2/14.
 */
public class DefaultUpdateListener extends AbstractUpdateListener {
    private NotificationManager notificationManager = null;
    private NotificationCompat.Builder notificationBuilder = null;
    private AlertDialog alertDialog = null;

    @Override
    public void onStart() {
        Context context = getContext();
        if (context != null) {
            alertDialog = new AlertDialog.Builder(context)
                    .setMessage("正在获取最新版本信息")
                    .setCancelable(false)
                    .create();
            alertDialog.show();
        }
    }

    @Override
    public void onFinish() {
        if (alertDialog != null) {
            alertDialog.dismiss();
        }
    }

    @Override
    public void onShowUpdateUI(final UpdateInfo info) {
        if (info == null) {
            return;
        }

        Context context = getContext();
        if (context != null) {
            AlertDialog dialog = new AlertDialog.Builder(context)
                    .setTitle("更新提示")
                    .setMessage(getUpdateTips(info))
                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            informUpdate(info);
                        }
                    })
                    .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            informCancel(info);
                        }
                    })
                    .setNeutralButton("忽略", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            informSkip(info);
                        }
                    })
                    .setCancelable(false)
                    .create();
            dialog.show();
        }
    }

    @Override
    public void onShowNoUpdateUI() {
        Context context = getContext();
        if (context != null) {
            AlertDialog dialog = new AlertDialog.Builder(context)
                    .setMessage("当前已是最新版本")
                    .setCancelable(true)
                    .create();
            dialog.show();
        }
    }

    @Override
    public void onShowUpdateProgressUI(final UpdateInfo info, final DownloadTask task, final int progress) {
        Context context = getContext();
        if (context != null && task != null && info != null) {
            try {
//                Bitmap largeIcon = null;
                PackageManager pm = context.getPackageManager();
                Drawable icon = pm.getApplicationIcon(context.getPackageName());
//                if (icon != null){
//                    largeIcon =((BitmapDrawable) icon).getBitmap();
//                }

                PendingIntent contentIntent = PendingIntent.getActivity(context, 0, new Intent(), PendingIntent.FLAG_CANCEL_CURRENT);
                String contentTitle = info.getAppName();
                String contentText = new StringBuffer().append(progress)
                        .append("%").toString();
                int smallIcon = context.getApplicationInfo().icon;
                if (notificationManager == null) {
                    notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                }

                if (notificationBuilder == null) {
                    notificationBuilder = new NotificationCompat.Builder(context)
                            // .setLargeIcon(largeIcon)
                            .setSmallIcon(smallIcon)
                            .setContentTitle(contentTitle)
                            .setContentText(contentText)
                            .setContentIntent(contentIntent)
                            .setAutoCancel(true);
                }
                notificationBuilder.setContentText(contentText);
                notificationBuilder.setProgress(100, progress, false);
                notificationManager.notify(0, notificationBuilder.build());
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
                Log.e("can not get the package info", e);
            }
        }
    }

    @Override
    public void ExitApp() {
        Context context = getContext();
        if (context != null) {
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_HOME);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        }
    }
}
