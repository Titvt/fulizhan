package com.titvt.fulizhan;

import android.os.Handler;
import android.os.Message;
import android.view.WindowManager;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;

class TranslateHandler extends Handler {
    private WindowManager windowManager;
    private WindowManager.LayoutParams layoutParams;

    TranslateHandler(WindowManager windowManager, WindowManager.LayoutParams layoutParams) {
        this.windowManager = windowManager;
        this.layoutParams = layoutParams;
    }

    @Override
    public void handleMessage(@NonNull Message msg) {
        windowManager.addView((FrameLayout) msg.obj, layoutParams);
    }
}
