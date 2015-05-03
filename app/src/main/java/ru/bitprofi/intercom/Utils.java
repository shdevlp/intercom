package ru.bitprofi.intercom;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.widget.TextView;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.UUID;

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
     * Возвращает новое имя устройство с известным префиксом
     * @return
     */
    public synchronized String getNewDeviceName() {
        return GlobalVars.PREFIX_DEVICE_NAME + UUID.randomUUID().toString();
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
     * Ждет установления нового статуса у Bluetooth
     * @param bluetooth
     * @param newState
     */
    public synchronized void waitBluetoothState(BluetoothHelper bluetooth, boolean newState) {
        while (bluetooth.isEnabled() != newState) {
            Utils.getInstance().sleep(5);
        }
        Utils.getInstance().sleep(100);
    }

    /**
     * Проверка возврата функции getMinBufferSize
     * @param buffSize
     */
    public synchronized void checkGetMinBufferSize(int buffSize) {
        if (buffSize == AudioRecord.ERROR) {
            throw new RuntimeException("AudioRecord.getMinBufferSize() = ERROR");
        }
        if (buffSize == AudioRecord.ERROR_BAD_VALUE) {
            throw new RuntimeException("AudioRecord.getMinBufferSize() = ERROR_BAD_VALUE");
        }
    }

    /**
     * Проверка возврата функции AudioRecoder.read
     * @param read
     */
    public synchronized void checkRead(int read) {
        if (read == AudioRecord.ERROR_INVALID_OPERATION) {
            throw new RuntimeException("AudioRecord.read() = ERROR_INVALID_OPERATION");
        }
        if (read == AudioRecord.ERROR_BAD_VALUE) {
            throw new RuntimeException("AudioRecord.read() = ERROR_BAD_VALUE");
        }
    }


    /**
     * Установка минимального размера буфера
     */
    public synchronized void setMinBufferSize() {
        if (GlobalVars.MIN_BUFFER_SIZE <= 0) {
            GlobalVars.MIN_BUFFER_SIZE = AudioRecord.getMinBufferSize(GlobalVars.AUDIO_SAMPLERATE,
                AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
            Utils.getInstance().checkGetMinBufferSize(GlobalVars.MIN_BUFFER_SIZE);
        }
    }
    /**
     * Максимальная громкость
     */
    public synchronized void setMaxVolume() {
        final AudioManager audioManager = (AudioManager) GlobalVars.context
                .getSystemService(GlobalVars.context.AUDIO_SERVICE);
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
                audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC), 0);
        audioManager.setSpeakerphoneOn(true);
    }

    /**
     * Минимальная громкость
     */
    public synchronized void setMinVolume() {
        final AudioManager audioManager = (AudioManager) GlobalVars.context
                .getSystemService(GlobalVars.context.AUDIO_SERVICE);
        final int originalVolume = audioManager
                .getStreamVolume(AudioManager.STREAM_MUSIC);
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, originalVolume, 0);
        audioManager.setSpeakerphoneOn(false);
    }
}