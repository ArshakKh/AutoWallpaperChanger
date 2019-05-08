package com.catchmar.autowallpaperchanger.Receiver;

import android.app.Service;
import android.app.WallpaperManager;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;

public class BackgroundWallChangeService extends Service {

    public static final int interval = 15000;  //interval between two services(Here Service run every 30 Minute 1800000)
    public int SCREEN_WIDTH;
    public int SCREEN_HEIGHT;
    private Handler mHandler = new Handler();   //run on another Thread to avoid crash
    private Timer mTimer = null;    //timer handling

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        screenSizePicker(); // getting device's screen size
        try {
            Toast.makeText(this, "Service created", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
        }
        //Timer Schedule
        if (mTimer != null) // Cancel if already existed
            mTimer.cancel();
        else
            mTimer = new Timer();   //recreate new
        mTimer.scheduleAtFixedRate(new TimeDisplay(), 0, interval);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        try {
            Toast.makeText(this, "Service Started", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mTimer.cancel();    //For Cancel Timer
        Toast.makeText(this, "Service is Destroyed", Toast.LENGTH_SHORT).show();
    }

    // getting device's screen size
    public void screenSizePicker() {
        SCREEN_WIDTH = Resources.getSystem().getDisplayMetrics().widthPixels;
        SCREEN_HEIGHT = Resources.getSystem().getDisplayMetrics().heightPixels;
    }

    //class TimeDisplay for handling task
    private class TimeDisplay extends TimerTask {
        @Override
        public void run() {
            // run on another thread
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    //run DownloadPicture class
                    DownloadPicture dp = new DownloadPicture();
                    dp.execute();
                }
            });
        }
    }

    //class DownloadPicture for Downloading random picture, and setting as Wallpaper
    private class DownloadPicture extends AsyncTask<URL, Void, Bitmap> {
        @Override
        protected Bitmap doInBackground(URL... urls) {
            String url = "https://picsum.photos/" + SCREEN_WIDTH + "/" + SCREEN_HEIGHT;
            try {
                HttpURLConnection con = (HttpURLConnection) (new URL(url)).openConnection();
                con.setDoInput(true);
                con.connect();
                InputStream is = con.getInputStream();
                return BitmapFactory.decodeStream(is);
            } catch (Throwable t) {
                t.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if (bitmap != null) {
                try {
                    WallpaperManager wallpaperManager = WallpaperManager.getInstance(getApplicationContext());
                    wallpaperManager.setBitmap(bitmap);
                    Toast.makeText(getApplicationContext(), "Wallpaper Changed", Toast.LENGTH_SHORT).show();
                } catch (IOException ex) {
                    ex.printStackTrace();
                    Toast.makeText(getApplicationContext(), "Failed to load", Toast.LENGTH_SHORT).show();
                }
            } else
                Toast.makeText(getApplicationContext(), "Failed to load. Check internet connection", Toast.LENGTH_SHORT).show();
        }
    }
}

