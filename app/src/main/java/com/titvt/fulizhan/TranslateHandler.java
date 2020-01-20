package com.titvt.fulizhan;

import android.os.Handler;
import android.os.Message;
import android.view.WindowManager;

import androidx.annotation.NonNull;

class TranslateHandler extends Handler {
    private WindowManager windowManager;

    TranslateHandler(WindowManager windowManager) {
        this.windowManager = windowManager;
    }

    @Override
    public void handleMessage(@NonNull Message msg) {
        windowManager.addView(((TranslateView) msg.obj).view, ((TranslateView) msg.obj).layoutParams);
    }
}
