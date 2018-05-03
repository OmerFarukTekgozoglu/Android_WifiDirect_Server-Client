package com.omerfaruk.syncdown;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;

/**
 * Created by asd on 30.3.2018.
 */

public class WifiClientBroadcastReceiver extends BroadcastReceiver {

    private WifiP2pManager manager;
    private WifiP2pManager.Channel channel;
    private ClientActivity activity;

    public WifiClientBroadcastReceiver(WifiP2pManager manager, WifiP2pManager.Channel channel, ClientActivity activity) {
        super();
        this.manager = manager;
        this.channel = channel;
        this.activity = activity;
        activity.setClientStatus("Client Broadcast Receiver oluşturuldu.");
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)){
            int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);

            if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED){
                activity.setClientWifiStatus("Wifi Direct Etkin!");
            }else {
                activity.setClientWifiStatus("Wifi Direct Devre Dışı!");
            }
        }else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)){
            manager.requestPeers(channel, new WifiP2pManager.PeerListListener() {
                @Override
                public void onPeersAvailable(WifiP2pDeviceList peers) {
                    activity.displayPeers(peers);
                }
            });
        }else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)){
            NetworkInfo networkState = (NetworkInfo) intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);
            WifiP2pInfo wifiInfo = intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_INFO);
            WifiP2pDevice device = intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_DEVICE);

            if (networkState.isConnected()){
                activity.setTransferStatus(true);
                activity.setNetworkToReadyState(true,wifiInfo,device);
                activity.setClientStatus("Connection status: Connected");
            }else {
                activity.setTransferStatus(false);
                activity.setClientStatus("Connection Status: Disconnected");
                manager.cancelConnect(channel,null);
            }

        }else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)){
            //peers listesi degisirse ne olacak burada belirtilir.

        }
    }
}
