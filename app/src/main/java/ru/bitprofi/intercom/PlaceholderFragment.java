package ru.bitprofi.intercom;

import android.bluetooth.BluetoothDevice;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
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

import at.markushi.ui.CircleButton;

public class PlaceholderFragment extends Fragment {
    private Utils _utils;
    private BluetoothHelper _bluetooth;
    private BluetoothClient _client;
    private BluetoothServer _server;

    private CircleButton _btnGo;       //Кнопка на все случаи жизни
    private ProgressBar _progressBar;  //Показывает длительность процесса

    private HashMap<String, String> _devices; //Список устройств вокруг

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        _utils = new Utils();
        _bluetooth = new BluetoothHelper();
        GlobalVars.currentDeviceName = _utils.getNewDeviceName();
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
                        changeBtnColor();
                        break;
                }
            }
        };

        _btnGo.setOnClickListener(onClickBtns);
    }

    /**
     * Изменить цвет кнопки в зависимости от состояния Bluetooth
     */
    private void changeBtnColor() {
        if (_bluetooth.isEnabled()) {
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
        final int idx = key.indexOf(GlobalVars.prefixDeviceName);
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
        _client.start();
    }

    /**
     * Запуск сервера
     */
    private void startServer() {
        _server = new BluetoothServer(_bluetooth.getAdapter());
        _server.start();
    }
    /**
     * Обработка нажатия
     */
    private void btnGoClicked() {
        if(!_bluetooth.isEnabled()) {
            _bluetooth.turnOn();
            //Ждем включения bluetooth
            while (!_bluetooth.isEnabled()) {
                _utils.sleep(5);
            }
            _utils.sleep(100);
            //Запоминаем адрес Bluetooth
            GlobalVars.currentAddress = _bluetooth.getAddress();
            //Запоминаем старое имя
            GlobalVars.oldDeviceName  = _bluetooth.getName();
            //Меняем на новое имя
            _bluetooth.changeDeviceName(GlobalVars.currentDeviceName);
            //Делаем доступным для обнаружения
            _bluetooth.makeDiscoverable();
            //Получаем список устройств вокруг
            _devices = _bluetooth.getBluetoothDevices();

            //Флаг подключения
            boolean bConnect = false;
            for (HashMap.Entry<String, String> entry : _devices.entrySet()) {
                String key = (String)entry.getKey();
                String value = (String)entry.getValue();

                //Нашли сервер, подключаемс к нему
                if (analizeKeyName(key)) {
                    bConnect = true;
                    BluetoothDevice device = _bluetooth.getDevice(value);
                    if (device != null) {
                        connectToServer(device, key, value);
                    } else {
                        //....
                    }
                    break;
                }
            }

            //Не нашли сервер, значит мы первые -
            // запускаем сервер
            if (!bConnect) {
                startServer();
            }

        } else {
            _bluetooth.turnOff();
            if (_server != null) {
                _server.close();
            }
            if (_client != null) {
                _client.close();
            }
        }
    }
}