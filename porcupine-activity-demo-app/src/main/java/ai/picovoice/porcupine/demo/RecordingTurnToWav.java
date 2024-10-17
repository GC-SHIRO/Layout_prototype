package ai.picovoice.porcupine.demo;
/*
开始录音，保存的是.3gp文件
结束录音，释放资源
将录音转化为.wav文件
 */

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Handler;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.arthenica.mobileffmpeg.Config;
import com.arthenica.mobileffmpeg.FFmpeg;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;

public class RecordingTurnToWav extends AppCompatActivity {

    private static final int NOISE_THRESHOLD = 2000; // 定义无声音量阈值
    private static final int INITIAL_DELAY = 1000; // 初始延迟1秒
    private static final int SILENCE_DURATION = 3000; // 需要检测到无声3秒才停止录音
    private long silenceStartTime = -1; // 用于记录无声开始的时间
    private Handler handler = new Handler();
    private Runnable checkSilenceRunnable;
    private boolean isRecording = false;
    private final File filePath;
    private MediaRecorder recorder;
    private Context context;
    String TAG = "recording";


    //构造函数，传入录音保存路径，context
    public RecordingTurnToWav(File filePath, Context context) {
        this.context = context;
        this.filePath = filePath;
        if(filePath.exists()){
            Log.i(TAG, "file_could_not_found");
        }
    }

    public void startRecording() {
        CountDownLatch latch = new CountDownLatch(1);
        if (recorder == null) {
            Log.i(TAG, "recording_begin");


            recorder = new MediaRecorder();
            recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            recorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
            recorder.setOutputFile(filePath);


            try {
                recorder.prepare();
                recorder.start();
                isRecording = true;

                // 初始延迟后开始检测声音
                checkSilenceRunnable = new Runnable() {
                    @Override
                    public void run() {
                        if (recorder != null) {
                            int maxAmplitude = recorder.getMaxAmplitude();
                            Log.d(TAG, "Max amplitude: " + maxAmplitude);

                            if (maxAmplitude < NOISE_THRESHOLD) {
                                if (silenceStartTime == -1) {
                                    // 无声刚刚开始
                                    Log.d(TAG, "无声开始了");
                                    silenceStartTime = System.currentTimeMillis();
                                }

                                // 检查无声是否持续了足够长时间
                                if (System.currentTimeMillis() - silenceStartTime >= SILENCE_DURATION) {
                                    Log.d(TAG, "Silence detected for " + SILENCE_DURATION + " ms, stopping recording...");
                                    stopRecording();
                                    latch.countDown();
                                } else {
                                    // 如果无声持续时间不够，继续检测
                                    handler.postDelayed(this, 1000); // 1秒后再次检查
                                }
                            } else {
                                // 如果有声音，重置无声计时器
                                silenceStartTime = -1;
                                handler.postDelayed(this, 1000); // 1秒后再次检查
                            }
                        }
                    }
                };

                // 在初始延迟后开始检测
                handler.postDelayed(checkSilenceRunnable, INITIAL_DELAY);
                Log.i(TAG, "recording_start");

            } catch (IOException e) {
                Log.e(TAG, "prepare() failed");
            } catch (RuntimeException e) {
                Log.e(TAG, "start() failed");
            }

            //保证完成之后再进行下一步
            try {
                // 等待操作完成
                latch.await();
                Log.i(TAG, "recording_ok");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
    }

    public void stopRecording() {
        if (recorder != null && isRecording) {
            try {
                recorder.stop();
            } catch (RuntimeException stopException) {
                Log.e(TAG, "stop() failed");
            } finally {
                recorder.release();
                recorder = null;
                isRecording = false;
                handler.removeCallbacks(checkSilenceRunnable); // 停止检查
                silenceStartTime = -1; // 重置无声开始时间
                Log.d(TAG, "Recording stopped");
            }
        }
    }

    //传入.3gp文件，输出.wav文件
    public void toWav(File inputFilePath, File outputFilePath){
        String input = inputFilePath.getAbsolutePath();
        String out = outputFilePath.getAbsolutePath();

        CountDownLatch latch = new CountDownLatch(1);
        // 判断是否存在
        File check_out = new File(out);
        if (check_out.exists()) {
            Log.i(TAG, "toWav: ???");
            check_out.delete();
        }
        //开始转换
        String[] cmd = {"-y", "-i", input, "-acodec", "pcm_s16le", "-ar", "44100", out};
        FFmpeg.executeAsync(cmd, (executionId, returnCode) -> {
            if (returnCode == Config.RETURN_CODE_SUCCESS) {
                Log.e(TAG, "Conversion to WAV successed");
            } else {
                Log.e(TAG, "Conversion to WAV failed");
            }
            latch.countDown();
        });

        //保证转换完成之后再进行下一步
        try {
            // 等待 FFmpeg 操作完成
            latch.await();
            Log.i(TAG, "changed_ok");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}