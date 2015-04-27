package ru.bitprofi.intercom;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;

/**
 * Created by Дмитрий on 22.04.2015.
 * Работа с микрофоном
 */
public class MicHelper implements Runnable {
    private boolean _isRunning = false;

    //Минимальные настройки оборудования должны поддерживаться "всеми" устройствами
    private static final int MIC_SAMPLERATE = 8000;
    private static final int MIC_CHANNELS   = AudioFormat.CHANNEL_IN_MONO;
    private static final int MIC_ENCODING   = AudioFormat.ENCODING_PCM_16BIT;

    /**
     * Остановка потока записи с микрофона
     */
    public void stop() {
        _isRunning = false;
    }

    @Override
    public void run() {
        _isRunning = true;

        final int bufferElements = 1024;
        final int bytesPerElement = 2;

        short buffer[] = new short[bufferElements];

        int buffSize = AudioRecord.getMinBufferSize(MIC_SAMPLERATE, MIC_CHANNELS, MIC_ENCODING);

        AudioRecord recorder = new AudioRecord(MediaRecorder.AudioSource.MIC,
                MIC_SAMPLERATE, MIC_CHANNELS,
                MIC_ENCODING, bufferElements * bytesPerElement);

        recorder.startRecording();
    }

    private void sendMsg(short[] data) {

    }
}
