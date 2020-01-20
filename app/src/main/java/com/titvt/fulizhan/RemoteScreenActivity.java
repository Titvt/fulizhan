package com.titvt.fulizhan;

import android.graphics.Point;
import android.os.Bundle;
import android.os.Message;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;

public class RemoteScreenActivity extends AppCompatActivity implements View.OnClickListener {
    ImageView iv;
    Socket socket;
    String host;
    RemoteScreenHandler remoteScreenHandler;
    GestureDetector gestureDetector;
    ScaleGestureDetector scaleGestureDetector;
    float scaleFactor = 1.0f;
    int screenWidth, screenHeight, targetWidth, targetHeight;
    long deltaTime = 0;
    boolean shift_on = false,
            ctrl_on = false,
            win_on = false,
            alt_on = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_remote_screen);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        iv = findViewById(R.id.iv);
        host = getIntent().getStringExtra("host");
        Point point = new Point();
        getWindowManager().getDefaultDisplay().getSize(point);
        screenWidth = point.x;
        screenHeight = point.y;
        remoteScreenHandler = new RemoteScreenHandler(iv);
        gestureDetector = new GestureDetector(this, new GestureDetector.OnGestureListener() {
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
                new RemoteScreenSocketSender(socket, 0, new Point(x, y)).start();
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
                new RemoteScreenSocketSender(socket, 1, new Point(x, y)).start();
            }

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                return true;
            }
        });
        scaleGestureDetector = new ScaleGestureDetector(this, new ScaleGestureDetector.OnScaleGestureListener() {
            @Override
            public boolean onScale(ScaleGestureDetector detector) {
                scaleFactor *= detector.getScaleFactor();
                iv.setLayoutParams(new FrameLayout.LayoutParams((int) (screenWidth * scaleFactor), (int) (screenHeight * scaleFactor)));
                return true;
            }

            @Override
            public boolean onScaleBegin(ScaleGestureDetector detector) {
                return true;
            }

            @Override
            public void onScaleEnd(ScaleGestureDetector detector) {

            }
        });
        new Thread() {
            @Override
            public void run() {
                try {
                    socket = new Socket(host, 9981);
                    DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
                    dataOutputStream.writeInt(1);
                    dataOutputStream.flush();
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
                        remoteScreenHandler.sendMessage(message);
                    }
                } catch (Exception ignored) {
                }
            }
        }.start();
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
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.keyboard_open:
                findViewById(R.id.keyboard).setVisibility(View.VISIBLE);
                view.setVisibility(View.GONE);
                break;
            case R.id.keyboard_close:
                findViewById(R.id.keyboard).setVisibility(View.GONE);
                findViewById(R.id.keyboard_open).setVisibility(View.VISIBLE);
                break;
            case R.id.keyboard_Shift:
                new RemoteScreenSocketSender(socket, 2, ((Button) view).getText().toString()).start();
                if (shift_on)
                    view.setBackground(getResources().getDrawable(R.drawable.keyboard));
                else
                    view.setBackground(getResources().getDrawable(R.drawable.keyboard_down));
                shift_on = !shift_on;
                break;
            case R.id.keyboard_Ctrl:
                new RemoteScreenSocketSender(socket, 2, ((Button) view).getText().toString()).start();
                if (ctrl_on)
                    view.setBackground(getResources().getDrawable(R.drawable.keyboard));
                else
                    view.setBackground(getResources().getDrawable(R.drawable.keyboard_down));
                ctrl_on = !ctrl_on;
                break;
            case R.id.keyboard_Win:
                new RemoteScreenSocketSender(socket, 2, ((Button) view).getText().toString()).start();
                if (win_on)
                    view.setBackground(getResources().getDrawable(R.drawable.keyboard));
                else
                    view.setBackground(getResources().getDrawable(R.drawable.keyboard_down));
                win_on = !win_on;
                break;
            case R.id.keyboard_Alt:
                new RemoteScreenSocketSender(socket, 2, ((Button) view).getText().toString()).start();
                if (alt_on)
                    view.setBackground(getResources().getDrawable(R.drawable.keyboard));
                else
                    view.setBackground(getResources().getDrawable(R.drawable.keyboard_down));
                alt_on = !alt_on;
                break;
            default:
                new RemoteScreenSocketSender(socket, 2, ((Button) view).getText().toString()).start();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        gestureDetector.onTouchEvent(event);
        scaleGestureDetector.onTouchEvent(event);
        return super.onTouchEvent(event);
    }
}