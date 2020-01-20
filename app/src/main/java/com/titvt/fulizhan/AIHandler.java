package com.titvt.fulizhan;

import android.os.Handler;
import android.os.Message;

import androidx.annotation.NonNull;

class AIHandler extends Handler {
    private AIAdapter aiAdapter;

    AIHandler(AIAdapter aiAdapter) {
        this.aiAdapter = aiAdapter;
    }

    @Override
    public void handleMessage(@NonNull Message msg) {
        aiAdapter.addMessage((String) msg.obj, false);
    }
}
