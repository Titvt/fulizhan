package com.titvt.fulizhan.Translate;

import android.os.Binder;

class TranslateBinder extends Binder {
    TranslateService service;
    TranslateFragment activity;

    TranslateBinder(TranslateService service) {
        this.service = service;
    }

    void setActivity(TranslateFragment activity) {
        this.activity = activity;
    }
}
