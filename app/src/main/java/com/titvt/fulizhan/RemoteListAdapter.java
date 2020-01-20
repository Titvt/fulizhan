package com.titvt.fulizhan;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

class RemoteListAdapter extends RecyclerView.Adapter<RemoteListAdapter.ViewHolder> {
    private Context context;
    private ArrayList<RemoteListHost> remoteListHosts = new ArrayList<>();

    RemoteListAdapter(Context context) {
        this.context = context;
    }

    void addHost(RemoteListHost remoteListHost) {
        this.remoteListHosts.add(remoteListHost);
        notifyItemInserted(getItemCount() - 1);
        notifyItemChanged(getItemCount() - 1);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RemoteListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new RemoteListAdapter.ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_remote_list, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RemoteListAdapter.ViewHolder holder, int position) {
        holder.hostName.setText(remoteListHosts.get(position).hostName);
        holder.host.setText(remoteListHosts.get(position).host);
        Bitmap bitmap = BitmapFactory.decodeByteArray(remoteListHosts.get(position).bytes, 0, remoteListHosts.get(position).bytes.length),
                thumb = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(thumb);
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColor(0xFF000000);
        Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        canvas.drawARGB(0, 0, 0, 0);
        canvas.drawRoundRect(new RectF(rect), 100, 100, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);
        holder.thumb.setImageBitmap(thumb);
    }

    @Override
    public int getItemCount() {
        return remoteListHosts.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView hostName, host;
        ImageView thumb;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            hostName = itemView.findViewById(R.id.hostName);
            host = itemView.findViewById(R.id.host);
            thumb = itemView.findViewById(R.id.thumb);
            itemView.setOnClickListener(v -> context.startActivity(new Intent(context, RemoteScreenActivity.class).putExtra("host", host.getText().toString())));
        }
    }
}
