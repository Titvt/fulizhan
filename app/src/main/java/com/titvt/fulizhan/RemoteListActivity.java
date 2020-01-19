package com.titvt.fulizhan;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;

public class RemoteListActivity extends Fragment {
    private RemoteListAdapter remoteListAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_remote_list, container, false);
        view.findViewById(R.id.btn_get_exe).setOnClickListener(v -> {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.titvt.com/flz/fulizhan.exe")));
        });
        RecyclerView rv = view.findViewById(R.id.rv);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        remoteListAdapter = new RemoteListAdapter();
        rv.setAdapter(remoteListAdapter);
        new Thread() {
            @Override
            public void run() {
                for (int i = 0; i < 256; i++)
                    new RemoteListThread("192.168." + i + ".").start();
            }
        }.start();
        return view;
    }

    class RemoteListAdapter extends RecyclerView.Adapter<RemoteListAdapter.MyViewHolder> {
        ArrayList<Hosts> hosts = new ArrayList<>();

        void addHost(Hosts hosts) {
            this.hosts.add(hosts);
            notifyItemInserted(getItemCount() - 1);
        }

        @NonNull
        @Override
        public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new MyViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_remote_list, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
            holder.hostName.setText(hosts.get(position).hostName);
            holder.host.setText(hosts.get(position).host);
            Bitmap bitmap = BitmapFactory.decodeByteArray(hosts.get(position).bytes, 0, hosts.get(position).bytes.length),
                    thumb = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(thumb);
            Paint paint = new Paint();
            paint.setAntiAlias(true);
            paint.setColor(0xff000000);
            Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
            canvas.drawARGB(0, 0, 0, 0);
            canvas.drawRoundRect(new RectF(rect), 100, 100, paint);
            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
            canvas.drawBitmap(bitmap, rect, rect, paint);
            holder.thumb.setImageBitmap(thumb);
        }

        @Override
        public int getItemCount() {
            return hosts.size();
        }

        class MyViewHolder extends RecyclerView.ViewHolder {
            TextView hostName, host;
            ImageView thumb;

            MyViewHolder(@NonNull View itemView) {
                super(itemView);
                hostName = itemView.findViewById(R.id.hostName);
                host = itemView.findViewById(R.id.host);
                thumb = itemView.findViewById(R.id.thumb);
                itemView.setOnClickListener(v -> {
                    startActivity(new Intent(getContext(), RemoteScreenActivity.class).putExtra("host", host.getText().toString()));
                });
            }
        }
    }

    class Hosts {
        String hostName;
        String host;
        byte[] bytes;

        Hosts(String hostName, String host, byte[] bytes) {
            this.hostName = hostName;
            this.host = host;
            this.bytes = bytes;
        }
    }

    class RemoteListThread extends Thread {
        private String prefix;

        RemoteListThread(String prefix) {
            this.prefix = prefix;
        }

        @Override
        public void run() {
            for (int i = 0; i < 256; i += 32) {
                new Thread() {
                    String prefix;
                    int offset;

                    Thread init(String prefix, int offset) {
                        this.prefix = prefix;
                        this.offset = offset;
                        return this;
                    }

                    @Override
                    public void run() {
                        for (int i = 0; i < 32; i++) {
                            try {
                                Socket socket = new Socket();
                                socket.connect(new InetSocketAddress(prefix + (offset + i), 9981), 1000);
                                DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
                                dataOutputStream.writeInt(0);
                                dataOutputStream.flush();
                                DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());
                                String hostName = dataInputStream.readUTF();
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
                                remoteListAdapter.addHost(new Hosts(hostName, prefix + (offset + i), byteArrayOutputStream.toByteArray()));
                            } catch (Exception ignored) {
                            }
                        }
                    }
                }.init(prefix, i).start();
            }
        }
    }
}
