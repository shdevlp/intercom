package ru.bitprofi.intercom;

import android.os.Handler;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

/** Базовый класс для наследования с поддержкой потоков
 * Created by Дмитрий on 29.04.2015.
 */
public class CommonThreadObject extends Thread {
    protected List<Handler> _recievers;       //Получатели данных
    protected Vector<byte[]> _vector = null;

    protected boolean _isRunning = false;     //Флаг работы потока
    private int _msgType = -1;

    /**
     * Инициализация
     */
    public CommonThreadObject(int msgType) {
        _msgType = msgType;
        _recievers = new ArrayList<Handler>();
        _vector = new Vector<byte[]>();
    }

    /**
     * Добавляем получателей
     * @param handler
     */
    protected void addReciever(Handler handler) {
        _recievers.add(handler);
    }

    /**
     * Добавить данные
     * @param data
     */
    protected void addData(byte[] data) {
        _vector.add(data);
    }

    /**
     * Размер данных
     * @return
     */
    protected int getCount() {
        return _vector.size();
    }

    /**
     * Вернуть элемент
     * @param index
     * @return
     */
    protected byte[] getData(int index) {
        return _vector.elementAt(index);
    }

    /**
     * Работает ли поток
     */
    protected boolean isRunning() {
        return _isRunning;
    }

    /**
     * Закрытие потока
     */
    protected void close() {
        _isRunning = false;
        _vector.removeAllElements();
        _vector = null;
        _recievers.clear();
        _recievers = null;
    }

    /**
     * Посылаем данные получателю
     * @param data
     */
    protected void sendMsg(byte[] data) {
        if (_recievers == null || data == null) {
            return;
        }

        for (Handler handler: _recievers) {
            handler.sendMessage(handler.obtainMessage(_msgType, data));
        }
    }

    /**
     * Отмена
     */
    protected void cancel() {
        _isRunning = false;
    }
}