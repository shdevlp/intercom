package ru.bitprofi.intercom;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Process;
import android.util.Log;

/**
 * Created by Дмитрий on 22.04.2015.
 */
public class SpeakerHelper extends CommonThread {
    private AudioTrack _player = null;

    /**
     * Инициализация
     */
    public SpeakerHelper() {
        super();
    }

    /**
     * Остановка потока
     */
    public void stopThread () {
        super.stopThread();
        if (null != _player) {
            _player.flush();
            _player.stop();
            _player.release();
            _player = null;
        }
    }


    @Override
    public void run() {
        android.os.Process.setThreadPriority(Process.THREAD_PRIORITY_URGENT_AUDIO);

        _isRunning = true;

        Utils.getInstance().setMinBufferSize();

        _player = new AudioTrack(AudioManager.STREAM_VOICE_CALL, GlobalVars.AUDIO_SAMPLERATE,
                AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT,
                GlobalVars.BYTES_PER_ELEMENT * GlobalVars.MIN_BUFFER_SIZE, AudioTrack.MODE_STREAM);

        if (_player.getState() != AudioTrack.STATE_INITIALIZED) {
            Log.e("SpeakerHelper.run", "AudioTrack.getState() != STATE_INITIALIZED");
            throw new RuntimeException("AudioTrack.getState() != STATE_INITIALIZED");
        }

        _player.setPlaybackRate(GlobalVars.AUDIO_SAMPLERATE);
        _player.play();

        while (_isRunning) {
            if (_vector.size() > 0) {
                byte[] buff = _vector.elementAt(0);
                _player.write(buff, 0, buff.length);
                _vector.removeElementAt(0);
            }
        }
    }
}
