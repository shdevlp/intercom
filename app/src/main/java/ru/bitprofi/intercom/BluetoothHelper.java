package ru.bitprofi.intercom;

/**
 * Created by Дмитрий on 22.04.2015.
 */
public class BluetoothHelper {
    private boolean _isEnable;

    public BluetoothHelper() {
        _isEnable = true;
    }

    /**
     * Состояние(Включен/Выключен) Bluetooth
     * @return
     */
    public boolean isBluetooth() {
        return _isEnable;
    }
}
