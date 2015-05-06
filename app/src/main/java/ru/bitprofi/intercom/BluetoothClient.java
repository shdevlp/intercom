package ru.bitprofi.intercom;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;
import android.widget.Toast;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

/**
 * Реализация клиента
 * Created by Дмитрий on 27.04.2015.
 */
public class BluetoothClient extends CommonThreadObject {
    private BluetoothSocket _socket = null;

    /**
     * Инициализация
     *
     * @param device
     */
    public BluetoothClient(BluetoothDevice device) {
        super(GlobalVars.SERVER_MSG_DATA);

        try {
            Utils.getInstance().setStatusText(GlobalVars.activity.getString(R.string.client_searching));
            _socket = device.createRfcommSocketToServiceRecord(UUID.fromString(GlobalVars.connectDeviceUUID));
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
        InputStream tmpIn = null;
        OutputStream tmpOut = null;
        DataInputStream inStream = null;
        DataOutputStream outStream = null;

        int availableBytes;
        int bytesRead;

        boolean success = false;
        try {
            _socket.connect();
            success = true;

            if (success) {
                _isRunning = true;
                //Успешно подлючились
                Utils.getInstance().setStatusText(GlobalVars.activity.getString(
                        R.string.client_is_connected));

                tmpIn = _socket.getInputStream();
                tmpOut = _socket.getOutputStream();

                inStream = new DataInputStream(tmpIn);
                outStream = new DataOutputStream(tmpOut);

                while (_isRunning) {
                    //Получаем данные с сервера(если они есть)
                    availableBytes = inStream.available();
                    if (availableBytes > 0) {
                        byte[] buffer = new byte[availableBytes];
                        bytesRead = inStream.read(buffer);
                        if (bytesRead > 0) {
                            sendMsg(buffer);
                        }
                    }

                    //Отправляем данные клиенту(если они есть)
                    //Отправляем серверу(если они есть)
                    if (getCount() > 0) {
                        byte[] buff = _vector.elementAt(0);
                        outStream.write(buff, 0, buff.length);
                        _vector.removeElementAt(0);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            Utils.getInstance().setStatusText(GlobalVars.activity.getString(
                    R.string.error_connection_dropped) + ":\n" + e.getMessage());
            try {
                _isRunning = false;
                _socket.close();
            } catch (IOException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
        }

        if (!success) {
            Utils.getInstance().setStatusText(GlobalVars.activity.getString(
                    R.string.error_connected));
        }
    }

    /**
     * Закрытие клиента
     */
    public void close() {
        super.close();
        try {
            if (_socket != null) {
                _socket.close();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        Utils.getInstance().setStatusText(GlobalVars.activity.getString(R.string.client_close));
    }


    /**
     * Отмена
     */
    public void cancel() {
        super.cancel();

        Toast.makeText(GlobalVars.activity.getApplicationContext(),
                GlobalVars.activity.getString(R.string.client_closing),
                Toast.LENGTH_LONG).show();

        try {
            _socket.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}