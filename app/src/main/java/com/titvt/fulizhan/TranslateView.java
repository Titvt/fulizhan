package com.titvt.fulizhan;

import android.view.View;
import android.view.WindowManager;

class TranslateView {
    View view;
    WindowManager.LayoutParams layoutParams;

    TranslateView(View view, WindowManager.LayoutParams layoutParams) {
        this.view = view;
        this.layoutParams = layoutParams;
    }
}
