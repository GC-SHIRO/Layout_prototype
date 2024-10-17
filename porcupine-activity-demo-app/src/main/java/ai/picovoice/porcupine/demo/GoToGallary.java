package ai.picovoice.porcupine.demo;

/*
功能：这里实现打开相册的操作，后续需要来队相册中的图片做什么操作在此处进行
openGallery：打开相册

ps.注意凡是跟UI相关的，都应该使用Activity做为Context来处理
其他的一些操作，Service,Activity,Application等实例都可以
注意Context引用的持有，防止内存泄漏
 */

import android.content.Context;
import android.content.Intent;
import android.provider.MediaStore;

import androidx.appcompat.app.AppCompatActivity;

public class GoToGallary extends AppCompatActivity {

    private final Context context;

    //构造函数，传入context值
    public GoToGallary(Context context){
        this.context = context;
    }

    //实现打开相册
    public void openGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        context.startActivity(galleryIntent);
    }

}
