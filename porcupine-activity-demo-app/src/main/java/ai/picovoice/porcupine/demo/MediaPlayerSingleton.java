package ai.picovoice.porcupine.demo;

import android.content.Context;
import android.media.MediaPlayer;

public class MediaPlayerSingleton {
    private static MediaPlayer instance;

    private MediaPlayerSingleton() {}

    public static synchronized MediaPlayer getInstance(Context context) {
        if (instance == null) {
            instance = new MediaPlayer();
        }
        return instance;
    }

    public static synchronized MediaPlayer getInstance(){
        return instance;
    }
}
