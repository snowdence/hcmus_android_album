package com.wifosoft.wumbum;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import android.util.Log;


import com.wifosoft.wumbum.filter.ImageFileFilter;
import com.wifosoft.wumbum.helper.MediaHelper;
import com.wifosoft.wumbum.helper.QueryAlbums;

import java.io.File;
import java.util.ArrayList;



@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class LookForMediaJob extends JobService {

    private final String TAG = "LookForMediaJob";
    private boolean DEBUG = true;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.wtf(TAG, "JOB created");
    }

    @Override
    public boolean onStartJob(final JobParameters jobParameters) {
        Log.wtf(TAG, "JOB started");
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    ArrayList<String> whiteList = QueryAlbums.getInstance(getApplicationContext()).getFolders(QueryAlbums.INCLUDED);
                    for (String s : whiteList) {
                        scanFolder(s);
                        Log.wtf(TAG, "Scanned: " + s);
                    }
                    if(DEBUG)
                        notification(whiteList);
                } finally {
                    jobFinished(jobParameters, false);
                }
            }
        }).start();

        return true;
    }

    private void notification(ArrayList<String> list) {

        StringBuilder builder = new StringBuilder();
        for (String s : list)
            builder.append(s).append("\n");


        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle("Looked for media")
                        .setAutoCancel(true)
                        .setContentText(builder.toString()); //Required on Gingerbread and below

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Service.NOTIFICATION_SERVICE);
        Notification notification = mBuilder.build();

        notificationManager.notify(0, notification);
    }

    private void scanFolder(String path) {
        String[] list = new File(path).list(new ImageFileFilter(true));
        if (list != null)
            MediaHelper.scanFile(getApplicationContext(), list);

    }

    @Override
    public boolean onStopJob(JobParameters jobParameters) {
        Log.wtf(TAG, "JOB stop");
        return false;
    }
}
