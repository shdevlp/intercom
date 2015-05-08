package ru.bitprofi.intercom;

import android.app.Service;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.IBinder;

import java.util.HashMap;

/**
 * Фоновая служба приложения
 * Created by Дмитрий on 05.05.2015.
 */
public class BackgroundService extends Service {
    private BluetoothHelper _bluetooth; //Помощь с bluetooth оборудованием
    private BluetoothClient _client;    //Клиент
    private BluetoothServer _server;    //Сервер
    private ThreadSearch _searchThread;
    private HashMap<String, String> _devices; //Список устройств вокруг

    @Override
    public void onCreate() {
        _bluetooth = new BluetoothHelper();
        _devices = new HashMap<String, String>();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
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

        AsyncThreadSearch ats = new AsyncThreadSearch();
        ats.execute();

        return START_NOT_STICKY;//Не перезапускать при аварии
    }

    @Override
    public void onDestroy() {
        Utils.getInstance().setNormalVolume();
        _bluetooth.changeDeviceName(GlobalVars.oldDeviceName);

        if (_bluetooth.isEnabled()) {
            _bluetooth.turnOff();
            Utils.getInstance().addStatusText(GlobalVars.context.getString(R.string.bt_turn_off));
        }

        stopServerOrClient();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * Подключение к серверу
     */
    private void connectToServer(BluetoothDevice device) {
        GlobalVars.connectDeviceName = device.getName();
        GlobalVars.connectDeviceAddrs = device.getAddress();
        GlobalVars.connectDeviceUUID = GlobalVars.connectDeviceName.split("_")[1];

        _client = new BluetoothClient(device);
        _client.start();
    }


    /**
     * Запуск сервера
     */
    private void startServerOrClient() {
        if (GlobalVars.isServer) {
            if (_server == null) {
                _server = new BluetoothServer(_bluetooth.getAdapter());
                _server.start();
            }
        } else {
            if (_client == null) {
                if (GlobalVars.serverDevice != null) {
                    connectToServer(GlobalVars.serverDevice);
                }
            }
        }
    }

    /**
     * Остановка сервера или клиента
     */
    private void stopServerOrClient() {
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

    /**
     *  Асинхронный поток, ждет окончания поиска устройств
     */
    private class AsyncThreadSearch extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            while (true) {
                if (GlobalVars.isBluetoothDiscoveryFinished) {
                    break;
                }
            }
            _searchThread = new ThreadSearch();
            _searchThread.start();

            return null;
        }
    }

    /**
     * Продолжает работу
     */
    private class ThreadSearch extends Thread {
        @Override
        public void run() {
            _devices.clear();
            _devices = _bluetooth.getDescoveredDevices();

            GlobalVars.serverDevice = findServer();
            if (GlobalVars.serverDevice == null) {
                GlobalVars.isServer = true;
            } else {
                GlobalVars.isServer = false;
            }
            Utils.getInstance().setMaxVolume();


            synchronized (this) {
                GlobalVars.isServiceThread = true;
            }

            boolean b = false;
            while (true) {
                if (GlobalVars.buttonState == GlobalVars.BUTTON_IS_ON && b == false) {
                    startServerOrClient();
                    b = true;
                }
                if (GlobalVars.buttonState == GlobalVars.BUTTON_IS_OFF && b == true) {
                    stopServerOrClient();
                    b = false;
                }
            }
        }
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
    private synchronized BluetoothDevice findServer() {
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