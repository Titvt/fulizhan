package com.titvt.fulizhan;

import android.os.Bundle;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.net.URLEncoder;

public class AIFragment extends Fragment {
    private AIAdapter aiAdapter;
    private AIHandler aiHandler;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_ai, container, false);
        view.findViewById(R.id.btn_send).setOnClickListener(v -> {
            String string = ((EditText) view.findViewById(R.id.et)).getText().toString();
            aiAdapter.addMessage(string, true);
            new Thread() {
                private AIHandler aiHandler;
                private String string;

                Thread init(AIHandler aiHandler, String string) {
                    this.aiHandler = aiHandler;
                    this.string = string;
                    return this;
                }

                @Override
                public void run() {
                    Message message = new Message();
                    String string = new Https("https://www.titvt.com/flz/ai.php").post("question=" + URLEncoder.encode(this.string));
                    if (string.equals(""))
                        message.obj = "emmm...";
                    else
                        message.obj = string;
                    aiHandler.sendMessage(message);
                }
            }.init(aiHandler, string).start();
            ((EditText) view.findViewById(R.id.et)).setText("");
        });
        RecyclerView rv = view.findViewById(R.id.rv);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        aiAdapter = new AIAdapter(getContext(), rv);
        rv.setAdapter(aiAdapter);
        aiHandler = new AIHandler(aiAdapter);
        return view;
    }
}