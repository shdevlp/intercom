package ru.bitprofi.intercom;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

/**
 * Фоновая служба приложения
 * Created by Дмитрий on 05.05.2015.
 */
public class BackgroundService extends Service {
    private BluetoothClient _client = null;
    private BluetoothServer _server = null;
    private SpeakerHelper _speaker = null;
    private MicHelper _mic = null;

    private Handler _handler = null;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        _handler = new Handler() {
            public void handleMessage(android.os.Message msg) {
                byte[] data = (byte[])msg.obj;
                switch (msg.what) {
                    //Обработка данных от микрофона
                    case GlobalVars.MIC_MSG_DATA:
                        if (GlobalVars.isServer) {
                            if (_server != null) {
                                if (_server.isRunning()) {
                                    _server.addData(data);
                                }
                            }
                        } else {
                            if (_client != null) {
                                if (_client.isRunning()) {
                                    _client.addData(data);
                                }
                            }
                        }
                        break;

                    //Данные для проигрывания
                    case GlobalVars.SPEAKER_MSG_DATA:
                        if (_speaker != null) {
                            if (_speaker.isRunning()) {
                                _speaker.addData(data);
                            }
                        }
                        break;
                }
            }
        };

        if (GlobalVars.isServer) {
            if (_server == null) {
                _server = new BluetoothServer();
                _server.setHandler(_handler);
                _server.start();
            }
        } else {
            if (_client == null && GlobalVars.serverDevice != null) {
                _client = new BluetoothClient(GlobalVars.serverDevice);
                _client.setHandler(_handler);
                _client.start();
            }
        }

        if (_speaker == null) {
            _speaker = new SpeakerHelper();
            _speaker.start();
        }

        if (_mic == null) {
            _mic = new MicHelper();
            _mic.setHandler(_handler);
            _mic.start();
        }

        return START_NOT_STICKY;//Не перезапускать при аварии
    }

    @Override
    public void onDestroy() {
        try {
            if (_mic != null) {
                _mic.stopThread();
                _mic = null;
            }

            if (_speaker != null) {
                _speaker.stopThread();
                _speaker = null;
            }

            if (GlobalVars.isServer) {
                if (_server != null) {
                    _server.stopThread();
                    _server = null;
                }
            } else {
                if (_client != null) {
                    _client.stopThread();
                    _client = null;
                }
            }
        } catch (Exception e) {
            Log.e("Background:onDestroy", e.getMessage());
            Utils.getInstance().addStatusText(e.getMessage());
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}