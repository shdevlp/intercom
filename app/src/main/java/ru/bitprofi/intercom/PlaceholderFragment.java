package ru.bitprofi.intercom;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

/**
 * A placeholder fragment containing a simple view.
 */
public class PlaceholderFragment extends Fragment {
    private Button _btnBluetoothOnOff;//Включить/Выключить Bluetooth
    private Button _btnDeviceConnect; //Подсоединиться к устройству
    private Button _btnSearchDevices; //Поиск устройств

    private TextView _tvBluetoothStatus;     //Просто текст
    private TextView _tvBluetoothStatusText; //Статус Bluetooth

    private BluetoothHelper _bluetooth;

    public PlaceholderFragment() {
        _bluetooth = new BluetoothHelper();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        prepeareForWork(rootView);
        return rootView;
    }

    /**
     * Подготовка к работе, настройка gui
     */
    private void prepeareForWork(View v) {
        _btnBluetoothOnOff   = (Button) v.findViewById(R.id.btnBluetoothOnOff);
        _btnDeviceConnect = (Button) v.findViewById(R.id.btnDeciveConnect);
        _btnSearchDevices = (Button) v.findViewById(R.id.btnSearchDevices);

        _tvBluetoothStatus     = (TextView) v.findViewById(R.id.tvBluetoothStatus);
        _tvBluetoothStatusText = (TextView) v.findViewById(R.id.tvBluetoothStatusText);

        //Обработка нажатий

        View.OnClickListener onClickBtns = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.btnBluetoothOnOff:
                        if (_bluetooth.isBluetooth()) {
                            _tvBluetoothStatusText.setText(GlobalVars.gvStatusTextOn);
                            _tvBluetoothStatusText.setTextColor(GlobalVars.gvStatusColorOn);
                            _btnSearchDevices.setEnabled(true);
                            _btnBluetoothOnOff.setText(GlobalVars.gvBtnStatusTextOff);
                        } else {
                            _tvBluetoothStatusText.setText(GlobalVars.gvStatusTextOff);
                            _tvBluetoothStatusText.setTextColor(GlobalVars.gvStatusColorOff);
                            _btnSearchDevices.setEnabled(false);
                            _btnBluetoothOnOff.setText(GlobalVars.gvBtnStatusTextOn);
                        }
                        break;
                    case R.id.btnDeciveConnect:
                        break;
                    case R.id.btnSearchDevices:
                        break;
                }
            }
        };

        _btnBluetoothOnOff.setOnClickListener(onClickBtns);
        _btnDeviceConnect.setOnClickListener(onClickBtns);
        _btnSearchDevices.setOnClickListener(onClickBtns);
    }
}