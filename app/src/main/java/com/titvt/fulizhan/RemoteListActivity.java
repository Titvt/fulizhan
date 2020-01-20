package com.titvt.fulizhan;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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

public class RemoteListActivity extends Fragment {
    private RemoteListAdapter remoteListAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_remote_list, container, false);
        view.findViewById(R.id.btn_get_exe).setOnClickListener(v -> startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.titvt.com/flz/fulizhan.exe"))));
        RecyclerView rv = view.findViewById(R.id.rv);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        remoteListAdapter = new RemoteListAdapter(getContext());
        rv.setAdapter(remoteListAdapter);
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
                                                remoteListAdapter.addHost(new RemoteListHost(hostName, prefix + (offset + i), byteArrayOutputStream.toByteArray()));
                                            } catch (Exception ignored) {
                                            }
                                        }
                                    }
                                }.init(prefix, i).start();
                            }
                        }
                    }.init("192.168." + i + ".").start();
            }
        }.start();
        return view;
    }
}
