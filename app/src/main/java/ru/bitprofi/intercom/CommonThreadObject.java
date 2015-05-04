package ru.bitprofi.intercom;

import android.os.Handler;
import android.widget.Toast;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

/** Базовый класс для наследования с поддержкой потоков
 * Created by Дмитрий on 29.04.2015.
 */
public class CommonThreadObject extends Thread {
    protected List<Handler> _recievers;       //Получатели данных
    protected Vector<byte[]> _vector = null;

    protected boolean _isRunning = false;     //Флаг работы потока
    private int _msgType = -1;

    /**
     * Инициализация
     */
    public CommonThreadObject(int msgType) {
        _msgType = msgType;
        _recievers = new ArrayList<Handler>();
        _vector = new Vector<byte[]>();
    }

    /**
     * Добавляем получателей
     * @param handler
     */
    protected void addReciever(Handler handler) {
        _recievers.add(handler);
    }

    /**
     * Добавить данные
     * @param data
     */
    protected void addData(byte[] data) {
        _vector.add(data);
    }

    /**
     * Размер данных
     * @return
     */
    protected int getCount() {
        return _vector.size();
    }

    /**
     * Вернуть элемент
     * @param index
     * @return
     */
    protected byte[] getData(int index) {
        return _vector.elementAt(index);
    }

    /**
     * Работает ли поток
     */
    protected boolean isRunning() {
        return _isRunning;
    }

    /**
     * Закрытие потока
     */
    protected void close() {
        _isRunning = false;
        _vector.removeAllElements();
        _vector = null;
        _recievers.clear();
        _recievers = null;
    }

    /**
     * Посылаем данные получателю
     * @param data
     */
    protected void sendMsg(byte[] data) {
        if (_recievers == null || data == null) {
            return;
        }

        for (Handler handler: _recievers) {
            handler.sendMessage(handler.obtainMessage(_msgType, data));
        }
    }

    /**
     * Отмена
     */
    protected void cancel() {
        _isRunning = false;
    }
}

/*

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

public class BluetoothReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (deviceDetectionListener.allDevicesFound())
            return;
        String action = intent.getAction();
        if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
            int newState = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1);
            state = BT_State.getInstanceForState(newState);
            Toast.makeText(activity, state.name(), Toast.LENGTH_SHORT).show();
            if (state == BT_State.On && enableAndDiscover)
                startDiscoveringDevices();
            return;
        }
        if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
            state = BT_State.Discovering;
            Toast.makeText(activity, state.name(), Toast.LENGTH_SHORT).show();
            return;
        }
        if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
            state = BT_State.On;
            Toast.makeText(activity, "Finished discovery", Toast.LENGTH_SHORT).show();
            return;
        }
        if (BluetoothAdapter.ACTION_SCAN_MODE_CHANGED.equals(action)) {
            int discoveryDuration = intent.getIntExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, -1);
            state = BT_State.Advertising;
            Toast.makeText(activity, state.name() + ", duration: " + discoveryDuration, Toast.LENGTH_SHORT).show();
        }
        if (!BluetoothDevice.ACTION_FOUND.equals(action))
            return;
        BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
        (Look at the LOG RESULT)
        Log.i(TAG, "Bluetooth device found: " + device.getName() + ", " + device.getBluetoothClass() + ", " + device.getAddress());
        deviceDetectionListener.newDeviceDetected(device);
        if (deviceDetectionListener.allDevicesFound())
            detectionCompleted();
    }

 */