package com.titvt.fulizhan.httpss;

public interface HttpssCallback {
    void onHttpssOK(byte[] data);

    void onHttpssFail(int responseCode);

    void onHttpssError();
}
