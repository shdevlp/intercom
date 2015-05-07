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
public class BluetoothServer2 extends CommonThread {
    private BluetoothServerSocket _serverSocket = null;
    private BluetoothAdapter _ba = null;

    public BluetoothServer2(BluetoothAdapter ba) {
        super();

        _ba = ba;
        try {
            String[] strs = GlobalVars.currentDeviceName.split("_");
            String name = strs[0];
            UUID uuid = UUID.fromString(strs[1]);

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

        BluetoothSocket socket = null;

        InputStream      tmpIn     = null;
        OutputStream     tmpOut    = null;
        DataInputStream  inStream  = null;
        DataOutputStream outStream = null;

        int bytesRead;
        int availableBytes;

        if (_serverSocket == null) {
            stopThread();
            Utils.getInstance().addStatusText(GlobalVars.activity.getString(R.string.server_close));
            return;
        }

        try {
             socket = _serverSocket.accept();
             _isRunning = true;

             BluetoothDevice remoteDevice = socket.getRemoteDevice();
             GlobalVars.connectDeviceName  = remoteDevice.getName();
             GlobalVars.connectDeviceAddrs = remoteDevice.getAddress();

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

             getMinBufferSize();
             createRecorder();
             createPlayer();
             startWork();

             //Буфер для аудиоданных
             byte[] buffer = new byte[GlobalVars.BYTES_PER_ELEMENT * GlobalVars.MIN_BUFFER_SIZE];
             while (_isRunning) {
                 //Чтение с микрофона
                 bytesRead = _recorder.read(buffer, 0, buffer.length);
                 if (bytesRead > 0) {
                     //Отправляем данные клиенту
                     outStream.write(buffer, 0, buffer.length);
                     //Получаем данные от клиента(если они есть)
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
             }
        } catch (IOException e) {
            stopThread();
            e.printStackTrace();
            Utils.getInstance().addStatusText(GlobalVars.activity.getString(
                    R.string.error_connection_dropped) + e.getMessage());
        }
    }

    /**
     * Остановка сервера
     */
    public void stopThread() {
        super.stopThread();

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
