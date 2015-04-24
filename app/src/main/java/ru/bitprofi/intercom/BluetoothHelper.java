package ru.bitprofi.intercom;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * Created by Дмитрий on 22.04.2015.
 */
public class BluetoothHelper {
    private static final int REQUEST_ENABLE_BT = 0;
    private static final int REQUEST_DISCOVERABLE_BT = 0;

    private BluetoothAdapter _ba;
    private BluetoothSocket _socket;
    private BluetoothServerSocket _serverSocket;
    private Utils _utils;

    public BluetoothHelper() {
        _ba = BluetoothAdapter.getDefaultAdapter();
        _utils = new Utils();
    }


    /**
     * Подключиться к устройству
     * @param device
     * @return
     */
    public boolean connectToDevice(BluetoothDevice device) {
       return true;
    }

    /**
     * Сделать устройство доступным для поиска других устройств
     * @return
     */
    public boolean makeDiscoverable() {
        if (_ba.isDiscovering()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            GlobalVars.activity.startActivityForResult(enableBtIntent, REQUEST_DISCOVERABLE_BT);
            return true;
        }
        return false;
    }

    /**
      * Ищем устройства
      * @return Map<name, address>
     */
    public HashMap<String, String> getBluetoothDevices() {
        _ba.startDiscovery();

        Set<BluetoothDevice> pairedDevices = _ba.getBondedDevices();
        HashMap map = new HashMap<String, String>();

        for (BluetoothDevice bt : pairedDevices) {
            map.put(bt.getName(), bt.getAddress());
        }
        return map;
    }

    /**
     * Сменить имя
     * @param name
     */
    public boolean changeDeviceName(String name) {
        return _ba.setName(name);
    }

    /**
     * Включить Bluetooth
     */
    public void turnOn() {
        if (_ba == null) {
            _utils.dialog(R.string.dialog_title,
                    R.string.bluetooth_not_supported, R.string.dialog_ok);
            return;
        }
       if (!_ba.isEnabled()) {
            Intent turnOn = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            GlobalVars.activity.startActivityForResult(turnOn, REQUEST_ENABLE_BT);
       }
    }

    /**
     * Вернуть текущее имя Bluetoth
     * @return
     */
    public String getName() {
        return _ba.getName();
    }

    /**
     * Вернуть текущий адрес Bluetooth
     * @return
     */
    public String getAddress() {
        return _ba.getAddress();
    }

    /**
     * Выключить Bluetooth
     */
    public void turnOff() {
        String old = GlobalVars.oldDeviceName;
        changeDeviceName(old);
        _utils.sleep(100);
        _ba.disable();
        _utils.dialog(R.string.dialog_title,
                     R.string.bluetooth_turn_off, R.string.dialog_ok);
    }

    /**
     * Состояние(Включен/Выключен) Bluetooth
     * @return
     */
    public boolean isEnabled() {
        return _ba.isEnabled();
    }
}
