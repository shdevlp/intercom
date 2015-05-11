package ru.bitprofi.intercom;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Handler;
import android.os.Process;
import android.util.Log;

/**
 * Created by Дмитрий on 22.04.2015.
 * Работа с микрофоном
 */
public class MicHelper extends CommonThread {
    private AudioRecord _recorder = null;
    private Handler _handler = null;

    /**
     * Инициализация
     */
    public MicHelper() {
        super();
    }

    /**
     *
     * @param handler
     */
    public void setHandler(Handler handler) {
        _handler = handler;
    }

    /**
     * Остановка потока записи с микрофона
     */
    public void stopThread() {
        super.stopThread();
        if (null != _recorder) {
            _recorder.stop();
            _recorder.release();
            _recorder = null;
        }
    }

    @Override
    public void run() {
        if (_handler == null) {
            throw new RuntimeException("MicHelper: handler = null");
        }

        android.os.Process.setThreadPriority(Process.THREAD_PRIORITY_URGENT_AUDIO);

        _isRunning = true;

        Utils.getInstance().setMinBufferSize();

        _recorder = new AudioRecord(MediaRecorder.AudioSource.MIC, GlobalVars.AUDIO_SAMPLERATE,
                AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT,
                GlobalVars.BYTES_PER_ELEMENT * GlobalVars.MIN_BUFFER_SIZE);

        final int state = _recorder.getState();
        if (state != AudioRecord.STATE_INITIALIZED) {
            Log.e("MicHelper.run", "AudioRecord.getState() != STATE_INITIALIZED");
            throw new RuntimeException("AudioRecord.getState() != STATE_INITIALIZED");
        }

        try {
            _recorder.startRecording();
        } catch(IllegalStateException e) {
            Log.e("MicHelper.run()", "startRecording:" + e.getMessage());
            throw new RuntimeException(e.getMessage());
        }

        // Циклический буфер буферов. Чтобы не затереть данные,
        // пока главный поток их обрабатывает
        byte[][] buffers = new byte[GlobalVars.BUFFER_COUNT][GlobalVars.BYTES_PER_ELEMENT * GlobalVars.MIN_BUFFER_SIZE];
        int count = 0;

        while (_isRunning) {
            if ( _recorder.read(buffers[count], 0, buffers[count].length) > 0) {
                _handler.sendMessage(_handler.obtainMessage(GlobalVars.MIC_MSG_DATA, buffers[count]));
            }
            count = (count + 1) % GlobalVars.BUFFER_COUNT;
        }
    }
}
