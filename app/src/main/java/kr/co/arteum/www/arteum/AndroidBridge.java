package kr.co.arteum.www.arteum;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import java.io.File;

import static kr.co.arteum.www.arteum.MainActivity.mainActivity;

/**
 * Created by Administrator on 2017-08-03.
 */

public class AndroidBridge {

    private static final String TAG = "AndroidBridge";

    private final Handler handler = new Handler();
    private final Handler handler2 = new Handler();
    private final Handler handler3 = new Handler();
    private WebView mWebView;
    private Integer curNum;

    // 생성자
    // 따로 사용할일 없으면 이거 안만들고 위의 변수도 안만들어도 됨.
    public AndroidBridge(WebView mWebView, Integer curNum) {
        this.mWebView = mWebView;
        this.curNum = curNum;
    }

    // 창닫기
    @JavascriptInterface
    public void activityClose() { // must be final
        handler.post(new Runnable() {
            public void run() {
                Log.e("HybridApp", "데이터 요청");
                String test = "닫기버튼이 눌렸습니다.";
                Log.e("HybridApp", test);
                //window.HybridApp.activityClose(); //웹쪽 자바스크립트단에서 activityClose 호출
                //mWebView.loadUrl("javascript:getAndroidData('"+test+"')"); //안드로이드에서 웹페이지 자바스크립트 호출하기
                //function getAndroidData(data) { //웹쪽 자바스크립트에서 데이터 받기
                //        alert(data);
                //}

                if (curNum == 1) {
                    PopViewActivity.openerActivity.onBackPressed();
                } else if (curNum == 2) {
                    PopMoreViewActivity.openerMoreActivity.onBackPressed();
                } else if (curNum == 3) {
                    PopMoreMoreViewActivity.openerMoreMoreActivity.onBackPressed();
                } else if (curNum == 0) {
                    mainActivity.onBackPressed();
                }

            }
        });
    }

    @JavascriptInterface
    public void activityCloseParentRefresh(final String url) { // must be final
        handler.post(new Runnable() {
            public void run() {
                Log.e("HybridApp", "데이터 요청");
                String test = "닫기버튼이 눌렸습니다.";
                Log.e("HybridApp", test);
                //window.HybridApp.activityClose(); //웹쪽 자바스크립트단에서 activityClose 호출
                //mWebView.loadUrl("javascript:getAndroidData('"+test+"')"); //안드로이드에서 웹페이지 자바스크립트 호출하기
                //function getAndroidData(data) { //웹쪽 자바스크립트에서 데이터 받기
                //        alert(data);
                //}

                if (curNum == 1) {
                    mainActivity.mWebView.loadUrl(url);
                    PopViewActivity.openerActivity.finish();
                } else if (curNum == 2) {
                    PopViewActivity.openerActivity.pWebView.loadUrl(url);
                    PopMoreViewActivity.openerMoreActivity.finish();
                } else if (curNum == 3) {
                    PopViewActivity.openerActivity.pWebView.loadUrl(url);
                    PopMoreViewActivity.openerMoreActivity.finish();
                    PopMoreMoreViewActivity.openerMoreMoreActivity.finish();
                } else if (curNum == 0) {
                    mainActivity.mWebView.loadUrl(url);
                    //mainActivity.onBackPressed();
                }

            }
        });
    }

    // 토큰 생성하기
    @JavascriptInterface
    public void callTokenValue(final String num, final String id, final String level) { // 토큰값을 호출하면 아래 함수로 값을 전송한다.
        handler2.post(new Runnable() {
            public void run() {
                Util.NUM = num;
                Util.MID = id;
                Util.MEMBER_LEVEL = level;
                Util.isLogin = true;
                Log.e("loginVal", Util.NUM + Util.MID + Util.MEMBER_LEVEL);
                //mWebView.loadUrl("javascript:getAndroidDataToken('"+Util.TOKEN+"')");
            }
        });
    }

    // 안드로이드 접속
    @JavascriptInterface
    public void connectAndroidApp() { // 토큰값을 호출하면 아래 함수로 값을 전송한다.
        handler3.post(new Runnable() {
            public void run() {
                mWebView.loadUrl("javascript:isAndroidApp('" + Util.TOKEN + "')"); //토큰값 입력
                //mWebView.loadUrl("javascript:nowAndroidAppVersion('"+getVersionInfo(mainActivity)+"')"); //현재 버전 비교
            }
        });
    }


    //웹 자바스크립트에서 호출
    @JavascriptInterface
    public void onDownloadStart(String url, String fileName) {

        Log.e(TAG, "clicked!");
        Log.e(TAG, "onDownloadStart: url : " + url);
        Log.e(TAG, "onDownloadStart: fileName : " + fileName);

        Toast toast = new Toast(mainActivity);

        //다운로드 모듈
        Util.DOWNLOAD_FILE_NAME = fileName;
        toast.makeText(mainActivity, "파일다운로드 중입니다.", Toast.LENGTH_LONG).show();

//        애플리케이션을 제작하다 보면 파일을 입/출력 기능을 구현해야 하는 경우가 있습니다.
//        파일 입/출력을 수행하려면 파일의 경로를 꼭 알아야 하는데, 안드로이드에서 파일을 저장할 수 있는 경로는 다양합니다.
//       가장 크게 애플리케이션 데이터가 저장되는 영역(일반적으로 '내부 저장소(Internal Storage)'라 불림)와
//       사진, 비디오, 데이터 등을 저장하는 영역(일반적으로 '외부 저장소(External Storage)(주1)'라 불림)으로 나뉠 수 있으며, 각 영역별로 다시 캐시 데이터가 저장되는
//       영역, 데이터베이스가 저장되는 영역 등으로 나뉩니다.

        //아래 줄은 외부저장소의 경로를 가져오는 코드  : 경로 =/mnt/sdcard/Download
        File file = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        String strDir = file.getAbsolutePath();

        storagePermissionCheck("https://www.arteum.co.kr" + url, strDir, fileName);
//        storagePermissionCheck(mainActivity.getString(R.string.arteumUrl) + url, strDir, fileName);

//        DownloadManager.getInstance().setSavePath(strDir + "/ARTEUM"); // 저장하려는 경로 지정.
//        DownloadManager.getInstance().setDownloadUrl("https://www.arteum.co.kr" + url);



    }


    private void storagePermissionCheck(String url, String dir, String fileName) {

        boolean result = false;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (mainActivity.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                    PackageManager.PERMISSION_DENIED) {
                //거부했을 때 다시 요청
                ActivityCompat.requestPermissions(mainActivity, mainActivity.REQUIRED_PERMISSIONS,
                        MainActivity.PERMISSIONS_REQUEST_CODE);

            } else {

                //동의 했을 때
                result = true;
            }

        } else {
            //버전 낮은경우
            result = true;
        }

        if (result) mainActivity.startDownload(url, dir, fileName);

    }



    public String getVersionInfo(Context context) {
        String version = null;
        try {
            PackageInfo i = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            version = i.versionName;
        } catch (PackageManager.NameNotFoundException e) {
        }
        return version;
    }


}

