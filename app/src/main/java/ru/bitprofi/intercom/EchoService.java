package ru.bitprofi.intercom;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * Created by Дмитрий on 05.05.2015.
 */
public class EchoService extends Service {
    private EchoPlayer _echo = null; //Эхо

    @Override
    public void onCreate() {
        if (_echo == null) {
            _echo = new EchoPlayer();
            _echo.start();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        if (_echo != null) {
            _echo.close();
            while (_echo.isRunning()) {
                Utils.getInstance().sleep(5);
            }
            _echo = null;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
