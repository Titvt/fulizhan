package com.titvt.fulizhan.Setting;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.titvt.fulizhan.MainActivity;
import com.titvt.fulizhan.R;

import java.util.Objects;

public class SettingFragment extends Fragment {
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_setting, container, false);
        view.findViewById(R.id.trans_en).setOnClickListener(v -> ((MainActivity) Objects.requireNonNull(getActivity())).language = "en");
        view.findViewById(R.id.trans_jp).setOnClickListener(v -> ((MainActivity) Objects.requireNonNull(getActivity())).language = "jp");
        return view;
    }
}