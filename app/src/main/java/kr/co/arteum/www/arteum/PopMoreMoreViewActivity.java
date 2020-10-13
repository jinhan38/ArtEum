package kr.co.arteum.www.arteum;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

public class PopMoreMoreViewActivity extends Activity {
    WebView pmmWebView;
    ProgressBar progressBar;
    public static PopMoreMoreViewActivity openerMoreMoreActivity;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pop_more_more_webview);
        Util.CURRENT_CONTEXT = this;
        this.openerMoreMoreActivity = PopMoreMoreViewActivity.this;
        Intent intent = getIntent();
        String newURL = intent.getExtras().getString("url");
        pmmWebView = findViewById(R.id.moremorePopView);

        progressBar = this.findViewById(R.id.progress4);

        browserSettings();

        pmmWebView.loadUrl(newURL);//
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Log.e("key", "백");
        progressBar.setVisibility(View.VISIBLE);
        if((keyCode == KeyEvent.KEYCODE_BACK) && pmmWebView.canGoBack()){
            pmmWebView.goBack();
            Log.e("key", "백2");
            return true;
        }else if(!pmmWebView.canGoBack()) {
            finish();
        }
        return super.onKeyDown(keyCode, event);
    }

    public class WishWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url){
            view.loadUrl(url);
            progressBar.setVisibility(View.VISIBLE);
            return true;
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
                    pmmWebView.loadUrl("about:blank"); // 빈페이지 출력
                    AlertDialog.Builder builder = new AlertDialog.Builder(PopMoreMoreViewActivity.this);
                    builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            finish();// 확인버튼 클릭시 이벤트
                        }});
                    builder.setMessage("네트워크 상태가 원활하지 않습니다. 어플을 종료합니다.");
                    builder.setCancelable(false); // 뒤로가기 버튼 차단
                    builder.show(); // 다이얼로그실행
                    break;
            }
        }
    }

    private void browserSettings(){
        pmmWebView.setBackgroundColor(0); //배경색
        pmmWebView.setHorizontalScrollBarEnabled(false); //가로 스크롤
        pmmWebView.setVerticalScrollBarEnabled(false); //세로 스크롤

        //mWebView.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY); //스크롤 노출타입
        if(Build.VERSION.SDK_INT >= 21) {
            pmmWebView.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }
        WebSettings settings = pmmWebView.getSettings();
        settings.setJavaScriptEnabled(true); //javascript 허용
        settings.setSupportMultipleWindows(true); //다중웹뷰 허용
        settings.setJavaScriptCanOpenWindowsAutomatically(true);//javascript의 window.open 허용

        //HTML을 파싱하여 웹뷰에서 보여주거나 하는 작업에서 width , height 가 화면 크기와 맞지 않는 현상이 발생한다
        //이를 잡아주기 위한 코드
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
        pmmWebView.setWebViewClient(new WishWebViewClient());
        pmmWebView.setWebChromeClient(new WebChromeClient() {
            public void onProgressChanged(WebView view, int progress) {
                progressBar.setProgress(progress);
                if (progress == 100) {
                    progressBar.setVisibility(View.GONE);

                } else {
                    progressBar.setVisibility(View.VISIBLE);

                }
            }
        });

        // Bridge 인스턴스 등록
        pmmWebView.addJavascriptInterface(new AndroidBridge(pmmWebView, 3), "HybridApp");
    }
}
