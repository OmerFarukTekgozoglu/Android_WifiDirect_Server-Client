package com.omerfaruk.syncdown;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.text.format.Formatter;
import android.util.Log;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import static android.content.Context.WIFI_SERVICE;

/**
 * Created by asd on 28.3.2018.
 *
 * Client Class'i dosya dizinini-adini, yigin boyutunu, yigin uyelerini (swarns) tutar.
 * Bu bilgiler her bir content icin ayri ayri depolanir.
 * Bu bilgileri Tracker Class'i kullanacaktir.
 * Hangi clientin request ettigini anlamak icinde bir cesit identifier olmak zorundadir. MAC/IP-Hash etc.
 *
 */
public class Client {
    private String folderName;
    private static String fileName;
    private static final String TAG = "Client";
    private static Context context;

    public static void setContext(Context context) {
        Client.context = context;
    }

    public static Context getContext() {
        return context;
    }


    //region GET IP and MAC
    protected String getIP() {
        WifiManager manager = (WifiManager) context.getApplicationContext().getSystemService(WIFI_SERVICE);
        String ipInfo;
        if (manager != null && manager.isWifiEnabled()) {
            ipInfo = Formatter.formatIpAddress(manager.getConnectionInfo().getIpAddress());
        } else {
            ipInfo = null;
        }
        Log.d(TAG, "IP adress: " + ipInfo);
        return ipInfo;
    }


    public String getMAC() {
        WifiManager manager = (WifiManager) context.getApplicationContext().getSystemService(WIFI_SERVICE);
        WifiInfo info = null;
        if (manager != null && manager.isWifiEnabled()) {
            info = manager.getConnectionInfo();
        } else {
            info = null;
        }
        Log.d(TAG, "MAC adress: " + info.getMacAddress());
        return info.getMacAddress();
    }
    //endregion

    //region GET-SET FİLENAME AND CREATE DİR

    public static String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    protected void createFolder(Context context) {
        String path = context.getFilesDir() + File.separator + getFileName() + ".txt";
        File contentFolder = new File(path);
        if (!contentFolder.exists()) {
            try {
                boolean newFile = contentFolder.createNewFile();
                if (newFile) {
                    Log.d(TAG, "File created: " + contentFolder.getPath());
                    FileWriter writer = new FileWriter(contentFolder, true);
                    writer.append(getFileName() + "\n" +
                            getIP() + "\n" +
                            getMAC() + "\n");
                    writer.flush();
                    writer.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    //endregion
}
