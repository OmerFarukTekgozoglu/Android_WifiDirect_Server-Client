package com.omerfaruk.syncdown;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.ResultReceiver;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;


public class ClientActivity extends Activity {

    public static final String TAG = "ClientActivity";

    public final int fileRequestID = 98;
    public final int port = 7950;

    private WifiP2pManager wifiManager;
    private WifiP2pManager.Channel wifiChannel;
    private BroadcastReceiver wifiClientReceiver;

    private IntentFilter wifiClientReceiverIntentFilter;

    private boolean connectedAndReadyToSendFile;

    private boolean filePathProvided;
    private File fileToSend;
    private boolean transferActive;

    private Intent clientServiceIntent;
    private WifiP2pDevice targetDevice;
    private WifiP2pInfo wifiInfo;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client);

        wifiManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);

        wifiChannel = wifiManager.initialize(this,getMainLooper(),null);
        wifiClientReceiver = new WifiClientBroadcastReceiver(wifiManager,wifiChannel,this);

        wifiClientReceiverIntentFilter = new IntentFilter();
        wifiClientReceiverIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        wifiClientReceiverIntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        wifiClientReceiverIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        wifiClientReceiverIntentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

        connectedAndReadyToSendFile = false;
        filePathProvided = false;
        fileToSend = null;
        transferActive = false;
        clientServiceIntent = null;
        targetDevice = null;
        wifiInfo = null;

        registerReceiver(wifiClientReceiver,wifiClientReceiverIntentFilter);

        setClientFileTransferStatus("Client şu anda boş!");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_client, menu);
        return true;
    }

    public void setTransferStatus(boolean status){
        connectedAndReadyToSendFile = status;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void setNetworkToReadyState(boolean status, WifiP2pInfo info, WifiP2pDevice device){
        wifiInfo = info;
        targetDevice = device;
        connectedAndReadyToSendFile = status;
    }

    private void stopClientReceiver(){
        try{
            unregisterReceiver(wifiClientReceiver);
        }catch (IllegalArgumentException illegal){
            //Do nothing!!
        }
    }

    public void searchForPeers(View view){
        wifiManager.discoverPeers(wifiChannel,null);
    }

    public void browseForFile(View view){
        Intent clientStartIntent = new Intent(ClientActivity.this, TransferFromServer.class);
        startActivityForResult(clientStartIntent,fileRequestID);
    }

    public void searchRequestToPeers(View view) {
        Intent TransferFromClient = new Intent(this,TransferFromClient.class);
        startActivityForResult(TransferFromClient,fileRequestID);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK && requestCode == fileRequestID){

            File targetDir = (File) data.getExtras().get("file");  //data.getExtras().get("file"); //new File((File) Client.getContext().getFilesDir() + File.separator);

            if (targetDir.isFile()){
                Log.d(TAG,"targetDir çalışıyor!");
                if (targetDir.canRead()){
                    fileToSend = targetDir;
                    filePathProvided = true;
                    setTargetFileStatus(targetDir.getName() + " boyutu:" + targetDir.length() +" transfer için seçilen dosya!");
                }else {
                    filePathProvided = false;
                    Log.d(TAG,"Dosya okunabilir değil! Üstteki if e bak!");
                    setTargetFileStatus("Dosya okuma izni verilmemiş!");
                }
            }else {
                filePathProvided =false;
                Log.d(TAG,"Dosya isfile sorgusu buraya atıyor!");
                setTargetFileStatus("Dizin transfer edilemez yalnızca dosya seçiniz.");
            }
        }
    }

    public void sendFile(View view){
        if (!transferActive){
            if (!filePathProvided){
                setClientFileTransferStatus("Dosya seçilmedi!");
            }else if (!connectedAndReadyToSendFile){
                setClientFileTransferStatus("Connect işlemi başarısız!");
            }else if (wifiInfo == null){
                setClientFileTransferStatus("Wifi P2P bilgisi tanımsız!");
            }else {
                clientServiceIntent = new Intent(this, ClientService.class);
                clientServiceIntent.putExtra("fileToSend",fileToSend);
                clientServiceIntent.putExtra("port",Integer.valueOf(port));
                clientServiceIntent.putExtra("targetDevice", targetDevice);
                clientServiceIntent.putExtra("wifiInfo", wifiInfo);
                clientServiceIntent.putExtra("clientResult", new ResultReceiver(null){
                    @Override
                    protected void onReceiveResult(int resultCode, final Bundle resultData) {
                        if (resultCode == port){
                            if (resultData == null){
                                transferActive = false;
                            }else {
                                final TextView client_status_text = (TextView) findViewById(R.id.file_transfer_status);
                                client_status_text.post(new Runnable() {
                                    public void run() {
                                        client_status_text.setText((String)resultData.get("message"));
                                    }
                                });
                            }
                        }
                    }
                });
                transferActive = true;
                startService(clientServiceIntent);
            }
        }
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
        stopClientReceiver();
    }

    public void setClientWifiStatus(String message)
    {
        TextView connectionStatusText = (TextView) findViewById(R.id.client_wifi_status_text);
        connectionStatusText.setText(message);
    }

    public void setClientStatus(String message)
    {
        TextView clientStatusText = (TextView) findViewById(R.id.client_status_text);
        clientStatusText.setText(message);
    }

    public void setClientFileTransferStatus(String message)
    {
        TextView fileTransferStatusText = (TextView) findViewById(R.id.file_transfer_status);
        fileTransferStatusText.setText(message);
    }

    public void setTargetFileStatus(String message)
    {
        TextView targetFileStatus = (TextView) findViewById(R.id.selected_filename);
        targetFileStatus.setText(message);
    }


    public void displayPeers(final WifiP2pDeviceList peers)
    {
        //Dialog to show errors/status
        final AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("WiFi Direct File Transfer");

        //Get list view
        ListView peerView = (ListView) findViewById(R.id.peers_listview);

        //Make array list
        ArrayList<String> peersStringArrayList = new ArrayList<String>();

        //Fill array list with strings of peer names
        for(WifiP2pDevice wd : peers.getDeviceList())
        {
            peersStringArrayList.add(wd.deviceName);
        }

        //Set list view as clickable
        peerView.setClickable(true);

        //Make adapter to connect peer data to list view
        ArrayAdapter arrayAdapter = new ArrayAdapter(ClientActivity.this, android.R.layout.simple_list_item_1, peersStringArrayList.toArray());

        //Show peer data in listview
        peerView.setAdapter(arrayAdapter);


        peerView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            public void onItemClick(AdapterView<?> arg0, View view, int arg2,long arg3) {

                //Get string from textview
                TextView tv = (TextView) view;

                WifiP2pDevice device = null;

                //Search all known peers for matching name
                for(WifiP2pDevice wd : peers.getDeviceList())
                {
                    if(wd.deviceName.equals(tv.getText()))
                        device = wd;
                }

                if(device != null)
                {
                    //Connect to selected peer
                    connectToPeer(device);

                }
                else
                {
                    dialog.setMessage("Failed");
                    dialog.show();

                }
            }
            // TODO Auto-generated method stub
        });

    }

    public void connectToPeer(final WifiP2pDevice wifiPeer)
    {
        this.targetDevice = wifiPeer;

        WifiP2pConfig config = new WifiP2pConfig();
        config.deviceAddress = wifiPeer.deviceAddress;
        wifiManager.connect(wifiChannel, config, new WifiP2pManager.ActionListener()  {
            public void onSuccess() {

                setClientStatus("Connection to " + targetDevice.deviceName + " sucessful");
            }

            public void onFailure(int reason) {
                setClientStatus("Connection to " + targetDevice.deviceName + " failed");

            }
        });
//        wifiManager.createGroup(wifiChannel, new WifiP2pManager.ActionListener() {
//            @Override
//            public void onSuccess() {
//                setClientStatus("Connection to " + targetDevice.deviceName + " sucessful");
//            }
//
//            @Override
//            public void onFailure(int i) {
//                setClientStatus("Connection to " + targetDevice.deviceName + " failed");
//            }
//        });

    }


}
