package com.titvt.fulizhan.remote;

class RemoteListHost {
    String hostName;
    String hostType;
    String host;
    byte[] thumb;

    RemoteListHost(String hostName, String hostType, String host, byte[] thumb) {
        this.hostName = hostName;
        this.hostType = hostType;
        this.host = host;
        this.thumb = thumb;
    }
}
