package ai.picovoice.porcupine.demo;
/*
时间展示函数
开启
结束
UI更新
 */

import android.os.Handler;
import android.os.Looper;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ShowTime {

    private final Handler handler;
    private Runnable runnable;
    private TextView time_hour;
    private TextView time_minute;
    private TextView time_day ;
    private TextView time_date;
    //构造函数
    public ShowTime(TextView time_hour, TextView time_minute, TextView time_day, TextView time_date) {
        this.time_hour = time_hour;
        this.time_minute = time_minute;
        this.time_day = time_day;
        this.time_date = time_date;
        handler = new Handler(Looper.getMainLooper());
        setupUpdater();
    }

    //开启一个线程，每1s更新一次UI
    private void setupUpdater() {
        runnable = new Runnable() {
            @Override
            public void run() {
                updateTime();
                handler.postDelayed(this, 1000); // 每秒更新一次
            }
        };
    }

    //开始更新
    public void startUpdating() {
        handler.post(runnable);
    }

    //停止更新
    public void stopUpdating() {
        handler.removeCallbacks(runnable); // 停止定时任务
    }

    //在UI中设置时间展示的格式，例如"yyyy-MM-dd HH:mm:ss"
    private void updateTime() {
        String currentHour = new SimpleDateFormat("HH", Locale.getDefault()).format(new Date());
        String currentMin = new SimpleDateFormat("mm", Locale.getDefault()).format(new Date());
        String currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        String currentDay = new SimpleDateFormat("EEEE", Locale.getDefault()).format(new Date());
        time_hour.setText(currentHour);
        time_minute.setText(currentMin);
        time_date.setText(currentDate);
        time_day.setText(currentDay);

    }

}
