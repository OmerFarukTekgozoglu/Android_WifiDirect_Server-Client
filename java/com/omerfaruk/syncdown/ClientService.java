package com.omerfaruk.syncdown;

import android.app.IntentService;
import android.content.Intent;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pInfo;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.util.Log;


import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;

/**
 * Created by asd on 30.3.2018.
 */

public class ClientService extends IntentService {

    public static final String TAG = "ClientService";

    private boolean serviceEnabled;

    private int port;
    private File fileToSend;
    private ResultReceiver clientResult;
    private WifiP2pDevice targetDevice;
    private WifiP2pInfo wifiInfo;


    public ClientService() {
        super("ClientService");
        serviceEnabled = true;
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        port = (Integer) intent.getExtras().get("port");
        fileToSend = (File) intent.getExtras().get("fileToSend");
        clientResult = (ResultReceiver) intent.getExtras().get("clientResult");
        targetDevice = (WifiP2pDevice) intent.getExtras().get("targetDevice");
        wifiInfo = (WifiP2pInfo) intent.getExtras().get("wifiInfo");

        if (!wifiInfo.isGroupOwner){
            InetAddress targetIP = wifiInfo.groupOwnerAddress;
            signalActivity(wifiInfo.isGroupOwner + " Transfering file " + fileToSend.getName() + " to " +
            wifiInfo.groupOwnerAddress.toString() + " on TCP Port: " + port);

            Socket clientSocket = null;
            OutputStream os = null;
            try {
                long time = System.currentTimeMillis();
                clientSocket = new Socket(targetIP,port);
                os = clientSocket.getOutputStream();
                PrintWriter pw = new PrintWriter(os);

                InputStream is = clientSocket.getInputStream();
                InputStreamReader isr = new InputStreamReader(is);
                BufferedReader br = new BufferedReader(isr);

                signalActivity("Handshake başladı");
                Log.d(TAG, "#########\nHandshake");
                byte[] buffer = new byte[4096];
                FileInputStream fis = new FileInputStream(fileToSend);
                BufferedInputStream bis = new BufferedInputStream(fis);
                while (true){
                    int bytesRead = bis.read(buffer,0,buffer.length);
                    if (bytesRead == -1){
                        break;
                    }
                    Log.d(TAG, "#######\nYAZIYOR");
                    os.write(buffer,0, bytesRead);
                    os.flush();
                }
                fis.close();
                bis.close();
                br.close();
                isr.close();
                is.close();
                pw.close();
                os.close();

                clientSocket.close();
                long time_stop = System.currentTimeMillis();
                signalActivity("File Transfer Complete, sent file: " + fileToSend.getName());
                Log.d(TAG, "#############################################\nBitti!");
                long sonuc = time_stop - time;
                Log.d(TAG,"Download hizi " + fileToSend.length() + " boyutu icin " + sonuc + " kadardir.");
            }catch (Exception e){
                e.printStackTrace();
                signalActivity(e.getMessage());
                Log.d(TAG, "#############################################\nWifi group sorunu");
            }

        }else {
            signalActivity("Device group owner oldugundan şu anda transfer mumkun degil ancak grup"+
            "owner kendi ip sini gönderirse mumkundur.");
        }

        clientResult.send(port,null);
    }

    public void signalActivity(String message){
        Bundle b = new Bundle();
        b.putString("message",message);
        clientResult.send(port,b);
    }

    public void onDestroy(){
        serviceEnabled = false;
        stopSelf();
    }

}
