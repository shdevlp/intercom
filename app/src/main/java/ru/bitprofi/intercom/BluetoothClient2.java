package ru.bitprofi.intercom;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

/**
 * Created by Дмитрий on 07.05.2015.
 */
public class BluetoothClient2 extends CommonThread {
    private BluetoothSocket _socket = null;

    public BluetoothClient2(BluetoothDevice device) {
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
        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);

        InputStream     tmpIn      = null;
        OutputStream    tmpOut     = null;
        DataInputStream inStream   = null;
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
                Utils.getInstance().addStatusText(GlobalVars.activity.getString(
                        R.string.client_is_connected));

                tmpIn     = _socket.getInputStream();
                tmpOut    = _socket.getOutputStream();
                inStream  = new DataInputStream(tmpIn);
                outStream = new DataOutputStream(tmpOut);

                getMinBufferSize();
                createRecorder();
                createPlayer();
                startWork();

                byte[] buffer = new byte[GlobalVars.BYTES_PER_ELEMENT * GlobalVars.MIN_BUFFER_SIZE];
                while (_isRunning) {
                    //Чтение с микрофона
                    bytesRead = _recorder.read(buffer, 0, buffer.length);
                    if (bytesRead > 0) {
                        //Отправляем данные на сервер
                        outStream.write(buffer, 0, buffer.length);
                        //Получаем данные с сервера(если они есть)
                        availableBytes = inStream.available();
                        if (availableBytes > 0) {
                            byte[] buffer2 = new byte[availableBytes];
                            //Читаем
                            bytesRead = inStream.read(buffer2);
                            if (bytesRead > 0) {
                                //Воспроизводим
                                _player.write(buffer2, 0, buffer2.length);
                            }//if
                        }//if
                    }//if
                } //while
            } //succses socket connection
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

        freePlayer();
        freeRecorder();
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
