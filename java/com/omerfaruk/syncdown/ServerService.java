package com.omerfaruk.syncdown;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.os.ResultReceiver;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by asd on 2.4.2018.
 */

public class ServerService extends IntentService {

    private boolean serviceEnabled;

    private int port;
    private File saveLocation;
    private ResultReceiver serverResult;

    public ServerService() {
        super("ServerService");
        serviceEnabled = true;
    }

    @Override
    protected void onHandleIntent(Intent intent) {
            port = (Integer) intent.getExtras().get("port");                            //port bilgisi
            saveLocation = (File) intent.getExtras().get("saveLocation");                           // dosya kayit adresi yeri
            serverResult = (ResultReceiver) intent.getExtras().get("serverResult");                 //server 'in cevabi


            ServerSocket welcomeSocket = null;
            Socket socket = null;

            try{
                welcomeSocket = new ServerSocket(port);
                while (true && serviceEnabled){
                    socket = welcomeSocket.accept();
                    signalActivity("TCP soketi aktif: " + socket.toString() + " Dosya transferi başlıyor.");

                    InputStream is = socket.getInputStream();
                    InputStreamReader isr = new InputStreamReader(is);
                    BufferedReader br = new BufferedReader(isr);

                    OutputStream os = socket.getOutputStream();
                    PrintWriter pw = new PrintWriter(os);

                    signalActivity("Handshake başladı");

                    String savedAs = "Indirilen_Dosya_"+System.currentTimeMillis();
                    File file = new File(saveLocation,savedAs);

                    byte[] buffer = new byte[4096];
                    int bytesRead;

                    FileOutputStream fos = new FileOutputStream(file);
                    BufferedOutputStream bos = new BufferedOutputStream(fos);

                    while (true){
                        bytesRead = is.read(buffer,0,buffer.length);
                        if (bytesRead == -1){
                            break;
                        }
                        bos.write(buffer,0,buffer.length);
                        bos.flush();
                    }
                    fos.close();
                    bos.close();

                    br.close();
                    isr.close();
                    is.close();

                    pw.close();
                    os.close();

                    socket.close();

                    signalActivity("Dosya aktarımı bitti --> " + savedAs + " olarak kayıt edildi.");
                }
            }catch (Exception except){
                except.printStackTrace();
            }
            serverResult.send(port,null);
    }

    public void signalActivity(String message){
        Bundle b = new Bundle();
        b.putString("message", message);
        serverResult.send(port, b);
    }

    public void onDestroy(){
        serviceEnabled = false;
        stopSelf();
    }
}

