package com.omerfaruk.syncdown;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pManager;

/**
 * Created by asd on 30.3.2018.
 */

public class WifiServerBroadcastReceiver extends BroadcastReceiver {

    private WifiP2pManager manager;
    private WifiP2pManager.Channel channel;
    private MainActivity activity;

    public WifiServerBroadcastReceiver(WifiP2pManager manager, WifiP2pManager.Channel channel, MainActivity activity) {
        super();
        this.manager = manager;
        this.channel = channel;
        this.activity = activity;

        activity.setServerStatus("Server Broadcast Receiver oluşturuldu!");
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)){
            int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE,-1);

            if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED){
                activity.setServerWifiStatus("Wifi Direct Etkin!");
            }else {
                activity.setServerWifiStatus("Wifi Direct Devredışı!");
            }
        }else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)){

        }else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)){
            NetworkInfo networkState = intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);

            if (networkState.isConnected()){
                activity.setServerStatus("Connection status: Connected!");
            }else {
                activity.setServerStatus("Connection status: Disconnected!");
                manager.cancelConnect(channel,null);
            }
        }else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)){
            //yeni device gelirse ne yapilacak burada olacak!
        }
    }
}
