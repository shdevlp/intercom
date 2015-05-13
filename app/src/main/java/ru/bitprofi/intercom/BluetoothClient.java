package ru.bitprofi.intercom;

import android.bluetooth.BluetoothAdapter;
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
    private DataInputStream _inStream = null;
    private DataOutputStream _outStream = null;
    private BluetoothAdapter _ba = null;

    /**
     * Инициализация
     * @param device
     */
    public BluetoothClient(BluetoothDevice device) {
        super();

        GlobalVars.connectDeviceName = device.getName();
        GlobalVars.connectDeviceAddrs = device.getAddress();

        Utils.getInstance().addStatusText(GlobalVars.activity.getString(R.string.client_searching));

        BluetoothSocket tmp;
        UUID uuid = UUID.fromString(GlobalVars.UUID);
        try {
            tmp = device.createRfcommSocketToServiceRecord(uuid);
        } catch (IOException e) {
            e.printStackTrace();
            stopThread();
            throw new RuntimeException(GlobalVars.activity.getString(
                    R.string.error_connection_dropped) + " : " + e.getMessage());
        }
        _socket = tmp;
        _ba = BluetoothAdapter.getDefaultAdapter();
    }

    @Override
    public void run() {
        if (_handler == null) {
            throw new RuntimeException("BluetoothClient : Handler == null");
        }

        try {
            if (_ba.isDiscovering()) {
                _ba.cancelDiscovery();
            }
            _socket.connect();
            _isRunning = true;
        } catch (IOException e) {
            e.printStackTrace();
            stopThread();
            throw new RuntimeException(GlobalVars.activity.getString(
                    R.string.error_connection_dropped) + " : " + e.getMessage());
        }

        Utils.getInstance().addInfoAboutDevice(_socket, false);

        InputStream tmpInput;
        OutputStream tmpOutput;

        try {
            tmpInput = _socket.getInputStream();
            tmpOutput = _socket.getOutputStream();
        } catch (IOException e) {
            e.printStackTrace();
            stopThread();
            throw new RuntimeException(GlobalVars.activity.getString(
                    R.string.error_get_io_stream) + " : " + e.getMessage());
        }

        _inStream = new DataInputStream(tmpInput);
        _outStream = new DataOutputStream(tmpOutput);

        int availableBytes;
        int bytesRead;

        while (_isRunning)
            try {
                availableBytes = _inStream.available();
                if (availableBytes > 0) {
                    byte[] buffer = new byte[availableBytes];
                    bytesRead = _inStream.read(buffer);
                    if (bytesRead > 0) {
                        _handler.sendMessage(_handler.obtainMessage(
                                GlobalVars.SPEAKER_MSG_DATA, buffer));
                    }//if
                }//if

                if (_vector.size() > 0) {
                    byte[] buff = _vector.elementAt(0);
                    _outStream.write(buff, 0, buff.length);
                    _vector.removeElementAt(0);
                }
            } catch (IOException e) {
                stopThread();
                e.printStackTrace();
                Utils.getInstance().addStatusText(GlobalVars.activity.getString(
                    R.string.error_connection_dropped) + " : " + e.getMessage());
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
            Utils.getInstance().addStatusText(GlobalVars.activity.getString(
                    R.string.error_stop_thread) + " : " + ex.getMessage());
        }

        Utils.getInstance().addStatusText(GlobalVars.activity.getString(R.string.client_close));
    }
}
