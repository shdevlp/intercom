package ru.bitprofi.intercom;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.os.*;
import android.os.Process;

/**
 * Created by Дмитрий on 22.04.2015.
 */
public class SpeakerHelper extends CommonThreadObject {
    private AudioTrack _player = null;

    /**
     * Инициализация
     */
    public SpeakerHelper() {
        super(GlobalVars.SPEAKER_MSG_DATA);
    }

    /**
     * Остановка потока
     */
    public void close() {
        super.close();
        if (null != _player) {
            _player.stop();
            _player.release();
            _player = null;
        }
    }


    @Override
    public void run() {
        _isRunning = true;

        android.os.Process.setThreadPriority(Process.THREAD_PRIORITY_URGENT_AUDIO);
        Utils.getInstance().setMinBufferSize();

        _player = new AudioTrack(AudioManager.STREAM_VOICE_CALL,
                                 GlobalVars.AUDIO_SAMPLERATE,
                                 AudioFormat.CHANNEL_OUT_MONO,
                                 AudioFormat.ENCODING_PCM_16BIT,
                                 GlobalVars.BYTES_PER_ELEMENT * GlobalVars.MIN_BUFFER_SIZE,
                                 AudioTrack.MODE_STREAM);

        if (_player.getState() != AudioTrack.STATE_INITIALIZED) {
            throw new RuntimeException("AudioTrack.getState() != STATE_INITIALIZED");
        }

        _player.setPlaybackRate(GlobalVars.AUDIO_SAMPLERATE);
        _player.play();

        while (_isRunning) {
            if (_isSendEnabled == false && _sendBuffer != null) {
                _player.write(_sendBuffer, 0, _sendBuffer.length); //Проигрываем
                _sendBuffer = null;
                _isSendEnabled = true; //Разрешаем запись новых данных
            }
        }
    }
}
