package ru.bitprofi.intercom;

import android.os.Handler;
import android.widget.Toast;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/** Базовый класс для наследования с поддержкой потоков
 * Created by Дмитрий on 29.04.2015.
 */
public class CommonThreadObject extends Thread {
    protected List<Handler> _recievers;       //Получатели данных
    protected byte[] _sendBuffer = null;      //Данные для отправки

    protected boolean _isRunning = false;     //Флаг работы потока
    protected boolean _isSendEnabled = false; //Разрешение записывать данные для отправки

    private int _msgType = -1;
    /**
     * Инициализация
     */
    public CommonThreadObject(int msgType) {
        _msgType = msgType;
        _recievers = new ArrayList<Handler>();
    }

    /**
     * Добавляем получателей
     * @param handler
     */
    protected void addReciever(Handler handler) {
        _recievers.add(handler);
    }

    /**
     * Передаем данные для отправки
     * @param data
     */
    protected void setSendData(byte[] data){
        if (_isSendEnabled) {
            final int len = data.length;
            _sendBuffer = new byte[len];
            System.arraycopy(data, 0, _sendBuffer, 0, len);

            _isSendEnabled = false;
        }
    }

    /**
     * Закрытие потока
     */
    protected void close() {
        _isRunning = false;
        _isSendEnabled = false;
    }

    /**
     * Разрешена ли отправка
     * @return
     */
    protected boolean isSendEnabled() {
        return _isSendEnabled;
    }

    /**
     * Посылаем данные получателю
     * @param data
     */
    protected void sendMsg(byte[] data) {
        for(Handler handler: _recievers) {
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
