package ru.bitprofi.intercom;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.media.AudioFormat;
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
        return GlobalVars.PREFIX_DEVICE_NAME + UUID.randomUUID().toString();
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

    /**
     * Проверка возврата функции getMinBufferSize
     * @param buffSize
     */
    public void checkGetMinBufferSize(int buffSize) {
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
    public void checkRead(int read) {
        if (read == AudioRecord.ERROR_INVALID_OPERATION) {
            throw new RuntimeException("AudioRecord.read() = ERROR_INVALID_OPERATION");
        }
        if (read == AudioRecord.ERROR_BAD_VALUE) {
            throw new RuntimeException("AudioRecord.read() = ERROR_BAD_VALUE");
        }
    }

    /*

     int[] sampleRates = new int[] { 44100, 22050, 11025, 8000 };
    Encoding[] encodings = new Encoding[] { Encoding.Pcm8bit, Encoding.Pcm16bit };
    ChannelIn[] channelConfigs = new ChannelIn[]{ ChannelIn.Mono, ChannelIn.Stereo };

    //Not all of the formats are supported on each device
    foreach (int sampleRate in sampleRates)
    {
        foreach (Encoding encoding in encodings)
        {
            foreach (ChannelIn channelConfig in channelConfigs)
            {
                try
                {
                    Console.WriteLine("Attempting rate " + sampleRate + "Hz, bits: " + encoding + ", channel: " + channelConfig);
                    int bufferSize = AudioRecord.GetMinBufferSize(sampleRate, channelConfig, encoding);

                    if (bufferSize > 0)
                    {
                        // check if we can instantiate and have a success
                        AudioRecord recorder = new AudioRecord(AudioSource.Mic, sampleRate, channelConfig, encoding, bufferSize);

                        if (recorder.State == State.Initialized)
                        {
                            mBufferSize = bufferSize;
                            mSampleRate = sampleRate;
                            mChannelConfig = channelConfig;
                            mEncoding = encoding;
                            recorder.Release();
                            recorder = null;
                            return true;
                        }
                    }
                }
                catch (Exception ex)
                {
                    Console.WriteLine(sampleRate + "Exception, keep trying." + ex.Message);
                }
            }
        }
    }
     */

    /*
      private static int[] mSampleRates = new int[] { 8000, 11025, 22050, 44100 };

    private AudioRecord findAudioRecord() {
        for (int rate : mSampleRates) {
            for (short audioFormat : new short[] { AudioFormat.ENCODING_PCM_8BIT, AudioFormat.ENCODING_PCM_16BIT }) {
                for (short channelConfig : new short[] { AudioFormat.CHANNEL_IN_MONO, AudioFormat.CHANNEL_IN_STEREO }) {
                    try {
                        int bufferSize = AudioRecord.getMinBufferSize(rate, channelConfig, audioFormat);

                        if (bufferSize != AudioRecord.ERROR_BAD_VALUE) {
                            AudioRecord recorder = new AudioRecord(MediaRecorder.AudioSource.DEFAULT, rate, channelConfig, audioFormat, bufferSize);
                            if (recorder.getState() == AudioRecord.STATE_INITIALIZED)
                                return recorder;
                        }
                    } catch (Exception e) {
                    }
                }
            }
        }
        return null;
    }
    * */
}