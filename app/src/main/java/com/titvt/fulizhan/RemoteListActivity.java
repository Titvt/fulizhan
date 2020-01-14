package com.titvt.fulizhan;

import android.content.Intent;
import android.graphics.BitmapFactory;
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
    private MyAdapter myAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_remote_list, container, false);
        view.findViewById(R.id.btn_refresh).setOnClickListener(v -> {
            myAdapter.clearHost();
            myAdapter.notifyDataSetChanged();
            for (int i = 0; i < 256; i++)
                new MyThread("192.168." + i + ".").start();
        });
        view.findViewById(R.id.btn_back).setOnClickListener(v -> {

        });
        RecyclerView rv = view.findViewById(R.id.rv);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        myAdapter = new MyAdapter();
        rv.setAdapter(myAdapter);
        for (int i = 0; i < 256; i++)
            new MyThread("192.168." + i + ".").start();
        return view;
    }

    class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {
        ArrayList<Hosts> hosts = new ArrayList<>();

        void addHost(Hosts hosts) {
            this.hosts.add(hosts);
            notifyItemChanged(getItemCount());
        }

        void clearHost() {
            for (int i = hosts.size(); i > 0; i--)
                hosts.remove(i - 1);
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
            holder.thumb.setImageBitmap(BitmapFactory.decodeByteArray(hosts.get(position).bytes, 0, hosts.get(position).bytes.length));
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
                    Intent intent = new Intent(getContext(), RemoteScreenActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putString("host", host.getText().toString());
                    intent.putExtra("bundle", bundle);
                    startActivity(intent);
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

    class MyThread extends Thread {
        String prefix;

        MyThread(String prefix) {
            this.prefix = prefix;
        }

        @Override
        public void run() {
            for (int i = 0; i < 256; i++) {
                try {
                    Socket socket = new Socket();
                    socket.connect(new InetSocketAddress(prefix + i, 9981), 50);
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
                    myAdapter.addHost(new Hosts(hostName, prefix + i, byteArrayOutputStream.toByteArray()));
                } catch (Exception ignored) {
                }
            }
        }
    }
}
