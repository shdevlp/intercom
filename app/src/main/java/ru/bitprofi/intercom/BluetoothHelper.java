package ru.bitprofi.intercom;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;


/**
 * Поддержка Bluetooth
 * Created by Дмитрий on 22.04.2015.
 */
public class BluetoothHelper {
    private static final int BT_DISCOVERABLE_DURATION = 600;

    private Set<BluetoothDevice> _devices; //Обнаруженные устройства для подключенния
    private BluetoothAdapter _ba;

    //Нашел Bluetooth устройство
    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                _devices.add(device);
                return;
            }
            if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                GlobalVars.isBluetoothDiscoveryFinished = false;
                _devices.clear();
                return;
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                GlobalVars.isBluetoothDiscoveryFinished = true;
                return;
            }
        }
    };

    public BluetoothHelper() {
        _ba = BluetoothAdapter.getDefaultAdapter();
        _devices = new HashSet<BluetoothDevice>();

        IntentFilter filterFound = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        IntentFilter filterStart = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        IntentFilter filterFinish = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);

        GlobalVars.activity.registerReceiver(receiver, filterFound);
        GlobalVars.activity.registerReceiver(receiver, filterStart);
        GlobalVars.activity.registerReceiver(receiver, filterFinish);
    }

    /**
     *
     */
    public void close() {
        GlobalVars.activity.unregisterReceiver(receiver);
    }

    /**
     * Сделать устройство доступным для поиска других устройств
     */
    public void makeDiscoverable() {
        Intent discoverableIntent = new
                Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION,
                BT_DISCOVERABLE_DURATION);
        GlobalVars.activity.startActivityForResult(discoverableIntent, 0);
    }

    /**
      * Выдает список ранее сопряженных устройств
      * @return Map<name, address>
     */
    public HashMap<String, String> getBoundedDevices() {
        Set<BluetoothDevice> pairedDevices = _ba.getBondedDevices();
        HashMap map = new HashMap<String, String>();

        for (BluetoothDevice bt : pairedDevices) {
            map.put(bt.getName(), bt.getAddress());
        }
        return map;
    }

    /**
     * Выдает список заново обнаруженных устройств
     * @return
     */
    public HashMap<String, String> getDescoveredDevices() {
        if (_devices.size() == 0) {
            return null;
        }

        HashMap map = new HashMap<String, String>();
        for (BluetoothDevice bt : _devices) {
            Utils.getInstance().addStatusText(bt.getName() + " : "+ bt.getAddress());
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
     * Старт обнаружения устройств
     */
    public void startDiscovery() {
        if (_ba.isDiscovering()) {
            _ba.cancelDiscovery();
            while (_ba.isDiscovering()) {
                ;
            }
        }
        _ba.startDiscovery();
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
            Utils.getInstance().dialog(R.string.information,
                    R.string.bt_not_supported, R.string.ok);
            return;
        }
       if (!_ba.isEnabled()) {
           _ba.enable();
           while (!_ba.isEnabled()) {
               ;
           }
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
        if (_ba.isEnabled()) {
            _ba.disable();
            while (_ba.isEnabled()) {
                ;
            }
        }
    }

    /**
     * Состояние(Включен/Выключен) Bluetooth
     * @return
     */
    public boolean isEnabled() {
        return _ba.isEnabled();
    }
}
