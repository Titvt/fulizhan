package com.titvt.fulizhan.Translate;

public class TranslateRecord {
    String target_text;
    int x, y, width, height;

    public TranslateRecord(String target_text, int x, int y, int width, int height) {
        this.target_text = target_text;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }
}
