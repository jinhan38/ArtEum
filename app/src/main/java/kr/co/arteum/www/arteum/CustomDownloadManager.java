package kr.co.arteum.www.arteum;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

public class CustomDownloadManager extends AppCompatActivity {

    private static final String TAG = "CustomDownloadManager";
    private Activity activity;

    public CustomDownloadManager(Activity activity, String url, String dir) {
        this.activity = activity;
        downloadTask(url, dir);
    }

    private void downloadTask(String url, String dir) {

        Log.e(TAG, "downloadTask: " );
        File file = new File(getUrlDecode(dir));
        Uri uri = FileProvider.getUriForFile(this,
                "kr.co.pionmanager.www.provider", file);

        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI |
                DownloadManager.Request.NETWORK_MOBILE);
        request.setTitle("Download");
        request.setDescription("Downloading file...");
        request.allowScanningByMediaScanner();
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, dir + "/ " + System.currentTimeMillis());

        DownloadManager manager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
        manager.enqueue(request);
    }

    public String getUrlDecode(String _strFileName)
    {
        String strRet = null;
        try {
            strRet = URLDecoder.decode(_strFileName, "UTF-8");
        } catch (UnsupportedEncodingException e) {

            strRet = "";
        }

        return strRet;
    }
}
