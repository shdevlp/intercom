package ru.bitprofi.intercom;

import java.util.Vector;

import android.os.*;

/**
 * Created by Дмитрий on 07.05.2015.
 */
public class CommonThread extends Thread {
    protected boolean _isRunning;
    protected Vector<byte[]> _vector;
    protected Handler _handler;

    public CommonThread() {
        _isRunning = false;
        _vector = new Vector<byte[]>();
    }

    /**
     *
     * @param handler
     */
    protected void setHandler(Handler handler) {
        _handler = handler;
    }

    /**
     * Добавить данные
     */
    protected void addData(byte[] data) {
        _vector.add(data);
    }

    /**
     * Поток работает?
     */
    protected boolean isRunning() {
        return _isRunning;
    }

    /**
     * Остановка потока
     */
    protected void stopThread() {
        _isRunning = false;
        Utils.getInstance().setBtnOnOff(false);
    }
}
