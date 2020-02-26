package com.titvt.fulizhan.httpss;

public interface HttpssCallback {
    void onSuccess(HttpssResult result, int responseCode, byte[] data);

    void onError(HttpssResult result);
}
