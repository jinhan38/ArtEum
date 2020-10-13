package kr.co.arteum.www.arteum;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class OldDownloadManager extends Thread
{
    public static final int HANDLE_START_DOWNLOAD = 1;
    public static final int HANDLE_END_DOWNLOAD = 2;
    public static final int HANDLE_FAIL_DOWNLOAD = 3;


    private static OldDownloadManager m_oInstance = null;
    private static Object m_oSync = new Object();
    private String m_oStrPath = "";
    private LinkedBlockingQueue<String> m_qUrl;
    private boolean m_bIsThreadStoped = false;
    private Thread m_pThread = null;
    private Context m_oContext = null;
    private boolean m_isDownload = false;

    public File oFile;

    public static OldDownloadManager getInstance() {
        synchronized (m_oSync) {

            return m_oInstance;
        }
    }

    public OldDownloadManager(Context _oContext)
    {
        m_oContext = _oContext;
        m_oInstance = this;

        m_qUrl = new LinkedBlockingQueue<String>(5);
        m_bIsThreadStoped = false;
        m_pThread = new Thread(this);
        m_pThread.start();

    }

    public void free()
    {
        m_bIsThreadStoped = true;
        for (int i=0; i<10; ++i)
        {
            try {
                m_pThread.join(1000);
            } catch (InterruptedException e) {

                break;
            }
            if (!m_pThread.isAlive())
            {
                break;
            }
        }
        m_pThread = null;

        if (m_qUrl != null)
        {
            m_qUrl.clear();
            m_qUrl = null;
        }
        m_oSync = null;
        m_oInstance = null;
    }

    // 경로 지정.
    public void setSavePath(String _strPath)
    {
        m_oStrPath = _strPath;
    }

    // URL 주소에서 파일명 가져오기
    protected String getFileName(String _strUrl)
    {
//        String[] strData = _strUrl.split("/");
//
//        if (strData.length == 0)
//            return "";
//
//        return strData[strData.length - 1];
        return Util.DOWNLOAD_FILE_NAME;
    }

    // 다운로드 하려는 Url 지정 (여러개 한번에 지정)
    public void setDownloadUrl(String[] _strUrl)
    {
        try
        {
            for (int i=0; i<_strUrl.length; ++i)
            {
                m_qUrl.offer(_strUrl[i], 100, TimeUnit.MILLISECONDS);
            }
            mmHandler.sendEmptyMessage(OldDownloadManager.HANDLE_START_DOWNLOAD);
        }
        catch (InterruptedException e)
        {
            Log.e("DownloadManager", "setDownloadUrl : Queue Offer Fail!!!");
        }
    }

    // 다운로드 하려는 Url 지정.
    public void setDownloadUrl(String _strUrl)
    {
        try
        {
            m_qUrl.offer(_strUrl, 100, TimeUnit.MILLISECONDS);
            mmHandler.sendEmptyMessage(OldDownloadManager.HANDLE_START_DOWNLOAD);
            Log.e("DownloadManager", "setDownloadUrl : " + _strUrl);
        }
        catch (InterruptedException e)
        {
            Log.e("DownloadManager", "setDownloadUrl : Queue Offer Fail!!!");
        }
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

    // 다운로드 하려는 경로 확인 후 없으면 생성..
    private void checkDir()
    {
        File oFile = new File(getUrlDecode(m_oStrPath));
        Log.e("checkDir", "checkDir : " + m_oStrPath);
        if (!oFile.exists()) {
            oFile.mkdirs();

            Log.e("mkdirs", "mkdirs : " + m_oStrPath);
        }
    }

    @Override
    public void run() {
        URL oUrl;
        int nRead;
        String strUrl;
        HttpURLConnection oConn;
        int nLen;
        byte[] byTmpByte;
        InputStream oIs;
        FileOutputStream oFos;

        while(!m_bIsThreadStoped)
        {
            try
            {
                strUrl = m_qUrl.poll(500, TimeUnit.MILLISECONDS);
            }
            catch (InterruptedException e)
            {
                strUrl = null;
                e.printStackTrace();
            }

            if (strUrl != null && strUrl.length() > 0)
            {
                // 다운로드 생성..
                try {
                    checkDir();
                    oUrl = new URL(strUrl);
                    oConn = (HttpURLConnection) oUrl.openConnection();
                    nLen = oConn.getContentLength();
                    if (nLen == -1) // 사이즈를 얻어오지 못한다면 버퍼 크기를 임의로 지정.
                        nLen = 1000000;

                    byTmpByte = new byte[nLen];
                    oIs = oConn.getInputStream();
                    oFile = new File(getUrlDecode(m_oStrPath+"/"+getFileName(strUrl)));
                    oFos = new FileOutputStream(oFile);

                    for (;;)
                    {
                        nRead = oIs.read(byTmpByte);

                        if (nRead <= 0)
                            break;

                        oFos.write(byTmpByte, 0, nRead);
                    }

                    oIs.close();
                    oFos.close();
                    oConn.disconnect();

                } catch (MalformedURLException e1) {
                    Log.e("DownloadManager", e1.getMessage());
                    mmHandler.sendEmptyMessage(OldDownloadManager.HANDLE_FAIL_DOWNLOAD);
                } catch (IOException e2) {
                    Log.e("DownloadManager", e2.getMessage());
                    mmHandler.sendEmptyMessage(OldDownloadManager.HANDLE_FAIL_DOWNLOAD);
                }
            }
            else if (m_isDownload)
            {
                mmHandler.sendEmptyMessage(OldDownloadManager.HANDLE_END_DOWNLOAD);
            }

            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                Log.e("DownloadManager", e.getMessage());
            }
        }
    }

    @SuppressLint("HandlerLeak")
    public Handler mmHandler = new Handler() {
        public void handleMessage(Message msg) {

            switch (msg.what)
            {
                case HANDLE_START_DOWNLOAD:
                    m_isDownload = true;
                    Toast.makeText (m_oContext, "다운로드를 시작합니다.", Toast.LENGTH_SHORT).show();

                    break;

                case HANDLE_END_DOWNLOAD:
                    m_isDownload = false;
                    Toast.makeText (m_oContext, "다운로드를 완료하였습니다.", Toast.LENGTH_SHORT).show();

//                    showDocumentFile();


//                    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
//                    Uri uri = Uri.parse(m_oStrPath);
//                    intent.setDataAndType(uri,"*/*"); //여러가지 Type은 아래 표로 정리해두었습니다.
//                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                    m_oContext.startActivity(Intent.createChooser(intent, "Open"));

/*                    Intent intent = new Intent();
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.setAction(android.content.Intent.ACTION_VIEW);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    if (oFile.getName().endsWith(".pdf")){
                        intent.setDataAndType(Uri.fromFile(oFile), "application/pdf");
                    }else if (oFile.getName().endsWith(".hwp")){
                        intent.setDataAndType(Uri.fromFile(oFile), "application/hwp");
                    }

                    try{
                        m_oContext.startActivity(intent);
                    }catch(ActivityNotFoundException e){
                        Toast.makeText(m_oContext, "해당파일을 실행할 수 있는 어플리케이션이 없습니다.\n파일을 열 수 없습니다.", Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }*/


                    break;

                case HANDLE_FAIL_DOWNLOAD:
                    m_isDownload = false;
                    Toast.makeText (m_oContext, "다운로드를 실패하였습니다.", Toast.LENGTH_SHORT).show();

                    break;
            }
        }
    };
    public void showDocumentFile()
    {

        Intent intent = new Intent(Intent.ACTION_VIEW);
        String _strFileName = Util.DOWNLOAD_FILE_NAME.toLowerCase().trim();
        Uri uri = FileProvider.getUriForFile(m_oContext,
                BuildConfig.APPLICATION_ID + ".provider",
                oFile);
        //Uri uri = Uri.fromFile(oFile);
//        Uri uri;
//        if (Build.VERSION.SDK_INT < 24) {
//            uri = Uri.fromFile(oFile);
//        } else {
//            uri = Uri.parse(oFile.getPath()); // My work-around for new SDKs, causes ActivityNotFoundException in API 10.
//        }

        // 파일 확장자별 Mime Type을 지정한다.
        boolean fileOpen = true;
        if (_strFileName.endsWith(".mp3"))
        {
            intent.setDataAndType(uri, "audio/*");
        }
        else if (_strFileName.endsWith(".mp4"))
        {
            intent.setDataAndType(uri, "vidio/*");
        }
        else if (_strFileName.endsWith(".jpg") || _strFileName.endsWith(".jpeg") ||
                _strFileName.endsWith(".JPG") || _strFileName.endsWith(".gif") ||
                _strFileName.endsWith(".png") || _strFileName.endsWith(".bmp"))
        {
            intent.setDataAndType(uri, "image/*");
        }
        else if (_strFileName.endsWith(".txt"))
        {
            intent.setDataAndType(uri, "text/*");
        }
        else if (_strFileName.endsWith(".doc") || _strFileName.endsWith(".docx"))
        {
            intent.setDataAndType(uri, "application/msword");
        }
        else if (_strFileName.endsWith(".xls") || _strFileName.endsWith(".xlsx"))
        {
            intent.setDataAndType(uri,
                    "application/vnd.ms-excel");
        }
        else if (_strFileName.endsWith(".ppt") || _strFileName.endsWith(".pptx")) {
            intent.setDataAndType(uri,"application/vnd.ms-powerpoint");
        }
        else if (_strFileName.endsWith(".pdf")) {
            intent.setDataAndType(uri, "application/pdf");
        }
        else if (_strFileName.endsWith(".hwp")) {
            intent.setDataAndType(uri, "application/x-hwp");
        }
        else {
            fileOpen = false;
            Toast.makeText(m_oContext, _strFileName, Toast.LENGTH_SHORT).show();
        }
        if (fileOpen) {
            Log.e("WebView", oFile.getPath());
            intent.putExtra(Intent.EXTRA_STREAM, uri);
//            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            m_oContext.startActivity(intent);

        }
    }
}


