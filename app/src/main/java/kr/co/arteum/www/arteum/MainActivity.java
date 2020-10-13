package kr.co.arteum.www.arteum;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.webkit.CookieManager;
import android.webkit.DownloadListener;
import android.webkit.URLUtil;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Timer;


public class MainActivity extends Activity implements ActivityCompat.OnRequestPermissionsResultCallback {

    private static final String TAG = "MainActivity";
    WebView mWebView;
    ProgressBar progressBar;
    LinearLayout ll_loading;
    LinearLayout mLayout;
    //    static LinearLayout ll_setting;
//    public static RelativeLayout rl_bottomMenu;
    TextView tv_loadMsg;
    TextView tv_loadPer;
    TextView tv_switch;
    Switch switch1;
    ValueCallback mFilePathCallback;
    ImageView logo;
    ImageView iv_setting;
    Boolean isStartAction = true;
    Boolean isWebLoaded = false;
    Boolean isAniLoaded = false;
    private long backKeyPressedTime = 0;
    AndroidBridge androidBridge;
    Timer timer;
    public static MainActivity mainActivity;

    public static final int PERMISSIONS_REQUEST_CODE = 100;
    public final String[] REQUIRED_PERMISSIONS = {Manifest.permission.CAMERA, // 카메라
            Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};  // 외부 저장소

    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT); //세로고정
        setContentView(R.layout.activity_main);
        Util.CURRENT_CONTEXT = this;
        this.mainActivity = MainActivity.this;
        progressBar = this.findViewById(R.id.progress);
        mLayout = findViewById(R.id.mainLayout);
        ll_loading = findViewById(R.id.ll_loading);
        tv_loadMsg = findViewById(R.id.tv_loadMsg);
        tv_loadPer = findViewById(R.id.tv_loadPer);
        tv_switch = findViewById(R.id.tv_switch);
        switch1 = findViewById(R.id.switch1);
        logo = findViewById(R.id.logo);
//        final Animation animationOpen = AnimationUtils.loadAnimation(this, R.anim.fade_up);
//        final Animation animationClose = AnimationUtils.loadAnimation(this, R.anim.fade_down);

        timer = new Timer(true);

        fadeInAndShowImageView(logo);
        //퍼미션 체크시작;
        permissionCheck();

        mWebView = findViewById(R.id.webView);

        browserSettings();

        mWebView.loadUrl(getString(R.string.arteumUrl));

        FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(MainActivity.this, new OnSuccessListener<InstanceIdResult>() {
            @Override
            public void onSuccess(InstanceIdResult instanceIdResult) {
                String newToken = instanceIdResult.getToken();
                Util.TOKEN = newToken;
                Log.e("newToken", newToken);
            }
        });

//        new kr.co.arteum.www.arteum.DownloadManager(this.getApplicationContext());
    }


    /*//*/

    private void browserSettings() {

        mWebView.setBackgroundColor(0); //배경색
        mWebView.setHorizontalScrollBarEnabled(false); //가로 스크롤
        mWebView.setVerticalScrollBarEnabled(false); //세로 스크롤
        if (Build.VERSION.SDK_INT >= 21) {
            mWebView.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }
        androidBridge = new AndroidBridge(mWebView, 0);
        mWebView.addJavascriptInterface(androidBridge, "HybridApp");


        WebSettings settings = mWebView.getSettings();
        settings.setJavaScriptEnabled(true); //javascript 허용
        settings.setSupportMultipleWindows(true); //다중웹뷰 허용
        settings.setJavaScriptCanOpenWindowsAutomatically(true);//javascript의 window.open 허용

        //HTML을 파싱하여 웹뷰에서 보여주거나 하는 작업에서 width , height 가 화면 크기와 맞지 않는 현상을 잡아주는 코드
        settings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
        //캐시파일 사용 금지(운영중엔 주석처리 할 것)
        settings.setCacheMode(WebSettings.LOAD_NO_CACHE); //개발중엔 no_cache, 배포중엔 load_default

        //zoom 허용
        settings.setBuiltInZoomControls(true);
        settings.setSupportZoom(true);

        //zoom 하단 도움바 삭제
        settings.setDisplayZoomControls(false);

        //meta태그의 viewport사용 가능
        settings.setUseWideViewPort(true);
        settings.setLoadWithOverviewMode(true);

        //기본 웹뷰 세팅
        //메인 추가 웹뷰 세팅
        settings.setAllowFileAccess(true);//파일 엑세스
//        settings.setLoadWithOverviewMode(true);
//        settings.setAppCachePath(mainActivity.getApplicationContext().getCacheDir().getAbsolutePath());
//        settings.setPluginState(WebSettings.PluginState.ON);


//        mWebView.setDownloadListener(new DownloadListener() {
//            @Override
//            public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimeType, long contentLength) {
//                mWebView.loadUrl(JavaScriptInterface.getBase64StringFromBlobUrl(url));
//                Log.e("logURL", url);
//            }
//        });

        mWebView.setWebViewClient(new WishWebViewClient());
        mWebView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onShowFileChooser(WebView webView, ValueCallback filePathCallback, FileChooserParams fileChooserParams) {
                mFilePathCallback = filePathCallback;

                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("image/*");

                startActivityForResult(intent, 0);
                return true;
            }

            @Override
            public void onProgressChanged(WebView view, int progress) {
                progressBar.setProgress(progress);
                Log.e(TAG, "onProgressChanged: progress : " + progress );
                if (Get_Internet(MainActivity.this) > 0) {
                    if (progress == 100) {
//                        progressBar.setVisibility(View.GONE);
                        isWebLoaded = true;

                        //최초 웹페이지 스크립트를 호출한다. 토큰값 전달
                        androidBridge.connectAndroidApp();

                        if (isAniLoaded && isStartAction) {
                            fadeOutAndHideLinearLayout(ll_loading);
                        }
                    } else {
                        if (isStartAction) {
                            tv_loadPer.setText("(" + progress + " / 100)");
//                            progressBar.setVisibility(View.VISIBLE);
                        }
                    }
                } else {
                    tv_loadMsg.setText("인터넷에 연결되지 않았습니다.\n잠시후 다시 접속해주세요");
                }
            }

            @Override
            public boolean onCreateWindow(final WebView view, boolean dialog, boolean userGesture, Message resultMsg) {
                WebView newWebView = new WebView(MainActivity.this);
                WebView.WebViewTransport transport
                        = (WebView.WebViewTransport) resultMsg.obj;
                transport.setWebView(newWebView);
                resultMsg.sendToTarget();

                newWebView.setWebViewClient(new WebViewClient() {
                    @Override
                    public boolean shouldOverrideUrlLoading(WebView view, String url) {
                        Intent browserIntent = new Intent(MainActivity.this, PopViewActivity.class);
                        browserIntent.setData(Uri.parse(url));
                        browserIntent.putExtra("url", url);
                        MainActivity.this.startActivity(browserIntent);
                        return true;
                    }
                });

                return true;
            }

        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        Log.e("resultCode:: ", String.valueOf(resultCode));
        if (requestCode == 0 && resultCode == Activity.RESULT_OK) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                mFilePathCallback.onReceiveValue(WebChromeClient.FileChooserParams.parseResult(resultCode, data));
            } else {
                mFilePathCallback.onReceiveValue(new Uri[]{data.getData()});
            }
            mFilePathCallback = null;
        } else {
            mFilePathCallback.onReceiveValue(null);
        }
    }


    /**
     * 자바스크립트에서 호출하면 파일 다운로드시키는 함수
     *
     * @param url
     * @param dir
     */
    public void startDownload(String url, String dir, String fileName) {

//        File file = File(getExternalFilesDir(null), "dev_submit.mp4")

        File file = new File(getUrlDecode(dir));


        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE);
        request.setTitle(fileName);
        request.setDescription("Downloading file...");
        request.allowScanningByMediaScanner();
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setDestinationUri(Uri.fromFile(file));
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "" + System.currentTimeMillis());
        request.setAllowedOverMetered(true);
        request.setAllowedOverRoaming(true);
        DownloadManager manager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
        manager.enqueue(request);

        Toast.makeText(mainActivity, "파일 다운로드가 완료되었습니다.", Toast.LENGTH_SHORT).show();

    }


    public String getUrlDecode(String _strFileName) {
        String strRet = null;
        try {
            strRet = URLDecoder.decode(_strFileName, "UTF-8");
        } catch (UnsupportedEncodingException e) {

            strRet = "";
        }

        return strRet;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
//        progressBar.setVisibility(View.VISIBLE);
        //Log.e("webView", mWebView.getUrl());
        if (mWebView.getUrl().toLowerCase().contains("/mobile/index_m") && !mWebView.getUrl().toLowerCase().contains("#menu")) {
            //mWebView.clearCache(true);
            mWebView.clearHistory();
            Log.e("webView", "clearHistory");
        }
        if ((keyCode == KeyEvent.KEYCODE_BACK) && mWebView.canGoBack()) {
            mWebView.goBack();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    public class WishWebViewClient extends WebViewClient {
        public static final String INTENT_PROTOCOL_START = "intent:";
        public static final String INTENT_PROTOCOL_INTENT = "#Intent;";
        public static final String INTENT_PROTOCOL_END = ";end;";
        public static final String GOOGLE_PLAY_STORE_PREFIX = "market://details?id=";

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            if (url.startsWith(INTENT_PROTOCOL_START)) {
                final int customUrlStartIndex = INTENT_PROTOCOL_START.length();
                final int customUrlEndIndex = url.indexOf(INTENT_PROTOCOL_INTENT);
                if (customUrlEndIndex < 0) {
                    return false;
                } else {
                    final String customUrl = url.substring(customUrlStartIndex, customUrlEndIndex);
                    try {
                        mainActivity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(customUrl)));
                    } catch (ActivityNotFoundException e) {
                        final int packageStartIndex = customUrlEndIndex + INTENT_PROTOCOL_INTENT.length();
                        final int packageEndIndex = url.indexOf(INTENT_PROTOCOL_END);

                        final String packageName = url.substring(packageStartIndex, packageEndIndex < 0 ? url.length() : packageEndIndex);
                        mainActivity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(GOOGLE_PLAY_STORE_PREFIX + packageName)));
                    }
                    return true;
                }
            } else {
                view.loadUrl(url);
            }
//            progressBar.setVisibility(View.VISIBLE);

            return true;
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            Log.e(TAG, "onPageStarted: " );
            progressBar.setVisibility(View.VISIBLE);
            super.onPageStarted(view, url, favicon);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            Log.e(TAG, "onPageFinished: " );
            progressBar.setVisibility(View.GONE);
            super.onPageFinished(view, url);
        }

        @Override
        public void onReceivedError(WebView view, int errorCode,
                                    String description, String failingUrl) {
            super.onReceivedError(view, errorCode, description, failingUrl);

            switch (errorCode) {
                case ERROR_AUTHENTICATION: // 서버에서 사용자 인증 실패
                case ERROR_BAD_URL: // 잘못된 URL
                case ERROR_CONNECT: // 서버로 연결 실패
                case ERROR_FAILED_SSL_HANDSHAKE: // SSL handshake 수행 실패
                case ERROR_FILE: // 일반 파일 오류
                case ERROR_FILE_NOT_FOUND: // 파일을 찾을 수 없습니다
                case ERROR_HOST_LOOKUP: // 서버 또는 프록시 호스트 이름 조회 실패
                case ERROR_IO: // 서버에서 읽거나 서버로 쓰기 실패
                case ERROR_PROXY_AUTHENTICATION: // 프록시에서 사용자 인증 실패
                case ERROR_REDIRECT_LOOP: // 너무 많은 리디렉션
                case ERROR_TIMEOUT: // 연결 시간 초과
                case ERROR_TOO_MANY_REQUESTS: // 페이지 로드중 너무 많은 요청 발생
                case ERROR_UNKNOWN: // 일반 오류
                case ERROR_UNSUPPORTED_AUTH_SCHEME: // 지원되지 않는 인증 체계
                case ERROR_UNSUPPORTED_SCHEME:
                    mWebView.loadUrl("about:blank"); // 빈페이지 출력
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            finish();// 확인버튼 클릭시 이벤트
                        }
                    });
                    builder.setMessage("네트워크 상태가 원활하지 않습니다. 어플을 종료합니다.");
                    builder.setCancelable(false); // 뒤로가기 버튼 차단
                    builder.show(); // 다이얼로그실행
                    break;
            }
        }
    }

    @Override
    public void onBackPressed() {
        Toast toast = null;
//        if(Util.isSetting) {
//            Animation animationClose = AnimationUtils.loadAnimation(this, R.anim.fade_down);
//            ll_setting.startAnimation(animationClose);
//            ll_setting.setVisibility(View.GONE);
//            Util.isSetting = false;
//        }else {

        if (mWebView.canGoBack()) {
            mWebView.goBack();
        } else {
//            progressBar.setVisibility(View.GONE);
            if (System.currentTimeMillis() > backKeyPressedTime + 2000) {
                backKeyPressedTime = System.currentTimeMillis();
                toast = Toast.makeText(this, "\'뒤로\'버튼을 한번 더 누르시면 종료됩니다", Toast.LENGTH_SHORT);
                toast.show();
                return;
            }
            if (System.currentTimeMillis() <= backKeyPressedTime + 2000) {
                if (timer != null) {
                    timer.cancel();
                    timer.purge();
                }
                finish();
            }
        }
//        }
    }

    private void mainAnimationStart() {
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (isWebLoaded) {
                    fadeOutAndHideLinearLayout(ll_loading);
                }
            }
        }, 2500); //2500
        final Handler handler2 = new Handler();
        handler2.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!isWebLoaded) {
                    tv_loadMsg.setVisibility(View.VISIBLE);
                    if (Get_Internet(MainActivity.this) == 0) { //연결되지 않았다면
                        tv_loadMsg.setText("인터넷에 연결되지 않았습니다.");
                    }
                } else {
                    if (!isAniLoaded) {
                        fadeOutAndHideLinearLayout(ll_loading);
                    }

                }
            }
        }, 4500); //4500
        final Handler handler3 = new Handler();
        handler3.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!isWebLoaded) {
                    if (Get_Internet(MainActivity.this) == 0) { //연결되지 않았다면
                        tv_loadMsg.setText("인터넷에 연결되지 않았습니다.\n잠시후 다시 접속해주세요");
                    } else if (Get_Internet(MainActivity.this) == 1) { //wifi 연결
                        tv_loadMsg.setText("wifi 연결상태가 좋지 않습니다.");
                    } else if (Get_Internet(MainActivity.this) == 2) { //데이터 연결
                        tv_loadMsg.setText("데이터 연결상태가 좋지 않습니다.");
                    }
                    tv_loadPer.setVisibility(View.VISIBLE);
                } else {
                    if (!isAniLoaded) {
                        fadeOutAndHideLinearLayout(ll_loading);
                    }
                }
            }
        }, 8000); //8500
        final Handler handler4 = new Handler();
        handler4.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!isWebLoaded) {
                    if (Get_Internet(MainActivity.this) == 0) { //연결되지 않았다면
                        tv_loadMsg.setText("인터넷에 연결되지 않았습니다.\n잠시후 다시 접속해주세요");
                    } else if (Get_Internet(MainActivity.this) == 1) { //wifi 연결
                        tv_loadMsg.setText("wifi 연결상태가 좋지 않습니다.\n잠시후 다시 접속해주세요");
                    } else if (Get_Internet(MainActivity.this) == 2) { //데이터 연결
                        tv_loadMsg.setText("데이터 연결상태가 좋지 않습니다.\n잠시후 다시 접속해주세요");
                    }
                    tv_loadPer.setVisibility(View.VISIBLE);
                } else {
                    if (!isAniLoaded) {
                        fadeOutAndHideLinearLayout(ll_loading);
                    }
                }
                //지연시키길 원하는 밀리초 뒤에 동작
            }
        }, 15000); //8500
    }

    private void fadeOutAndHideLinearLayout(final LinearLayout linearLayout) {
        isAniLoaded = true;
        isStartAction = false;
        Animation fadeOut = new AlphaAnimation(1, 0);
        fadeOut.setInterpolator(new AccelerateInterpolator());
        fadeOut.setDuration(1000);

        fadeOut.setAnimationListener(new Animation.AnimationListener() {
            public void onAnimationEnd(Animation animation) {
                linearLayout.setVisibility(View.GONE);
            }

            public void onAnimationRepeat(Animation animation) {
            }

            public void onAnimationStart(Animation animation) {
            }
        });

        linearLayout.startAnimation(fadeOut);
    }

    private void fadeInAndShowImageView(final ImageView iv) {
        Animation fadeIn = new AlphaAnimation(0, 1);
        fadeIn.setInterpolator(new AccelerateInterpolator());
        fadeIn.setDuration(600);

        fadeIn.setAnimationListener(new Animation.AnimationListener() {
            public void onAnimationEnd(Animation animation) {
                iv.setVisibility(View.VISIBLE);
            }

            public void onAnimationRepeat(Animation animation) {
            }

            public void onAnimationStart(Animation animation) {
            }
        });

        iv.startAnimation(fadeIn);
    }

    /*public static void fadeOutBottomMenu()
    {
        Animation fadeOut = new AlphaAnimation(1, 0);
        fadeOut.setInterpolator(new AccelerateInterpolator());
        fadeOut.setDuration(100);

        fadeOut.setAnimationListener(new Animation.AnimationListener()
        {
            public void onAnimationEnd(Animation animation)
            {
                rl_bottomMenu.setVisibility(View.GONE);
            }
            public void onAnimationRepeat(Animation animation) {}
            public void onAnimationStart(Animation animation) {}
        });

        ll_setting.setVisibility(View.GONE);
        Util.isSetting = false;
        rl_bottomMenu.startAnimation(fadeOut);
    }*/
    /*public static void fadeInBottomMenu() {
        Animation fadeIn = new AlphaAnimation(0, 1);
        fadeIn.setInterpolator(new AccelerateInterpolator());
        fadeIn.setDuration(600);

        fadeIn.setAnimationListener(new Animation.AnimationListener()
        {
            public void onAnimationEnd(Animation animation)
            {
                rl_bottomMenu.setVisibility(View.VISIBLE);
            }
            public void onAnimationRepeat(Animation animation) {}
            public void onAnimationStart(Animation animation) {}
        });

        rl_bottomMenu.startAnimation(fadeIn);
    }*/
    /*
Get_Internet
: 인터넷 연결환경에 대해 체크한다.
0을 리턴할 경우, 인터넷 연결끊김
1을 리턴할 경우, 와이파이 연결상태
2를 연결할 경우, 인터넷 연결상태
 */
    public static int Get_Internet(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        if (activeNetwork != null) {
            if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI) {
                return 1;
            } else if (activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE) {
                return 2;
            }
        }
        return 0;
    }

    public void permissionCheck() {


        if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            // 1. 카메라 퍼미션과 외부 저장소 퍼미션을 가지고 있는지 체크합니다.
            int cameraPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
            int writeExternalStoragePermission = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
            int readExternalStoragePermission = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
            if (cameraPermission == PackageManager.PERMISSION_GRANTED
                    && writeExternalStoragePermission == PackageManager.PERMISSION_GRANTED && readExternalStoragePermission == PackageManager.PERMISSION_GRANTED) {

                // 2. 이미 퍼미션을 가지고 있다면
                // ( 안드로이드 6.0 이하 버전은 런타임 퍼미션이 필요없기 때문에 이미 허용된 걸로 인식합니다.)
                mainAnimationStart();

            } else {  //2. 퍼미션 요청을 허용한 적이 없다면 퍼미션 요청이 필요합니다. 2가지 경우(3-1, 4-1)가 있습니다.

                // 3-1. 사용자가 퍼미션 거부를 한 적이 있는 경우에는
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[0])
                        || ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[1])) {

                    // 3-2. 요청을 진행하기 전에 사용자가에게 퍼미션이 필요한 이유를 설명해줄 필요가 있습니다.
                    AlertDialog.Builder dialog = new AlertDialog.Builder(mainActivity);
                    dialog.setTitle("권한 설정")
                            .setMessage("이 앱을 실행하려면 카메라와 외부 저장소 접근 권한이 필요합니다.")
                            .setPositiveButton("다음", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    // 3-3. 사용자게에 퍼미션 요청을 합니다. 요청 결과는 onRequestPermissionResult에서 수신됩니다.
                                    ActivityCompat.requestPermissions(MainActivity.this, REQUIRED_PERMISSIONS,
                                            PERMISSIONS_REQUEST_CODE);
                                }
                            }).create().show();
                } else {
                    // 4-1. 사용자가 퍼미션 거부를 한 적이 없는 경우에는 퍼미션 요청을 바로 합니다.
                    // 요청 결과는 onRequestPermissionResult에서 수신됩니다.
                    ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS,
                            PERMISSIONS_REQUEST_CODE);
                }
            }
        } else {

            final Snackbar snackbar = Snackbar.make(mLayout, "디바이스가 카메라를 지원하지 않습니다.",
                    Snackbar.LENGTH_INDEFINITE);
            snackbar.setAction("확인", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    snackbar.dismiss();
                }
            });
            snackbar.show();
        }


    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grandResults) {

        Log.e(TAG, "onRequestPermissionsResult: ");
        if (requestCode == PERMISSIONS_REQUEST_CODE && grandResults.length == REQUIRED_PERMISSIONS.length) {

            // 요청 코드가 PERMISSIONS_REQUEST_CODE 이고, 요청한 퍼미션 개수만큼 수신되었다면

            boolean check_result = true;

            // 모든 퍼미션을 허용했는지 체크합니다.
            for (int result : grandResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    check_result = false;
                    break;
                }
            }

            if (check_result) {

                // 모든 퍼미션을 허용했다면 카메라 프리뷰를 시작합니다.

                mainAnimationStart();
            } else {
                // 거부한 퍼미션이 있다면 앱을 사용할 수 없는 이유를 설명해주고 앱을 종료합니다.2 가지 경우가 있습니다.

                if (ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[0])
                        || ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[1])) {


                    // 사용자가 거부만 선택한 경우에는 앱을 다시 실행하여 허용을 선택하면 앱을 사용할 수 있습니다.
                    AlertDialog.Builder dialog = new AlertDialog.Builder(mainActivity);
                    dialog.setTitle("권한알림")
                            .setMessage("권한이 거부되었습니다. 앱을 다시 실행하여 권한을 허용해주세요 ")
                            .setPositiveButton("네", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    finish();
                                }
                            }).create().show();
//                    Snackbar.make(mLayout, "퍼미션이 거부되었습니다. 앱을 다시 실행하여 퍼미션을 허용해주세요. ",
//                            Snackbar.LENGTH_INDEFINITE).setAction("확인", new View.OnClickListener() {
//
//                        @Override
//                        public void onClick(View view) {
//
//
//                        }
//                    }).show();

                } else {


                    // “다시 묻지 않음”을 사용자가 체크하고 거부를 선택한 경우에는 설정(앱 정보)에서 퍼미션을 허용해야 앱을 사용할 수 있습니다.
                    AlertDialog.Builder dialog = new AlertDialog.Builder(mainActivity);
                    dialog.setTitle("권한알림")
                            .setMessage("권한이 거부되었습니다. 설정(앱 정보)에서 퍼미션을 허용해야 합니다. ")
                            .setPositiveButton("네", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    finish();
                                }
                            }).create().show();
                }
            }

        }


    }


}
