package com.titvt.fulizhan.Setting;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.titvt.fulizhan.MainActivity;
import com.titvt.fulizhan.R;

public class SettingFragment extends Fragment {
    private MainActivity mainActivity;

    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_setting, container, false);
        mainActivity = (MainActivity) getActivity();
        view.findViewById(R.id.trans_en).setOnClickListener(v -> mainActivity.language = "en");
        view.findViewById(R.id.trans_jp).setOnClickListener(v -> mainActivity.language = "jp");
        EditText quality = view.findViewById(R.id.trans_quality);
        quality.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String quality = s.toString().equals("") ? "60" : s.toString();
                if (Integer.parseInt(quality) > 100) {
                    quality = "100";
                    s.clear();
                    s.append("100");
                }
                mainActivity.getSharedPreferences("flz", Context.MODE_PRIVATE).edit().putString("quality", quality).apply();
                mainActivity.quality = Integer.parseInt(quality);
            }
        });
        quality.setText(mainActivity.getSharedPreferences("flz", Context.MODE_PRIVATE).getString("quality", "60"));
        EditText offset = view.findViewById(R.id.trans_offset);
        offset.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String offset = (s.toString().equals("") || s.toString().equals("-")) ? "0" : s.toString();
                mainActivity.getSharedPreferences("flz", Context.MODE_PRIVATE).edit().putString("offset", offset).apply();
                mainActivity.offset = Integer.parseInt(offset);
            }
        });
        offset.setText(mainActivity.getSharedPreferences("flz", Context.MODE_PRIVATE).getString("offset", "0"));
        Spinner spinner = view.findViewById(R.id.spinner);
        spinner.setAdapter(new ArrayAdapter<>(mainActivity, R.layout.spinner_item, new String[]{"2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20"}));
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mainActivity.getSharedPreferences("flz", Context.MODE_PRIVATE).edit().putString("num", String.valueOf(position + 2)).apply();
                if (mainActivity.web != null)
                    mainActivity.web.wv.loadUrl("https://www.titvt.com/flz/web/main.html?num=" + (position + 2));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        spinner.setSelection(Integer.parseInt(mainActivity.getSharedPreferences("flz", Context.MODE_PRIVATE).getString("num", "4")) - 2);
        view.findViewById(R.id.remote_get).setOnClickListener(v -> startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.titvt.com/flz/fulizhan.exe"))));
        return view;
    }
}