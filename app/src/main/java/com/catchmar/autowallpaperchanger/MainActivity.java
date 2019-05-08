package com.catchmar.autowallpaperchanger;

import android.app.WallpaperManager;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.view.Display;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.catchmar.autowallpaperchanger.Receiver.BackgroundWallChangeService;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    public int SCREEN_HEIGHT, SCREEN_WIDTH;
    public ImageView imgPreview;
    Bitmap bitmap;
    BitmapDrawable bitmapDrawable;
    Button setAs, reload, btnStartSrv, btnStopSrv, btnSave;
    TextView tvInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        screenSizePicker();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imgPreview = findViewById(R.id.imgPreview);
        tvInfo = findViewById(R.id.tvInfo);
        tvInfo.setText("Screen size: " + SCREEN_WIDTH + "/" + SCREEN_HEIGHT);

        reload = findViewById(R.id.btnReload);
        reload.setOnClickListener(this);
        setAs = findViewById(R.id.btnSetAs);
        setAs.setOnClickListener(this);
        btnStartSrv = findViewById(R.id.btnStartSrv);
        btnStartSrv.setOnClickListener(this);
        btnStopSrv = findViewById(R.id.btnStopSrv);
        btnStopSrv.setOnClickListener(this);
        btnSave = findViewById(R.id.btnSave);
        btnSave.setOnClickListener(this);

        loadPicture();
    }

    public void screenSizePicker() {
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        SCREEN_WIDTH = size.x;
        SCREEN_HEIGHT = size.y;
    }

    @Override
    public void onClick(View v) {

        Intent intent = new Intent(this, BackgroundWallChangeService.class);

        switch (v.getId()) {
            case R.id.btnStartSrv:
                startService(intent);
                break;
            case R.id.btnStopSrv:
                stopService(intent);
                break;
            case R.id.btnReload:
                loadPicture();
                break;
            case R.id.btnSetAs:
                setWallpaper();
                break;
            case R.id.btnSave:
                try {
                    saveImage();
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(this, "Failed to save image", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    private void loadPicture() {
        String url = "https://picsum.photos/" + SCREEN_WIDTH + "/" + SCREEN_HEIGHT;
        try {
            Glide.with(this)
                    .load(url)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .skipMemoryCache(true)
                    .into(imgPreview);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    private void setWallpaper() {
        try {
            if (imgPreview != null) {
                bitmapDrawable = (BitmapDrawable) imgPreview.getDrawable();
                bitmap = bitmapDrawable.getBitmap();
                WallpaperManager wallpaperManager = WallpaperManager.getInstance(this);
                wallpaperManager.setBitmap(bitmap);
                Toast.makeText(this, "Wallpaper Changed", Toast.LENGTH_SHORT).show();
            } else loadPicture();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void saveImage() throws IOException {

        if (imgPreview != null) {
            bitmapDrawable = (BitmapDrawable) imgPreview.getDrawable();
            bitmap = bitmapDrawable.getBitmap();
        } else loadPicture();

        FileOutputStream outStream = null;
        File sdCard = Environment.getExternalStorageDirectory();
        File dir = new File(sdCard.getAbsolutePath() + "/AutoWallpaperChanger");    // folder name, where will be save images
        dir.mkdirs();
        String fileName = String.format(Locale.getDefault(), "%d.jpg", System.currentTimeMillis());
        File outFile = new File(dir, fileName);
        outStream = new FileOutputStream(outFile);
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outStream);
        outStream.flush();
        outStream.close();

        Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        intent.setData(Uri.fromFile(outFile));
        sendBroadcast(intent);

        Toast.makeText(this, "Image Saved", Toast.LENGTH_SHORT).show();
    }

}


