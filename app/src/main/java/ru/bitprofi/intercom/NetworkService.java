package ru.bitprofi.intercom;

import android.app.Service;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.view.View;

import java.util.HashMap;

/**
 * Created by Дмитрий on 13.05.2015.
 */
public class NetworkService extends Service {
    private BluetoothHelper _bluetooth = null;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        _bluetooth = new BluetoothHelper();

        Thread btThread = new Thread(new Runnable() {
            private void findDevice(BluetoothHelper bluetooth) {
                HashMap<String, String> devices = bluetooth.getDescoveredDevices();
                if (devices != null) {
                    for (HashMap.Entry<String, String> entry : devices.entrySet()) {
                        String key = (String) entry.getKey();
                        String value = (String) entry.getValue();

                        if (key.indexOf(GlobalVars.BLUETOOTH_NAME) != -1) {
                            //Нашли сервер, подключаемс к нему
                            BluetoothDevice device = bluetooth.getDevice(value);
                            if (device != null) {
                                GlobalVars.serverDevice = device;
                                return;
                            }
                        }
                    }
                }
            }

            @Override
            public void run() {
                //Ждем включения bluetooth
                if (!_bluetooth.isEnabled()) {
                    _bluetooth.turnOn();
                }

                GlobalVars.oldDeviceName        = _bluetooth.getName();
                GlobalVars.currentDeviceName    = GlobalVars.BLUETOOTH_NAME;
                GlobalVars.currentDeviceAddress = _bluetooth.getAddress();

                _bluetooth.changeDeviceName(GlobalVars.currentDeviceName);
                _bluetooth.makeDiscoverable();

                Utils.getInstance().addStatusText(GlobalVars.context.getString(R.string.bt_turn_on));

                GlobalVars.isBluetoothDiscoveryFinished = false;
                _bluetooth.startDiscovery();
                Utils.getInstance().waitScreenBTDiscovery();

                while (!GlobalVars.isBluetoothDiscoveryFinished) {
                    ;
                }

                findDevice(_bluetooth);

                if (GlobalVars.serverDevice == null) {
                    GlobalVars.isServer = true;
                    Utils.getInstance().addStatusText(GlobalVars.context.getString(R.string.i_am_server));
                } else {
                    GlobalVars.isServer = false;
                    Utils.getInstance().addStatusText(GlobalVars.context.getString(R.string.i_am_client));
                }

                Utils.getInstance().setBtnVisible(View.VISIBLE);
            }
        });
        btThread.start();

        return START_NOT_STICKY;//Не перезапускать при аварии
    }

    @Override
    public void onDestroy() {
        _bluetooth.changeDeviceName(GlobalVars.oldDeviceName);
        if (_bluetooth.isEnabled()) {
            _bluetooth.turnOff();
            Utils.getInstance().addStatusText(GlobalVars.context.getString(R.string.bt_turn_off));
        }
        GlobalVars.activity.finish();
        System.exit(0);
    }

}
