package ru.bitprofi.intercom;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;


import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

/**
 * Created by Дмитрий on 07.05.2015.
 */
public class BluetoothClient extends CommonThread {
    private BluetoothSocket _socket = null;

    public BluetoothClient(BluetoothDevice device) {
        super();
        try {
            Utils.getInstance().addStatusText(GlobalVars.activity.getString(R.string.client_searching));
            _socket = device.createRfcommSocketToServiceRecord(UUID.fromString(GlobalVars.connectDeviceUUID));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        int availableBytes;
        int bytesRead;

        boolean success = false;
        try {
            _socket.connect();
            success = true;

            if (success) {
                _isRunning = true;
                //Успешно подлючились
                Utils.getInstance().addStatusText(GlobalVars.activity.getString(
                        R.string.client_is_connected));

                InputStream tmpInput = _socket.getInputStream();
                OutputStream tmpOutput = _socket.getOutputStream();
                DataInputStream inStream = new DataInputStream(tmpInput);
                DataOutputStream outStream = new DataOutputStream(tmpOutput);

                while (_isRunning) {
                    availableBytes = inStream.available();
                    if (availableBytes > 0) {
                        byte[] buffer = new byte[availableBytes];
                        bytesRead = inStream.read(buffer);
                        if (bytesRead > 0) {
                            _handler.sendMessage(_handler.obtainMessage(GlobalVars.SPEAKER_MSG_DATA, buffer));
                        }
                    }

                    if (_vector.size() > 0) {
                        byte[] buff = _vector.elementAt(0);
                        outStream.write(buff, 0, buff.length);
                        _vector.removeElementAt(0);
                    }
                }
            }
        } catch (IOException e) {
            stopThread();
            e.printStackTrace();
            Utils.getInstance().addStatusText(GlobalVars.activity.getString(
                    R.string.error_connection_dropped) + ":\n" + e.getMessage());
        }

        if (!success) {
            Utils.getInstance().addStatusText(GlobalVars.activity.getString(
                    R.string.error_connected));
        }
    }

    /**
     * Остановка потока
     */
    @Override
    public void stopThread() {
        super.stopThread();

        try {
            if (_socket != null) {
                _socket.close();
                _socket = null;
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        Utils.getInstance().addStatusText(GlobalVars.activity.getString(R.string.client_close));
    }
}
