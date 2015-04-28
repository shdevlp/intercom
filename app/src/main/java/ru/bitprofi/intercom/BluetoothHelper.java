package ru.bitprofi.intercom;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.provider.Settings;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;


/**
 * Created by Дмитрий on 22.04.2015.
 */
public class BluetoothHelper {
    private static final int REQUEST_ENABLE_BT = 0;
    private static final int REQUEST_DISCOVERABLE_BT = 0;

    private BluetoothAdapter _ba;

    public BluetoothHelper() {
        _ba = BluetoothAdapter.getDefaultAdapter();
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
     * Возвращает устройство по uuid
     * @param uuid
     * @return
     */
    public BluetoothDevice getDevice(String uuid) {
        Set<BluetoothDevice> pairedDevices = _ba.getBondedDevices();
        for (BluetoothDevice bt : pairedDevices) {
            if (uuid.equals(bt.getAddress())) {
                return bt;
            }
        }
        return null;
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
            Utils.getInstance().dialog(R.string.dialog_title,
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
     * Вернуть адаптер
     * @return
     */
    public BluetoothAdapter getAdapter() {
        return _ba;
    }

    /**
     * Выключить Bluetooth
     */
    public void turnOff() {
        String old = GlobalVars.oldDeviceName;
        changeDeviceName(old);
        Utils.getInstance().sleep(100);
        _ba.disable();
        Utils.getInstance().dialog(R.string.dialog_title,
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
