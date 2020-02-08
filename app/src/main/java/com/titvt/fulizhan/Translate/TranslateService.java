package com.titvt.fulizhan.Translate;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.Image;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.titvt.fulizhan.R;

import java.util.ArrayList;
import java.util.Objects;

public class TranslateService extends Service {
    private TranslateBinder binder = new TranslateBinder(this);
    private ImageReader imageReader;
    private VirtualDisplay virtualDisplay;
    private WindowManager windowManager;
    private WindowManager.LayoutParams frameLayoutParams, ivLayoutParams;
    private ImageView iv;
    private FrameLayout frameLayout;
    private float x, y;
    private int screenWidth, screenHeight;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onCreate() {
        super.onCreate();
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        Point point = new Point();
        windowManager.getDefaultDisplay().getSize(point);
        screenWidth = point.x;
        screenHeight = point.y;
        frameLayoutParams = new WindowManager.LayoutParams();
        frameLayoutParams.type = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY : WindowManager.LayoutParams.TYPE_PHONE;
        frameLayoutParams.width = screenWidth;
        frameLayoutParams.height = screenHeight;
        frameLayoutParams.x = 0;
        frameLayoutParams.y = 0;
        frameLayoutParams.format = PixelFormat.RGBA_8888;
        frameLayoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;
        ivLayoutParams = new WindowManager.LayoutParams();
        ivLayoutParams.type = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY : WindowManager.LayoutParams.TYPE_PHONE;
        ivLayoutParams.width = 128;
        ivLayoutParams.height = 128;
        ivLayoutParams.x = 0;
        ivLayoutParams.y = 0;
        ivLayoutParams.format = PixelFormat.RGBA_8888;
        ivLayoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;
        iv = new ImageView(getApplicationContext());
        iv.setImageResource(R.mipmap.ic_launcher_round);
        iv.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    x = event.getRawX();
                    y = event.getRawY();
                    break;
                case MotionEvent.ACTION_MOVE:
                    ivLayoutParams.x += event.getRawX() - x;
                    ivLayoutParams.y += event.getRawY() - y;
                    windowManager.updateViewLayout(iv, ivLayoutParams);
                    x = event.getRawX();
                    y = event.getRawY();
            }
            return false;
        });
        iv.setOnClickListener(v -> {
            windowManager.removeView(iv);
            binder.activity.screenshot();
        });
        windowManager.addView(iv, ivLayoutParams);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        windowManager.removeView(iv);
        if (virtualDisplay != null)
            virtualDisplay.release();
        stopSelf();
    }

    public void initScreenShot(MediaProjectionManager mediaProjectionManager, int resultCode, Intent data) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            Notification notification = new Notification.Builder(getApplicationContext(), "福利栈").setContentTitle("福利栈").setContentText("已开启截屏翻译").setWhen(System.currentTimeMillis()).setSmallIcon(R.mipmap.ic_launcher_round).setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher_round)).build();
            ((NotificationManager) Objects.requireNonNull(getSystemService(Context.NOTIFICATION_SERVICE))).createNotificationChannel(new NotificationChannel("福利栈", "福利栈", NotificationManager.IMPORTANCE_NONE));
            startForeground(1, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION);
        }
        MediaProjection mediaProjection = mediaProjectionManager.getMediaProjection(resultCode, data);
        imageReader = ImageReader.newInstance(screenWidth, screenHeight, PixelFormat.RGBA_8888, 1);
        virtualDisplay = mediaProjection.createVirtualDisplay("福利栈", screenWidth, screenHeight, getApplicationContext().getResources().getDisplayMetrics().densityDpi, DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR, imageReader.getSurface(), null, null);
    }

    public Bitmap screenshot() {
        Image image = imageReader.acquireLatestImage();
        Bitmap bitmap = Bitmap.createBitmap(image.getPlanes()[0].getRowStride() / image.getPlanes()[0].getPixelStride(), image.getHeight(), Bitmap.Config.ARGB_8888);
        bitmap.copyPixelsFromBuffer(image.getPlanes()[0].getBuffer());
        bitmap = Bitmap.createBitmap(bitmap, 0, 0, image.getWidth(), image.getHeight());
        image.close();
        return bitmap;
    }

    public void screenshotCallback(ArrayList<TranslateRecord> translateRecords) {
        frameLayout = new FrameLayout(getApplicationContext());
        for (TranslateRecord translateRecord : translateRecords) {
            if (translateRecord.y < 30)
                continue;
            TextView textView = new TextView(getApplicationContext());
            textView.setLayoutParams(new FrameLayout.LayoutParams(translateRecord.width, translateRecord.height));
            textView.setX(translateRecord.x);
            textView.setY(translateRecord.y - 15);
            textView.setText(translateRecord.target_text);
            textView.setGravity(Gravity.CENTER);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                textView.setAutoSizeTextTypeWithDefaults(TextView.AUTO_SIZE_TEXT_TYPE_UNIFORM);
            textView.setSingleLine();
            textView.setTextColor(getResources().getColor(R.color.blue));
            textView.setBackground(getDrawable(R.color.white));
            frameLayout.addView(textView);
        }
        Button button = new Button(getApplicationContext());
        button.setLayoutParams(new FrameLayout.LayoutParams(240, 120));
        button.setX(screenWidth / 2 - 120);
        button.setY(screenHeight - 150);
        button.setText("关闭");
        button.setTextSize(18);
        button.setTextColor(getResources().getColor(R.color.white));
        button.setBackground(getDrawable(R.drawable.close_button));
        button.setOnClickListener(v -> {
            windowManager.removeView(frameLayout);
            windowManager.addView(iv, ivLayoutParams);
        });
        frameLayout.addView(button);
        new Handler(getMainLooper()).post(() -> windowManager.addView(frameLayout, frameLayoutParams));
    }
}