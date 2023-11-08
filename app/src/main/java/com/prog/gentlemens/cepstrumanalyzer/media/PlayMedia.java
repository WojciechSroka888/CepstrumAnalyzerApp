package com.prog.gentlemens.cepstrumanalyzer.media;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.widget.Button;

import com.prog.gentlemens.cepstrumanalyzer.R;
import com.prog.gentlemens.cepstrumanalyzer.activity.ResultActivity;

import java.util.logging.Logger;

public class PlayMedia {
    private Logger logger = Logger.getLogger(PlayMedia.class.getName());
    private AudioTrack audioTrack;
    private ResultActivity resultActivity;

    public PlayMedia(ResultActivity resultActivity) {
        this.resultActivity = resultActivity;
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void startPlaying(final byte[] musicFile) {
        int minBufferSize = AudioTrack.getMinBufferSize(22050, AudioFormat.CHANNEL_CONFIGURATION_MONO, AudioFormat.ENCODING_PCM_16BIT);

        audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, 22050, AudioFormat.CHANNEL_CONFIGURATION_MONO, AudioFormat.ENCODING_PCM_16BIT, minBufferSize, AudioTrack.MODE_STREAM);
        audioTrack.play();


        Thread playingThread = new Thread(new Runnable() {
            @Override
            public void run() {
                audioTrack.write(musicFile, 0, musicFile.length);
            }
        }, "Audio Track Thread");
        playingThread.start();

        try {
            playingThread.join();
            ((Button) resultActivity.findViewById(R.id.play_button)).setText("play");
        } catch (InterruptedException e) {
            logger.warning(e.getMessage());
        }
    }

    public void stopPlaying() {
        if (audioTrack != null) {
            if (audioTrack.getPlayState() == AudioTrack.PLAYSTATE_PLAYING || //
                    audioTrack.getPlayState() == AudioTrack.PLAYSTATE_STOPPED) {
                audioTrack.pause();
            }
            audioTrack.flush();
            audioTrack.release();
            ((Button) resultActivity.findViewById(R.id.play_button)).setText("play");
        }
    }

    public boolean isPlaying() {
        return audioTrack == null ? false : audioTrack.getState() == AudioTrack.PLAYSTATE_PLAYING ? true : false;
    }

}
