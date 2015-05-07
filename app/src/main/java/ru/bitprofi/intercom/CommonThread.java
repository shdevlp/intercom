package ru.bitprofi.intercom;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.util.Log;

/**
 * Created by Дмитрий on 07.05.2015.
 */
public class CommonThread extends Thread {
    protected boolean _isRunning = false;
    protected AudioRecord _recorder = null;
    protected AudioTrack _player = null;

    /**
     * Поток работает?
     */
    protected boolean isRunning() {
        return _isRunning;
    }

    /**
     * Остановка потока
     */
    protected void stopThread() {
        _isRunning = false;
    }

    /**
     * Установка размера буфера под аудиоданные
     */
    protected void getMinBufferSize() {
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
     * Создает экземпляр AudioRecord
     */
    protected void createRecorder() {
        _recorder = new AudioRecord(MediaRecorder.AudioSource.MIC,
                GlobalVars.AUDIO_SAMPLERATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT,
                GlobalVars.BYTES_PER_ELEMENT * GlobalVars.MIN_BUFFER_SIZE);

        if (_recorder.getState() != AudioRecord.STATE_INITIALIZED) {
            Log.e("ERROR", "AudioRecord.getState() != STATE_INITIALIZED");
            throw new RuntimeException("AudioRecord.getState() != STATE_INITIALIZED");
        }
    }

    /**
     * Создает экземпляр AudioTrack
     */
    protected void createPlayer() {
        _player = new AudioTrack(AudioManager.STREAM_VOICE_CALL,
                GlobalVars.AUDIO_SAMPLERATE, AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                GlobalVars.BYTES_PER_ELEMENT * GlobalVars.MIN_BUFFER_SIZE, AudioTrack.MODE_STREAM);

        if (_player.getState() != AudioTrack.STATE_INITIALIZED) {
            Log.e("ERROR", "AudioTrack.getState() != STATE_INITIALIZED");
            throw new RuntimeException("AudioTrack.getState() != STATE_INITIALIZED");
        }
    }

    /**
     * Запись и воспроизведение - Старт!
     */
    protected void startWork() {
        try {
            if (_recorder != null) {
                _recorder.startRecording();
            }
            if (_player != null) {
                //Установка частоты
                _player.setPlaybackRate(GlobalVars.AUDIO_SAMPLERATE);
                _player.play();
            }
        } catch (IllegalStateException e) {
            Log.e("ERROR", "startWork()" + e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }

    /**
     * Освободить AudioRecord
     */
    protected void freeRecorder() {
        if (_recorder != null) {
            _recorder.release();
            _recorder = null;
        }
    }

    /**
     * Освободить AudioTrack
     */
    protected void freePlayer() {
        if (_player != null) {
            _player.release();
            _player = null;
        }
    }
}
