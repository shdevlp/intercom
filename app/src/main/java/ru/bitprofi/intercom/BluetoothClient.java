package ru.bitprofi.intercom;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.UUID;

/**
 * Created by Дмитрий on 07.05.2015.
 */
public class BluetoothClient extends CommonThread {
    private BluetoothSocket _socket = null;
    private BluetoothDevice _device = null;

    public BluetoothClient(BluetoothDevice device) {
        super();
        Utils.getInstance().addStatusText(GlobalVars.activity.getString(R.string.client_searching));
        _device = device;
    }

    @Override
    public void run() {
        InputStream tmpInput = null;
        OutputStream tmpOutput = null;
        DataInputStream inStream = null;
        DataOutputStream outStream = null;

        boolean success = false;
        try {
            _socket = _device.createRfcommSocketToServiceRecord(UUID.fromString(GlobalVars.connectDeviceUUID));
            if (_socket == null) {
                Method m = _device.getClass().getMethod("createRfcommSocket", new Class[]{int.class});
                _socket = (BluetoothSocket) m.invoke(_device, Integer.valueOf(1));
                _socket.connect();
            }

            if (_socket == null) {
                stopThread();
                throw new RuntimeException(GlobalVars.activity.getString(
                        R.string.error_connection_dropped));
            }

            success = true;

            tmpInput = _socket.getInputStream();
            tmpOutput = _socket.getOutputStream();

            inStream = new DataInputStream(tmpInput);
            outStream = new DataOutputStream(tmpOutput);
        } catch (IOException e) {
            e.printStackTrace();
            stopThread();
            Utils.getInstance().addStatusText(GlobalVars.activity.getString(
                    R.string.error_connection_dropped) + " : " + e.getMessage());
        } catch (NoSuchMethodException e1) {
            e1.printStackTrace();
            stopThread();
            Utils.getInstance().addStatusText(GlobalVars.activity.getString(
                    R.string.error_connection_dropped) + " : " + e1.getMessage());
        } catch (InvocationTargetException e2) {
            e2.printStackTrace();
            stopThread();
            Utils.getInstance().addStatusText(GlobalVars.activity.getString(
                    R.string.error_connection_dropped) + " : " + e2.getMessage());

        } catch (IllegalAccessException e3) {
            e3.printStackTrace();
            stopThread();
            Utils.getInstance().addStatusText(GlobalVars.activity.getString(
                    R.string.error_connection_dropped) + " : " + e3.getMessage());
        }

        if (!success) {
            stopThread();
            Utils.getInstance().addStatusText(GlobalVars.activity.getString(
                    R.string.error_connected));
        }

        try {
            int availableBytes;
            int bytesRead;

            _isRunning = true;
            //Успешно подлючились
            Utils.getInstance().addStatusText(GlobalVars.activity.getString(
                R.string.client_is_connected));

            while (_isRunning) {
                availableBytes = inStream.available();
                if (availableBytes > 0) {
                    byte[] buffer = new byte[availableBytes];
                    bytesRead = inStream.read(buffer);
                    if (bytesRead > 0) {
                        _handler.sendMessage(_handler.obtainMessage(GlobalVars.SPEAKER_MSG_DATA, buffer));
                    }//if
                }//if

                if (_vector.size() > 0) {
                    byte[] buff = _vector.elementAt(0);
                    outStream.write(buff, 0, buff.length);
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
                    R.string.error_connection_dropped) + " : " + ex.getMessage());
        }

        Utils.getInstance().addStatusText(GlobalVars.activity.getString(R.string.client_close));
    }
}
