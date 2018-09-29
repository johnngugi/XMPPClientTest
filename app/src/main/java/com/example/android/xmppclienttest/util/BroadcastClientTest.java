package com.example.android.xmppclienttest.util;

import com.example.android.xmppclienttest.AppExecutors;
import com.example.android.xmppclienttest.database.AppDatabase;
import com.example.android.xmppclienttest.database.MessageEntry;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class BroadcastClientTest {

    private AppDatabase mDb;

    private DatagramSocket socket;
    private boolean running;
    private byte[] buf = new byte[256];
    private String received;

    public BroadcastClientTest(AppDatabase database) {
        mDb = database;
        try {
            socket = new DatagramSocket(4445);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void broadCast() {

        try {

            DatagramPacket packet = new DatagramPacket(buf, buf.length);
            socket.receive(packet);
            System.out.println("1");
            InetAddress address = packet.getAddress();
            int port = packet.getPort();
            packet = new DatagramPacket(buf, buf.length, address, port);
            String received = new String(packet.getData(), 0, packet.getLength());
            this.received = received;

            System.out.println(received);
            final MessageEntry messageEntry = new MessageEntry("Title", received);
            AppExecutors.getsInstance().getDiskIO().execute(new Runnable() {
                @Override
                public void run() {
                    mDb.messageDao().insertSingleMessage(messageEntry);
                }
            });

            buf = new byte[256];

            if (received.equals("end")) {
                socket.send(packet);
                return;
            }
            socket.send(packet);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            socket.close();
        }
    }

}
