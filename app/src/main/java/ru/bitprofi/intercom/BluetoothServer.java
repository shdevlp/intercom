package ru.bitprofi.intercom;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;

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
    private SpeakerHelper _speaker = null;
    private MicHelper _mic = null;

    public BluetoothServer() {
        super();
        BluetoothHelper bluetooth = new BluetoothHelper();
        BluetoothAdapter ba = bluetooth.getAdapter();

        try {
            String[] strs = GlobalVars.currentDeviceName.split("_");
            String name = strs[0];
            UUID uuid = UUID.fromString(strs[1]);
            _serverSocket = ba.listenUsingRfcommWithServiceRecord(name, uuid);

            Utils.getInstance().addStatusText(GlobalVars.activity.getString(
                    R.string.server_wait_connection));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);

        if (_serverSocket == null) {
            stopThread();
            Utils.getInstance().addStatusText(GlobalVars.activity.getString(R.string.server_close));
            return;
        }

        int bytesRead;
        int availableBytes;

        try {
            BluetoothSocket socket = _serverSocket.accept();
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

            InputStream tmpInput = socket.getInputStream();
            OutputStream tmpOutput = socket.getOutputStream();
            DataInputStream inStream = new DataInputStream(tmpInput);
            DataOutputStream outStream = new DataOutputStream(tmpOutput);

            while (_isRunning) {
                availableBytes = inStream.available();
                if (availableBytes > 0) {
                    byte[] buffer = new byte[availableBytes];
                    bytesRead = inStream.read(buffer);
                    if (bytesRead > 0) {
                        _handler.sendMessage(_handler.obtainMessage(GlobalVars.SPEAKER_MSG_DATA, buffer));
                    }
                }

                if (_vector.size() > 0) {
                    byte[] buff = _vector.elementAt(0);
                    outStream.write(buff, 0, buff.length);
                    _vector.removeElementAt(0);
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
