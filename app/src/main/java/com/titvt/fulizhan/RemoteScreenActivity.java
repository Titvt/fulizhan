package com.titvt.fulizhan;

import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;

import butterknife.BindView;
import butterknife.ButterKnife;

public class RemoteScreenActivity extends AppCompatActivity {
    Socket socket;
    String host = "localhost";
    MyHandler handler;
    GestureDetector gestureDetector;
    ScaleGestureDetector scaleGestureDetector;
    float scaleFactor = 1.0f;
    int screenWidth, screenHeight, targetWidth, targetHeight;
    long deltaTime = 0;
    @BindView(R.id.iv)
    ImageView iv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_remote_screen);
        ButterKnife.bind(this);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        Bundle bundle = getIntent().getBundleExtra("bundle");
        if (bundle != null)
            host = bundle.getString("host");
        Point point = new Point();
        getWindowManager().getDefaultDisplay().getSize(point);
        screenWidth = point.x;
        screenHeight = point.y;
        gestureDetector = new GestureDetector(this, new OnGestureListener());
        scaleGestureDetector = new ScaleGestureDetector(this, new OnScaleGestureListener());
        handler = new MyHandler(iv);
        new SocketThread().start();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK)
            if (System.currentTimeMillis() - deltaTime > 1000) {
                deltaTime = System.currentTimeMillis();
                Toast.makeText(this, "再次按下返回键退出远控", Toast.LENGTH_SHORT).show();
                return true;
            }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        gestureDetector.onTouchEvent(event);
        scaleGestureDetector.onTouchEvent(event);
        return super.onTouchEvent(event);
    }

    class OnGestureListener implements GestureDetector.OnGestureListener {
        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        @Override
        public void onShowPress(MotionEvent e) {

        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            int x = (int) (targetWidth / 2 - (screenWidth / 2.0 - (e.getX() - iv.getX()) / scaleFactor) / screenHeight * targetHeight),
                    y = (int) ((e.getY() - iv.getY()) / scaleFactor / screenHeight * targetHeight);
            if (x < 0 || x >= targetWidth || y < 0 || y >= targetHeight)
                return true;
            new SocketSender(0, new Point(x, y)).start();
            return true;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            iv.setX(iv.getX() - distanceX);
            iv.setY(iv.getY() - distanceY);
            return true;
        }

        @Override
        public void onLongPress(MotionEvent e) {
            int x = (int) (targetWidth / 2 - (screenWidth / 2.0 - (e.getX() - iv.getX()) / scaleFactor) / screenHeight * targetHeight),
                    y = (int) ((e.getY() - iv.getY()) / scaleFactor / screenHeight * targetHeight);
            if (x < 0 || x >= targetWidth || y < 0 || y >= targetHeight)
                return;
            new SocketSender(1, new Point(x, y)).start();
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            return true;
        }
    }

    class OnScaleGestureListener implements ScaleGestureDetector.OnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            scaleFactor *= detector.getScaleFactor();
            Point point = new Point();
            getWindowManager().getDefaultDisplay().getSize(point);
            iv.setLayoutParams(new LinearLayout.LayoutParams((int) (point.x * scaleFactor), (int) (point.y * scaleFactor)));
            return true;
        }

        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector) {
            return true;
        }

        @Override
        public void onScaleEnd(ScaleGestureDetector detector) {

        }
    }

    static class MyHandler extends Handler {
        ImageView iv;

        MyHandler(ImageView iv) {
            this.iv = iv;
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            iv.setImageBitmap(BitmapFactory.decodeByteArray((byte[]) msg.obj, 0, ((byte[]) msg.obj).length));
        }
    }

    class SocketThread extends Thread {
        @Override
        public void run() {
            try {
                socket = new Socket(host, 9981);
                DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
                dataOutputStream.writeInt(1);
                dataOutputStream.flush();
                new SocketReceiver().start();
            } catch (Exception ignored) {
            }
        }
    }

    class SocketSender extends Thread {
        int code;
        Object object;

        SocketSender(int code, Object object) {
            this.code = code;
            this.object = object;
        }

        @Override
        public void run() {
            try {
                DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
                dataOutputStream.write(code);
                switch (code) {
                    case 0:
                    case 1:
                        dataOutputStream.writeInt(((Point) object).x);
                        dataOutputStream.writeInt(((Point) object).y);
                }
                dataOutputStream.flush();
            } catch (Exception ignored) {
            }
        }
    }

    class SocketReceiver extends Thread {
        @Override
        public void run() {
            try {
                DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());
                int size, num;
                ByteArrayOutputStream byteArrayOutputStream;
                byte[] bytes = new byte[2048];
                Message message;
                targetWidth = dataInputStream.readInt();
                targetHeight = dataInputStream.readInt();
                while (!socket.isClosed()) {
                    while ((size = dataInputStream.readInt()) == 0)
                        sleep(1);
                    byteArrayOutputStream = new ByteArrayOutputStream(size);
                    do {
                        if (size < 2048)
                            num = dataInputStream.read(bytes, 0, size);
                        else
                            num = dataInputStream.read(bytes);
                        byteArrayOutputStream.write(bytes, 0, num);
                        size -= num;
                    } while (size != 0);
                    message = new Message();
                    message.obj = byteArrayOutputStream.toByteArray();
                    handler.sendMessage(message);
                }
            } catch (Exception ignored) {
            }
        }
    }
}