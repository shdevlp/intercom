package ru.bitprofi.intercom;

import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.HashMap;

import at.markushi.ui.CircleButton;

public class PlaceholderFragment extends Fragment {
    private CircleButton _btnGo;   //Кнопка на все случаи жизни
    private Intent _mainService;   //Фоновая служба

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        prepeareForWork(rootView);
        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();

        GlobalVars.contextFragment = PlaceholderFragment.this.getActivity();
        _mainService = new Intent(GlobalVars.contextFragment, BackgroundService.class);

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
                BluetoothHelper bluetooth = new BluetoothHelper();
                GlobalVars.bluetoothAdapter = bluetooth.getAdapter();

                //Ждем включения bluetooth
                if (!bluetooth.isEnabled()) {
                    bluetooth.turnOn();
                }

                GlobalVars.oldDeviceName        = bluetooth.getName();
                GlobalVars.currentDeviceName    = GlobalVars.BLUETOOTH_NAME;
                GlobalVars.currentDeviceAddress = bluetooth.getAddress();

                bluetooth.changeDeviceName(GlobalVars.currentDeviceName);
                bluetooth.makeDiscoverable();

                Utils.getInstance().addStatusText(GlobalVars.context.getString(R.string.bt_turn_on));

                GlobalVars.isBluetoothDiscoveryFinished = false;
                bluetooth.startDiscovery();
                Utils.getInstance().waitScreenBTDiscovery();

                while (!GlobalVars.isBluetoothDiscoveryFinished) {
                    ;
                }

                findDevice(bluetooth);

                if (GlobalVars.serverDevice == null) {
                    GlobalVars.isServer = true;
                    Utils.getInstance().addStatusText(GlobalVars.context.getString(R.string.i_am_server));
                } else {
                    GlobalVars.isServer = false;
                    Utils.getInstance().addStatusText(GlobalVars.context.getString(R.string.i_am_client));
                }

                Utils.getInstance().setBtnColor(GlobalVars.activity.getResources().getColor(R.color.seagreen));
                Utils.getInstance().setBtnEnabled(true);
            }
        });
        btThread.start();
    }

    /**
     * Подготовка к работе, настройка gui
     */
    private void prepeareForWork(View v) {
        _btnGo = (CircleButton) v.findViewById(R.id.btnGo);
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
        Utils.getInstance().setBtnEnabled(false);
    }

    /**
     * Обработка нажатия
     */
    private void btnGoClicked() {
        //Программа уже работает - надо выключить
        if (GlobalVars.buttonState == GlobalVars.BUTTON_IS_ON) {
            if (Utils.getInstance().isServiceRunning(BackgroundService.class)) {
                GlobalVars.contextFragment.stopService(_mainService);
            }

            Utils.getInstance().setBtnOnOff(false);
            return;
        }

        //Включаем работу
        if (GlobalVars.buttonState == GlobalVars.BUTTON_IS_OFF) {
            if (!Utils.getInstance().isServiceRunning(BackgroundService.class)) {
                GlobalVars.contextFragment.startService(_mainService);
            }

            Utils.getInstance().setBtnOnOff(true);
            return;
        }
    }
}