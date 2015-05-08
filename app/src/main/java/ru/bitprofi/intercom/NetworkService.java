package ru.bitprofi.intercom;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * Created by Дмитрий on 08.05.2015.
 */
public class NetworkService extends Service {
    private BluetoothClient _client = null; //Клиент
    private BluetoothServer _server = null; //Сервер

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (GlobalVars.isServer) {
            if (_server == null) {
                _server = new BluetoothServer();
                _server.start();
            }
        } else {
            if (_client == null) {
                if (GlobalVars.serverDevice != null) {
                    GlobalVars.connectDeviceName  = GlobalVars.serverDevice.getName();
                    GlobalVars.connectDeviceAddrs = GlobalVars.serverDevice.getAddress();
                    GlobalVars.connectDeviceUUID  = GlobalVars.connectDeviceName.split("_")[1];

                    _client = new BluetoothClient(GlobalVars.serverDevice);
                    _client.start();
                }
            }
        }

        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        if (GlobalVars.isServer) {
            if (_server != null) {
                _server.stopThread();
                _server = null;
            }
        } else {
            if (_client != null) {
                _client.stopThread();
                _client = null;
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
