package ru.bitprofi.intercom;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.widget.Toast;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

/**
 * Реализация сервера
 * Created by Дмитрий on 27.04.2015.
 */
public class BluetoothServer extends CommonThreadObject {
    private BluetoothServerSocket _serverSocket = null;
    private BluetoothAdapter _ba = null;

    /**
     * Инициализация
     * @param ba
     */
    public BluetoothServer(BluetoothAdapter ba) {
        super(GlobalVars.CLIENT_MSG_DATA);
         _ba = ba;
        try {
            String[] strs = GlobalVars.currentDeviceName.split("_");
            String name = strs[0];
            UUID uuid = UUID.fromString(strs[1]);

            _serverSocket = _ba.listenUsingRfcommWithServiceRecord(name, uuid);

            Utils.getInstance().setStatusText(GlobalVars.activity.getString(
                    R.string.server_wait_connection));
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * Закрытие сервера
     */
    public void close() {
        super.close();
        try {
            if (_serverSocket != null) {
                _serverSocket.accept(50);
                _serverSocket.close();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        Utils.getInstance().setStatusText(GlobalVars.activity.getString(R.string.server_close));
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

        int bytesRead;
        int availableBytes;

        if (_serverSocket != null){
            try {
                socket = _serverSocket.accept();
                _isRunning = true;
                BluetoothDevice remoteDevice = socket.getRemoteDevice();

                GlobalVars.connectDeviceName = remoteDevice.getName();
                GlobalVars.connectDeviceAddrs = remoteDevice.getAddress();

                final String strConnected = GlobalVars.activity.getString(
                        R.string.server_is_connected)+":\n" +
                        remoteDevice.getName() + "\n" + remoteDevice.getAddress();

                //Есть подключение
                Utils.getInstance().setStatusText(strConnected);

                _serverSocket.close();

                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();

                inStream = new DataInputStream(tmpIn);
                outStream = new DataOutputStream(tmpOut);

                while (_isRunning) {
                    //Получаем данные(если есть)
                    availableBytes = inStream.available();
                    if(availableBytes > 0) {
                        byte[] buffer = new byte[availableBytes];
                        bytesRead = inStream.read(buffer);
                        if (bytesRead > 0) {
                            sendMsg(buffer);
                        }
                    }

                    //Отправляем серверу(если они есть)
                    if (_isSendEnabled == false && _sendBuffer != null) {
                        outStream.write(_sendBuffer, 0, _sendBuffer.length);
                        _sendBuffer = null;
                        _isSendEnabled = true;
                    }
                }
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                Utils.getInstance().setStatusText(GlobalVars.activity.getString(
                        R.string.something_went_wrong) + e.getMessage());
            }
        }else{
            _isRunning = false;
            Utils.getInstance().setStatusText(GlobalVars.activity.getString(
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
            _isRunning = false;
            _serverSocket.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
