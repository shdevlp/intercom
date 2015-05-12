package ru.bitprofi.intercom;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;

/**
 * Фоновая служба приложения
 * Created by Дмитрий on 05.05.2015.
 */
public class BackgroundService extends Service {
    private BluetoothClient _client;
    private BluetoothServer _server;
    private SpeakerHelper _speaker;
    private MicHelper _mic;

    private Handler _handler;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        _handler = new Handler() {
            public void handleMessage(android.os.Message msg) {
                byte[] data = (byte[])msg.obj;
                switch (msg.what) {
                    //Обработка данных от микрофона
                    case GlobalVars.MIC_MSG_DATA:
                        if (GlobalVars.isServer) {
                            if (_server != null && _server.isRunning()) {
                                _server.addData(data);
                            }
                        } else {
                            if (_client != null && _client.isRunning()) {
                                _client.addData(data);
                            }
                        }
                        break;

                    //Данные для проигрывания
                    case GlobalVars.SPEAKER_MSG_DATA:
                        if (_speaker != null && _speaker.isRunning()) {
                            _speaker.addData(data);
                        }
                        break;
                }
            }
        };

        if (GlobalVars.isServer) {
            stopServer();
            startServer();
        } else {
            stopClient();
            startClient();
        }

        stopMic();
        startMic();

        stopSpeaker();
        startSpeaker();

        Utils.getInstance().setMaxVolume();

        return START_NOT_STICKY;//Не перезапускать при аварии
    }

    @Override
    public void onDestroy() {
        Utils.getInstance().setNormalVolume();
        stopSpeaker();
        stopMic();

        if (GlobalVars.isServer) {
            stopServer();
        } else {
            stopClient();
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     *
     * @return
     */
    private boolean startMic() {
        if (_mic == null) {
            _mic = new MicHelper();
            _mic.setHandler(_handler);
            _mic.start();
            return true;
        }
        return false;
    }

    /**
     *
     * @return
     */
    private boolean stopMic() {
        if (_mic != null) {
            _mic.stopThread();
            _mic = null;
            return true;
        }
        return false;
    }

    /**
     *
     * @return
     */
    private boolean startSpeaker() {
        if (_speaker == null) {
            _speaker = new SpeakerHelper();
            _speaker.start();
            return true;
        }
        return false;
    }

    /**
     *
     * @return
     */
    private boolean stopSpeaker() {
        if (_speaker != null) {
            _speaker.stopThread();
            _speaker = null;
            return true;
        }
        return false;
    }

    /**
     *
     * @return
     */
    private boolean startServer() {
        if (_server == null) {
            _server = new BluetoothServer();
            _server.start();
            return true;
        }
        return false;
    }

    /**
     *
     * @return
     */
    private boolean stopServer() {
        if (_server != null) {
            _server.stopThread();
            _server = null;
            return true;
        }
        return false;
    }

    /**
     *
     * @return
     */
    private boolean startClient() {
        if (_client == null && GlobalVars.serverDevice != null) {
            _client = new BluetoothClient(GlobalVars.serverDevice);
            _client.start();
            return true;
        }
        return false;
    }

    /**
     *
     * @return
     */
    private boolean stopClient() {
        if (_client != null) {
            _client.stopThread();
            _client = null;
            return true;
        }
        return false;
    }
}