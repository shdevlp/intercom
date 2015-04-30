package ru.bitprofi.intercom;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;

/**
 * Запись и проигрывание одновременно на
 * локальном устройстве
 */
public class EchoPlayer extends CommonThreadObject {
    private AudioRecord _recorder = null;
    private AudioTrack  _player = null;

    /**
     * Инициализация
     */
    public EchoPlayer() {
        super(GlobalVars.PLAYER_MSG_DATA);
    }

    /**
     *
     */
    public void close() {
        super.close();

        if (null != _player) {
            _player.stop();
            _player.release();
            _player = null;
        }
        if (null != _recorder) {
            _recorder.stop();
            _recorder.release();
            _recorder = null;
        }
    }

    @Override
    public void run() {
        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);

        _isRunning = true;

        Utils.getInstance().setMinBufferSize();

        _recorder = new AudioRecord(MediaRecorder.AudioSource.MIC,
                GlobalVars.AUDIO_SAMPLERATE,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                GlobalVars.BYTES_PER_ELEMENT * GlobalVars.MIN_BUFFER_SIZE);

        _player = new AudioTrack(AudioManager.STREAM_VOICE_CALL,
                GlobalVars.AUDIO_SAMPLERATE,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                GlobalVars.BYTES_PER_ELEMENT * GlobalVars.MIN_BUFFER_SIZE,
                AudioTrack.MODE_STREAM);

        if (_player.getState() != AudioTrack.STATE_INITIALIZED) {
            throw new RuntimeException("AudioTrack.getState() != STATE_INITIALIZED");
        }
        if (_recorder.getState() != AudioRecord.STATE_INITIALIZED) {
            throw new RuntimeException("AudioRecord.getState() != STATE_INITIALIZED");
        }

        try {
            _recorder.startRecording();
            _player.play();
        } catch(IllegalStateException e) {
            throw new RuntimeException(e.getMessage());
        }
        _player.setPlaybackRate(GlobalVars.AUDIO_SAMPLERATE);

        short[] buffer = new short[GlobalVars.MIN_BUFFER_SIZE];
        while (_isRunning) {
            int samplesRead = _recorder.read(buffer, 0, buffer.length);
            Utils.getInstance().checkRead(samplesRead);
            _player.write(buffer, 0, buffer.length);
        }
    }
}
