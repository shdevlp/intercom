package ru.bitprofi.intercom;

import android.app.ProgressDialog;
import android.app.Service;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;

import java.util.HashMap;


/**
 * Фоновая служба приложения
 * Created by Дмитрий on 05.05.2015.
 */
public class BackgroundService extends Service {
    private BluetoothHelper _bluetooth; //Помощь с bluetooth оборудованием
    private BluetoothClient _client;    //Клиент
    private BluetoothServer _server;    //Сервер

    private HashMap<String, String> _devices; //Список устройств вокруг

    private MicHelper     _mic;     //Микрофон
    private SpeakerHelper _speaker; //Динамик
    private ConThread  _conThread;

    private Handler _handler;       //Обработчик


    @Override
    public void onCreate() {
        _bluetooth = new BluetoothHelper();
        _conThread = null;

        _handler = new Handler() {
            public void handleMessage(android.os.Message msg) {
                byte[] data = (byte[])msg.obj;

                switch (msg.what) {
                    case GlobalVars.MIC_MSG_DATA:
                        //Данные с микрофона передаем
                        if (GlobalVars.isServer) {
                            if (_server.isRunning()) {
                                _server.addData(data);
                            }
                        } else {
                            if (_client.isRunning()) {
                                _client.addData(data);
                            }
                        }
                        break;

                    case GlobalVars.SERVER_MSG_DATA:
                    case GlobalVars.CLIENT_MSG_DATA:
                        //Данные от сервера или клиента - проигрываем
                        _speaker.addData(data);
                        break;
                }
            }
        };
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        GlobalVars.currentDeviceName = Utils.getInstance().getNewDeviceName();
        GlobalVars.oldDeviceName = _bluetooth.getName();
        GlobalVars.currentAddress = _bluetooth.getAddress();

        //Utils.getInstance().addStatusText(GlobalVars.context.getString(R.string.bt_wait_on));
        //Ждем включения bluetooth
        if (!_bluetooth.isEnabled()) {
            _bluetooth.turnOn();
        }
        Utils.getInstance().addStatusText(GlobalVars.context.getString(R.string.bt_turn_on));

        _bluetooth.changeDeviceName(GlobalVars.currentDeviceName);

        GlobalVars.isBluetoothDiscoveryFinished = false;
        _bluetooth.startDiscovery();
        Utils.getInstance().waitScreenBTDiscovery();

        Async async = new Async();
        async.execute();

        return START_NOT_STICKY;//super.onStartCommand(intent, flags, startId);
    }

    /**
     * Продолжаем работу
     */
    private class ConThread extends Thread {
        @Override
        public void run() {
            //Получаем список устройств вокруг
            _devices = _bluetooth.getDescoveredDevices();

            BluetoothDevice dev = findServer();
            if (dev == null) {
                //Сервер не найден - значит мы первые
                GlobalVars.isServer = true;
                _bluetooth.makeDiscoverable();
                startServer();
            } else {
                //Сервер найден, подключаемся к нему
                GlobalVars.isServer = false;
                connectToServer(dev);
            }

            //Пошел микрофон, динамик
            startMic();
            startSpeaker();
            Utils.getInstance().setMaxVolume();

            //Новый статус у приложения
            GlobalVars.currentProgramState = GlobalVars.IS_ON;
        }
    };

    /**
     *  Асинхронный поток, ждет окончания поиска устройств
     */
    private class Async extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            while (true) {
                if (GlobalVars.isBluetoothDiscoveryFinished) {
                    break;
                }
            }
            _conThread = new ConThread();
            _conThread.start();

            return null;
        }
    };

    @Override
    public void onDestroy() {
        Utils.getInstance().setNormalVolume();
        _bluetooth.changeDeviceName(GlobalVars.oldDeviceName);

        if (_bluetooth.isEnabled()) {
            _bluetooth.turnOff();
            Utils.getInstance().addStatusText(GlobalVars.context.getString(R.string.bt_turn_off));
        }

        stopServerOrClient();
        stopMic();
        stopSpeaker();

        GlobalVars.currentProgramState = GlobalVars.IS_OFF;
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
     * Подключение к серверу
     */
    private void connectToServer(BluetoothDevice device) {
        GlobalVars.connectDeviceName = device.getName();
        GlobalVars.connectDeviceAddrs = device.getAddress();

        _client = new BluetoothClient(device, GlobalVars.connectDeviceAddrs);
        _client.addReciever(_handler);
        _client.start();
    }

    /**
     * Запуск сервера
     */
    private void startServer() {
        _server = new BluetoothServer(_bluetooth.getAdapter());
        _server.addReciever(_handler);
        _server.start();
    }

    /**
     * Запуск микрофона
     */
    private void startMic() {
        if (_mic == null) {
            _mic = new MicHelper();
            _mic.addReciever(_handler);
            _mic.start();
        }
    }

    /**
     * Запуск динамика
     */
    private void startSpeaker() {
        if (_speaker == null) {
            _speaker = new SpeakerHelper();
            _speaker.start();
        }
    }

    /**
     * Остановка микрофона
     */
    private void stopMic() {
        if (_mic != null) {
            _mic.close();
            _mic = null;
        }
    }

    /**
     * Остановка динамика
     */
    private void stopSpeaker() {
        if (_speaker != null) {
            _speaker.close();
            _speaker = null;
        }
    }

    /**
     * Остановка сервера или клиента
     */
    private void stopServerOrClient() {
        if (GlobalVars.isServer) {
            if (_server != null) {
                _server.close();
                _server = null;
            }
        } else {
            if (_client != null) {
                _client.close();
                _client = null;
            }
        }
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
