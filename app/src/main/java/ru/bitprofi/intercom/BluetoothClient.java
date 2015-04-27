package ru.bitprofi.intercom;

import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.provider.Settings;
import android.widget.TextView;
import android.widget.Toast;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

/**
 * Created by Дмитрий on 27.04.2015.
 */

public class BluetoothClient extends Thread {
    private Utils _utils = null;
    private BluetoothSocket _socket = null;
    private volatile boolean _isEnable = false;

    public BluetoothClient(BluetoothDevice device, String uuid) {
        _utils = new Utils();
        try {
            _utils.setStatusText(GlobalVars.activity.getString(R.string.client_searching));
            _socket = device.createRfcommSocketToServiceRecord(UUID.fromString(uuid));
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * Запуск клиента
     */
    @Override
    public void run() {
        InputStream tmpIn  = null;
        OutputStream tmpOut = null;
        DataInputStream inStream = null;
        DataOutputStream outStream = null;

        byte[] buffer = new byte[1024];
        int bytesRead;
        int availableBytes;

        boolean success = false;
        try {
            _socket.connect();
            success = true;

            if (success) {
                _isEnable = true;
                //Успешно подлючились
                _utils.setStatusText(GlobalVars.activity.getString(
                        R.string.client_is_connected));

                tmpIn = _socket.getInputStream();
                tmpOut = _socket.getOutputStream();

                inStream = new DataInputStream(tmpIn);
                outStream = new DataOutputStream(tmpOut);

                while (_isEnable) {
                    availableBytes = inStream.available();
                    if (availableBytes > 0) {
                        bytesRead = inStream.read(buffer);
                    }
                }

            }
        } catch (IOException e) {
            e.printStackTrace();
            _utils.setStatusText(GlobalVars.activity.getString(
                    R.string.something_went_wrong) + ":\n" + e.getMessage());
            try {
                _isEnable = false;
                _socket.close();
            } catch (IOException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
        }

        if (!success) {
            _utils.setStatusText(GlobalVars.activity.getString(
                    R.string.error_connected));
        }
    }

    /**
     * Закрытие клиента
     */
    public void close() {
        try {
            if (_socket != null) {
                _isEnable = false;
                _socket.close();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        _utils.setStatusText(GlobalVars.activity.getString(R.string.client_close));
    }


    /**
     * Отмена
     */
    public void cancel() {
        Toast.makeText(GlobalVars.activity.getApplicationContext(),
                GlobalVars.activity.getString(R.string.client_closing),
                Toast.LENGTH_LONG).show();

        try {
            _isEnable = false;
            _socket.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }
}