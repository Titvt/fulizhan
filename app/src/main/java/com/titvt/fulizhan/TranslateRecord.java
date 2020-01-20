package com.titvt.fulizhan;

class TranslateRecord {
    String target_text;
    int x, y, width, height;

    TranslateRecord(String target_text, int x, int y, int width, int height) {
        this.target_text = target_text;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }
}
