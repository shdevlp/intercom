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
Utils.getInstance().addStatusText(">КЛИЕНТ СТАРТУЕТ");
        try {
            Utils.getInstance().addStatusText(GlobalVars.activity.getString(R.string.client_searching));
Utils.getInstance().addStatusText(">КЛИЕНТ УСТАНАВЛИВАЕТ СОЕДИНЕНИЕ С СЕРВЕРОМ");
Utils.getInstance().addStatusText(">ИМЯ СЕРВЕРА:"+device.getName());
Utils.getInstance().addStatusText(">АДРЕС СЕРВЕРА:"+device.getAddress());
Utils.getInstance().addStatusText(">UUID СЕРВЕРА:"+GlobalVars.connectDeviceUUID);
            _socket = device.createRfcommSocketToServiceRecord(UUID.fromString(GlobalVars.connectDeviceUUID));
        } catch (IOException e) {
Utils.getInstance().addStatusText(">ОШИБКА ПОДКЛЮЧЕНИЯ К СЕРВЕРУ:"+e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);
Utils.getInstance().addStatusText(">ПОТОК КЛИЕНТА - УСТАНОВКА ПРИОРИТЕТА");

        InputStream     tmpIn      = null;
        OutputStream    tmpOut     = null;
        DataInputStream inStream   = null;
        DataOutputStream outStream = null;

        int availableBytes;
        int bytesRead;

        boolean success = false;
        try {
Utils.getInstance().addStatusText(">ПОТОК КЛИЕНТА - ПОПЫТКА ПОДКЛЮЧЕНИЯ");
            _socket.connect();
Utils.getInstance().addStatusText(">ПОТОК КЛИЕНТА - КЛИЕНТ ПОДКЛЮЧИЛСЯ К СЕРВЕРУ");
            success = true;

            if (success) {
                _isRunning = true;
                //Успешно подлючились
                Utils.getInstance().addStatusText(GlobalVars.activity.getString(
                        R.string.client_is_connected));

Utils.getInstance().addStatusText(">ПОТОК КЛИЕНТА - КЛИЕНТ ПОДКЛЮЧИЛСЯ К СЕРВЕРУ");
                tmpIn     = _socket.getInputStream();
                tmpOut    = _socket.getOutputStream();
                inStream  = new DataInputStream(tmpIn);
                outStream = new DataOutputStream(tmpOut);

Utils.getInstance().addStatusText(">ПОТОК КЛИЕНТА - НАСТРОЙКА ВХОДНОГО И ВЫХОДНОГО ПОТОКА");

                getMinBufferSize();
                createRecorder();
                createPlayer();
                startWork();

                byte[] buffer = new byte[GlobalVars.BYTES_PER_ELEMENT * GlobalVars.MIN_BUFFER_SIZE];
Utils.getInstance().addStatusText(">ПОТОК КЛИЕНТА - БУФЕР ДЛЯ АУДИОДАННЫХ СОЗДАН:"+String.valueOf(buffer.length));
                while (_isRunning) {
                    //Чтение с микрофона
                    bytesRead = _recorder.read(buffer, 0, buffer.length);
Utils.getInstance().addStatusText(">ПОТОК КЛИЕНТА - ЧТЕНИЕ ДАННЫХ С МИКРОФОНА:"+String.valueOf(bytesRead));
                    if (bytesRead > 0) {
                        //Отправляем данные на сервер
Utils.getInstance().addStatusText(">ПОТОК КЛИЕНТА - ОТПРАВКА ДАННЫХ СЕРВЕРУ");
                        outStream.write(buffer, 0, buffer.length);
                    }

                    //Получаем данные с сервера(если они есть)
                    availableBytes = inStream.available();
Utils.getInstance().addStatusText(">ПОТОК КЛИЕНТА - ПОЛУЧАЕМ ДАННЫЕ ОТ СЕРВЕРА:"+String.valueOf(availableBytes));
                    if (availableBytes > 0) {
                        byte[] buffer2 = new byte[availableBytes];
                        //Читаем
                        bytesRead = inStream.read(buffer2);
Utils.getInstance().addStatusText(">ПОТОК КЛИЕНТА - ЧТЕНИЕ ДАННЫХ ОТ СЕРВЕРА:"+String.valueOf(bytesRead));
                        if (bytesRead > 0) {
                            //Воспроизводим
Utils.getInstance().addStatusText(">ПОТОК КЛИЕНТА - ВОСПРОИЗВОДИМ ПОЛУЧЕННЫЕ ДАННЫЕ СЕРВЕРА");
                            _player.write(buffer2, 0, buffer2.length);
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

Utils.getInstance().addStatusText(">ПОТОК КЛИЕНТА - ОСТАНОВКА КЛИЕНТА");

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
