package com.titvt.fulizhan.Remote;

import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Message;
import android.widget.ImageView;

import androidx.annotation.NonNull;

class RemoteScreenHandler extends Handler {
    private ImageView iv;

    RemoteScreenHandler(ImageView iv) {
        this.iv = iv;
    }

    @Override
    public void handleMessage(@NonNull Message msg) {
        iv.setImageBitmap(BitmapFactory.decodeByteArray((byte[]) msg.obj, 0, ((byte[]) msg.obj).length));
    }
}
