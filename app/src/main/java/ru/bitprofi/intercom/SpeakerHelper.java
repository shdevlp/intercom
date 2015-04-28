package ru.bitprofi.intercom;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;

/**
 * Created by Дмитрий on 22.04.2015.
 */
public class SpeakerHelper implements Runnable {
    private AudioTrack _player = null;
    private boolean _isRunning = false;
    private short[][] _buffers = null;
    private int _count = 0;

    /**
     * Конструктор
     */
    public SpeakerHelper() {
        _buffers = new short[GlobalVars.BUFFER_COUNT][GlobalVars.BUFFER_ELEMENTS];
    }

    /**
     * Добавляем данные для воспроизведения
     * @param buffer
     */
    public void addData(short[] buffer) {
        if (buffer != null) {
            System.arraycopy(buffer, 0, _buffers[_count], 0, buffer.length);
            _count = (_count + 1) % GlobalVars.BUFFER_COUNT;
        }
    }

    /**
     * Остановка потока
     */
    public void stop() {
        if (null != _player) {
            _isRunning = false;
            _player.stop();
            _player.release();
            _player = null;
        }
    }

    @Override
    public void run() {
        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);

        _isRunning = true;

        int buffSize = AudioRecord.getMinBufferSize(GlobalVars.AUDIO_SAMPLERATE,
                AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT);

        Utils.getInstance().checkGetMinBufferSize(buffSize);

        _player = new AudioTrack(AudioManager.STREAM_MUSIC, GlobalVars.AUDIO_SAMPLERATE,
                AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT, buffSize, AudioTrack.MODE_STREAM);

        if (_player.getState() != AudioTrack.STATE_INITIALIZED) {
            throw new RuntimeException("AudioTrack.getState() != STATE_INITIALIZED");
        }

        _player.play();
        while (_isRunning) {
            for (int i = 0; i < GlobalVars.BUFFER_COUNT; i++) {
                if (_buffers[i] != null) {
                    _player.write(_buffers[i], 0, _buffers[i].length);
                    _buffers[i] = null;
                }
            }
        }
    }
}
