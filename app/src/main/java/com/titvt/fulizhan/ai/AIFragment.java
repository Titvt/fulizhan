package com.titvt.fulizhan.ai;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.titvt.fulizhan.Https;
import com.titvt.fulizhan.R;

import java.net.URLEncoder;

public class AIFragment extends Fragment {
    private AIAdapter aiAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_ai, container, false);
        view.findViewById(R.id.ai_send).setOnClickListener(v -> {
            String string = ((EditText) view.findViewById(R.id.et)).getText().toString();
            if (string.equals(""))
                return;
            aiAdapter.addMessage(string, true);
            new Thread() {
                private String string;

                Thread init(String string) {
                    this.string = string;
                    return this;
                }

                @Override
                public void run() {
                    String string = new Https("https://www.titvt.com/flz/ai.php").post("question=" + URLEncoder.encode(this.string));
                    string = string.equals("") ? "emmm..." : string;
                    aiAdapter.addMessage(string, false);
                }
            }.init(string).start();
            ((EditText) view.findViewById(R.id.et)).setText("");
        });
        RecyclerView rv = view.findViewById(R.id.rv);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        aiAdapter = new AIAdapter(getContext(), rv);
        rv.setAdapter(aiAdapter);
        return view;
    }

    public void clearMessage() {
        aiAdapter.clearMessage();
    }
}