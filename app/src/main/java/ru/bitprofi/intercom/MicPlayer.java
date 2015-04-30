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
public class MicPlayer extends Thread {
    private boolean _isRunning = false;

    private AudioRecord _recorder = null;
    private AudioTrack  _player = null;

    public void close() {
        _isRunning = false;
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

        int buffSize = AudioRecord.getMinBufferSize(GlobalVars.AUDIO_SAMPLERATE,
                AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);

        Utils.getInstance().checkGetMinBufferSize(buffSize);

        _recorder = new AudioRecord(MediaRecorder.AudioSource.MIC,
                GlobalVars.AUDIO_SAMPLERATE,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                GlobalVars.BYTES_PER_ELEMENT * buffSize);

        _player = new AudioTrack(AudioManager.STREAM_VOICE_CALL,
                GlobalVars.AUDIO_SAMPLERATE,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                GlobalVars.BYTES_PER_ELEMENT * buffSize, AudioTrack.MODE_STREAM);

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
        //_player.setVolume(1.0f);

        short[] buffer = new short[buffSize];
        while (_isRunning) {
            int samplesRead = _recorder.read(buffer, 0, buffer.length);
            Utils.getInstance().checkRead(samplesRead);
            _player.write(buffer, 0, buffer.length);
        }
    }

}
