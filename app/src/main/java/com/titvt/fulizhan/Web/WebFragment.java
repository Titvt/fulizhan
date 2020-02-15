package com.titvt.fulizhan.Web;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.titvt.fulizhan.R;

import java.util.Objects;

public class WebFragment extends Fragment {
    public WebView wv;

    @SuppressLint("SetJavaScriptEnabled")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_web_view, container, false);
        SharedPreferences sharedPreferences = Objects.requireNonNull(getContext()).getSharedPreferences("flz", Context.MODE_PRIVATE);
        wv = view.findViewById(R.id.wv);
        wv.getSettings().setJavaScriptEnabled(true);
        wv.setWebViewClient(new WebViewClient());
        wv.setLongClickable(true);
        wv.setOnLongClickListener(v -> true);
        wv.loadUrl("https://www.titvt.com/flz/web/?num=" + sharedPreferences.getString("num", "4"));
        return view;
    }
}