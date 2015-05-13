package ru.bitprofi.intercom;

import android.bluetooth.BluetoothAdapter;
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
    private DataInputStream _inStream = null;
    private DataOutputStream _outStream = null;

    private BluetoothSocket _socket = null;
    private BluetoothServerSocket _serverSocket = null;

    /**
     * Конструктор
     */
    public BluetoothServer() {
        super();

        BluetoothAdapter ba = BluetoothAdapter.getDefaultAdapter();
        UUID uuid = UUID.fromString(GlobalVars.UUID);

        try {
            _serverSocket = ba.listenUsingRfcommWithServiceRecord(
                    GlobalVars.BLUETOOTH_SERVER, uuid);
        } catch (IOException e) {
            stopThread();
            Utils.getInstance().addStatusText(GlobalVars.activity.getString(
                    R.string.error_connection_dropped) + " : " + e.getMessage());
            e.printStackTrace();
        }

        Utils.getInstance().addStatusText(GlobalVars.activity.getString(
                R.string.server_wait_connection));
    }

    @Override
    public void run() {
        if (_handler == null) {
            throw new RuntimeException("BluetoothServer : Handler == null");
        }

        BluetoothSocket tmpSocket = null;
        while (true) {
            try {
                tmpSocket = _serverSocket.accept(50);
                _isRunning = true;
                break;
            } catch (IOException e) {
         //       stopThread();
                e.printStackTrace();
            }
            Utils.getInstance().sleep(50);
        }

        _socket = tmpSocket;

        Utils.getInstance().addInfoAboutDevice(_socket, true);

        try {
            _serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
            stopThread();
        }

        InputStream tmpInput;
        OutputStream tmpOutput;

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

        int bytesRead;
        int availableBytes;

        while (_isRunning)
            try {
                availableBytes = _inStream.available();
                if (availableBytes > 0) {
                    byte[] buffer = new byte[availableBytes];
                    bytesRead = _inStream.read(buffer);
                    if (bytesRead > 0) {
                        _handler.sendMessage(_handler.obtainMessage(
                                GlobalVars.SPEAKER_MSG_DATA, buffer));
                    }//if
                }//if

                if (_vector.size() > 0) {
                    byte[] buff = _vector.elementAt(0);
                    _outStream.write(buff, 0, buff.length);
                    _vector.removeElementAt(0);
                }
            } catch (IOException e) {
                e.printStackTrace();
                stopThread();
                throw new RuntimeException(GlobalVars.activity.getString(
                        R.string.something_went_wrong) + " : " + e.getMessage());
            }//try
    }

    /**
     * Остановка сервера
     */
    public void stopThread() {
        super.stopThread();

        //Utils.getInstance().waitScreenServerSocketClose(_serverSocket);

        try {
            if (_serverSocket != null) {
                _serverSocket.close();
            }
        } catch (IOException e) {
            Utils.getInstance().addStatusText(e.getMessage());
        }

        Utils.getInstance().addStatusText(GlobalVars.activity.getString(R.string.server_close));
    }
}
