package com.titvt.fulizhan.Setting;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.titvt.fulizhan.MainActivity;
import com.titvt.fulizhan.R;
import com.titvt.fulizhan.Web.WebFragment;

import java.util.Objects;

public class SettingFragment extends Fragment {
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_setting, container, false);
        view.findViewById(R.id.trans_en).setOnClickListener(v -> ((MainActivity) Objects.requireNonNull(getActivity())).language = "en");
        view.findViewById(R.id.trans_jp).setOnClickListener(v -> ((MainActivity) Objects.requireNonNull(getActivity())).language = "jp");
        Spinner spinner = view.findViewById(R.id.spinner);
        spinner.setAdapter(new ArrayAdapter<>(Objects.requireNonNull(getContext()), R.layout.spinner_item, new String[]{"2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20"}));
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Objects.requireNonNull(getContext()).getSharedPreferences("flz", Context.MODE_PRIVATE).edit().putString("num", String.valueOf(position + 2)).apply();
                WebFragment web = (WebFragment) ((MainActivity) Objects.requireNonNull(getActivity())).web;
                if (web != null)
                    web.wv.loadUrl("https://www.titvt.com/flz/web/main.html?num=" + (position + 2));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        spinner.setSelection(Integer.parseInt(getContext().getSharedPreferences("flz", Context.MODE_PRIVATE).getString("num", "4")) - 2);
        view.findViewById(R.id.remote_get).setOnClickListener(v -> startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.titvt.com/flz/fulizhan.exe"))));
        return view;
    }
}