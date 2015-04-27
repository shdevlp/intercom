package ru.bitprofi.intercom;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
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

/**
 * Реализация сервера
 */
public class BluetoothServer extends Thread {
    private BluetoothServerSocket _serverSocket = null;
    private BluetoothAdapter _ba = null;
    private Utils _utils = null;
    private volatile boolean _isEnable = false;

    public BluetoothServer(BluetoothAdapter ba) {
        _utils = new Utils();
        _ba = ba;
        try {
            String[] strs = GlobalVars.currentDeviceName.split("_");
            String name = strs[0];
            UUID uuid = UUID.fromString(strs[1]);

            _serverSocket = _ba.listenUsingRfcommWithServiceRecord(name, uuid);

            _utils.setStatusText(GlobalVars.activity.getString(R.string.server_wait_connection));
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * Закрытие сервера
     */
    public void close() {
        try {
            if (_serverSocket != null) {
                _isEnable = false;
                _serverSocket.accept(50);
                _serverSocket.close();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        _utils.setStatusText(GlobalVars.activity.getString(R.string.server_close));
    }

    /**
     * Запуск сервера
     */
    @Override
    public void run() {
        InputStream      tmpIn  = null;
        OutputStream     tmpOut = null;
        BluetoothSocket  socket = null;
        DataInputStream  inStream = null;
        DataOutputStream outStream = null;

        byte[] buffer = new byte[1024];
        int bytesRead;
        int availableBytes;

        if (_serverSocket != null){
            try {
                socket = _serverSocket.accept();
                _isEnable = true;
                BluetoothDevice remoteDevice = socket.getRemoteDevice();

                GlobalVars.connectDeviceName = remoteDevice.getName();
                GlobalVars.connectDeviceAddrs = remoteDevice.getAddress();

                final String strConnected = GlobalVars.activity.getString(
                        R.string.server_is_connected)+":\n" +
                        remoteDevice.getName() + "\n" + remoteDevice.getAddress();

                //Есть подключение
                _utils.setStatusText(strConnected);

                _serverSocket.close();

                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();

                inStream = new DataInputStream(tmpIn);
                outStream = new DataOutputStream(tmpOut);

                while (_isEnable) {
                    availableBytes = inStream.available();
                    if(availableBytes > 0) {
                        bytesRead = inStream.read(buffer);
                    }
                }

            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                _utils.setStatusText(GlobalVars.activity.getString(
                        R.string.something_went_wrong) + e.getMessage());
            }
        }else{
            _isEnable = false;
            _utils.setStatusText(GlobalVars.activity.getString(
                    R.string.server_close));
        }
    }

    /**
     * Отмена
     */
    public void cancel() {
        Toast.makeText(GlobalVars.context.getApplicationContext(),
                GlobalVars.activity.getString(R.string.server_closing),
                Toast.LENGTH_LONG).show();

        try {
            _isEnable = false;
            _serverSocket.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
