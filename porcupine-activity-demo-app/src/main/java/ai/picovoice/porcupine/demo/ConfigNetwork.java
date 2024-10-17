package ai.picovoice.porcupine.demo;
/*
作用：这串代码实现扫码之后一键配网
实现开启二维码扫描器
提取二维码中信息
实现wifi链接
 */


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiNetworkSpecifier;
import android.os.Build;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.journeyapps.barcodescanner.CaptureActivity;

public class ConfigNetwork {

    private Activity activity;
    private Context context;

    //构造函数，保证操作的activity，context和main中的一致
    public ConfigNetwork(Activity activity, Context context){
        this.activity = activity;
        this.context = context;
    }

    //开启二维码扫描器
    public void startQRScanner() {
        IntentIntegrator integrator = new IntentIntegrator(activity);
        integrator.setPrompt("scan a QR code");
        integrator.setOrientationLocked(true);
        integrator.setCaptureActivity(CaptureActivity.class);
        integrator.initiateScan();
    }

    //识别到二维码后实现从二维码中提取信息，并调用链接WIFI函数
    public void handleActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() == null) {
                Toast.makeText(context, "Cancelled", Toast.LENGTH_LONG).show();
            } else {
                String qrData = result.getContents();
                // 解析二维码数据
                String ssid = parseSsidFromQrData(qrData);
                String password = parsePasswordFromQrData(qrData);
                boolean isHidden = parseIsHiddenFromQrData(qrData);

                // 连接到 Wi-Fi
                connectToWifi(ssid, password, isHidden);
            }
        }
    }

    //实现wifi链接
    public void connectToWifi(String ssid, String password, boolean isHidden) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // 使用 WifiNetworkSpecifier 连接 Wi-Fi (Android 10及以上)
            WifiNetworkSpecifier.Builder builder = new WifiNetworkSpecifier.Builder();
            builder.setSsid(ssid);
            if (password != null && !password.isEmpty()) {
                builder.setWpa2Passphrase(password);
            }
            builder.setIsHiddenSsid(isHidden);

            WifiNetworkSpecifier wifiNetworkSpecifier = builder.build();
            NetworkRequest networkRequest = new NetworkRequest.Builder()
                    .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                    .setNetworkSpecifier(wifiNetworkSpecifier)
                    .build();

            ConnectivityManager connectivityManager = ContextCompat.getSystemService(context, ConnectivityManager.class);
            if (connectivityManager != null) {
                connectivityManager.requestNetwork(networkRequest, new ConnectivityManager.NetworkCallback() {
                    @Override
                    public void onAvailable(Network network) {
                        super.onAvailable(network);
                        // 连接成功
                        connectivityManager.bindProcessToNetwork(network);
                        Toast.makeText(context, "Connected to " + ssid, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onUnavailable() {
                        super.onUnavailable();
                        // 连接失败
                        Toast.makeText(context, "Failed to connect to " + ssid, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        } else {
            // 对于低于 Android 10 的版本
            WifiConfiguration wifiConfig = new WifiConfiguration();
            wifiConfig.SSID = "\"" + ssid + "\"";
            if (password != null && !password.isEmpty()) {
                wifiConfig.preSharedKey = "\"" + password + "\"";
            } else {
                wifiConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            }

            WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            if (wifiManager != null) {
                int networkId = wifiManager.addNetwork(wifiConfig);
                if (networkId != -1) {
                    wifiManager.disconnect();
                    wifiManager.enableNetwork(networkId, true);
                    wifiManager.reconnect();
                    Toast.makeText(context, "Connecting to " + ssid + "...", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(context, "Failed to configure network.", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    //从QRcode中提取password提取SSID值
    private String parseSsidFromQrData(String qrData) {
        // 解析逻辑
        String ssid = "";
        // 例子: 从二维码数据中提取SSID
        String[] parts = qrData.split(";");
        for (String part : parts) {
            if (part.startsWith("S:")) {
                ssid = part.substring(2);
            }
        }
        return ssid;
    }

    //从QRcode中提取password提取password
    private String parsePasswordFromQrData(String qrData) {
        // 解析逻辑
        String password = "";
        // 例子: 从二维码数据中提取密码
        String[] parts = qrData.split(";");
        for (String part : parts) {
            if (part.startsWith("P:")) {
                password = part.substring(2);
            }
        }
        return password;
    }

    //从QRcode中提取password提取hidden值
    private boolean parseIsHiddenFromQrData(String qrData) {
        // 解析逻辑
        boolean isHidden = false;
        // 例子: 从二维码数据中提取是否隐藏
        String[] parts = qrData.split(";");
        for (String part : parts) {
            if (part.startsWith("H:")) {
                isHidden = part.substring(2).equals("true");
            }
        }
        return isHidden;
    }

}
