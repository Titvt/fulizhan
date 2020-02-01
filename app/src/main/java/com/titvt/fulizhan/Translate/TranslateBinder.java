package com.titvt.fulizhan.Translate;

import android.os.Binder;

import com.titvt.fulizhan.MainActivity;

public class TranslateBinder extends Binder {
    public TranslateService service;
    MainActivity activity;

    TranslateBinder(TranslateService service) {
        this.service = service;
    }

    public void setActivity(MainActivity activity) {
        this.activity = activity;
    }
}
