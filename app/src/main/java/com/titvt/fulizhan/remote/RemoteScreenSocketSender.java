package com.titvt.fulizhan.remote;

import android.graphics.Point;

import java.io.DataOutputStream;
import java.net.Socket;

class RemoteScreenSocketSender extends Thread {
    private Socket socket;
    private int code;
    private Object object;

    RemoteScreenSocketSender(Socket socket, int code, Object object) {
        this.socket = socket;
        this.code = code;
        this.object = object;
    }

    @Override
    public void run() {
        try {
            DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
            dataOutputStream.write(code);
            switch (code) {
                case 0:
                case 1:
                    dataOutputStream.writeInt(((Point) object).x);
                    dataOutputStream.writeInt(((Point) object).y);
                    break;
                case 2:
                    dataOutputStream.writeUTF((String) object);
            }
            dataOutputStream.flush();
        } catch (Exception ignored) {
        }
    }
}
