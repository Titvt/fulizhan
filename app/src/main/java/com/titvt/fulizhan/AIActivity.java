package com.titvt.fulizhan;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.net.URLEncoder;
import java.util.ArrayList;

public class AIActivity extends Fragment {
    private RecyclerView rv;
    private AIAdapter aiAdapter;
    private AIHandler aiHandler;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_ai, container, false);
        view.findViewById(R.id.btn_send).setOnClickListener(v -> {
            String string = ((EditText) view.findViewById(R.id.et)).getText().toString();
            aiAdapter.addMessage(string, true);
            new AIThread(string).start();
            ((EditText) view.findViewById(R.id.et)).setText("");
        });
        rv = view.findViewById(R.id.rv);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        aiAdapter = new AIAdapter();
        rv.setAdapter(aiAdapter);
        aiHandler = new AIHandler(aiAdapter);
        return view;
    }

    class AIAdapter extends RecyclerView.Adapter<AIAdapter.MyViewHolder> {
        ArrayList<String> messages = new ArrayList<>();
        ArrayList<Boolean> sended = new ArrayList<>();

        void addMessage(String message, boolean sended) {
            messages.add(message);
            this.sended.add(sended);
            notifyItemInserted(getItemCount() - 1);
            rv.scrollToPosition(getItemCount() - 1);
        }

        @NonNull
        @Override
        public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new MyViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_ai, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
            holder.message.setText(messages.get(position));
            if (sended.get(position)) {
                holder.message_layout.setGravity(Gravity.END);
                holder.message_layout.setPadding(100, 0, 0, 0);
                holder.message.setBackground(getResources().getDrawable(R.drawable.message_right));
            } else {
                holder.message_layout.setPadding(0, 0, 100, 0);
                holder.message.setBackground(getResources().getDrawable(R.drawable.message_left));
            }
        }

        @Override
        public int getItemCount() {
            return messages.size();
        }

        class MyViewHolder extends RecyclerView.ViewHolder {
            LinearLayout message_layout;
            TextView message;

            MyViewHolder(@NonNull View itemView) {
                super(itemView);
                message_layout = itemView.findViewById(R.id.message_layout);
                message = itemView.findViewById(R.id.message);
            }
        }
    }

    static class AIHandler extends Handler {
        AIAdapter aiAdapter;

        AIHandler(AIAdapter aiAdapter) {
            this.aiAdapter = aiAdapter;
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            aiAdapter.addMessage((String) msg.obj, false);
        }
    }

    class AIThread extends Thread {
        private String string;

        AIThread(String string) {
            this.string = string;
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
    }
}