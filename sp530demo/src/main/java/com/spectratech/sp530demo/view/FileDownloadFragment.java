package com.spectratech.sp530demo.view;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewAnimator;

import com.spectratech.lib.Callback;
import com.spectratech.lib.Logger;
import com.spectratech.lib.ResourcesHelper;
import com.spectratech.lib.StringHelper;
import com.spectratech.sp530demo.DemoMainActivity;
import com.spectratech.sp530demo.R;
import com.spectratech.sp530demo.data.Data_S3INS_response;
import com.spectratech.sp530demo.sp530class.SP530_bt_S3INS_FILEDL;
import com.spectratech.spectratechlib_ftp.FtpClientHelper;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * FileDownloadFragment - Data file download fragment class
 */
public class FileDownloadFragment extends Fragment {

    private static final String m_className="FileDownloadFragment";

    private static final DateFormat timeStampFmt = new SimpleDateFormat("HH:mm:ss.SSS  ", Locale.US);

    private static int REQUEST_DATA_FILE_SELECT_CODE=2001;

    private final int MSG_HANDLER_TOASTMSG_UI=1102;
    private final int MSG_HANDLER_UPDATE_LOG_UI_BTN_OK=1106;
    private final int MSG_HANDLER_SEND_LOGDOWNLOADMSG=1108;
    private final int MSG_HANDLER_SEND_BRIEFDOWNLOADMSG=1109;
    private final int MSG_HANDLER_UPDATE_TMSDLPROGRESS_UI_BTN_OK=1110;
    private final int MSG_HANDLER_UPDATE_TMSDLPROGRESS_UI_PROGRESSMSG=1111;
    private final int MSG_HANDLER_UPDATE_TMSDLPROGRESS_UI_PROGRESSBAR_VISIBILITY=1112;

    private Activity m_activity;
    private Context m_context;

    private Handler m_handler;

    private Toast m_toast;

    private LinearLayout m_mainll;

    private ViewAnimator m_views_viewanimator;

    private TextView m_tv_file_path;
    private TextView m_tv_file_size;

    // tms download
    private Button m_btn_tmsfiles_download;

    // transfer tms to sp530
    private Button m_btn_transfertms2device;

    // brief log
    private ScrollView m_sv_brief_log;
    private TextView m_tv_brief_log;

    private ViewTMSDownloadFragmentLogLinearLayout m_viewFragmentLogll;

    private TMSDlProgressOverlayFragment m_frag_tmsDlProgressOverlay;

    private Uri m_uri_file;

    private void initHandler() {
        final String strOK= ResourcesHelper.getBaseContextString(m_context, R.string.ok);

        m_handler=new Handler() {
            public void handleMessage(Message msg) {
                //Logger.i(m_className, "Handler message: "+msg.what);
                switch (msg.what) {
                    case MSG_HANDLER_TOASTMSG_UI: {
                        String strMsg=(String)msg.obj;
                        ToastMessage(strMsg);
                    }
                    break;
                    case MSG_HANDLER_UPDATE_LOG_UI_BTN_OK: {
                        m_viewFragmentLogll.setButtonText(strOK);

                        DemoMainActivity act=(DemoMainActivity)m_context;
                        act.setLockActionBarVisibilityStatus(false);
                        act.setActionBarShow();
                        act.setLockActionBarVisibilityStatus(true);
                    }
                    break;
                    case MSG_HANDLER_SEND_LOGDOWNLOADMSG: {
                        String strLogDownload = (String) msg.obj;
                        if (m_viewFragmentLogll!=null) {
                            m_viewFragmentLogll.log_noRunnable("DOWNLOAD", strLogDownload);
                        }
                    }
                    break;
                    case MSG_HANDLER_SEND_BRIEFDOWNLOADMSG: {
                        String strBriefDownloadMsg=(String)msg.obj;
                        if (m_tv_brief_log!=null) {
                            log_briefDownloadMessage(strBriefDownloadMsg);
                        }
                    }
                    break;

                    case MSG_HANDLER_UPDATE_TMSDLPROGRESS_UI_BTN_OK: {
                        if (m_frag_tmsDlProgressOverlay!=null) {
                            m_frag_tmsDlProgressOverlay.setButtonText(strOK);
                        }
                    }
                    break;
                    case MSG_HANDLER_UPDATE_TMSDLPROGRESS_UI_PROGRESSMSG: {
                        if (m_frag_tmsDlProgressOverlay!=null) {
                            String strMsg=(String)msg.obj;
                            m_frag_tmsDlProgressOverlay.setText(strMsg);
                        }
                    }
                    break;
                    case MSG_HANDLER_UPDATE_TMSDLPROGRESS_UI_PROGRESSBAR_VISIBILITY: {
                        if (m_frag_tmsDlProgressOverlay!=null) {
                            boolean flag=(Boolean)msg.obj;
                            m_frag_tmsDlProgressOverlay.showProgressBar(flag);
                        }
                    }
                    break;
                }
            }
        };
    }

    public void log_briefDownloadMessage(String tag, Object... messageFragments) {
        Logger.i(m_className, "log_briefDownloadMessage");
        StringBuilder message = new StringBuilder();
        for (Object fragment : messageFragments) {
            message.append(fragment.toString());
        }
        String text = message.toString();

        StringBuilder logCache = new StringBuilder();

        logCache.append(timeStampFmt.format(new Date())).append(tag).append(" ").append(text).append('\n');

        if (m_tv_brief_log != null) {
            if (m_tv_brief_log.getText().length() > 4 * 1024) {
                m_tv_brief_log.setText(logCache);
            }
            else {
                m_tv_brief_log.append(logCache);
            }
            if (m_sv_brief_log != null) {
                m_sv_brief_log.scrollTo(0, m_tv_brief_log.getHeight());
            }
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        m_activity=getActivity();
        m_context=(Context)m_activity;

        initHandler();

        m_frag_tmsDlProgressOverlay=null;

        m_uri_file=null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        m_mainll=(LinearLayout) inflater.inflate(R.layout.view_filedownload, null);
        return m_mainll;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        final String strPleaseWait=ResourcesHelper.getBaseContextString(m_context, R.string.please_wait);
        final String strCancel=ResourcesHelper.getBaseContextString(m_context, R.string.cancel);
        final String strCanceling=ResourcesHelper.getBaseContextString(m_context, R.string.canceling);

        m_views_viewanimator=(ViewAnimator)m_mainll.findViewById(R.id.views_viewanimator);

        // first
        m_tv_file_path=(TextView)m_mainll.findViewById(R.id.tv_file_path);
        m_tv_file_size=(TextView)m_mainll.findViewById(R.id.tv_file_size);
        setUI_fileInfo();

        m_btn_tmsfiles_download=(Button)m_mainll.findViewById(R.id.btn_tmsfiles_download);
        {
            View.OnClickListener listener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showFileChooser();
                }
            };
            m_btn_tmsfiles_download.setOnClickListener(listener);
        }

        m_btn_transfertms2device=(Button)m_mainll.findViewById(R.id.btn_transfertms2device);
        {
            View.OnClickListener listener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startTransferFile2SP530();
                }
            };
            m_btn_transfertms2device.setOnClickListener(listener);
        }


        // second
        m_viewFragmentLogll=(ViewTMSDownloadFragmentLogLinearLayout)m_mainll.findViewById(R.id.viewfragmentlogll);
        m_viewFragmentLogll.initUIs();
        {
            View.OnClickListener listener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String strText=((Button)v).getText().toString();
                    if (strText.equals(strCancel)) {
                        FtpClientHelper instFtpClientHelper= FtpClientHelper.getInstance();
                        instFtpClientHelper.cancelDownloadThread();

                        String strTextNext=strCanceling;
                        ToastMessage(strTextNext);
                        m_viewFragmentLogll.setButtonText(strTextNext);
                        m_handler.sendEmptyMessageDelayed(MSG_HANDLER_UPDATE_LOG_UI_BTN_OK, 2 * 1000);
                        return;
                    }
                    else if (strText.equals(strCanceling)) {
                        ToastMessage(strCanceling+", "+strPleaseWait);
                        return;
                    }
                    m_views_viewanimator.setDisplayedChild(0);
                }
            };
            m_viewFragmentLogll.setButtonOnClickListener(listener);
        }


        // brief log
        m_sv_brief_log=(ScrollView)m_mainll.findViewById(R.id.sv_brief_log);
        m_tv_brief_log=(TextView)m_mainll.findViewById(R.id.tv_brief_log);

        // focus me please
        m_mainll.setFocusable(true);
        m_mainll.setFocusableInTouchMode(true);
        m_mainll.requestFocus();
    }

    private void setUI_fileInfo() {
        setUI_filePath();
        setUI_fileSize();
    }
    private void setUI_fileSize() {
        int size = -1;
        if (m_uri_file!=null) {
            InputStream is=null;
            try {
                is = m_context.getContentResolver().openInputStream(m_uri_file);
            }
            catch (FileNotFoundException fnex) {
                Logger.w(m_className, "setUI_fileSize, fnex: " + fnex.toString());
                is=null;
            }

            if (is!=null) {
                try {
                    size = is.available();
                }
                catch (IOException ioex) {
                    Logger.w(m_className, "setUI_fileSize, ioex: " + ioex.toString());
                    size=-1;
                }
            }

        }
        String strText="";
        if (size>-1) {
            strText=""+size;
            StringHelper instStringHelper=StringHelper.getInstance();
            strText=instStringHelper.addPaddingStartingFromTail(",", strText, 3, false);
            strText="file size: "+strText+" byte";
        }
        m_tv_file_size.setText(strText);
    }
    private void setUI_filePath() {
        String path = "";
        if (m_uri_file!=null) {
            path=m_uri_file.toString();
        }
        m_tv_file_path.setText(path);
    }

    private void startTransferFile2SP530() {
        String strSelectFile=ResourcesHelper.getBaseContextString(m_context, R.string.please_select_afile);
        if (m_uri_file==null) {
            String strMsg=strSelectFile;
            if (m_context instanceof DemoMainActivity) {
                DemoMainActivity act = (DemoMainActivity) m_context;
                act.showMessageDialog(m_context, strMsg);
            }
            else {
                Toast.makeText(m_context, strMsg, Toast.LENGTH_SHORT).show();
            }
            return;
        }
        startFile2SP530Event(0);
    }

    private void startFile2SP530Event(final int idx) {
        String strCancel=ResourcesHelper.getBaseContextString(m_context, R.string.cancel);
        if (m_context instanceof DemoMainActivity) {
            DemoMainActivity act = (DemoMainActivity) m_context;
            boolean bCommandValid2Start=act.isCommandValid2Start(false);
            if (!bCommandValid2Start) {
                return;
            }
        }

        if ( (idx!=0) ) {
            Logger.w(m_className, "startFile2SP530Event, invalid idx: "+idx);
            return;
        }
        if (m_context instanceof DemoMainActivity) {
            DemoMainActivity act = (DemoMainActivity) m_context;
            Callback<Object> cb_finish=new Callback<Object>() {
                @Override
                public Object call() throws Exception {
                    Logger.i(m_className, "startFile2SP530Event, idx: "+idx+", cb_finish call");
                    Object obj=this.getParameter();

                    if (obj==null) {
                        Logger.i(m_className, "startFile2SP530Event, idx: "+idx+", obj is null");
                    }
                    else {
                        String strMsg;
                        boolean bValidResult = SP530_bt_S3INS_FILEDL.isResponseResultValid(obj);
                        if (bValidResult) {
                        }
                        else {
                            strMsg = "ERROR";
                            Data_S3INS_response dataS3INSResponse=(Data_S3INS_response)obj;
                            if ((dataS3INSResponse.m_data != null) && (dataS3INSResponse.m_data.length > 1)) {
                                byte val = dataS3INSResponse.m_data[1];
                                String strRet= SP530_bt_S3INS_FILEDL.getErrorCodeString(val);
                                strMsg += " ("+strRet+")";
                            }
                            else {
                                String strStatus=SP530_bt_S3INS_FILEDL.getTransStatusString(obj);
                                strMsg += ", "+strStatus;
                            }
                            publishProgressMessage(strMsg);
                        }
                    }

                    publishShowProgressBar(false);
                    m_handler.sendEmptyMessage(MSG_HANDLER_UPDATE_TMSDLPROGRESS_UI_BTN_OK);
                    return null;
                };
            };

            Callback<Object> cb_progress=new Callback<Object>() {
                @Override
                public Object call() throws Exception {
                    //Logger.i(m_className, "startFile2SP530Event, cb_progress call");
                    String strMsg=(String)this.getParameter();
                    publishProgressMessage(strMsg);
                    return null;
                };
            };

            if (m_frag_tmsDlProgressOverlay==null) {
                Logger.i(m_className, "startFile2SP530Event, m_frag_tmsDlProgressOverlay initial");
                m_frag_tmsDlProgressOverlay=new TMSDlProgressOverlayFragment();
            }
            else {
                m_frag_tmsDlProgressOverlay.setButtonText(strCancel);
                publishShowProgressBar(true);
            }
            act.setOverlayFragment(m_frag_tmsDlProgressOverlay);
            act.disableAllUIInputs();

            // clear progress message
            publishProgressMessage("");

            if (idx==0) {
                act.startTransferFile(m_uri_file, cb_finish, cb_progress);
            }
        }
    }

    private void publishShowProgressBar(boolean flag) {
        Message msg=m_handler.obtainMessage(MSG_HANDLER_UPDATE_TMSDLPROGRESS_UI_PROGRESSBAR_VISIBILITY);
        msg.obj=flag;
        m_handler.sendMessage(msg);
    }

    private void publishProgressMessage(String strMsg) {
        Message msg = m_handler.obtainMessage(MSG_HANDLER_UPDATE_TMSDLPROGRESS_UI_PROGRESSMSG);
        msg.obj = strMsg;
        m_handler.sendMessage(msg);
    }

    private void ToastBySendMessage(String strMsg) {
        Message msg = m_handler.obtainMessage(MSG_HANDLER_TOASTMSG_UI);
        msg.obj = strMsg;
        m_handler.sendMessage(msg);
    }

    private void ToastMessage(String strMessage) {
        if (m_toast!=null) {
            m_toast.cancel();
            m_toast=null;
        }
        m_toast=Toast.makeText(m_context, strMessage, Toast.LENGTH_SHORT);
        m_toast.show();
    }

    private void sendBriefDownloadMessage(String strMsg) {
        Message msg=m_handler.obtainMessage(MSG_HANDLER_SEND_BRIEFDOWNLOADMSG);
        msg.obj=strMsg;
        m_handler.sendMessage(msg);
    }

    private void sendLogDownloadMessage(String strMsg) {
        Message msg=m_handler.obtainMessage(MSG_HANDLER_SEND_LOGDOWNLOADMSG);
        msg.obj=strMsg;
        m_handler.sendMessage(msg);
    }

    private void showFileChooser() {
        final String strPleaseInstallAFileMan=ResourcesHelper.getBaseContextString(m_context, R.string.please_install_afile_manager);
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        try {
            int reqCode=REQUEST_DATA_FILE_SELECT_CODE;
            startActivityForResult(Intent.createChooser(intent, "Select a File"), reqCode);
        } catch (android.content.ActivityNotFoundException ex) {
            ToastMessage(strPleaseInstallAFileMan);
        }
    }

    public static Uri filepath2uri(String strDir, String fname) {
        Uri uri_file=null;
        File f=new File(strDir, fname);
        if (f!=null) {
            if (f.exists()&&f.isFile()) {
                uri_file=Uri.fromFile(f);
            }
        }
        if (uri_file==null) {
            Logger.w(m_className, "filepath2uri, uri_file is null, strDir: "+strDir+", fname: "+fname);
        }
        return uri_file;
    }

    private void onFileLocated(Intent data) {
        Logger.i(m_className, "onFileLocated");

        // Get the Uri of the selected file
        m_uri_file = data.getData();

        setUI_fileInfo();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Logger.i(m_className, "onActivityResult called");
        if (requestCode==REQUEST_DATA_FILE_SELECT_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                onFileLocated(data);
            }
        }
    }

}
