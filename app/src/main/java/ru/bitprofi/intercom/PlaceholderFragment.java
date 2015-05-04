package ru.bitprofi.intercom;

import android.bluetooth.BluetoothDevice;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import java.util.HashMap;

import com.dd.CircularProgressButton;

import java.util.List;
import java.util.Map;
import java.util.Set;
import android.os.*;
import java.util.logging.LogRecord;

import at.markushi.ui.CircleButton;

public class PlaceholderFragment extends Fragment {
    private BluetoothHelper _bluetooth;
    private BluetoothClient _client;
    private BluetoothServer _server;

    private CircleButton _btnGo;       //Кнопка на все случаи жизни
    private ProgressBar _progressBar;  //Показывает длительность процесса

    private HashMap<String, String> _devices; //Список устройств вокруг

    private MicHelper     _mic;
    private SpeakerHelper _speaker;
    private EchoPlayer    _echo;

    private Handler _handler;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        _bluetooth = new BluetoothHelper();

        //Включаем эхо
        startEcho();

        //Максимальная громкость
        Utils.getInstance().setMaxVolume();

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

        GlobalVars.currentDeviceName = Utils.getInstance().getNewDeviceName();
        prepeareForWork(rootView);
        return rootView;
    }

    /**
     * Подготовка к работе, настройка gui
     */
    private void prepeareForWork(View v) {
        _btnGo = (CircleButton) v.findViewById(R.id.btnGo);
        _progressBar = (ProgressBar) v.findViewById(R.id.progressBar);
        _progressBar.setVisibility(View.INVISIBLE);

        //Обработка нажатий
        View.OnClickListener onClickBtns = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.btnGo:
                        btnGoClicked();
                    break;
                }
            }
        };

        _btnGo.setOnClickListener(onClickBtns);
    }

    /**
     * Изменить цвет кнопки в зависимости от состояния Bluetooth
     */
    private void changeBtnColor(final boolean enable) {
        if (enable) {
            _btnGo.setColor(getResources().getColor(R.color.crimson));
        } else {
            _btnGo.setColor(getResources().getColor(R.color.seagreen));
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
     * Подключение к серверу
     * @param key Имя
     * @param value Адрес
     */
    private void connectToServer(BluetoothDevice device, String key, String value) {
        GlobalVars.connectDeviceName = key;
        GlobalVars.connectDeviceAddrs = value;

        _client = new BluetoothClient(device, value);
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
     *
     */
    private void startEcho() {
        if (_echo == null) {
            _echo = new EchoPlayer();
            _echo.start();
        }
    }

    /**
     *
     */
    private void stopEcho() {
        if (_echo != null) {
            _echo.close();
            while (_echo.isRunning()) {
                Utils.getInstance().sleep(5);
            }
            _echo = null;
        }
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
     * 
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
     * Поиск и подключение к серверу
     * @return
     */
    private boolean findConnectServer() {
        boolean bFindConnectServer = false;
        //В поисках сервера
        for (HashMap.Entry<String, String> entry : _devices.entrySet()) {
            String key = (String) entry.getKey();
            String value = (String) entry.getValue();

            if (analizeKeyName(key)) {
                //Нашли сервер, подключаемс к нему
                bFindConnectServer = true;
                BluetoothDevice device = _bluetooth.getDevice(value);

                if (device != null) {
                    GlobalVars.isServer = false;
                    connectToServer(device, key, value);
                }
                break;
            }
        }
        return bFindConnectServer;
    }

    /**
     * Обработка нажатия
     */
    private void btnGoClicked() {
        //Программа уже работает - надо выключить
        if (GlobalVars.currentProgramState == GlobalVars.IS_ON) {
            changeBtnColor(false);

            stopServerOrClient();
            stopMicSpeaker();
            startEcho();

            if (_bluetooth.isEnabled()) {
                _bluetooth.turnOff();
                //Ждем выключения bluetooth
                Utils.getInstance().waitBluetoothState(_bluetooth, false);
            }

            GlobalVars.currentProgramState = GlobalVars.IS_OFF;
            return;
        }

        //Включаем работу
        if (GlobalVars.currentProgramState == GlobalVars.IS_OFF) {
            changeBtnColor(true);

            //Ждем включения bluetooth
            if (!_bluetooth.isEnabled()) {
                _bluetooth.turnOn();
                Utils.getInstance().waitBluetoothState(_bluetooth, true);
            }

            //Выключаем Эхо
            stopEcho();
            //Запоминаем адрес Bluetooth
            GlobalVars.currentAddress = _bluetooth.getAddress();
            //Запоминаем старое имя
            GlobalVars.oldDeviceName = _bluetooth.getName();
            //Меняем на новое имя
            _bluetooth.changeDeviceName(GlobalVars.currentDeviceName);
            //Делаем доступным для обнаружения
            _bluetooth.makeDiscoverable();
            //Получаем список устройств вокруг
            _devices = _bluetooth.getBluetoothDevices();

            if (!findConnectServer()) {
                //Сервер не найден - значит мы первые
                GlobalVars.isServer = true;
                startServer();
            }

            //Пошел микрофон, динамик
            startMicSpeaker();
            //Новый статус у приложения
            GlobalVars.currentProgramState = GlobalVars.IS_ON;
            return;
        }
    }
}