package ai.picovoice.porcupine.demo;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class MusicListFragment extends Fragment {


    ImageView nextIv, playIv, lastIv, musicIcon;
    TextView singerTv, songTv;
    RecyclerView musicRv;

    MediaPlayer mediaPlayer;

    List<LocalMusicBean> metaDatas;
    private LocalMusicAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.player_activity_main, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 在视图完全创建后加载数据
//        loadLocalData(view);
//        initView();

        mediaPlayer = new MediaPlayer();
        metaDatas = new ArrayList<>();

        // 创建适配器
//        adapter = new LocalMusicAdapter(this, metaDatas);
        musicRv.setAdapter(adapter);

        //设置布局管理器
//        LinearLayoutManager manager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
//        musicRv.setLayoutManager(manager);

        //加载本地数据源

        Log.i("test", "onCreate: 到这里没");
//        loadLocalMusicData();

        // 设置每一项的点击事件
//        setEventListener();
    }

//    private void initView() {
//        Log.i("test", "initView: 进来了吗");
//        nextIv = findViewById(R.id.local_music_iv_next);
//        playIv = findViewById(R.id.local_music_iv_play);
//        lastIv = findViewById(R.id.local_music_iv_last);
//        singerTv = findViewById(R.id.local_music_tv_singer);
//        songTv = findViewById(R.id.local_music_tv_song);
//        musicRv = findViewById(R.id.local_music_rv);
//        lyricTv = findViewById(R.id.lyricText);
//
////        musicIcon = findViewById(R.id.local_music_iv_icon);
//
//        Log.i("test", "initView: 能到着吗");
//
//        nextIv.setOnClickListener(this);
//        lastIv.setOnClickListener(this);
//        playIv.setOnClickListener(this);
//
////        musicIcon.setOnClickListener(this);
//
//        Log.i("test", "initView: 那这里呢");
//
//    }
}
