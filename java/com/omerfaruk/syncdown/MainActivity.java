package com.omerfaruk.syncdown;

import android.app.Activity;
import android.app.IntentService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.ResultReceiver;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import java.io.File;
import java.nio.channels.Channel;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {
    List<String> list;
    private static final String TAG = "MainActivity";

    public final int fileRequestID = 55;
    public final int port = 7950;


    private WifiP2pManager wifiManager;
    private WifiP2pManager.Channel wifiChannel;
    private BroadcastReceiver wifiServerReceiver;

    private IntentFilter wifiServerReceiverIntentFilter;

    private String path;
    private File downloadTarget;

    private Intent serverServiceIntent;

    private boolean serverThreadActive;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        Client client = new Client();
        Client.setContext(MainActivity.this);
        client.getIP();
        client.getMAC();
        //client.setFileName("80GundeDevriAlem");
        //client.createFolder(MainActivity.this);
        list = new ArrayList<>();

        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        wifiManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        wifiChannel = wifiManager.initialize(this,getMainLooper(),null);
        wifiServerReceiver = new WifiServerBroadcastReceiver(wifiManager,wifiChannel,this);

        wifiServerReceiverIntentFilter = new IntentFilter();
        wifiServerReceiverIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        wifiServerReceiverIntentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
        wifiServerReceiverIntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        wifiServerReceiverIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);

        TextView serverServiceStatus = (TextView) findViewById(R.id.server_status_text);
        serverServiceStatus.setText(R.string.server_stopped);

        path = "/";                         //server icinde bir yer
        downloadTarget = new File(path);

        serverServiceIntent = null;
        serverThreadActive = false;

        setServerFileTransferStatus("İletimde olan bir dosya yok!");

        registerReceiver(wifiServerReceiver,wifiServerReceiverIntentFilter);

    }

    public void onClickBtnHost(View view){
        File file = MainActivity.this.getFilesDir();
        File[] listFiles = file.listFiles();
        for (File f : listFiles){
            list.add(f.getName());
        }
        Intent intent = new Intent(MainActivity.this, ShowContent.class);
        intent.putExtra("liste", (ArrayList<String>) list);
        startActivity(intent);
    }

    public void onClickBtnLocal(View view){
        Intent intent = new Intent(MainActivity.this, ShowVideo.class);
        intent.putExtra("url",this.getFilesDir()+File.separator+"Indirilen_Dosya_1522825517421");
        startActivity(intent);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }

    public void startFileBrowserActivity(View view){
        Intent clientStartIntent = new Intent(this, TransferFromServer.class);
        startActivityForResult(clientStartIntent,fileRequestID);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode == Activity.RESULT_OK && requestCode == fileRequestID){
            File targetDir = (File) Client.getContext().getFilesDir(); //data.getExtras().get("file");

            if(targetDir.isDirectory()){
                if (targetDir.canWrite()){
                    downloadTarget = targetDir;
                    TextView filePath = (TextView) findViewById(R.id.server_file_path);
                    filePath.setText(targetDir.getPath());
                    setServerFileTransferStatus("Download dizini: " + targetDir.getName());
                }else {
                    setServerFileTransferStatus("Dosya dizinine ulaşım izni yok!");
                }
            }
        }else {
            setServerFileTransferStatus("Lütfen biz dizin seçin bir dosya seçmiş olabilirsiniz!");
        }
    }

    public void startServer(View view){
        if (!serverThreadActive){
            serverServiceIntent = new Intent(this, ServerService.class);
            serverServiceIntent.putExtra("saveLocation",downloadTarget);
            serverServiceIntent.putExtra("port",new Integer(port));
            serverServiceIntent.putExtra("serverResult", new ResultReceiver(null){
                @Override
                protected void onReceiveResult(int resultCode, final Bundle resultData) {
                    if (resultCode == port){
                        if (resultData == null){
                            serverThreadActive = false;
                            final TextView server_status_text = (TextView) findViewById(R.id.server_status_text);
                            server_status_text.post(new Runnable() {
                                @Override
                                public void run() {
                                    server_status_text.setText(R.string.server_stopped);
                                }
                            });
                        }else  {
                            final TextView server_file_status_text = (TextView) findViewById(R.id.server_file_transfer_status);

                            server_file_status_text.post(new Runnable() {
                                @Override
                                public void run() {
                                    server_file_status_text.setText((String)resultData.get("message"));
                                }
                            });
                        }
                    }
                }
            });
            serverThreadActive = true;
            startService(serverServiceIntent);

            TextView serverServiceStatus = (TextView) findViewById(R.id.server_status_text);
            serverServiceStatus.setText(R.string.server_running);

        }else {
            TextView serverServiceStatus = (TextView) findViewById(R.id.server_status_text);
            serverServiceStatus.setText("Server halihazirda çalısıyor!");
        }
    }


    public void stopServer(View view) {
        if(serverServiceIntent != null)
        {
            stopService(serverServiceIntent);
        }
    }

    public void startClientActivity(View view) {
        stopServer(null);
        Intent clientStartIntent = new Intent(this, ClientActivity.class);
        startActivity(clientStartIntent);
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopServer(null);
        stopService(serverServiceIntent);
        try {
            unregisterReceiver(wifiServerReceiver);
        } catch (IllegalArgumentException e) {}
    }

    public void setServerWifiStatus(String message)
    {
        TextView server_wifi_status_text = (TextView) findViewById(R.id.server_wifi_status_text);
        server_wifi_status_text.setText(message);
    }

    public void setServerStatus(String message)
    {
        TextView server_status_text = (TextView) findViewById(R.id.server_status_text_2);
        server_status_text.setText(message);
    }


    public void setServerFileTransferStatus(String message)
    {
        TextView server_status_text = (TextView) findViewById(R.id.server_file_transfer_status);
        server_status_text.setText(message);
    }
}
