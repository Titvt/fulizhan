package com.titvt.fulizhan;

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
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Objects;

public class TranslateService extends Service {
    TranslateBinder binder = new TranslateBinder(this);
    TranslateHandler translateHandler;
    ImageReader imageReader;
    VirtualDisplay virtualDisplay;
    WindowManager windowManager;
    WindowManager.LayoutParams layoutParams;
    ImageView iv;
    float x, y;
    int screenWidth, screenHeight;
    boolean translated = false;
    ArrayList<View> views = new ArrayList<>();

    public TranslateService() {
    }

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
        layoutParams = new WindowManager.LayoutParams();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            layoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        else
            layoutParams.type = WindowManager.LayoutParams.TYPE_PHONE;
        layoutParams.width = 128;
        layoutParams.height = 128;
        layoutParams.x = 0;
        layoutParams.y = 0;
        layoutParams.format = PixelFormat.RGBA_8888;
        layoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        iv = new ImageView(getApplicationContext());
        iv.setImageResource(R.mipmap.ic_launcher_round);
        iv.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    x = event.getRawX();
                    y = event.getRawY();
                    break;
                case MotionEvent.ACTION_MOVE:
                    layoutParams.x += event.getRawX() - x;
                    layoutParams.y += event.getRawY() - y;
                    windowManager.updateViewLayout(iv, layoutParams);
                    x = event.getRawX();
                    y = event.getRawY();
            }
            return false;
        });
        iv.setOnClickListener(v -> {
            if (translated) {
                while (views.size() > 0) {
                    windowManager.removeView(views.get(views.size() - 1));
                    views.remove(views.size() - 1);
                }
                translated = false;
            } else {
                windowManager.removeView(iv);
                binder.activity.screenShot();
                translated = true;
            }
        });
        windowManager.addView(iv, layoutParams);
        translateHandler = new TranslateHandler(windowManager);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        windowManager.removeView(iv);
        while (views.size() > 0) {
            windowManager.removeView(views.get(views.size() - 1));
            views.remove(views.size() - 1);
        }
        translated = false;
        if (virtualDisplay != null) {
            virtualDisplay.release();
            virtualDisplay = null;
        }
        stopSelf();
    }

    void initScreenShot(MediaProjectionManager mediaProjectionManager, int resultCode, Intent data) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            Notification notification = new Notification.Builder(getApplicationContext(), "福利栈").setContentTitle("福利栈").setContentText("已开启截屏翻译").setWhen(System.currentTimeMillis()).setSmallIcon(R.mipmap.ic_launcher_round).setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher_round)).build();
            (((NotificationManager) Objects.requireNonNull(getSystemService(Context.NOTIFICATION_SERVICE)))).createNotificationChannel(new NotificationChannel("福利栈", "福利栈", NotificationManager.IMPORTANCE_NONE));
            startForeground(1, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION);
        }
        MediaProjection mediaProjection = mediaProjectionManager.getMediaProjection(resultCode, data);
        imageReader = ImageReader.newInstance(screenWidth, screenHeight, PixelFormat.RGBA_8888, 1);
        virtualDisplay = mediaProjection.createVirtualDisplay("福利栈", screenWidth, screenHeight, getApplicationContext().getResources().getDisplayMetrics().densityDpi, DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR, imageReader.getSurface(), null, null);
    }

    Bitmap screenShot() {
        Image image = imageReader.acquireLatestImage();
        Bitmap bitmap = Bitmap.createBitmap(image.getPlanes()[0].getRowStride() / image.getPlanes()[0].getPixelStride(), image.getHeight(), Bitmap.Config.ARGB_8888);
        bitmap.copyPixelsFromBuffer(image.getPlanes()[0].getBuffer());
        bitmap = Bitmap.createBitmap(bitmap, 0, 0, image.getWidth(), image.getHeight());
        image.close();
        return bitmap;
    }

    void screenShotCallback(ArrayList<TranslateActivity.TranslateRecord> translateRecords) {
        for (TranslateActivity.TranslateRecord translateRecord : translateRecords) {
            WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                layoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
            else
                layoutParams.type = WindowManager.LayoutParams.TYPE_PHONE;
            layoutParams.width = translateRecord.width + 20;
            layoutParams.height = translateRecord.height + 20;
            layoutParams.x = translateRecord.x + (translateRecord.width - screenWidth) / 2;
            layoutParams.y = translateRecord.y - screenHeight / 2;
            layoutParams.format = PixelFormat.RGBA_8888;
            layoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
            TextView textView = new TextView(getApplicationContext());
            textView.setText(translateRecord.target_text);
            textView.setGravity(Gravity.CENTER);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                textView.setAutoSizeTextTypeWithDefaults(TextView.AUTO_SIZE_TEXT_TYPE_UNIFORM);
            }
            textView.setMaxLines(1);
            textView.setTextColor(getApplicationContext().getResources().getColor(R.color.blue));
            textView.setBackground(getApplicationContext().getDrawable(R.color.white));
            Message message = new Message();
            message.obj = new TranslateObject(textView, layoutParams);
            translateHandler.sendMessage(message);
            views.add(textView);
        }
        Message message = new Message();
        message.obj = new TranslateObject(iv, layoutParams);
        translateHandler.sendMessage(message);
    }

    static class TranslateHandler extends Handler {
        WindowManager windowManager;

        TranslateHandler(WindowManager windowManager) {
            this.windowManager = windowManager;
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            windowManager.addView(((TranslateObject) msg.obj).view, ((TranslateObject) msg.obj).layoutParams);
        }
    }

    class TranslateBinder extends Binder {
        TranslateService service;
        TranslateActivity activity;

        TranslateBinder(TranslateService service) {
            this.service = service;
        }

        void setActivity(TranslateActivity activity) {
            this.activity = activity;
        }
    }

    class TranslateObject {
        View view;
        WindowManager.LayoutParams layoutParams;

        TranslateObject(View view, WindowManager.LayoutParams layoutParams) {
            this.view = view;
            this.layoutParams = layoutParams;
        }
    }
}