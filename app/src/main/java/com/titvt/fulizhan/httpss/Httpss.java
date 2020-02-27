package com.titvt.fulizhan.httpss;

import android.os.Handler;
import android.os.Looper;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;

public class Httpss {
    private String url, method;
    private byte[] dataBytes;
    private InputStream dataStream;
    private int connectTimeout = 1000, readTimeout = 1000;
    private HashMap<String, String> requestProperty;
    private HttpssCallback callback;

    public Httpss() {
        method = "GET";
    }

    public Httpss(String url) {
        this.url = url;
        method = "GET";
    }

    public Httpss(boolean post) {
        method = post ? "POST" : "GET";
    }

    public Httpss(String url, boolean post) {
        this.url = url;
        method = post ? "POST" : "GET";
    }

    public Httpss setUrl(String url) {
        this.url = url;
        return this;
    }

    public Httpss setGet() {
        method = "GET";
        return this;
    }

    public Httpss setPost() {
        method = "POST";
        return this;
    }

    public Httpss setDataBytes(byte[] dataBytes) {
        this.dataBytes = dataBytes;
        if (dataBytes != null)
            dataStream = null;
        return this;
    }

    public Httpss setDataStream(InputStream dataStream) {
        this.dataStream = dataStream;
        if (dataStream != null)
            dataBytes = null;
        return this;
    }

    public Httpss setConnectTimeout(int connectTimeout) {
        if (connectTimeout >= 0)
            this.connectTimeout = connectTimeout;
        return this;
    }

    public Httpss setReadTimeout(int readTimeout) {
        if (readTimeout >= 0)
            this.readTimeout = readTimeout;
        return this;
    }

    public Httpss setRequestProperty(HashMap<String, String> requestProperty) {
        this.requestProperty = requestProperty;
        return this;
    }

    public Httpss setRequestProperty(HashMap<String, String> requestProperty, boolean append) {
        if (append && this.requestProperty != null)
            this.requestProperty.putAll(requestProperty);
        else
            this.requestProperty = requestProperty;
        return this;
    }

    public Httpss setRequestProperty(String key, String value) {
        requestProperty = new HashMap<>();
        requestProperty.put(key, value);
        return this;
    }

    public Httpss setRequestProperty(String key, String value, boolean append) {
        if (append && requestProperty != null)
            requestProperty.put(key, value);
        else {
            requestProperty = new HashMap<>();
            requestProperty.put(key, value);
        }
        return this;
    }

    public Httpss setCallback(HttpssCallback callback) {
        this.callback = callback;
        return this;
    }

    public Httpss request() {
        if (url != null) {
            new Thread() {
                private String url, method;
                private byte[] dataBytes;
                private InputStream dataStream;
                private int connectTimeout, readTimeout;
                private HashMap<String, String> requestProperty;
                private HttpssCallback callback;

                Thread init(String url, String method, byte[] dataBytes, InputStream dataStream, int connectTimeout, int readTimeout, HashMap<String, String> requestProperty, HttpssCallback callback) {
                    this.url = url;
                    this.method = method;
                    this.dataBytes = dataBytes;
                    this.dataStream = dataStream;
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
                        httpURLConnection.setConnectTimeout(connectTimeout);
                        httpURLConnection.setReadTimeout(readTimeout);
                        if (requestProperty != null)
                            for (String key : requestProperty.keySet())
                                httpURLConnection.setRequestProperty(key, requestProperty.get(key));
                        if (method.equals("POST")) {
                            if (dataBytes != null) {
                                httpURLConnection.setDoOutput(true);
                                DataOutputStream dataOutputStream = new DataOutputStream(httpURLConnection.getOutputStream());
                                dataOutputStream.write(dataBytes);
                                dataOutputStream.flush();
                                dataOutputStream.close();
                            } else if (dataStream != null) {
                                httpURLConnection.setDoOutput(true);
                                DataOutputStream dataOutputStream = new DataOutputStream(httpURLConnection.getOutputStream());
                                DataInputStream dataInputStream = new DataInputStream(dataStream);
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
                                dataOutputStream.write(byteArrayOutputStream.toByteArray());
                                dataOutputStream.flush();
                                dataOutputStream.close();
                            }
                        }
                        httpURLConnection.connect();
                        if (callback != null) {
                            int responseCode = httpURLConnection.getResponseCode();
                            if (responseCode == 200) {
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
                                byte[] data = byteArrayOutputStream.toByteArray();
                                new Handler(Looper.getMainLooper()).post(() -> callback.onHttpssOK(data));
                            } else
                                new Handler(Looper.getMainLooper()).post(() -> callback.onHttpssFail(responseCode));
                        }
                    } catch (Exception e) {
                        if (callback != null)
                            new Handler(Looper.getMainLooper()).post(() -> callback.onHttpssError());
                    }
                }
            }.init(url, method, dataBytes, dataStream, connectTimeout, readTimeout, requestProperty, callback).start();
        } else if (callback != null)
            new Handler(Looper.getMainLooper()).post(() -> callback.onHttpssError());
        return this;
    }
}