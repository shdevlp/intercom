package ru.bitprofi.intercom;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.*;
import android.os.Process;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Дмитрий on 22.04.2015.
 * Работа с микрофоном
 */
public class MicHelper implements Runnable {
    private boolean _isRunning = false;
    private AudioRecord _recorder = null;
    private List<Handler> _handlers;

    /**
     * Конструктор
     */
    public MicHelper(){
        _handlers = new ArrayList<Handler>();
    }

    /**
     * Остановка потока записи с микрофона
     */
    public void stop() {
        if (null != _recorder) {
            _isRunning = false;
            _recorder.stop();
            _recorder.release();
            _recorder = null;
        }
    }

    /**
     *
     * @param handler
     */
    public void addHandler(Handler handler) {
        _handlers.add(handler);
    }

    @Override
    public void run() {
        //android.os.Process.setThreadPriority(Process.THREAD_PRIORITY_URGENT_AUDIO);

        _isRunning = true;

        int buffSize = AudioRecord.getMinBufferSize(GlobalVars.AUDIO_SAMPLERATE,
                AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);

        Utils.getInstance().checkGetMinBufferSize(buffSize);

        _recorder = new AudioRecord(MediaRecorder.AudioSource.MIC, GlobalVars.AUDIO_SAMPLERATE,
                AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT,
                GlobalVars.BUFFER_ELEMENTS * GlobalVars.BYTES_PER_ELEMENT);

        if (_recorder.getState() != AudioRecord.STATE_INITIALIZED) {
            throw new RuntimeException("AudioRecoder.getState() != STATE_INITIALIZED");
        }

        try {
            _recorder.startRecording();
        } catch(IllegalStateException e) {
            throw new RuntimeException(e.getMessage());
        }

        // Циклический буфер буферов. Чтобы не затереть данные,
        // пока главный поток их обрабатывает
        short[][] buffers = new short[GlobalVars.BUFFER_COUNT][GlobalVars.BUFFER_ELEMENTS];
        int count = 0;

        while (_isRunning) {
            int samplesRead = _recorder.read(buffers[count], 0, buffers[count].length);

            Utils.getInstance().checkRead(samplesRead);

            sendMsg(buffers[count]);
            count = (count + 1) % GlobalVars.BUFFER_COUNT;
        }
    }

    /**
     * Посылаем данные получателю
     * @param data
     */
    private void sendMsg(short[] data) {
        for(Handler handler : _handlers) {
            handler.sendMessage(handler.obtainMessage(GlobalVars.MIC_MSG_DATA, data));
        }
    }
}
