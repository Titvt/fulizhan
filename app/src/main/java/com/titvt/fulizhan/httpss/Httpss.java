package com.titvt.fulizhan.httpss;

import android.os.Handler;
import android.os.Looper;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;

public class Httpss {
    private String url, method;
    private byte[] data;
    private int connectTimeout, readTimeout;
    private HashMap<String, String> requestProperty;
    private HttpssCallback callback;

    public Httpss() {
        method = "GET";
    }

    public Httpss(String url) {
        this.url = url;
        method = "GET";
    }

    public Httpss(String url, String method) {
        this.url = url;
        if (method != null)
            this.method = method.toUpperCase();
        else
            this.method = "GET";
    }

    public Httpss setUrl(String url) {
        this.url = url;
        return this;
    }

    public Httpss setMethod(String method) {
        if (method != null)
            this.method = method.toUpperCase();
        else
            this.method = "GET";
        return this;
    }

    public Httpss setData(byte[] data) {
        this.data = data;
        return this;
    }

    public Httpss setConnectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
        return this;
    }

    public Httpss setReadTimeout(int readTimeout) {
        this.readTimeout = readTimeout;
        return this;
    }

    public Httpss setRequestProperty(HashMap<String, String> requestProperty) {
        this.requestProperty = requestProperty;
        return this;
    }

    public Httpss setCallback(HttpssCallback callback) {
        this.callback = callback;
        return this;
    }

    public Httpss request() {
        if (url == null) {
            if (callback != null)
                new Handler(Looper.getMainLooper()).post(() -> callback.onError(HttpssResult.HTTPSS_NO_URL));
        } else if (method.equals("GET") || method.equals("POST")) {
            new Thread() {
                private String url, method;
                private byte[] data;
                private int connectTimeout, readTimeout;
                private HashMap<String, String> requestProperty;
                private HttpssCallback callback;

                Thread init(String url, String method, byte[] data, int connectTimeout, int readTimeout, HashMap<String, String> requestProperty, HttpssCallback callback) {
                    this.url = url;
                    this.method = method;
                    this.data = data;
                    this.connectTimeout = connectTimeout;
                    this.readTimeout = readTimeout;
                    this.requestProperty = requestProperty;
                    this.callback = callback;
                    return this;
                }

                @Override
                public void run() {
                    try {
                        HttpURLConnection httpURLConnection = (HttpURLConnection) new URL(url).openConnection();
                        httpURLConnection.setRequestMethod(method);
                        if (connectTimeout > 0)
                            httpURLConnection.setConnectTimeout(connectTimeout);
                        if (readTimeout > 0)
                            httpURLConnection.setReadTimeout(readTimeout);
                        if (requestProperty != null)
                            for (String key : requestProperty.keySet())
                                httpURLConnection.setRequestProperty(key, requestProperty.get(key));
                        if (method.equals("POST") || data != null) {
                            httpURLConnection.setDoOutput(true);
                            DataOutputStream dataOutputStream = new DataOutputStream(httpURLConnection.getOutputStream());
                            dataOutputStream.write(data);
                            dataOutputStream.flush();
                            dataOutputStream.close();
                        }
                        httpURLConnection.connect();
                        DataInputStream dataInputStream = new DataInputStream(httpURLConnection.getInputStream());
                        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(0);
                        byte[] bytes = new byte[2048];
                        int size;
                        while (true) {
                            size = dataInputStream.read(bytes);
                            if (size == 2048)
                                byteArrayOutputStream.write(bytes);
                            else if (size > 0)
                                byteArrayOutputStream.write(Arrays.copyOf(bytes, size));
                            else
                                break;
                        }
                        final int responseCode = httpURLConnection.getResponseCode();
                        final byte[] data = byteArrayOutputStream.toByteArray();
                        if (callback != null)
                            new Handler(Looper.getMainLooper()).post(() -> callback.onSuccess(HttpssResult.HTTPSS_OK, responseCode, data));
                    } catch (Exception e) {
                        if (callback != null)
                            new Handler(Looper.getMainLooper()).post(() -> callback.onError(HttpssResult.HTTPSS_FAIL));
                    }
                }
            }.init(url, method, data, connectTimeout, readTimeout, requestProperty, callback).start();
        } else {
            if (callback != null)
                new Handler(Looper.getMainLooper()).post(() -> callback.onError(HttpssResult.HTTPSS_ERROR_METHOD));
        }
        return this;
    }
}