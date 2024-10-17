package ai.picovoice.porcupine.demo;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Handler;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import com.airbnb.lottie.LottieAnimationView;

public class LottieAnimationVisualizer {

    private Handler handler1 =  new Handler();

    private static final int REQUEST_PERMISSION = 1;
    private boolean isRecording = false;
    private AudioRecord audioRecord;
    private double scaledAmplitude;
    private Context context;

    private int sampleRate = 44100;
    int channelConfig = AudioFormat.CHANNEL_IN_MONO;
    int audioFormat = AudioFormat.ENCODING_PCM_16BIT;
    int bufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat);


    float progress = -1;
    float former_progress = -1;
    public LottieAnimationVisualizer(Context context) {
        this.context = context;
    }//初始化

    public void startAudioCapture(LottieAnimationView lottieAnimationView) {//开启声音捕获
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.RECORD_AUDIO}, REQUEST_PERMISSION);
        }
        progress = -1;
        former_progress = -1;
        audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, sampleRate, channelConfig, audioFormat, bufferSize);
        audioRecord.startRecording();
        isRecording = true;

        new Thread(() -> {
            short[] audioBuffer = new short[bufferSize];
            while (isRecording) {
                int readSize = audioRecord.read(audioBuffer, 0, bufferSize);
                double sum = 0;
                for (short sample : audioBuffer) {
                    sum += Math.abs(sample);
                }

                double amplitude = sum / readSize;
                scaledAmplitude = Math.min(amplitude / Short.MAX_VALUE, 1.0);
                handler1.post(() -> {
                    if(isRecording){
                        progress = (float) (scaledAmplitude * 5);
                        lottieAnimationView.setProgress(progress);
                    }
//                    progress = (float) (scaledAmplitude * 5);
//                    if(former_progress == -1){
//                        former_progress = progress;
//                    }
//
//                    if(isRecording){
//                        if(progress - former_progress >= 0){
//                            lottieAnimationView.setSpeed(1);
//                        }else{
//                            lottieAnimationView.setSpeed(-1);
//                        }
//                        //lottieAnimationView.setProgress(progress);
//                    }
//                    former_progress = progress;
                    float currentProgress = lottieAnimationView.getProgress();
                    //Log.d("LottieAnimation", "当前进度: " + currentProgress);
                    //lottieAnimationView.playAnimation();
                });
            }
        }).start();
    }
    public void stopAudioCapture(LottieAnimationView lottieAnimationView) {//停止音频捕获，清空缓存
        handler1.post(() ->{
            lottieAnimationView.setProgress(0);
            lottieAnimationView.pauseAnimation();
        });
        if (audioRecord != null) {

            isRecording = false;
            audioRecord.release();
            audioRecord = null;
        }
    }

}
