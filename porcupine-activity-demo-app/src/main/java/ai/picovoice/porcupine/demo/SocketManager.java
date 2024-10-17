package ai.picovoice.porcupine.demo;
/*
socket通讯
socket连接
发送数据流
接收数据流
socket结束
 */
import android.media.MediaPlayer;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

public class SocketManager {

    private Socket socket;
    private OutputStream outputStream;

    //创建Socket连接
    public void connectSocket(String host, int port) throws IOException {
        socket = new Socket(host, port);
        outputStream = socket.getOutputStream();
    }

    //定义buffer大小，用"?!"作为分割符
    public void sendFile(File file) throws IOException {

        byte[] buffer = new byte[4096];
        FileInputStream fis = new FileInputStream(file);
        BufferedInputStream bis = new BufferedInputStream(fis);

        int bytesRead;
        while ((bytesRead = bis.read(buffer, 0, buffer.length)) != -1) {
            outputStream.write(buffer, 0, bytesRead);
        }

//      尝试增加结束符给后端
        socket.getOutputStream().write("?!".getBytes());

        outputStream.flush();
        bis.close();
    }

    //
    public void receiveFile(File savePath) throws IOException {
        try {
            DataInputStream dataInputStream = null;
            FileOutputStream fileOutputStream = null;

            dataInputStream = new DataInputStream(socket.getInputStream());
            MediaPlayer mediaPlayer = new MediaPlayer();
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    mp.reset();
                }
            });

            //循环读取，
            while (true) {
                // 读取文件大小
                long fileSize = dataInputStream.readLong();  // 读取8字节的文件大小
                if (fileSize == 1) {
                    break;
                } else {
                    long totalBytesRead = 0;
                    // 创建文件存储路径
                    File file = new File(savePath, "received_audio.wav");
                    fileOutputStream = new FileOutputStream(file);

                    // 接收文件数据
                    byte[] buffer = new byte[4096];
                    int bytesRead;
                    while ((bytesRead = dataInputStream.read(buffer, 0, buffer.length)) != -1) {
                        fileOutputStream.write(buffer, 0, bytesRead);
                        totalBytesRead += bytesRead;
                        // 判断是否接收完毕
                        if (totalBytesRead >= fileSize) {
                            break;
                        }
                    }

                    // 接收完毕后关闭文件流
                    // 确保接收完才播放
                    while(mediaPlayer.isPlaying()){}
                    mediaPlayer.setDataSource(savePath + "/received_audio.wav");
                    mediaPlayer.prepare();
                    mediaPlayer.start();
                    fileOutputStream.close();

                    //确保播完了（PS这段代码未测试）
//                    while(true){
//                        if(!mediaPlayer.isPlaying()){
//                            file.delete();
//                            break;
//                        }
//                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //关闭socket连接
    public void closeSocket() throws IOException {
        if (outputStream != null) {
            outputStream.close();
        }
        if (socket != null) {
            socket.close();
        }

    }

}
