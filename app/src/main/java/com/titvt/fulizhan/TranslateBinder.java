package com.titvt.fulizhan;

import android.os.Binder;

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
