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

Utils.getInstance().addStatusText(">РАЗМЕР БУФЕРА ДЛЯ АУДИОДАННЫХ:" + String.valueOf(GlobalVars.MIN_BUFFER_SIZE));

            if (GlobalVars.MIN_BUFFER_SIZE == AudioRecord.ERROR) {
Utils.getInstance().addStatusText(">ОШИБКА ПОЛУЧЕНИЯ РАЗМЕРА БУФЕРА ДЛЯ АУДИОДАННЫХ" + "ERROR");
                Log.e("ERROR", "AudioRecord.getMinBufferSize() = ERROR");
                Log.e("ERROR", "AudioRecord.getMinBufferSize() = ERROR");
                throw new RuntimeException("AudioRecord.getMinBufferSize() = ERROR");
            }
            if (GlobalVars.MIN_BUFFER_SIZE == AudioRecord.ERROR_BAD_VALUE) {
Utils.getInstance().addStatusText(">ОШИБКА ПОЛУЧЕНИЯ РАЗМЕРА БУФЕРА ДЛЯ АУДИОДАННЫХ" + "ERROR_BAD_VALUE");
                Log.e("ERROR", "AudioRecord.getMinBufferSize() = ERROR_BAD_VALUE");
                throw new RuntimeException("AudioRecord.getMinBufferSize() = ERROR_BAD_VALUE");
            }
        }
    }

    /**
     * Создает экземпляр AudioRecord
     */
    protected void createRecorder() {
Utils.getInstance().addStatusText(">ДОСТУП К МИКРОФОНУ");
        _recorder = new AudioRecord(MediaRecorder.AudioSource.MIC,
                GlobalVars.AUDIO_SAMPLERATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT,
                GlobalVars.BYTES_PER_ELEMENT * GlobalVars.MIN_BUFFER_SIZE);

        if (_recorder.getState() != AudioRecord.STATE_INITIALIZED) {
Utils.getInstance().addStatusText(">ЗАПРЕЩЕН - ОШИБКА ИНИЦИАЛИЗАЦИИ");
            Log.e("ERROR", "AudioRecord.getState() != STATE_INITIALIZED");
            throw new RuntimeException("AudioRecord.getState() != STATE_INITIALIZED");
        }
Utils.getInstance().addStatusText(">РАЗРЕШЕН");
    }

    /**
     * Создает экземпляр AudioTrack
     */
    protected void createPlayer() {
Utils.getInstance().addStatusText(">ДОСТУП К ДИНАМИКУ");
        _player = new AudioTrack(AudioManager.STREAM_VOICE_CALL,
                GlobalVars.AUDIO_SAMPLERATE, AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                GlobalVars.BYTES_PER_ELEMENT * GlobalVars.MIN_BUFFER_SIZE, AudioTrack.MODE_STREAM);

        if (_player.getState() != AudioTrack.STATE_INITIALIZED) {
Utils.getInstance().addStatusText(">ЗАПРЕЩЕН - ОШИБКА ИНИЦИАЛИЗАЦИИ");
            Log.e("ERROR", "AudioTrack.getState() != STATE_INITIALIZED");
            throw new RuntimeException("AudioTrack.getState() != STATE_INITIALIZED");
        }
Utils.getInstance().addStatusText(">РАЗРЕШЕН");
    }

    /**
     * Запись и воспроизведение - Старт!
     */
    protected void startWork() {
        try {
            if (_recorder != null) {
Utils.getInstance().addStatusText(">МИКРОФОН - ПОШЛА ЗАПИСЬ ЗВУКА");
                _recorder.startRecording();
            }
            if (_player != null) {
                //Установка частоты
Utils.getInstance().addStatusText(">ДИНАМИК - УСТАНОВКА ЧАСТОТЫ ВЫСПРОИЗВЕДЕНИЯ ЗВУКА");
                _player.setPlaybackRate(GlobalVars.AUDIO_SAMPLERATE);
Utils.getInstance().addStatusText(">ДИНАМИК - НАЧАЛ ПРОИГРЫВАТЬ ЗВУК");
                _player.play();
            }
        } catch (IllegalStateException e) {
Utils.getInstance().addStatusText(">МИКРОФОН ИЛИ ДИНАМИК - ОШИБКА");
            Log.e("ERROR", "startWork()" + e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }

    /**
     * Освободить AudioRecord
     */
    protected void freeRecorder() {
        if (_recorder != null) {
Utils.getInstance().addStatusText(">МИКРОФОН ОСТАНОВКА");
            _recorder.release();
            _recorder = null;
        }
    }

    /**
     * Освободить AudioTrack
     */
    protected void freePlayer() {
        if (_player != null) {
Utils.getInstance().addStatusText(">ДИНАМИК ОСТАНОВКА");
            _player.release();
            _player = null;
        }
    }
}
