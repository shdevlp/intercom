package ru.bitprofi.intercom;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.widget.TextView;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.UUID;

/**
 * Created by Дмитрий on 24.04.2015.
 */
public class Utils {
    /**
     * Диалог с одной кнопкой
     * @param title
     * @param message
     * @param okButtonText
     */
    public void dialog(int title, int message, int okButtonText) {
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
    public void dialog(String title, String message, String okButtonText) {
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
    public void dialog(String title, String message, String okButtonText, String cancelButtonText) {
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
    public String getNewDeviceName() {
        return GlobalVars.prefixDeviceName + UUID.randomUUID().toString();
    }

    /**
     * Установить строку статуса
     * @param text
     * @return
     */
    public boolean setStatusText(final String text) {
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
    public void sleep(int delay) {
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
    public byte[] short2byte(short[] buffer) {
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
    public void writeAudioDataToFile(String filePath, short[] buffer) {
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
}