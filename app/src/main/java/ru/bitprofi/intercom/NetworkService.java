package ru.bitprofi.intercom;

import android.app.Service;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.IBinder;

import java.util.HashMap;

/**
 * Created by Дмитрий on 08.05.2015.
 */
public class NetworkService extends Service {
    private BluetoothHelper _bluetooth; //Помощь с bluetooth оборудованием
    private HashMap<String, String> _devices; //Список устройств вокруг

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        _bluetooth = new BluetoothHelper();
        _devices = new HashMap<String, String>();

        //Ждем включения bluetooth
        if (!_bluetooth.isEnabled()) {
            _bluetooth.turnOn();
        }

        GlobalVars.oldDeviceName        = _bluetooth.getName();
        GlobalVars.currentDeviceName    = Utils.getInstance().getNewDeviceName();
        GlobalVars.currentDeviceAddress = _bluetooth.getAddress();

        _bluetooth.changeDeviceName(GlobalVars.currentDeviceName);
        _bluetooth.makeDiscoverable();

        Utils.getInstance().addStatusText(GlobalVars.context.getString(R.string.bt_turn_on));

        GlobalVars.isBluetoothDiscoveryFinished = false;
        _bluetooth.startDiscovery();
        Utils.getInstance().waitScreenBTDiscovery();

        AsyncTask<Void, Void, Void> at = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                while (!GlobalVars.isBluetoothDiscoveryFinished) {
                    ;
                }
                _devices = _bluetooth.getDescoveredDevices();
                GlobalVars.serverDevice = findServer();

                if (GlobalVars.serverDevice == null) {
                    GlobalVars.isServer = true;
                    Utils.getInstance().addStatusText(GlobalVars.context.getString(R.string.i_am_server));
                } else {
                    GlobalVars.isServer = false;
                    Utils.getInstance().addStatusText(GlobalVars.context.getString(R.string.i_am_client));
                }
                Utils.getInstance().setBtnColor(getResources().getColor(R.color.seagreen));
                Utils.getInstance().setBtnEnabled(true);


                return null;
            }
        };
        at.execute();

        return START_NOT_STICKY;
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

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * Изучает имя устройства, если найдено сопрягаемое устройство возвращает true
     * @param key
     * @return
     */
    private boolean analizeKeyName(String key) {
        final int idx = key.indexOf(GlobalVars.PREFIX_DEVICE_NAME);
        if (idx == -1) {
            return false;
        }
        return true;
    }


    /**
     * Поиск сервера
     * @return
     */
    private BluetoothDevice findServer() {
        if (_devices != null) {
            for (HashMap.Entry<String, String> entry : _devices.entrySet()) {
                String key = (String) entry.getKey();
                String value = (String) entry.getValue();

                if (analizeKeyName(key)) {
                    //Нашли сервер, подключаемс к нему
                    BluetoothDevice device = _bluetooth.getDevice(value);
                    if (device != null) {
                        return device;
                    }
                }
            }
        }
        return null;
    }
}
