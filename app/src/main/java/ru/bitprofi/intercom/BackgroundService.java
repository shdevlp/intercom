package ru.bitprofi.intercom;

import android.app.Service;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Handler;
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

    private HashMap<String, String> _devices; //Список устройств вокруг

    private MicHelper     _mic;     //Микрофон
    private SpeakerHelper _speaker; //Динамик

    private Handler _handler;       //Обработчик


    @Override
    public void onCreate() {
        _bluetooth = new BluetoothHelper();

        _handler = new Handler() {
            public void handleMessage(android.os.Message msg) {
                byte[] data = (byte[])msg.obj;

                switch (msg.what) {
                    case GlobalVars.MIC_MSG_DATA:
                        //Данные с микрофона
                        //Log.d("MIC_MSG_DATA", String.valueOf(data));
                        _speaker.addData(data);
                        /*
                        if (GlobalVars.isServer) {
                            _server.setSendData(data);
                        } else {
                            _client.setSendData(data);
                        }
                        */
                        break;

                    case GlobalVars.SERVER_MSG_DATA:
                    case GlobalVars.CLIENT_MSG_DATA:
                        //Данные от сервера или клиента - проигрываем
                        //_speaker.setSendData(data);
                        break;
                }
            }
        };
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //Максимальная громкость
        Utils.getInstance().setMaxVolume();

        GlobalVars.currentDeviceName = Utils.getInstance().getNewDeviceName();

        //Ждем включения bluetooth
        if (!_bluetooth.isEnabled()) {
            _bluetooth.turnOn();
            Utils.getInstance().waitBluetoothState(_bluetooth, true);
        }

        //Запоминаем адрес Bluetooth
        GlobalVars.currentAddress = _bluetooth.getAddress();
        //Запоминаем старое имя
        GlobalVars.oldDeviceName = _bluetooth.getName();
        //Меняем на новое имя
        _bluetooth.changeDeviceName(GlobalVars.currentDeviceName);
        //Делаем доступным для обнаружения
        _bluetooth.makeDiscoverable();
        //Получаем список устройств вокруг
        _bluetooth.startDiscovery();
        //Получаем результат
        _devices = _bluetooth.getDescoveredDevices();

        BluetoothDevice dev = findServer();
        if (dev == null) {
            //Сервер не найден - значит мы первые
            GlobalVars.isServer = true;
            startServer();
        } else {
            //Сервер найден, подключаемся к нему
            GlobalVars.isServer = false;
            connectToServer(dev);
        }

        //Пошел микрофон, динамик
        startMicSpeaker();
        //Новый статус у приложения
        GlobalVars.currentProgramState = GlobalVars.IS_ON;

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        stopServerOrClient();
        stopMicSpeaker();

        if (_bluetooth.isEnabled()) {
            _bluetooth.turnOff();
            //Ждем выключения bluetooth
            Utils.getInstance().waitBluetoothState(_bluetooth, false);
        }

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
     * Запуск микрофона и динамика
     */
    private void startMicSpeaker() {
        if (_mic == null) {
            _mic = new MicHelper();
            _mic.addReciever(_handler);
            _mic.start();
        }
        if (_speaker == null) {
            _speaker = new SpeakerHelper();
            _speaker.start();
        }
    }

    /**
     * Остановка микрофона и динамика
     */
    private void stopMicSpeaker() {
        if (_mic != null) {
            _mic.close();
            while (_mic.isRunning()) {
                Utils.getInstance().sleep(10);
            }
            _mic = null;
        }

        if (_speaker != null) {
            _speaker.close();
            while (_speaker.isRunning()) {
                Utils.getInstance().sleep(10);
            }
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
            }
        } else {
            if (_client != null) {
                _client.close();
            }
        }
    }


    /**
     * Поиск сервера
     * @return
     */
    private BluetoothDevice findServer() {
        //В поисках сервера
        if (_devices == null) {
            return null;
        }

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

        return null;
    }
}
