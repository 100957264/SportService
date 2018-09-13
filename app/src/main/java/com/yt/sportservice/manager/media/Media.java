package com.yt.sportservice.manager.media;

import android.media.AudioManager;
import android.media.MediaPlayer;

import java.io.IOException;

/**
 * Created by B415 on 2018/3/5.
 */

public class Media {

    public MediaPlayer mediaPlayer = new MediaPlayer();

    public static String name;

    public static int id;

    public void load() throws IOException {
//        mediaPlayer = MediaPlayer.create(context, R.raw.e01001);
        mediaPlayer.setDataSource(name);
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

        mediaPlayer.prepare();
        mediaPlayer.start();
    }

    public void reload() {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
            mediaPlayer.release();
        }
        try {
            load();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
