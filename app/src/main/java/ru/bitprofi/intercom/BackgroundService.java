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
    private BluetoothClient _client;   //Клиент
    private BluetoothServer _server;   //Сервер
    private HashMap<String, String> _devices; //Список устройств вокруг
    private ConThread  _conThread;


    @Override
    public void onCreate() {
        _bluetooth = new BluetoothHelper();

Utils.getInstance().addStatusText(">ЗАПУСК СЛУЖБЫ");

        _conThread = null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //Ждем включения bluetooth
        if (!_bluetooth.isEnabled()) {

Utils.getInstance().addStatusText(">ВКЛЮЧИТЬ BLUETOOTH");

            _bluetooth.turnOn();
        }

Utils.getInstance().addStatusText(">BLUETOOTH ВКЛЮЧЕН");

        GlobalVars.oldDeviceName        = _bluetooth.getName();
        GlobalVars.currentDeviceName    = Utils.getInstance().getNewDeviceName();
        GlobalVars.currentDeviceAddress = _bluetooth.getAddress();

        _bluetooth.changeDeviceName(GlobalVars.currentDeviceName);

Utils.getInstance().addStatusText(">НОВОЕ ИМЯ УСТРОЙСТВА:" + GlobalVars.currentDeviceName);

Utils.getInstance().addStatusText(">УСТРОЙСТВО ДОСТУПНО ДЛЯ ОБНАРУЖЕНИЯ");

        _bluetooth.makeDiscoverable();

        Utils.getInstance().addStatusText(GlobalVars.context.getString(R.string.bt_turn_on));

        GlobalVars.isBluetoothDiscoveryFinished = false;
        _bluetooth.startDiscovery();

Utils.getInstance().addStatusText(">НАЧАЛ ПОИСК УСТРОЙСТВ");

        Utils.getInstance().waitScreenBTDiscovery();

        Async async = new Async();
        async.execute();

        return START_NOT_STICKY;//Не перезапускать при аварии
    }

    /**
     * Продолжаем работу
     */
    private class ConThread extends Thread {
        @Override
        public void run() {
            //Получаем список устройств вокруг

Utils.getInstance().addStatusText(">ЗАКОНЧИЛ ПОИСК УСТРОЙСТВ");

            _devices = _bluetooth.getDescoveredDevices();

Utils.getInstance().addStatusText(">ПОИСК СЕРВЕРА В НАЙДЕННЫХ УСТРОЙСТВАХ");

            BluetoothDevice dev = findServer();
            if (dev == null) {
                //Запуск сервера
Utils.getInstance().addStatusText(">СЕРВЕР НЕ НАЙДЕН");
                startServer();
Utils.getInstance().addStatusText(">СЕРВЕР СТУРТУЕТ");
            } else {
Utils.getInstance().addStatusText(">СЕРВЕР НАЙДЕН");
                //Сервер уже был запущен!
                //Запуск клиента
Utils.getInstance().addStatusText(">КЛИЕНТА СТАРТУЕТ");
                connectToServer(dev);
            }

Utils.getInstance().addStatusText(">УСТАНОВКА МАКСИМАЛЬНОЙ ГРОМКОСТИ ЗВУКА");

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
        GlobalVars.connectDeviceUUID = GlobalVars.connectDeviceName.split("_")[1];

        GlobalVars.isServer = false;
        _client = new BluetoothClient(device);
        _client.start();
    }


    /**
     * Запуск сервера
     */
    private boolean startServer() {
        GlobalVars.isServer = true;
        if (_server == null) {
            _server = new BluetoothServer(_bluetooth.getAdapter());
            _server.start();
            return true;
        }
Utils.getInstance().addStatusText(">ОШИБКА ЗАПУСКА СЕРВЕРА - СЕРВЕР УЖЕ РАБОТАЕТ");
        return false;
    }

    /**
     * Остановка сервера или клиента
     */
    private void stopServerOrClient() {
        if (GlobalVars.isServer) {
            if (_server != null) {
Utils.getInstance().addStatusText(">ОСТАНОВКА СЕРВЕРА");
                _server.stopThread();
                _server = null;
            }
        } else {
            if (_client != null) {
Utils.getInstance().addStatusText(">ОСТАНОВКА КЛИЕНТА");
                _client.stopThread();
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
Utils.getInstance().addStatusText(">НАШЕЛ СЕРВЕР:"+device.getName());
                        return device;
                    }
                }
            }
        }
        return null;
    }
}