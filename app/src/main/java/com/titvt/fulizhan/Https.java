package com.titvt.fulizhan;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

class Https {
    private String uri;

    Https(String uri) {
        this.uri = uri;
    }

    String get() {
        try {
            HttpURLConnection httpURLConnection = (HttpURLConnection) new URL(uri).openConnection();
            httpURLConnection.setConnectTimeout(1000);
            httpURLConnection.setReadTimeout(1000);
            httpURLConnection.connect();
            if (httpURLConnection.getResponseCode() == 200) {
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
                StringBuilder stringBuilder = new StringBuilder();
                String temp;
                while ((temp = bufferedReader.readLine()) != null)
                    stringBuilder.append(temp).append('\n');
                stringBuilder.deleteCharAt(stringBuilder.length() - 1);
                return stringBuilder.toString();
            }
        } catch (Exception ignored) {
        }
        return "";
    }

    String post(String body) {
        try {
            HttpURLConnection httpURLConnection = (HttpURLConnection) new URL(uri).openConnection();
            httpURLConnection.setRequestMethod("POST");
            httpURLConnection.setConnectTimeout(1000);
            httpURLConnection.setReadTimeout(1000);
            httpURLConnection.setDoOutput(true);
            httpURLConnection.setRequestProperty("Content-type", "application/x-www-form-urlencoded");
            DataOutputStream dataOutputStream = new DataOutputStream(httpURLConnection.getOutputStream());
            dataOutputStream.writeBytes(body);
            dataOutputStream.flush();
            dataOutputStream.close();
            httpURLConnection.connect();
            if (httpURLConnection.getResponseCode() == 200) {
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
                StringBuilder stringBuilder = new StringBuilder();
                String temp;
                while ((temp = bufferedReader.readLine()) != null)
                    stringBuilder.append(temp).append('\n');
                stringBuilder.deleteCharAt(stringBuilder.length() - 1);
                return stringBuilder.toString();
            }
        } catch (Exception ignored) {
        }
        return "";
    }
}
