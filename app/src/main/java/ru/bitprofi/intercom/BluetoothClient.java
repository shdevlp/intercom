package ru.bitprofi.intercom;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.*;
import android.os.Process;

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
    private BluetoothDevice _device = null;

    private DataInputStream _inStream = null;
    private DataOutputStream _outStream = null;

    /**
     * Инициализация
     * @param device
     */
    public BluetoothClient(BluetoothDevice device) {
        super();
        _device = device;

        GlobalVars.connectDeviceName = device.getName();
        GlobalVars.connectDeviceAddrs = device.getAddress();

        Utils.getInstance().addStatusText(GlobalVars.activity.getString(R.string.client_searching));

        BluetoothSocket tmp = null;
        try {
            tmp = device.createRfcommSocketToServiceRecord(UUID.fromString(GlobalVars.UUID));
        } catch (IOException e) {
            e.printStackTrace();
            stopThread();
            throw new RuntimeException(GlobalVars.activity.getString(
                    R.string.error_connection_dropped) + " : " + e.getMessage());
        }
        _socket = tmp;
    }

    @Override
    public void run() {
        android.os.Process.setThreadPriority(Process.THREAD_PRIORITY_DEFAULT);

        if (_handler == null) {
            throw new RuntimeException("BluetoothClient : Handler == null");
        }

        try {
            GlobalVars.bluetoothAdapter.cancelDiscovery();
            _socket.connect();

            BluetoothDevice remoteDevice = _socket.getRemoteDevice();
            GlobalVars.connectDeviceName  = remoteDevice.getName();
            GlobalVars.connectDeviceAddrs = remoteDevice.getAddress();

            //Есть подключение
            final String strConnected = GlobalVars.activity.getString(
                    R.string.server_is_connected)+":\n" +
                    remoteDevice.getName() + "\n" + remoteDevice.getAddress();
            Utils.getInstance().addStatusText(strConnected);
        } catch (IOException e) {
            e.printStackTrace();
            stopThread();
            throw new RuntimeException(GlobalVars.activity.getString(
                    R.string.error_connection_dropped) + " : " + e.getMessage());
        }

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

        try {
            int availableBytes;
            int bytesRead;

            _isRunning = true;
            while (_isRunning) {

                availableBytes = _inStream.available();
                if (availableBytes > 0) {
                    byte[] buffer = new byte[availableBytes];
                    bytesRead = _inStream.read(buffer);
                    if (bytesRead > 0) {
                        _handler.sendMessage(_handler.obtainMessage(GlobalVars.SPEAKER_MSG_DATA, buffer));
                    }//if
                }//if

                if (_vector.size() > 0) {
                    byte[] buff = _vector.elementAt(0);
                    _outStream.write(buff, 0, buff.length);
                    _vector.removeElementAt(0);
                }
            }//while
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
