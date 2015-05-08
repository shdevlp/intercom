package ru.bitprofi.intercom;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
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
public class BluetoothServer extends CommonThread {
    private BluetoothServerSocket _serverSocket = null;
    private BluetoothAdapter _ba = null;

    public BluetoothServer(BluetoothAdapter ba) {
        super();

        _ba = ba;
        try {
Utils.getInstance().addStatusText(">CЕРВЕР СТАРТУЕТ");
            String[] strs = GlobalVars.currentDeviceName.split("_");
            String name = strs[0];
            UUID uuid = UUID.fromString(strs[1]);
Utils.getInstance().addStatusText(">ИМЯ СЕРВЕРА:"+name);
Utils.getInstance().addStatusText(">UUID СЕРВЕРА:"+strs[1]);
            _serverSocket = _ba.listenUsingRfcommWithServiceRecord(name, uuid);

            Utils.getInstance().addStatusText(GlobalVars.activity.getString(
                    R.string.server_wait_connection));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);
Utils.getInstance().addStatusText(">ПОТОК СЕРВЕРА - УСТАНОВКА ПРИОРИТЕТА");

        BluetoothSocket socket = null;

        InputStream      tmpIn     = null;
        OutputStream     tmpOut    = null;
        DataInputStream  inStream  = null;
        DataOutputStream outStream = null;

        int bytesRead;
        int availableBytes;

        if (_serverSocket == null) {
Utils.getInstance().addStatusText(">ПОТОК СЕРВЕРА - _serverSocket == null");
            stopThread();
            Utils.getInstance().addStatusText(GlobalVars.activity.getString(R.string.server_close));
            return;
        }

        try {
Utils.getInstance().addStatusText(">ПОТОК СЕРВЕРА - СЕРВЕР ЖДЕТ ПОДКЛЮЧЕНИЯ");
             socket = _serverSocket.accept();
Utils.getInstance().addStatusText(">ПОТОК СЕРВЕРА - СЕРВЕР ПОДКЛЮЧИЛ КЛИЕНТА");
             _isRunning = true;

             BluetoothDevice remoteDevice = socket.getRemoteDevice();
             GlobalVars.connectDeviceName  = remoteDevice.getName();
             GlobalVars.connectDeviceAddrs = remoteDevice.getAddress();

Utils.getInstance().addStatusText(">ПОТОК СЕРВЕРА - ИМЯ КЛИЕНТА:"+GlobalVars.connectDeviceName);
Utils.getInstance().addStatusText(">ПОТОК СЕРВЕРА - АДРЕС КЛИЕНТА:"+GlobalVars.connectDeviceAddrs);

             //Есть подключение
             final String strConnected = GlobalVars.activity.getString(
                      R.string.server_is_connected)+":\n" +
                      remoteDevice.getName() + "\n" + remoteDevice.getAddress();
             Utils.getInstance().addStatusText(strConnected);

             _serverSocket.close();

             tmpIn     = socket.getInputStream();
             tmpOut    = socket.getOutputStream();
             inStream  = new DataInputStream(tmpIn);
             outStream = new DataOutputStream(tmpOut);

Utils.getInstance().addStatusText(">ПОТОК СЕРВЕРА - НАСТРОЙКА ВХОДНОГО И ВЫХОДНОГО ПОТОКА");

             getMinBufferSize();
             createRecorder();
             createPlayer();
             startWork();

             //Буфер для аудиоданных
             byte[] buffer = new byte[GlobalVars.BYTES_PER_ELEMENT * GlobalVars.MIN_BUFFER_SIZE];
Utils.getInstance().addStatusText(">ПОТОК СЕРВЕРА - БУФЕР ДЛЯ АУДИОДАННЫХ СОЗДАН:"+String.valueOf(buffer.length));
             while (_isRunning) {
                 //Получаем данные от клиента(если они есть)
                 availableBytes = inStream.available();
Utils.getInstance().addStatusText(">ПОТОК СЕРВЕРА - ПОЛУЧАЕМ ДАННЫЕ ОТ КЛИЕНТА:"+String.valueOf(availableBytes));
                 if (availableBytes > 0) {
                    byte[] buffer2 = new byte[availableBytes];
                    //Читаем
                    bytesRead = inStream.read(buffer2);
Utils.getInstance().addStatusText(">ПОТОК СЕРВЕРА - ЧТЕНИЕ ДАННЫХ ОТ КЛИЕНТА:"+String.valueOf(bytesRead));
                    if (bytesRead > 0) {
                        //Воспроизводим
Utils.getInstance().addStatusText(">ПОТОК СЕРВЕРА - ВОСПРОИЗВОДИМ ПОЛУЧЕННЫЕ ДАННЫЕ КЛИЕНТА");
                        _player.write(buffer2, 0, buffer2.length);
                    }//if
                 }//if

                 //Чтение с микрофона
                 bytesRead = _recorder.read(buffer, 0, buffer.length);
Utils.getInstance().addStatusText(">ПОТОК СЕРВЕРА - ЧТЕНИЕ ДАННЫХ С МИКРОФОНА:"+String.valueOf(bytesRead));
                 if (bytesRead > 0) {
                     //Отправляем данные клиенту
Utils.getInstance().addStatusText(">ПОТОК СЕРВЕРА - ОТПРАВКА ДАННЫХ КЛИЕНТУ");
                     outStream.write(buffer, 0, buffer.length);
                 }
             }
        } catch (IOException e) {
            stopThread();
            Utils.getInstance().addStatusText(GlobalVars.activity.getString(
                    R.string.error_connection_dropped) + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Остановка сервера
     */
    public void stopThread() {
        super.stopThread();

Utils.getInstance().addStatusText(">ПОТОК СЕРВЕРА - ОСТАНОВКА СЕРВЕРА");

        freePlayer();
        freeRecorder();
        try {
            if (_serverSocket != null) {
                _serverSocket.accept(50);
                _serverSocket.close();
                _serverSocket = null;
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        Utils.getInstance().addStatusText(GlobalVars.activity.getString(R.string.server_close));
    }
}
