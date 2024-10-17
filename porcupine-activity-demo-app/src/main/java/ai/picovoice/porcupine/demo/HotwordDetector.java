package ai.picovoice.porcupine.demo;

import ai.picovoice.porcupine.demo.SocketManager;
import ai.picovoice.porcupine.demo.RecordingTurnToWav;

import android.app.Activity;
import android.content.Context;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Spinner;

import java.io.File;
import java.io.IOException;

import ai.picovoice.porcupine.*;

public class HotwordDetector {

    private static final String ACCESS_KEY = "DCMxFDdbdQm+e0hBNUB7fbfRKpYJJdK3y9LdRnINmf/3JQWTZ3JVdA==";
    private PorcupineManager porcupineManager;
    private Context context;
    private MediaPlayer notificationPlayer;
    private final PorcupineManagerCallback porcupineManagerCallback;

    private Handler handler = new Handler(Looper.getMainLooper());
    private boolean continueProcessing = true;  // 标志位控制循环

    private String TAG = "gogo";

    private RecordingTurnToWav recordingTurnToWav;
    private SocketManager socketManager;

    public HotwordDetector(Context context, MediaPlayer notificationPlayer, SocketManager socketManager) {
        this.socketManager = socketManager;
        this.context = context;
        this.notificationPlayer = notificationPlayer;
        this.porcupineManagerCallback = new PorcupineManagerCallback() {
            @Override
            public void invoke(int keywordIndex) {
                handleHotwordDetection();
                socketStuff();
            }
        };
    }

    private void handleHotwordDetection() {
        // Run on UI thread to interact with UI elements
        if (context instanceof Activity) {
            ((Activity) context).runOnUiThread(() -> {
                if (!notificationPlayer.isPlaying()) {
                    Log.i(TAG, "Hotword Detected");
                    notificationPlayer.start();
                }
            });
        }
    }

    private void socketStuff(){
        if (context instanceof Activity) {
            ((Activity) context).runOnUiThread(() -> {
                File file = new File(String.valueOf(context.getCacheDir()));
                File filePath_original = new File(file, "test_recording.3gp");
                File filePath_changed = new File(file,"test_recording.wav");

                recordingTurnToWav = new RecordingTurnToWav(filePath_original,context);

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try{
                            while(true){
                                recordingTurnToWav.startRecording();
                                Log.i(TAG, "run___________");
                                recordingTurnToWav.toWav(filePath_original,filePath_changed);
                                if(filePath_changed.length() > 200 * 1024){
                                    Log.i(TAG, "start_change");
                                    socketManager.sendFile(filePath_changed);
                                    Log.i(TAG, "send_is_ok");
                                    socketManager.receiveFile(file);
                                    Log.i(TAG, "receive_is_ok");
                                }else{
                                    break;
                                }

                                // 添加时间间隔
                                try {
                                    Thread.sleep(3000); //
                                    Log.i(TAG, "sleep_over");
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                        }catch(IOException e){
                            Log.e(TAG, String.valueOf(e));
                        }
                    }
                }).start();
            });
        }
    }

//    private void checkSoundAndContinue() {
//        handler.postDelayed(() -> {
//            if (detectSound()) {
//                Log.i(TAG, "Sound detected, continue socket operations...");
//                socketStuff();  // 检测到声音后，继续执行 socket 操作
//            } else {
//                Log.i(TAG, "No sound detected, returning to hotword detection...");
//                continueProcessing = false;  // 停止循环
//            }
//        }, 2000);  // 延时 2 秒后执行
//    }


    public void startHotwordDetection(Spinner spinner) {
        try {
            final Spinner mySpinner = spinner;
            final String keywordName = mySpinner.getSelectedItem().toString();
            //创建Porcupine对象，实例化
            PorcupineManager.Builder builder = new PorcupineManager.Builder()
                    .setAccessKey(ACCESS_KEY)
                    .setSensitivity(0.9f);
            Log.i(TAG, "builder_is_ok");

            Log.i(TAG, "check");
            String keywordFile = keywordName.toLowerCase().replace(" ", "_") + ".ppn";
            builder.setKeywordPath("keywords/" + keywordFile);
            String model = "porcupine_params_" + "zh"+ ".pv";
            builder.setModelPath("models/" + model);

            porcupineManager = builder.build(context.getApplicationContext(), porcupineManagerCallback);
            porcupineManager.start();
            Log.i(TAG, "start_to_get_hot_word");
        }  catch (PorcupineException e) {
            Log.e(TAG, "Failed to initialize Porcupine: " + e.getMessage());
        }
    }

    public void stopHotwordDetection() {
        if (porcupineManager != null) {
            try {
                porcupineManager.stop();
                porcupineManager.delete();
                Log.i(TAG, "Hotword detection stopped");
            } catch (PorcupineException e) {
                Log.e(TAG, "Failed to stop Porcupine: " + e.getMessage());
            }
        }
    }


}

