package com.titvt.fulizhan;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Base64;
import android.util.JsonReader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.io.ByteArrayOutputStream;
import java.io.StringReader;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Objects;

public class TranslateFragment extends Fragment implements ServiceConnection {
    private String language = "en";
    private TranslateBinder binder;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_translate, container, false);
        view.findViewById(R.id.trans_switch).setOnClickListener(v -> {
            if (((Switch) v).isChecked())
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (Settings.canDrawOverlays(getContext())) {
                        Objects.requireNonNull(getActivity()).bindService(new Intent(getActivity(), TranslateService.class), this, Context.BIND_AUTO_CREATE);
                        startActivityForResult(((MediaProjectionManager) Objects.requireNonNull(getActivity().getSystemService(Context.MEDIA_PROJECTION_SERVICE))).createScreenCaptureIntent(), 1);
                    } else
                        startActivityForResult(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + Objects.requireNonNull(getContext()).getPackageName())), 0);
                } else {
                    ((Switch) v).setChecked(false);
                    Toast.makeText(getContext(), "安卓版本太低，不支持悬浮窗", Toast.LENGTH_LONG).show();
                }
            else {
                Objects.requireNonNull(getActivity()).stopService(new Intent(getActivity(), TranslateService.class));
                getActivity().unbindService(this);
            }
        });
        view.findViewById(R.id.trans_en).setOnClickListener(v -> language = "en");
        view.findViewById(R.id.trans_jp).setOnClickListener(v -> language = "jp");
        Point point = new Point();
        Objects.requireNonNull(getActivity()).getWindowManager().getDefaultDisplay().getSize(point);
        return view;
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        binder = (TranslateBinder) service;
        binder.setActivity(this);
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {

    }

    void screenShot() {
        new Thread() {
            @Override
            public void run() {
                Bitmap bitmap = binder.service.screenShot();
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 40, byteArrayOutputStream);
                ArrayList<TranslateRecord> translateRecords = new ArrayList<>();
                JsonReader jsonReader = new JsonReader(new StringReader(new Https("https://www.titvt.com/flz/translate.php").post("language=" + language + "&image=" + URLEncoder.encode(Base64.encodeToString(byteArrayOutputStream.toByteArray(), Base64.NO_WRAP)))));
                try {
                    jsonReader.beginObject();
                    while (jsonReader.hasNext()) {
                        if (jsonReader.nextName().equals("data")) {
                            jsonReader.beginObject();
                            while (jsonReader.hasNext()) {
                                if (jsonReader.nextName().equals("image_records")) {
                                    jsonReader.beginArray();
                                    while (jsonReader.hasNext()) {
                                        String target_text = "";
                                        int x = 0, y = 0, width = 0, height = 0;
                                        jsonReader.beginObject();
                                        while (jsonReader.hasNext()) {
                                            switch (jsonReader.nextName()) {
                                                case "target_text":
                                                    target_text = jsonReader.nextString();
                                                    break;
                                                case "x":
                                                    x = jsonReader.nextInt();
                                                    break;
                                                case "y":
                                                    y = jsonReader.nextInt();
                                                    break;
                                                case "width":
                                                    width = jsonReader.nextInt();
                                                    break;
                                                case "height":
                                                    height = jsonReader.nextInt();
                                                    break;
                                                default:
                                                    jsonReader.skipValue();
                                            }
                                        }
                                        jsonReader.endObject();
                                        translateRecords.add(new TranslateRecord(target_text, x, y, width, height));
                                    }
                                    jsonReader.endArray();
                                } else
                                    jsonReader.skipValue();
                            }
                            jsonReader.endObject();
                        } else
                            jsonReader.skipValue();
                    }
                    jsonReader.endObject();
                } catch (Exception ignored) {
                }
                binder.service.screenShotCallback(translateRecords);
            }
        }.start();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case 0:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (Settings.canDrawOverlays(getContext())) {
                        Objects.requireNonNull(getContext()).bindService(new Intent(getContext(), TranslateService.class), this, Context.BIND_AUTO_CREATE);
                        startActivityForResult(((MediaProjectionManager) Objects.requireNonNull(Objects.requireNonNull(getActivity()).getSystemService(Context.MEDIA_PROJECTION_SERVICE))).createScreenCaptureIntent(), 1);
                    } else
                        ((Switch) Objects.requireNonNull(getActivity()).findViewById(R.id.trans_switch)).setChecked(false);
                }
                break;
            case 1:
                if (resultCode == Activity.RESULT_OK)
                    binder.service.initScreenShot((MediaProjectionManager) Objects.requireNonNull(getActivity()).getSystemService(Context.MEDIA_PROJECTION_SERVICE), resultCode, data);
                else {
                    ((Switch) Objects.requireNonNull(getActivity()).findViewById(R.id.trans_switch)).setChecked(false);
                    Objects.requireNonNull(getActivity()).stopService(new Intent(getActivity(), TranslateService.class));
                    getActivity().unbindService(this);
                }
        }
    }
}