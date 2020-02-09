package com.titvt.fulizhan.Remote;

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
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.titvt.fulizhan.ProgressButton;
import com.titvt.fulizhan.R;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;

class RemoteListAdapter extends RecyclerView.Adapter<RemoteListAdapter.ViewHolder> {
    private Context context;
    private ProgressButton pb;
    private int pn;
    private ArrayList<RemoteListHost> remoteListHosts = new ArrayList<>();

    RemoteListAdapter(Context context, ProgressButton pb) {
        this.context = context;
        this.pb = pb;
    }

    void getHosts() {
        pb.setProgress(pn = 0);
        new Thread() {
            @Override
            public void run() {
                for (int i = 0; i < 256; i++)
                    new Thread() {
                        private String prefix;

                        Thread init(String prefix) {
                            this.prefix = prefix;
                            return this;
                        }

                        @Override
                        public void run() {
                            for (int i = 0; i < 256; i++) {
                                try {
                                    Socket socket = new Socket();
                                    socket.connect(new InetSocketAddress(prefix + i, 9981), 125);
                                    DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
                                    dataOutputStream.writeInt(0);
                                    dataOutputStream.flush();
                                    DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());
                                    String hostName = dataInputStream.readUTF();
                                    String hostType = "未知";
                                    switch (dataInputStream.readInt()) {
                                        case 1:
                                            hostType = "普通模式";
                                            break;
                                        case 2:
                                            hostType = "高清模式";
                                            break;
                                        case 3:
                                            hostType = "高帧率模式";
                                            break;
                                        case 4:
                                            hostType = "仅观看模式";
                                            break;
                                        case 5:
                                            hostType = "高清仅观看模式";
                                            break;
                                        case 6:
                                            hostType = "高帧率仅观看模式";
                                    }
                                    int size = dataInputStream.readInt(), num;
                                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(size);
                                    byte[] bytes = new byte[2048];
                                    do {
                                        if (size < 2048)
                                            num = dataInputStream.read(bytes, 0, size);
                                        else
                                            num = dataInputStream.read(bytes);
                                        byteArrayOutputStream.write(bytes, 0, num);
                                        size -= num;
                                    } while (size != 0);
                                    remoteListHosts.add(new RemoteListHost(hostName, hostType, prefix + i, byteArrayOutputStream.toByteArray()));
                                    new Handler(Looper.getMainLooper()).post(() -> notifyItemInserted(getItemCount() - 1));
                                } catch (Exception ignored) {
                                }
                                pb.setProgress(++pn);
                                pb.postInvalidate();
                            }
                        }
                    }.init("192.168." + i + ".").start();
            }
        }.start();
    }

    void refresh() {
        remoteListHosts.clear();
        new Handler(Looper.getMainLooper()).post(this::notifyDataSetChanged);
        getHosts();
    }

    @NonNull
    @Override
    public RemoteListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new RemoteListAdapter.ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_remote_list, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RemoteListAdapter.ViewHolder holder, int position) {
        holder.hostName.setText(remoteListHosts.get(position).hostName);
        holder.hostType.setText(remoteListHosts.get(position).hostType);
        holder.host.setText(remoteListHosts.get(position).host);
        Bitmap bitmap = BitmapFactory.decodeByteArray(remoteListHosts.get(position).thumb, 0, remoteListHosts.get(position).thumb.length),
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
        TextView hostName, hostType, host;
        ImageView thumb;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            hostName = itemView.findViewById(R.id.hostName);
            hostType = itemView.findViewById(R.id.hostType);
            host = itemView.findViewById(R.id.host);
            thumb = itemView.findViewById(R.id.thumb);
            itemView.setOnClickListener(v -> context.startActivity(new Intent(context, RemoteScreenActivity.class).putExtra("host", host.getText().toString())));
        }
    }
}
