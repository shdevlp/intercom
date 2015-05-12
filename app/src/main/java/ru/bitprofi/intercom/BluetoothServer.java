package ru.bitprofi.intercom;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.os.*;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;


/**
 * Created by Дмитрий on 07.05.2015.
 */
public class BluetoothServer extends CommonThread {
    private BluetoothSocket _socket = null;
    private DataInputStream _inStream = null;
    private DataOutputStream _outStream = null;
    private BluetoothServerSocket _serverSocket = null;

    public BluetoothServer() {
        super();
        Utils.getInstance().addStatusText(GlobalVars.activity.getString(
                R.string.server_wait_connection));
    }

    @Override
    public void run() {
        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_DEFAULT);
        try {
           if (GlobalVars.bluetoothAdapter == null) {
                throw new RuntimeException(GlobalVars.activity.getString(R.string.bt_not_available));
            }
            _serverSocket = GlobalVars.bluetoothAdapter.listenUsingRfcommWithServiceRecord(
                    GlobalVars.BLUETOOTH_NAME, UUID.fromString(GlobalVars.UUID));
        } catch (IOException e) {
            e.printStackTrace();
            Utils.getInstance().addStatusText(GlobalVars.activity.getString(R.string.server_close) + " : " + e.getMessage());
            stopThread();
            return;
        }

        int bytesRead;
        int availableBytes;

        try {
            _socket = _serverSocket.accept();
            _isRunning = true;

            BluetoothDevice remoteDevice = _socket.getRemoteDevice();
            GlobalVars.connectDeviceName  = remoteDevice.getName();
            GlobalVars.connectDeviceAddrs = remoteDevice.getAddress();

            //Есть подключение
            final String strConnected = GlobalVars.activity.getString(
                      R.string.server_is_connected)+":\n" +
                     remoteDevice.getName() + "\n" + remoteDevice.getAddress();
            Utils.getInstance().addStatusText(strConnected);

            _serverSocket.close();

            InputStream tmpInput = _socket.getInputStream();
            OutputStream tmpOutput = _socket.getOutputStream();

            _inStream = new DataInputStream(tmpInput);
            _outStream = new DataOutputStream(tmpOutput);

            android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);
            while (_isRunning) {
                availableBytes = _inStream.available();
                if (availableBytes > 0 && _inStream != null) {
                    byte[] buffer = new byte[availableBytes];
                    bytesRead = _inStream.read(buffer);
                    if (bytesRead > 0) {
                        _handler.sendMessage(_handler.obtainMessage(GlobalVars.SPEAKER_MSG_DATA, buffer));
                    }//if
                }//if

                if (_vector.size() > 0 && _outStream != null) {
                    byte[] buff = _vector.elementAt(0);
                    _outStream.write(buff, 0, buff.length);
                    _vector.removeElementAt(0);
                }
            }
        } catch (IOException e) {
            stopThread();
            Utils.getInstance().addStatusText(GlobalVars.activity.getString(
                    R.string.error_connection_dropped) + " : " + e.getMessage());
            e.printStackTrace();
        }//try
    }

    /**
     * Остановка сервера
     */
    public void stopThread() {
        super.stopThread();

        try {
            if (_serverSocket != null) {
                _serverSocket.accept(5);
            }
        } catch (IOException ex) {
            Utils.getInstance().addStatusText(ex.getMessage());
        }
        try {
            if (_serverSocket != null) {
                _serverSocket.close();
            }
        } catch (IOException e) {
                Utils.getInstance().addStatusText(e.getMessage());
        }
        _serverSocket = null;

        Utils.getInstance().addStatusText(GlobalVars.activity.getString(R.string.server_close));
    }
}
