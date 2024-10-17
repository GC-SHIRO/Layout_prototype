package ai.picovoice.porcupine.demo;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class player_MainActivity extends AppCompatActivity implements View.OnClickListener {

    // 视图定义
    ImageView nextIv, playIv, lastIv, musicIcon;
    TextView singerTv, songTv;
    RecyclerView musicRv;
    SeekBar seekBar;
    View seekBarOverlay;
//    LyricManager mLyricManager;
//    TextView lyricTv ;


    // 数据源
    List<LocalMusicBean> metaDatas;
    private LocalMusicAdapter adapter;
//    private int position;
//    List<LyricInfo> mLyricInfos;
//    private int currentLyricIndex;

    //记录当前正在播放的音乐的位置
    public static int currentPlayPosition = -1;
    //记录暂停音乐时进度条的位置
    int currentPausePositionInSong = 0;
    //记录当前正在播放音乐的路径
    public static String currentMusicPath;
    public static String songName, artistName;
    Intent intent;

    MediaPlayer mediaPlayer;

    public static MyViewModel myViewModel;

    private Handler handler = new Handler();
    private Runnable updateSeekBar;

//    private static final int REQUEST_PERMISSION_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        Log.i("test", "onCreate: 111");
        setContentView(R.layout.player_activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Log.i("test", "onCreate: 222");

        Button backbutton = findViewById(R.id.go_back);
        backbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent gobackintent = new Intent(player_MainActivity.this, MainActivity.class);
                startActivity(gobackintent);
                overridePendingTransition(R.anim.zoom_in, R.anim.zoom_out);
            }
        });

        Log.i("test", "onCreate: 333");
//        getSupportFragmentManager().beginTransaction()
//                .replace(R.id.fragmentContainer, new MusicListFragment())
//                .commit();


        Log.i("test", "onCreate: 444");
        initView();

        mediaPlayer = MediaPlayerSingleton.getInstance(this);

        metaDatas = new ArrayList<>();

        // 创建适配器
        adapter = new LocalMusicAdapter(this, metaDatas);
        musicRv.setAdapter(adapter);

        //设置布局管理器
        LinearLayoutManager manager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        musicRv.setLayoutManager(manager);

        //加载本地数据源
        Log.i("test", "onCreate: 到这里没");
        loadLocalMusicData();

        myViewModel = new ViewModelProvider(this).get(MyViewModel.class);

        intent = new Intent(this, LyricActivity.class);

        // 设置每一项的点击事件
        setEventListener();
    }


    private void setEventListener() {
        // 设置每一项的点击事件
        adapter.setOnItemClickListener(new LocalMusicAdapter.OnItemClickListener() {
            @Override
            public void OnItemClick(View view, int position) {

                Log.i("test", "OnItemClick: clicking the icon button");

                currentPlayPosition = position;
                LocalMusicBean musicBean = metaDatas.get(position);

                updateMessage(musicBean);
//                currentMusicPath = musicBean.getPath();
//                songName = musicBean.getSong();
//                artistName = musicBean.getSinger();

                Log.i("test", "OnItemClick: 寄了没");

                playMusicInMusicBean(musicBean);

//                myViewModel.setMyVariable(currentMusicPath);

//                Log.i("test", "before enter");
//                loadLyricFile(musicBean);
//                Log.i("test", "after enter");

                // 播放完自动下一首
                mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        // test
                        Log.i("test", "the next song");

                        if (currentPlayPosition == metaDatas.size() - 1) {
                            currentPlayPosition = 0;
                        } else {
                            currentPlayPosition = currentPlayPosition + 1;
                        }
                        LocalMusicBean nextBean = metaDatas.get(currentPlayPosition);
                        playMusicInMusicBean(nextBean);
                        updateMessage(nextBean);
//                        currentMusicPath = nextBean.getPath();
//                        songName = nextBean.getSong();
//                        artistName = nextBean.getSinger();
//                        myViewModel = new ViewModelProvider().get(MyViewModel.class);
//                        Log.i("test", String.valueOf(myViewModel));
//                        new MyViewModel().setMyVariable(currentMusicPath);
//                        intent = new Intent(this, LyricActivity.class);
                        Log.i("test", "onCompletion: 到setvalue后面");
////                        intent = new Intent(this, LyricActivity.class);
//                        intent.putExtra("path", currentMusicPath);
                    }
                });
            }
        });
    }

    private void updateMessage(LocalMusicBean musicBean){
        currentMusicPath = musicBean.getPath();
        songName = musicBean.getSong();
        artistName = musicBean.getSinger();
    }


//    private void loadLyricFile(LocalMusicBean musicBean) {
////        Log.i("test", "I am here");
//
//        String musicPath = musicBean.getPath();
//        String songFileNameWithoutExtension = musicPath.substring(0, musicPath.lastIndexOf("."));
//
//        File lrcFile = new File(songFileNameWithoutExtension + ".lrc");
//
//        // test
//        Log.i("test", String.valueOf(lrcFile));
//
//        if (lrcFile.exists()) {
//            Log.i("test", lrcFile + "=============exists");
//            try {
//                mLyricInfos = parseLrcFile(lrcFile);
//                Handler handler = new Handler();
//                currentLyricIndex = 0;
////                Intent intent = new Intent(this, LyricActivity.class);
//                syncLyricsWithMusic(mediaPlayer, handler);
//            } catch (IOException e) {
//                throw new RuntimeException(e);
//            }
//        } else {
//            Log.i("test", String.valueOf(lrcFile) + "=============g");
//        }
//    }

//    private void syncLyricsWithMusic(MediaPlayer mediaPlayer, Handler handler) {
//        Runnable lyricsUpdater = new Runnable() {
//            @Override
//            public void run() {
//                if(mediaPlayer.isPlaying()){
//                    long currentPosition = mediaPlayer.getCurrentPosition();
//
//                    while(currentLyricIndex < mLyricInfos.size() - 1 && currentPosition >= mLyricInfos.get(currentLyricIndex + 1).time){
//                        currentLyricIndex++;
//                    }
//
//                    displayLyrics(mLyricInfos.get(currentLyricIndex).content);
////                    String lyricText = mLyricInfos.get(currentLyricIndex).content;
////                    if(lyricText != null) {
////                        intent.putExtra("lyric", lyricText);
////                    }
//                    handler.postDelayed(this, 500);
//
//                }
//            }
//        };
//
//        handler.post(lyricsUpdater);
//    }
//
//    private void displayLyrics(String content) {
////        lyricTv.setText(content);
//    }
//
//    private List<LyricInfo> parseLrcFile(File lyricsFile) throws IOException {
//        List<LyricInfo> LyricInfos = new ArrayList<>();
//        FileInputStream fis = new FileInputStream(lyricsFile);
//        InputStreamReader inputStreamReader = new InputStreamReader(fis, "GBK");
//        BufferedReader reader = new BufferedReader(inputStreamReader);
//        String line;
//        Pattern pattern = Pattern.compile("\\[(\\d{2}):(\\d{2})\\.(\\d{2})\\](.*)");
//
//        while ((line = reader.readLine()) != null) {
//            Matcher matcher = pattern.matcher(line);
//            if (matcher.matches()) {
//                long minutes = Long.parseLong(matcher.group(1));
//                long seconds = Long.parseLong(matcher.group(2));
//                long milliseconds = Long.parseLong(matcher.group(3)) * 10;  // 将小数部分乘以 10 得到毫秒
//                long time = (minutes * 60 + seconds) * 1000 + milliseconds;  // 转换为毫秒
//                String content = matcher.group(4).trim();
//                LyricInfos.add(new LyricInfo(time, content));
//            }
//        }
//
////        for(int i = 0; i < LyricInfos.size(); i++){
////            Log.i("lyric", LyricInfos.get(i).content);
////        }
//
//        reader.close();
//        return LyricInfos;
//    }

    private void playMusicInMusicBean(LocalMusicBean musicBean) {
//        test
//        Log.i("test", "++++++++++++++++++++++++++++++++++++++++++++");

//        根据传入对象播放音乐
        // 设置底部显示的歌手名称和歌曲名
        singerTv.setText(artistName);
        songTv.setText(songName);
        stopMusic();

        // 重置多媒体播放器
        mediaPlayer.reset();
        try {
            mediaPlayer.setDataSource(musicBean.getPath());
            playMusic();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void pauseMusic() {
        // 暂停音乐
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            currentPausePositionInSong = mediaPlayer.getCurrentPosition();
            mediaPlayer.pause();
            playIv.setImageResource(R.mipmap.icon_play);
        }
    }

    private void playMusic() {
        // 播放音乐
        if (mediaPlayer != null && !mediaPlayer.isPlaying()) {
            if (currentPausePositionInSong == 0) {
                try {
                    mediaPlayer.prepare();
                    mediaPlayer.start();
                    seekBar.setMax(mediaPlayer.getDuration());
                    updateSeekBar();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            } else {
                mediaPlayer.seekTo(currentPausePositionInSong);
                mediaPlayer.start();
            }

            playIv.setImageResource(R.mipmap.icon_pause);
        }
    }

    private void stopMusic() {
        // 停止音乐的函数
        if (mediaPlayer != null) {
            currentPausePositionInSong = 0;
            mediaPlayer.pause();
            mediaPlayer.seekTo(0);
            mediaPlayer.stop();
            playIv.setImageResource(R.mipmap.icon_play);
        }
    }

    private void loadLocalMusicData() {
        //加载本地音乐文件到集合当中
        // 1. 获取contentResolver对象
        ContentResolver resolver = getContentResolver();
        // 2. 获取本地音乐存储的uri地址
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;

        // test
        Log.i("test", String.valueOf(uri));

        // 3. 开始查询
        Cursor cursor = resolver.query(uri, null, null, null, null);
        // 4. 遍历cursor对象
        int id = 0;
        if (cursor != null) {
            while (cursor.moveToNext()) {
                String song = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE));
                String singer = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST));
                id++;
                String sid = String.valueOf(id);
                String path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));
                long duration = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION));
                SimpleDateFormat sdf = new SimpleDateFormat("mm:ss");
                String time = sdf.format(new Date(duration));
                if (time.equals("00:00")) {
                    id--;
                    continue;
                } else {
                    // 将一行的数据封装到对象当中
                    LocalMusicBean bean = new LocalMusicBean(sid, song, singer, time, path);
                    metaDatas.add(bean);

//                    // test
//                    Log.i("test", path);

                }
            }
            // 数据源发生更新，提示适配器更新
            adapter.notifyDataSetChanged();
        }
        assert cursor != null;
        cursor.close();
    }

    private void initView() {
        Log.i("test", "initView: 进来了吗");
        nextIv = findViewById(R.id.local_music_iv_next);
        playIv = findViewById(R.id.local_music_iv_play);
        lastIv = findViewById(R.id.local_music_iv_last);
        singerTv = findViewById(R.id.local_music_tv_singer);
        songTv = findViewById(R.id.local_music_tv_song);
        musicRv = findViewById(R.id.local_music_rv);
        seekBar = findViewById(R.id.seekBar);
//        lyricTv = findViewById(R.id.lyricText);

        musicIcon = findViewById(R.id.local_music_iv_icon);
        RelativeLayout parentLayout = findViewById(R.id.main);

        Log.i("test", "initView: 能到着吗");

        nextIv.setOnClickListener(this);
        lastIv.setOnClickListener(this);
        playIv.setOnClickListener(this);

        musicIcon.setOnClickListener(this);

//        // 设置 SeekBar 的触摸监听器
//        seekBar.setOnTouchListener(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//                // 当用户开始触摸 SeekBar 时，阻止父布局拦截触摸事件
//                switch (event.getAction()) {
//                    case MotionEvent.ACTION_DOWN:
//                        parentLayout.requestDisallowInterceptTouchEvent(true);
//                        break;
//                    case MotionEvent.ACTION_UP:
//                    case MotionEvent.ACTION_CANCEL:
//                        // 用户停止触摸时，恢复父布局的事件拦截
//                        parentLayout.requestDisallowInterceptTouchEvent(false);
//                        break;
//                }
//                return false; // 返回 false，让 SeekBar 继续处理触摸事件
//            }
//        });

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    mediaPlayer.seekTo(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                handler.removeCallbacks(updateSeekBar);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mediaPlayer.seekTo(seekBar.getProgress());
                updateSeekBar();
            }
        });

        updateSeekBar = new Runnable() {
            @Override
            public void run() {
                seekBar.setProgress(mediaPlayer.getCurrentPosition());
                handler.postDelayed(this, 500);
            }
        };

//        seekBarOverlay = findViewById(R.id.seekBarOverlay);
//
//// 给透明遮罩设置触摸监听器
//        seekBarOverlay.setOnTouchListener(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//                // 将触摸事件传递给 SeekBar
//                seekBar.onTouchEvent(event);
//                return true; // 返回 true 表示事件被处理
//            }
//        });

    }

    private void updateSeekBar() {
        handler.post(updateSeekBar);
    }

    @Override
    public void onClick(View view) {
        int nowId = view.getId();
        if (nowId == R.id.local_music_iv_next) {
            if (currentPlayPosition == metaDatas.size() - 1) {
                currentPlayPosition = 0;
            } else {
                currentPlayPosition = currentPlayPosition + 1;
            }
            LocalMusicBean nextBean = metaDatas.get(currentPlayPosition);
//            currentMusicPath = nextBean.getPath();
            updateMessage(nextBean);
            playMusicInMusicBean(nextBean);
        } else if (nowId == R.id.local_music_iv_play) {
            if (currentPlayPosition == -1) {
                // 并音乐在播放
                Toast.makeText(this, "请选择您想要播放的音乐", Toast.LENGTH_SHORT).show();
                return;
            }
            if (mediaPlayer.isPlaying()) {
                // 此时处于播放状态，需要暂停音乐
                pauseMusic();
            } else {
                // 处于暂停播放状态，需要开始播放
                playMusic();
            }
        } else if (nowId == R.id.local_music_iv_last) {
            if (currentPlayPosition == 0 || currentPlayPosition == -1) {
                currentPlayPosition = metaDatas.size() - 1;
            } else {
                currentPlayPosition = currentPlayPosition - 1;
            }
            LocalMusicBean lastBean = metaDatas.get(currentPlayPosition);
//            currentMusicPath = lastBean.getPath();
            updateMessage(lastBean);
            playMusicInMusicBean(lastBean);
        } else if (nowId == R.id.local_music_iv_icon) {
            Log.i("test", "onClick: I am click this button");

            myViewModel.setMyVariable(currentMusicPath);
//            intent.putExtra("path", currentMusicPath);
//            Log.i("test", "onClick: 寄了吗");
            startActivity(intent);
//            Log.i("test", "onClick: 寄了没");
//            ImageView iv = findViewById(R.id.local_music_iv_icon);
//            iv.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
////                    getSupportFragmentManager().beginTransaction()
////                            .replace(R.id.fragmentContainer, new LyricFragment())
////                            .commit();
//                }
//            });

        }
//        Log.i("test", String.valueOf(nowId));
    }

//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
//        stopMusic();
//    }


//    class LyricInfo {
//        long time;
//        String content;
//
//        public LyricInfo() {
//        }
//
//        public LyricInfo(long time, String content) {
//            this.time = time;
//            this.content = content;
//        }
//    }

}