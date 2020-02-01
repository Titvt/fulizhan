package com.titvt.fulizhan.AI;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.titvt.fulizhan.R;

import java.util.ArrayList;

class AIAdapter extends RecyclerView.Adapter<AIAdapter.ViewHolder> {
    private Context context;
    private RecyclerView rv;
    private ArrayList<String> messages = new ArrayList<>();
    private ArrayList<Boolean> sended = new ArrayList<>();

    AIAdapter(Context context, RecyclerView rv) {
        this.context = context;
        this.rv = rv;
    }

    void addMessage(String message, boolean sended) {
        messages.add(message);
        this.sended.add(sended);
        new Handler(Looper.getMainLooper()).post(() -> {
            notifyItemInserted(getItemCount() - 1);
            rv.scrollToPosition(getItemCount() - 1);
        });
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_ai, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.message.setText(messages.get(position));
        if (sended.get(position)) {
            holder.message_layout.setGravity(Gravity.END);
            holder.message_layout.setPadding(100, 0, 0, 0);
            holder.message.setBackground(context.getResources().getDrawable(R.drawable.message_right));
        } else {
            holder.message_layout.setGravity(Gravity.START);
            holder.message_layout.setPadding(0, 0, 100, 0);
            holder.message.setBackground(context.getResources().getDrawable(R.drawable.message_left));
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        LinearLayout message_layout;
        TextView message;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            message_layout = itemView.findViewById(R.id.message_layout);
            message = itemView.findViewById(R.id.message);
        }
    }
}
