/*
    Copyright 2021-2023 Picovoice Inc.

    You may not use this file except in compliance with the license. A copy of the license is
    located in the "LICENSE" file accompanying this source.

    Unless required by applicable law or agreed to in writing, software distributed under the
    License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
    express or implied. See the License for the specific language governing permissions and
    limitations under the License.
*/

//更改！两处即可
//
package ai.picovoice.porcupine.demo;

import static java.nio.file.Files.exists;
import static java.nio.file.Files.find;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.IOException;
import java.nio.file.ClosedFileSystemException;
import java.util.ArrayList;
import java.util.Objects;


import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.airbnb.lottie.LottieAnimationView;

import java.io.File;
import java.io.IOException;
import java.net.Socket;

import ai.picovoice.porcupine.demo.GoToGallary;
import ai.picovoice.porcupine.demo.ShowTime;
import ai.picovoice.porcupine.demo.RecordingTurnToWav;
import ai.picovoice.porcupine.demo.ConfigNetwork;
import ai.picovoice.porcupine.demo.SocketManager;

public class MainActivity extends AppCompatActivity {

    //初始化需要调用的类
    private final GoToGallary goToGallary = new GoToGallary(this);
    private ShowTime showTime;
    private RecordingTurnToWav recordingTurnToWav;
    private ConfigNetwork configNetwork;
    private SocketManager socketManager;
    private HotwordDetector hotwordDetector;
    private LottieAnimationVisualizer lottieAnimationVisualizer;
    private LottieAnimationView lottieAnimationView;
    //初始化需要的定值
    private static final int REQUEST_CODE = 101;
    private String ipAddress;
    private int port;
    private Socket socket;
    private final String TAG = "gogo";
    private MediaPlayer notificationPlayer;
    private Spinner spinner;
    private int isCapturing = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.layout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        getWindow().getDecorView().post(() -> hideSystemBars());//隐藏系统栏

        notificationPlayer = MediaPlayer.create(this, R.raw.notification);
        if (notificationPlayer == null) {
            Log.e(TAG, "Failed to initialize MediaPlayer");
        }

        //申请权限
        setAskPermission(this);
        Log.d(TAG, getCacheDir().toString());

        //socket连接
        socketManager = new SocketManager();
        new Thread(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "socket_start_run");
                try {
                    socketManager.connectSocket("10.151.9.47", 38438);
                    Log.d(TAG, "socket_is_ok");
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }).start();

        //设置监听热词
        hotwordDetector = new HotwordDetector(this, notificationPlayer, socketManager);
        configureKeywordSpinner();
        //开始热词监听
        new Thread(new Runnable() {
            @Override
            public void run() {
                process();
            }
        }).start();

        //设置lottieanimationVisualizer
        lottieAnimationVisualizer = new LottieAnimationVisualizer(this);
        lottieAnimationView = findViewById(R.id.lottieAnimation);
        lottieAnimationVisualizer.startAudioCapture(lottieAnimationView);

        //实时显示时间,showTime 在 onDestroy 之中释放掉
        TextView time_hour = findViewById(R.id.time_hour);
        TextView time_minute = findViewById(R.id.time_minute);
        TextView time_day = findViewById(R.id.time_day);
        TextView time_date = findViewById(R.id.time_date);
        showTime = new ShowTime(time_hour, time_minute, time_day, time_date);
        showTime.startUpdating();

        //textview渐入
        TextView textView = findViewById(R.id.ConversationView);
        textviewanimation(textView);


        //设置进入设置页面时的按钮
        Button Settingbutton = findViewById(R.id.setting_button);
        Settingbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent Settingintent = new Intent(MainActivity.this,SettingActivity.class);
                startActivity(Settingintent);
                overridePendingTransition(R.anim.zoom_in, R.anim.zoom_out);
            }
        });

        //设置进入播放器页面的按钮
        Button Musicplayerbutton = findViewById(R.id.Musicplayer_button);
        Musicplayerbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent musicintent = new Intent(MainActivity.this, player_MainActivity.class);
                startActivity(musicintent);
                overridePendingTransition(R.anim.zoom_in, R.anim.zoom_out);
            }
        });

        //设置一个按钮实现点开摄像头扫码配网
        configNetwork  = new ConfigNetwork(this,this);
        Button congigNetworkButton = findViewById(R.id.congigNetworkButton);
        congigNetworkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                configNetwork.startQRScanner();
            }
        });

        //点击打开相册
        Button openGalleryButton = findViewById(R.id.openGalleryButton);
        openGalleryButton.setOnClickListener(view -> goToGallary.openGallery());

        //监听到热词之后实现socket通讯传输
    }

    //设置Spinner，用来设置选择选项
    private void configureKeywordSpinner() {
        spinner = findViewById(R.id.keyword_spinner);
        ArrayList<String> spinnerItems = new ArrayList<>();

        try {
            for (String keyword : this.getAssets().list("keywords")) {
                spinnerItems.add(keyword.replace("_", " ").replace(".ppn", ""));
            }
        } catch (IOException ex) {
            Log.i(TAG, "configureKeywordSpinner: ");
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                R.layout.keyword_spinner_item,
                spinnerItems);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        //final ToggleButton recordButton = findViewById(R.id.record_button);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
//                if (recordButton.isChecked()) {
//
//                    recordButton.toggle();
//                }
//                Log.i(TAG,"关关关");
//                hotwordDetector.stopHotwordDetection();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // Do nothing.
            }
        });
    }

    public void process() {
        //ToggleButton recordButton = findViewById(R.id.record_button);
        try {
            //   if (recordButton.isChecked()) {
            // Log.i(TAG, "button_checked");
            if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.RECORD_AUDIO) ==
                    PackageManager.PERMISSION_GRANTED) {
                Log.i(TAG, "detetion0");
                hotwordDetector.startHotwordDetection(spinner);
                Log.i(TAG, "detetion1");
            } else {
                Log.i(TAG, "detetion2");
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.RECORD_AUDIO}, 0);
                Log.i(TAG, "detetion3");
            }

            // }else {
            //      hotwordDetector.stopHotwordDetection();
            //  }
        } catch (Exception e) {
            Log.e(TAG, "Error occurred during process: " + e.getMessage());
            e.printStackTrace();
        }
    }

//    private boolean hasRecordPermission() {
//        return ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) ==
//                PackageManager.PERMISSION_GRANTED;
//    }
//
//    private void requestRecordPermission() {
//        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, 0);
//    }
//
//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//            hotwordDetector.startHotwordDetection(spinner);
//        }
//    }

    //定义一个请求权限的函数,需要什么权限直接添加
    private void setAskPermission(Activity activity){
        Log.i(TAG, "askPermission: begin");
        //先检查是否有权限，如果没有再去申请

        if (ContextCompat.checkSelfPermission(activity, android.Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(activity, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_MEDIA_AUDIO) != PackageManager.PERMISSION_GRANTED
        ) {
            // 如果没有权限，请求权限
            ActivityCompat.requestPermissions(activity,
                    new String[]{android.Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_CODE);
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }
        Log.i(TAG, "permission_is_ok");
    }

    //隐藏bar
    private void hideSystemBars() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // API 30 及以上：使用 WindowInsetsController
            getWindow().setDecorFitsSystemWindows(false);  // 使内容可以延伸到系统栏区域
            View decorView = getWindow().getDecorView();
            WindowInsetsController insetsController = decorView.getWindowInsetsController();
            if (insetsController != null) {
                insetsController.hide(WindowInsets.Type.statusBars() | WindowInsets.Type.navigationBars());
                insetsController.setSystemBarsBehavior(WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
            }
        } else {
            // API 30 以下：使用 SYSTEM_UI_FLAG
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        }
    }
    private void textviewanimation(TextView textView){

        ScaleAnimation scaleAnimation = new ScaleAnimation(
                0.5f, 1.0f,  // X轴从0.5放大到1.0
                0.5f, 1.0f,  // Y轴从0.5放大到1.0
                Animation.RELATIVE_TO_SELF, 0.5f,  // 动画中心点 X（相对于自身的50%）
                Animation.RELATIVE_TO_SELF, 0.5f); // 动画中心点 Y（相对于自身的50%）
        scaleAnimation.setDuration(500);
        scaleAnimation.setFillAfter(true);
        // 启动动画
        textView.startAnimation(scaleAnimation);
    }
    //配置网络
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        configNetwork.handleActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        if(showTime != null){
            showTime.stopUpdating();
            Log.i(TAG, "have_been_destroy");
        }
        if(socketManager != null){
            try {
                socketManager.closeSocket();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
