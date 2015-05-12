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
            /*
             Method m = _device.getClass().getMethod("createRfcommSocket", new Class[]{int.class});
             _socket = (BluetoothSocket) m.invoke(_device, 1);
            */
        } catch (IOException e) {
            e.printStackTrace();
            stopThread();
            throw new RuntimeException(GlobalVars.activity.getString(
                    R.string.error_connection_dropped) + " : " + e.getMessage());
        }
        _socket = tmp;

        Thread connectionThread  = new Thread(new Runnable() {
            @Override
            public void run() {
                GlobalVars.bluetoothAdapter.cancelDiscovery();
                try {
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
                    try {
                        _socket.close();
                    } catch (IOException e2) {
                        e2.printStackTrace();
                    }
                }
            }
        });
        connectionThread.start();

        InputStream tmpInput = null;
        OutputStream tmpOutput = null;

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
    }

    @Override
    public void run() {
        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);

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
                }//if
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
