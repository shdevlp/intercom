package ru.bitprofi.intercom;

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

            for (HashMap.Entry<String, String> entry : _devices.entrySet()) {
                String key = (String)entry.getKey();
                String value = (String)entry.getValue();
            }
        } else {
            _bluetooth.turnOff();
        }

    }
}