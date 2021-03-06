package ru.bitprofi.intercom;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.util.Log;
import android.widget.TextView;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.UUID;

import at.markushi.ui.CircleButton;

/**
 * Created by Дмитрий on 24.04.2015.
 */
public class Utils {
    private static class LazyHolder {
        private static final Utils INSTANCE = new Utils();
    }

    /**
     * Реализация синглтона
     * @return
     */
    public static Utils getInstance() {
        return LazyHolder.INSTANCE;
    }


    /**
     * Диалог с одной кнопкой
     * @param title
     * @param message
     * @param okButtonText
     */
    public synchronized void dialog(int title, int message, int okButtonText) {
        AlertDialog.Builder builder1 = new AlertDialog.Builder(GlobalVars.activity);
        Context _context = GlobalVars.context;
        builder1.setTitle(_context.getString(title));
        builder1.setMessage(_context.getString(message));
        builder1.setCancelable(true);
        builder1.setNeutralButton(_context.getString(okButtonText),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert11 = builder1.create();
        alert11.show();
    }

    /**
     * Диалог с одной кнопкой
     * @param title
     * @param message
     * @param okButtonText
     */
    public synchronized void dialog(String title, String message, String okButtonText) {
        AlertDialog.Builder builder1 = new AlertDialog.Builder(GlobalVars.activity);
        builder1.setTitle(title);
        builder1.setMessage(message);
        builder1.setCancelable(true);
        builder1.setNeutralButton(okButtonText,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert11 = builder1.create();
        alert11.show();
    }

    /**
     * Диалог с двумя кнопками
     * @param title
     * @param message
     * @param okButtonText
     * @param cancelButtonText
     */
    public synchronized void dialog(String title, String message, String okButtonText, String cancelButtonText) {
        AlertDialog.Builder builder1 = new AlertDialog.Builder(GlobalVars.activity);
        Context _context = GlobalVars.context;
        builder1.setTitle(title);
        builder1.setMessage(message);
        builder1.setCancelable(true);
        builder1.setPositiveButton(okButtonText,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        builder1.setNegativeButton(cancelButtonText,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        AlertDialog alert11 = builder1.create();
        alert11.show();
    }

    /**
     * Установить цвет кнопки
     * @param color
     * @return
     */
    public synchronized boolean setBtnColor(final int color) {
        final CircleButton btn = (CircleButton)GlobalVars.activity.findViewById(R.id.btnGo);
        if (btn != null) {
            GlobalVars.activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    btn.setColor(color);
                }
            });
            return true;
        }
        return false;
    }

    /**
     * Включить/Выключить кнопку
     * @param flgEnabled
     * @return
     */
    public synchronized boolean setBtnEnabled(final boolean flgEnabled) {
        final CircleButton btn = (CircleButton)GlobalVars.activity.findViewById(R.id.btnGo);
        if (btn != null) {
            GlobalVars.activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    btn.setEnabled(flgEnabled);
                }
            });
            return true;
        }
        return false;
    }

    /**
     * Видимость кнопки
     * @param visible
     * @return
     */
    public synchronized boolean setBtnVisible(final int visible) {
        final CircleButton btn = (CircleButton)GlobalVars.activity.findViewById(R.id.btnGo);
        if (btn != null) {
            GlobalVars.activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    btn.setVisibility(visible);
                }
            });
            return true;
        }
        return false;
    }

    /**
     * Ждет и показывает анимацию пока не закончится поиск устройств
     */
    public void waitScreenBTDiscovery() {
        GlobalVars.activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                final ProgressDialog dialog = ProgressDialog.show(GlobalVars.contextFragment,
                        GlobalVars.contextFragment.getString(R.string.wait),
                        GlobalVars.contextFragment.getString(R.string.bt_find_devices), true);
                Thread th = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        while (true) {
                            if (GlobalVars.isBluetoothDiscoveryFinished) {
                                dialog.dismiss();
                                break;
                            }
                        }
                    }
                });
                th.start();
            }
        });
    }

    public void waitScreenServerSocketClose(final BluetoothServerSocket serverSocket) {
        GlobalVars.activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                final ProgressDialog dialog = ProgressDialog.show(GlobalVars.contextFragment,
                        GlobalVars.contextFragment.getString(R.string.wait),
                        GlobalVars.contextFragment.getString(R.string.server_closing), true);
                Thread th = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        while (true) {
                            if (serverSocket != null) {
                                dialog.dismiss();
                                break;
                            }
                        }
                    }
                });
                th.start();
            }
        });
    }

    /**
     * Текущая дата и время
     * @return
     */
    public synchronized String currentDateTime(String format) {
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        return sdf.format(new Date());
    }

    /**
     * Добавить текст в статус
     * @param text
     * @return
     */
    public synchronized boolean addStatusText(final String text) {
        final TextView status = (TextView)GlobalVars.activity.findViewById(R.id.tvStatus);
        if (status != null) {
            GlobalVars.activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    String txt = status.getText().toString();
                    txt = currentDateTime("HH:mm:ss") +" " + text + "\n" + txt;
                    status.setText(txt);
                }
            });
            return true;
        }
        return false;
    }

    /**
     * Установить строку статуса
     * @param text
     * @return
     */
    public synchronized boolean setStatusText(final String text) {
        final TextView status = (TextView)GlobalVars.activity.findViewById(R.id.tvStatus);
        if (status != null) {
            GlobalVars.activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    status.setText(text);
                }
            });
            return true;
        }
        return false;
    }

    /**
      * Задержка основного потока
      * @param delay
     */
    public synchronized void sleep(int delay) {
        try {
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Преобразование звуковых данных для записи в файл
     * @param buffer
     * @return
     */
    public synchronized byte[] short2byte(short[] buffer) {
        int size = buffer.length;
        byte[] bytes = new byte[size * 2];
        for (int i = 0; i < size; i++) {
            bytes[i * 2] = (byte) (buffer[i] & 0x00FF);
            bytes[(i * 2) + 1] = (byte) (buffer[i] >> 8);
            buffer[i] = 0;
        }
        return bytes;
    }

    /**
     *
     * @param filePath - "/sdcard/voice8K16bitmono.pcm"
     * @param buffer
     */
    public synchronized void writeAudioDataToFile(String filePath, short[] buffer) {
        FileOutputStream os = null;
        try {
            os = new FileOutputStream(filePath);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        try {
            final int bufferSize = buffer.length * 2;
            byte bufferData[] = short2byte(buffer);
            os.write(bufferData, 0, bufferSize);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            os.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Нормальная громкость
     */
    public synchronized void setNormalVolume() {
        final AudioManager audioManager = (AudioManager) GlobalVars.context
                .getSystemService(GlobalVars.context.AUDIO_SERVICE);

        audioManager.setSpeakerphoneOn(GlobalVars.isSpeakerPhoneOn);
        audioManager.setMode(GlobalVars.oldAudioMode);
        audioManager.setRingerMode(GlobalVars.oldRingerMode);
    }

    /**
     * Максимальная громкость
     */
    public synchronized void setMaxVolume() {
        final AudioManager audioManager = (AudioManager) GlobalVars.context
                .getSystemService(GlobalVars.context.AUDIO_SERVICE);

        GlobalVars.oldAudioMode = audioManager.getMode();
        GlobalVars.oldRingerMode = audioManager.getRingerMode();
        GlobalVars.isSpeakerPhoneOn = audioManager.isSpeakerphoneOn();

        audioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);
        audioManager.setMode(AudioManager.MODE_NORMAL);
        audioManager.setSpeakerphoneOn(true);
    }

    /**
     * Установка размера буфера под аудиоданные
     */
    public synchronized void setMinBufferSize() {
        if (GlobalVars.MIN_BUFFER_SIZE <= 0) {
            GlobalVars.MIN_BUFFER_SIZE = AudioRecord.getMinBufferSize(GlobalVars.AUDIO_SAMPLERATE,
                    AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
            if (GlobalVars.MIN_BUFFER_SIZE == AudioRecord.ERROR) {
                Log.e("ERROR", "AudioRecord.getMinBufferSize() = ERROR");
                throw new RuntimeException("AudioRecord.getMinBufferSize() = ERROR");
            }
            if (GlobalVars.MIN_BUFFER_SIZE == AudioRecord.ERROR_BAD_VALUE) {
                Log.e("ERROR", "AudioRecord.getMinBufferSize() = ERROR_BAD_VALUE");
                throw new RuntimeException("AudioRecord.getMinBufferSize() = ERROR_BAD_VALUE");
            }
        }
    }

    /**
     * Добавить информацию о подключившемся устройстве на экран
     * @param socket
     */
    public synchronized void addInfoAboutDevice(BluetoothSocket socket, boolean server) {
        BluetoothDevice remoteDevice = socket.getRemoteDevice();
        GlobalVars.connectDeviceName  = remoteDevice.getName();
        GlobalVars.connectDeviceAddrs = remoteDevice.getAddress();

        //Есть подключение
        final String strConnected = GlobalVars.activity.getString(
                (server == true) ? R.string.server_is_connected : R.string.client_is_connected)+":\n" +
                remoteDevice.getName() + "\n" + remoteDevice.getAddress();
        Utils.getInstance().addStatusText(strConnected);
    }


    /**
     * Запущена ли служба
     * @param serviceClass
     * @return
     */
    public synchronized boolean isServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) GlobalVars.context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    public synchronized void setBtnOnOff(boolean on) {
        if (on) {
            setBtnColor(GlobalVars.activity.getResources().getColor(R.color.crimson));
            GlobalVars.buttonState = GlobalVars.BUTTON_IS_ON;
        } else {
            setBtnColor(GlobalVars.activity.getResources().getColor(R.color.seagreen));
            GlobalVars.buttonState = GlobalVars.BUTTON_IS_OFF;
        }
    }
}