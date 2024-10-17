package ai.picovoice.porcupine.demo;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class LyricActivity extends AppCompatActivity {

    private TextView lyricTv;
//    private int currentLyricIndex;
    private MediaPlayer mediaPlayer;

    private OkHttpClient client = new OkHttpClient();

    private String musicPath = player_MainActivity.currentMusicPath;


    List<LyricInfo> mLyricInfos;
    private int currentLyricIndex;
//    String current;
//    MyViewModel myViewModel;




    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.lyric_sample);
        lyricTv = findViewById(R.id.lyricText);
        mediaPlayer = MediaPlayerSingleton.getInstance(this);

        startListen();
//        Log.i("test", "onCreate: wei");


        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                // 当 MediaPlayer 准备好时执行的操作
                loadLyricFile(player_MainActivity.currentMusicPath);
            }
        });

//        myViewModel = new ViewModelProvider(this).get(MyViewModel.class);
//        myViewModel.getMyVariable().observe(this, new Observer<String>() {
//            @Override
//            public void onChanged(String s) {
//                Log.i("test", "onChanged: what the help");
//                Log.i("test", s);
//                Log.i("test", "onChanged: fuck you");
//                loadLyricFile(s);
//            }
//        });

//        Log.i("test", "onCreate: oi!!!!!!!!!!");
//        if(mediaPlayer.isPlaying()){
//            Log.i("haha", "onCreate: is the same mediaPlayer");
//        }else{
//            Log.i("haha", "onCreate: not the same");
//        }



//        current = MainActivity.currentMusicPath;


//        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
//            @Override
//            public void onCompletion(MediaPlayer mp) {
//                if (current == metaDatas.size() - 1) {
//                    current = 0;
//                } else {
//                    current = current + 1;
//                }
//                LocalMusicBean nextBean = metaDatas.get(current);
//                playMusicInMusicBean(nextBean);
////                        currentMusicPath = nextBean.getPath();
//////                        intent = new Intent(this, LyricActivity.class);
////                        intent.putExtra("path", currentMusicPath);
//            }
//        });

//        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
//            @Override
//            public void onCompletion(MediaPlayer mp) {
//                startListen();
//            }
//        });
//
//
//        if(lyricText != null){
//            lyricTv.setText(lyricText);
//        }else {
//            lyricTv.setText("暂无歌词");
//        }
    }


    private void startListen(){
        musicPath = player_MainActivity.currentMusicPath;
        if (musicPath != null) {
            loadLyricFile(musicPath);
        }else {
            Toast.makeText(this,"g了",Toast.LENGTH_SHORT).show();
        }
    }

    private void loadLyricFile(String musicPath) {
//        Log.i("haha", musicPath);

//        String musicPath = musicBean.getPath();
        String songFileNameWithoutExtension = musicPath.substring(0, musicPath.lastIndexOf("."));

        File lrcFile = new File(songFileNameWithoutExtension + ".lrc");

        // test
//        Log.i("haha", String.valueOf(lrcFile));

        if (lrcFile.exists()) {
//            Log.i("haha", lrcFile + "=============exists");
            try {
                mLyricInfos = parseLrcFile(lrcFile);
                Handler handler =
                        new Handler();
                currentLyricIndex = 0;
                syncLyricsWithMusic(mediaPlayer, handler);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
//            Log.i("Search", MainActivity.songName + "\n" + MainActivity.artistName);
            searchLyricsBySongAndArtist(player_MainActivity.songName, player_MainActivity.artistName);
//            Log.i("test", String.valueOf(lrcFile) + "=============g");
//            lyricTv.setText("暂无歌词");
        }
    }

    private void syncLyricsWithMusic(MediaPlayer mediaPlayer, Handler handler) {
        Runnable lyricsUpdater = new Runnable() {
            @Override
            public void run() {
                if(mediaPlayer.isPlaying() || mediaPlayer.getCurrentPosition() != 0){
                    long currentPosition = mediaPlayer.getCurrentPosition();

                    while(currentLyricIndex < mLyricInfos.size() - 1 && currentPosition >= mLyricInfos.get(currentLyricIndex + 1).time){
                        currentLyricIndex++;
                    }
                    if (mLyricInfos.get(currentLyricIndex).content != null) {
                        displayLyrics(mLyricInfos.get(currentLyricIndex).content);
//                        displayLyrics(currentLyricIndex);
//                        displayLyricsWithAnimation(mLyricInfos.get(currentLyricIndex).content);
//                    String lyricText = mLyricInfos.get(currentLyricIndex).content;
                    }else {
                        displayLyrics(mLyricInfos.get(currentLyricIndex+1).content);
                    }
                    handler.postDelayed(this, 50);

                }
            }
        };

        handler.post(lyricsUpdater);
    }

//    // 添加动态显示歌词的函数
//    private void displayLyricsWithAnimation(String lyricContent) {
//        final int lyricLength = lyricContent.length();
//        final Handler animationHandler = new Handler();
//        final StringBuilder displayedLyric = new StringBuilder();
//
//        // 清空当前歌词显示
//        lyricTv.setText("");
//
//        // 逐字逐字显示歌词的逻辑
//        Runnable animateLyric = new Runnable() {
//            int index = 0;
//
//            @Override
//            public void run() {
//                if (index < lyricLength) {
//                    // 每次添加一个字符
//                    displayedLyric.append(lyricContent.charAt(index));
//                    lyricTv.setText(displayedLyric.toString());
//                    index++;
//                    // 每100毫秒显示一个字，可以根据需求调整速度
//                    animationHandler.postDelayed(this, 10);
//                }
//            }
//        };
//
//        // 开始执行逐字显示的动画
//        animationHandler.post(animateLyric);
//    }


    private void displayLyrics(String content){

        lyricTv.setText(content);
    }

//    private void displayLyrics(int index) {
////        lyricTv.setText(content);
//        lyricTv.animate().rotationY(90).setDuration(500).withEndAction(new Runnable() {
//            @Override
//            public void run() {
//                lyricTv.setText(mLyricInfos.get(index).content);
//                lyricTv.setRotationY(-90);
//                lyricTv.animate().rotationY(0).setDuration(500).start();
//            }
//        }).start();
//    }

    private List<LyricInfo> parseLrcFile(File lyricsFile) throws IOException {
        List<LyricInfo> LyricInfos = new ArrayList<>();
        FileInputStream fis = new FileInputStream(lyricsFile);
        InputStreamReader inputStreamReader = new InputStreamReader(fis);
        BufferedReader reader = new BufferedReader(inputStreamReader);
        String line;
        Pattern pattern = Pattern.compile("\\[(\\d{2}):(\\d{2})\\.(\\d{2})\\](.*)");

        while ((line = reader.readLine()) != null) {
            Matcher matcher = pattern.matcher(line);
            if (matcher.matches()) {
                long minutes = Long.parseLong(matcher.group(1));
                long seconds = Long.parseLong(matcher.group(2));
                long milliseconds = Long.parseLong(matcher.group(3)) * 10;  // 将小数部分乘以 10 得到毫秒
                long time = (minutes * 60 + seconds) * 1000 + milliseconds;  // 转换为毫秒
                String content = matcher.group(4).trim();
                LyricInfos.add(new LyricInfo(time, content));
            }
        }

//        for(int i = 0; i < LyricInfos.size(); i++){
//            Log.i("lyric", LyricInfos.get(i).content);
//        }

        reader.close();
        return LyricInfos;
    }

    // 根据歌曲名和歌手名搜索歌词
    public void searchLyricsBySongAndArtist(String songName, String artistName) {
        // 将歌曲名和歌手名组合成关键词
        String keyword = songName + " " + artistName;
        String url = "https://c.y.qq.com/soso/fcgi-bin/client_search_cp?w=" + keyword + "&p=1&n=3";

        // 创建请求
        Request request = new Request.Builder()
                .url(url)
                .addHeader("referer", "https://y.qq.com/portal/search.html")  // 添加referer避免被拒
                .build();

        // 发送异步请求
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
//                e.printStackTrace();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String responseData = null;
                String jsonResponse = null;
                if (response.body() != null) {
                    responseData = response.body().string();
                    jsonResponse = responseData.substring(responseData.indexOf("(") + 1, responseData.lastIndexOf(")"));
                }
//                Log.i("requests", "-------"+responseData);
//                Log.i("Search", jsonResponse);
//                Log.i("Search", "-------------------------------------");


                try {
                    // 解析返回的JSON
                    JSONObject jsonObject = new JSONObject(jsonResponse);
                    JSONArray songList = jsonObject.getJSONObject("data")
                            .getJSONObject("song")
                            .getJSONArray("list");

                    if (songList.length() > 0) {
//                        Log.i("Search", String.valueOf(songList.length()));
//                        Log.i("Search", "------------------------");
//                        String name = songList.getJSONObject(0).getString("songname");
//                        Log.i("Search", name);

                        // 获取第一个搜索结果的 songmid
//                        String songmid = songList.getJSONObject(0).getString("songmid");

                        String songmid = null;

                        for(int i = 0; i < 3; i++){
                            String name = songList.getJSONObject(i).getString("songname");
//                            Log.i("Search", name);
                            if(name.equals(songName)){
                                songmid = songList.getJSONObject(i).getString("songmid");
                                break;
                            }
                        }

                        if (songmid == null){
                            lyricTv.setText("暂时找不到歌词");
                        }else{
                            Log.i("Search", songmid);
//                            Log.i("Search", "-------------------------------------");

                            // 根据 songmid 获取歌词
                            getLyricsFromSongMid(songmid);
                        }


                    } else {
                        System.out.println("未找到相关歌词");
                    }
                } catch (Exception e) {
//                    e.printStackTrace();
                }
            }
        });
    }

    private void getLyricsFromSongMid(String songmid) {
        String url = "https://c.y.qq.com/lyric/fcgi-bin/fcg_query_lyric_new.fcg?songmid=" + songmid + "&format=json&nobase64=1";

        Request request = new Request.Builder()
                .url(url)
                .addHeader("referer", "https://y.qq.com/portal/player.html")  // 添加referer避免被拒
                .build();

        // 发送异步请求
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.i("requests", "onFailure: fail");
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseData = response.body().string();
//                Log.i("requests", "-------"+responseData);
                try {
                    // 解析返回的歌词数据
                    JSONObject jsonObject = new JSONObject(responseData);
                    String lyrics = jsonObject.getString("lyric");


//                    Log.i("lyric", lyrics);
//                    Log.i("lyric", "-----------------------------");
//                    runOnUiThread(() -> {
//                        lyricsView.setText(lyrics);
//                    });

                    saveToFile(lyrics);
                    // 在主线程上更新UI
//                    System.out.println("Lyrics: " + lyrics);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void saveToFile(String lyrics) throws IOException {
        String musicPath = player_MainActivity.currentMusicPath;
        String songFileNameWithoutExtension = musicPath.substring(0, musicPath.lastIndexOf("."));
//        Log.i("Search", "saveToFile: before new file");
        File lrcFile = new File(songFileNameWithoutExtension + ".lrc");
//        Log.i("Search", String.valueOf(lrcFile));
        FileOutputStream fos = new FileOutputStream(lrcFile);
        fos.write(lyrics.getBytes());
        fos.close();
        if(lrcFile.exists()){
//            Log.i("Search", "saveToFile: fjldahgeoiawgheroawgeraf");
//            Log.i("Search", "------------------\n" + musicPath);
//            loadLyricFile(musicPath);
            startListen();
        }else {
            lyricTv.setText("暂无歌词");
        }
    }


    class LyricInfo {
        long time;
        String content;

        public LyricInfo() {
        }

        public LyricInfo(long time, String content) {
            this.time = time;
            this.content = content;
        }
    }

}


