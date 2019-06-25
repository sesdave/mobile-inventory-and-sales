package com.spectratech.sp530demo.view;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewAnimator;

import com.spectratech.lib.Callback;
import com.spectratech.lib.Logger;
import com.spectratech.lib.ResourcesHelper;
import com.spectratech.sp530demo.DemoMainActivity;
import com.spectratech.sp530demo.R;
import com.spectratech.sp530demo.sp530class.SP530_bt_S3INS_TMSDL;
import com.spectratech.spectratechlib_ftp.FtpClientHelper;
import com.spectratech.spectratechlib_ftp.ShowFilePathStringListListViewActivity;
import com.spectratech.spectratechlib_ftp.data.Data_ftpClient;
import com.spectratech.spectratechlib_ftp.data.Data_ftpdlObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Scanner;

/**
 * TMSDownloadFragment - TMS download fragment class
 */
public class TMSDownloadFragment extends Fragment {

    private static final String m_className="TMSDownloadFragment";

    private static final DateFormat timeStampFmt = new SimpleDateFormat("HH:mm:ss.SSS  ", Locale.US);

    private final int MSG_HANDLER_TOASTMSG_UI=1102;
    private final int MSG_HANDLER_UPDATE_LOG_UI_BTN_OK=1106;
    private final int MSG_HANDLER_SEND_LOGDOWNLOADMSG=1108;
    private final int MSG_HANDLER_SEND_BRIEFDOWNLOADMSG=1109;
    private final int MSG_HANDLER_UPDATE_TMSDLPROGRESS_UI_BTN_OK=1110;
    private final int MSG_HANDLER_UPDATE_TMSDLPROGRESS_UI_PROGRESSMSG=1111;
    private final int MSG_HANDLER_UPDATE_TMSDLPROGRESS_UI_PROGRESSBAR_VISIBILITY=1112;

    private static String DEFAULTROOTPATH= Environment.getExternalStorageDirectory().getPath();

    private Activity m_activity;
    private Context m_context;

    private Handler m_handler;

    private Toast m_toast;

    private LinearLayout m_mainll;

    private ViewAnimator m_views_viewanimator;

    private EditText m_et_ftp_address;
    private EditText m_et_ftp_port;
    private CheckBox m_cb_ftp_passivemode;
    private EditText m_et_ftp_username;
    private EditText m_et_ftp_password;
    private EditText m_et_ftp_folder;
    private EditText m_et_ftp_filelist;

    private TextView m_tv_default_rootpath;

    private CheckBox m_cb_ftp_forcedownload;

    // tms file list
    private Button m_btn_tmsfilelist_get;
    private TextView m_tv_tmsfilecount;
    private Button m_btn_tmsfilelist_refresh;
    private Button m_btn_tmsfilelist_show;
    private String m_tmsFileNameListString;
    private ArrayList<Data_ftpdlObject> m_tmsFtpdlObjectList;

    // tms download
    private Button m_btn_tmsfiles_download;

    // transfer tms to sp530
    private Button m_btn_transfertms2device;

    // get last transfer tms result
    private Button m_btn_transferttmsresult;

    // brief log
    private ScrollView m_sv_brief_log;
    private TextView m_tv_brief_log;

    private int m_idxLogFileName;

    private ViewTMSDownloadFragmentLogLinearLayout m_viewFragmentLogll;

    private Callback<Object> m_cb_finish_tmsListDownload;

    private TMSDlProgressOverlayFragment m_frag_tmsDlProgressOverlay;

    public static void setDefaultRootPath(String strRootPath) {
        DEFAULTROOTPATH=strRootPath;
    }

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

        m_idxLogFileName=-1;
        m_frag_tmsDlProgressOverlay=null;

        initCallbacks();
    }

    private void initCallbacks() {

        m_cb_finish_tmsListDownload=new Callback<Object>() {
            @Override
            public Object call() throws Exception {
                ((DemoMainActivity)m_context).dismissProgressDialogRunOnUiThread();
                Object obj=this.getParameter();
                if (obj!=null) {
                    if (obj instanceof Data_ftpdlObject) {
                        Data_ftpdlObject dataFtpdlObject=(Data_ftpdlObject)obj;
                        sendBriefDownloadMessage(dataFtpdlObject);

                        fetchTMSListFileParams();
                    }
                }
                return null;
            }
        };
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        m_mainll=(LinearLayout) inflater.inflate(R.layout.view_tmsdownload, null);

        m_tmsFileNameListString="";
        m_tmsFtpdlObjectList=null;

        return m_mainll;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
//        final String strDLTMSFiles=ResourcesHelper.getBaseContextString(m_context, R.string.dl_tms_files);
//        final String strTransferTMS2SP530=ResourcesHelper.getBaseContextString(m_context, R.string.transfer_tms_sp530);
        final String strShow=ResourcesHelper.getBaseContextString(m_context, R.string.show);
        final String strNoTMSFilesInList=ResourcesHelper.getBaseContextString(m_context, R.string.no_tms_files_in_list);
        final String strNoTmsFilesInDLList=ResourcesHelper.getBaseContextString(m_context, R.string.no_tms_files_in_download_list);
        final String strTMSFileLIst=ResourcesHelper.getBaseContextString(m_context, R.string.tms_file_list);
        final String strTMSListFileNotExist=ResourcesHelper.getBaseContextString(m_context, R.string.tms_list_file_notexist_pls_dl_it);
        final String strTMSListFileEmpty=ResourcesHelper.getBaseContextString(m_context, R.string.tms_list_file_empty);
        final String strRefresh=ResourcesHelper.getBaseContextString(m_context, R.string.refresh);
        final String strTMSFileList=ResourcesHelper.getBaseContextString(m_context, R.string.tms_file_list);
        final String strFileList=ResourcesHelper.getBaseContextString(m_context, R.string.file_list);
        final String strCancel=ResourcesHelper.getBaseContextString(m_context, R.string.cancel);
        final String strCanceling=ResourcesHelper.getBaseContextString(m_context, R.string.canceling);
        final String strPleaseWait=ResourcesHelper.getBaseContextString(m_context, R.string.please_wait);

        m_views_viewanimator=(ViewAnimator)m_mainll.findViewById(R.id.views_viewanimator);

        // first
        m_et_ftp_address=(EditText)m_mainll.findViewById(R.id.et_ftp_address);
        m_et_ftp_port=(EditText)m_mainll.findViewById(R.id.et_ftp_port);

        m_cb_ftp_passivemode=(CheckBox)m_mainll.findViewById(R.id.cb_ftp_passivemode);

        m_cb_ftp_forcedownload=(CheckBox)m_mainll.findViewById(R.id.cb_ftp_forcedownload);

        m_et_ftp_username=(EditText)m_mainll.findViewById(R.id.et_ftp_username);
        m_et_ftp_password=(EditText)m_mainll.findViewById(R.id.et_ftp_password);

        m_et_ftp_folder=(EditText)m_mainll.findViewById(R.id.et_ftp_folder);
        m_et_ftp_filelist=(EditText)m_mainll.findViewById(R.id.et_ftp_filelist);

        m_tv_default_rootpath=(TextView)m_mainll.findViewById(R.id.tv_default_rootpath);
        m_tv_default_rootpath.setText(DEFAULTROOTPATH);

        m_btn_tmsfilelist_refresh=(Button)m_mainll.findViewById(R.id.btn_tmsfilelist_refresh);
        {
            View.OnClickListener listener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    sendBriefDownloadMessage(strRefresh+" - TMS "+strFileList);
                    fetchTMSListFileParams();
                }
            };
            m_btn_tmsfilelist_refresh.setOnClickListener(listener);
        }

        m_tv_tmsfilecount=(TextView)m_mainll.findViewById(R.id.tv_tmsfilecount);

        m_btn_tmsfilelist_get=(Button)m_mainll.findViewById(R.id.btn_tmsfilelist_get);
        {
            View.OnClickListener listener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((DemoMainActivity)m_context).showProgressDialog();
                    downloadTMSFileListFile();
                }
            };
            m_btn_tmsfilelist_get.setOnClickListener(listener);
        }

        m_btn_tmsfilelist_show=(Button)m_mainll.findViewById(R.id.btn_tmsfilelist_show);
        {
            View.OnClickListener listener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    fetchTMSListFileParams();
                    if (m_tmsFileNameListString==null) {
                        ToastMessage(strTMSListFileNotExist);
                        return;
                    }
                    else if (m_tmsFileNameListString.equals("")) {
                        ToastMessage(strTMSListFileEmpty);
                        return;
                    }
                    sendBriefDownloadMessage(strShow+" - "+strTMSFileLIst);
                    startShowStringListActivity(m_tmsFtpdlObjectList);
                }
            };
            m_btn_tmsfilelist_show.setOnClickListener(listener);
        }

        m_btn_tmsfiles_download=(Button)m_mainll.findViewById(R.id.btn_tmsfiles_download);
        {
            View.OnClickListener listener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    fetchTMSListFileParams();
                    if ( (m_tmsFtpdlObjectList==null)||(m_tmsFtpdlObjectList.size()==0) ) {
                        ToastMessage(strNoTmsFilesInDLList);
                        return;
                    }
                    startTMSFileDownloadInstance();
                }
            };
            m_btn_tmsfiles_download.setOnClickListener(listener);
        }

        m_btn_transfertms2device=(Button)m_mainll.findViewById(R.id.btn_transfertms2device);
        {
            View.OnClickListener listener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if ( (m_tmsFtpdlObjectList==null)||(m_tmsFtpdlObjectList.size()==0) ) {
                        ToastMessage(strNoTMSFilesInList);
                        return;
                    }
                    startTransferTms2SP530();
                }
            };
            m_btn_transfertms2device.setOnClickListener(listener);
        }


        m_btn_transferttmsresult=(Button)m_mainll.findViewById(R.id.btn_transferttmsresult);
        {
            View.OnClickListener listener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    getTransferTmsResult();
                }
            };
            m_btn_transferttmsresult.setOnClickListener(listener);
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

    private void updateUI_logTitle() {
        final String strFile=ResourcesHelper.getBaseContextString(m_context, R.string.file);
        Data_ftpdlObject dataFtpdlObject=m_tmsFtpdlObjectList.get(m_idxLogFileName);
        String fname=dataFtpdlObject.m_relative_pathlist;
        String strTitleLog=strFile+": \n"+(m_idxLogFileName+1)+"/"+m_tmsFtpdlObjectList.size()+": "+fname;
        m_viewFragmentLogll.setTitleByPost(strTitleLog);
    }

    private void startTMSFileDownloadInstance() {
        Logger.i(m_className, "startTMSFileDownloadInstance called");
        final String strCancel=ResourcesHelper.getBaseContextString(m_context, R.string.cancel);

        m_idxLogFileName=0;

        DemoMainActivity act=(DemoMainActivity)m_context;
        act.setLockActionBarVisibilityStatus(false);
        act.setActionBarHide();
        act.setLockActionBarVisibilityStatus(true);

        m_viewFragmentLogll.setButtonText(strCancel);
        m_views_viewanimator.setDisplayedChild(1);
        updateUI_logTitle();
        m_viewFragmentLogll.clearLog();

        updateUI_logTitle();

        updateUI_log_startMessage(m_idxLogFileName);

        downloadTMSListFile(m_tmsFtpdlObjectList);
    }

    private void getTransferTmsResult() {
        startTms2SP530Event(1);
    }

    private void startTransferTms2SP530() {
        startTms2SP530Event(0);
    }

    private void startTms2SP530Event(final int idx) {
        final String strCancel=ResourcesHelper.getBaseContextString(m_context, R.string.cancel);
        final String strError=ResourcesHelper.getBaseContextString(m_context, R.string.error);

        if (m_context instanceof DemoMainActivity) {
            DemoMainActivity act = (DemoMainActivity) m_context;
            boolean bCommandValid2Start=act.isCommandValid2Start(false);
            if (!bCommandValid2Start) {
                return;
            }
        }

        if ( (idx<0)||(idx>1) ) {
            Logger.w(m_className, "startTms2SP530Event, invalid idx: "+idx);
            return;
        }
        if (m_context instanceof DemoMainActivity) {
            DemoMainActivity act = (DemoMainActivity) m_context;
            Callback<Object> cb_finish=new Callback<Object>() {
                @Override
                public Object call() throws Exception {
                    Logger.i(m_className, "startTms2SP530Event, idx: "+idx+", cb_finish call");
                    Object obj=this.getParameter();

                    if (obj==null) {
                        Logger.i(m_className, "startTms2SP530Event, idx: "+idx+", obj is null");
                    }
                    else {
                        String strMsg;
                        boolean bValidResult = SP530_bt_S3INS_TMSDL.isResponseResultValid(obj);
                        if (bValidResult) {
                        }
                        else {
                            strMsg = strError;
                            byte[] responseCodeArray = SP530_bt_S3INS_TMSDL.getResponseCode(obj);
                            if ((responseCodeArray != null) && (responseCodeArray.length > 0)) {
                                byte val = responseCodeArray[0];
                                int iVal= SP530_bt_S3INS_TMSDL.getResponseCodeMessageValue(val);
                                String strRet=Integer.toString(iVal);
                                strMsg += " ("+strRet+")";
                            }
                            else {
                                String strStatus=SP530_bt_S3INS_TMSDL.getTransStatusString(obj);
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
                    //Logger.i(m_className, "startTransferTms2SP530, cb_progress call");
                    String strMsg=(String)this.getParameter();
                    publishProgressMessage(strMsg);
                    return null;
                };
            };

            if (m_frag_tmsDlProgressOverlay==null) {
                Logger.i(m_className, "startTransferTms2SP530, m_frag_tmsDlProgressOverlay initial");
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
                ArrayList<Data_ftpdlObject> dataFtpdlObjectList = m_tmsFtpdlObjectList;
                String downloadFilePath = DEFAULTROOTPATH;
                downloadFilePath = fixFolderSlash(DEFAULTROOTPATH);
                act.startTransferTms(dataFtpdlObjectList, downloadFilePath, cb_finish, cb_progress);
            }
            else if (idx==1) {
                act.getTransferTmsResult(cb_finish, cb_progress);
            }
        }
    }

    private void startShowStringListActivity(ArrayList<Data_ftpdlObject> list) {
        String strAutoRunPath=getLocalFullPathTmsListFileDirectory();
        Intent intent=new Intent(m_context, ShowFilePathStringListListViewActivity.class);
        Bundle b=new Bundle();
        //b.putString(ShowFilePathStringListListViewActivity.KEY_INPUTSTRING_COMMON_PREFIX, strAutoRunPath);
        intent.putExtras(b);

        ShowFilePathStringListListViewActivity.setList(list);

        m_context.startActivity(intent);
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

    private void sendBriefDownloadMessage(Data_ftpdlObject dataFtpdlObject) {
        String strDownload=ResourcesHelper.getBaseContextString(m_context, R.string.download);
        String strObject=ResourcesHelper.getBaseContextString(m_context, R.string.object);
        String strNotEnable=ResourcesHelper.getBaseContextString(m_context, R.string.not_enable);
        String strSkip=ResourcesHelper.getBaseContextString(m_context, R.string.skip);
        String strSuccess=ResourcesHelper.getBaseContextString(m_context, R.string.success);
        String strFail=ResourcesHelper.getBaseContextString(m_context, R.string.fail);
        String strDL=ResourcesHelper.getBaseContextString(m_context, R.string.download);

        if (dataFtpdlObject==null) {
            sendBriefDownloadMessage("FTP "+strDownload+" "+strObject+" is null");
        }
        String filename=dataFtpdlObject.m_relative_pathlist;
        if (!dataFtpdlObject.m_bEnable) {
            sendBriefDownloadMessage(strNotEnable+" - " + filename);
        }
        else if (dataFtpdlObject.m_bSkip) {
            sendBriefDownloadMessage(strSkip+" "+strDL+" - " + filename);
        }
        else if (dataFtpdlObject.m_bDownloadSuccess) {
            sendBriefDownloadMessage(strSuccess+" "+strDL+" - " + filename);
        }
        else {
            sendBriefDownloadMessage(strFail+" "+strDL+" - " + filename);
        }
    }
    private void sendBriefDownloadMessage(String strMsg) {
        Message msg=m_handler.obtainMessage(MSG_HANDLER_SEND_BRIEFDOWNLOADMSG);
        msg.obj=strMsg;
        m_handler.sendMessage(msg);
    }

    private void sendLogDownloadMessage(Data_ftpdlObject dataFtpdlObject) {
        String strObject=ResourcesHelper.getBaseContextString(m_context, R.string.object);
        String strNotEnable=ResourcesHelper.getBaseContextString(m_context, R.string.not_enable);
        String strSkip=ResourcesHelper.getBaseContextString(m_context, R.string.skip);
        String strSuccess=ResourcesHelper.getBaseContextString(m_context, R.string.success);
        String strFail=ResourcesHelper.getBaseContextString(m_context, R.string.fail);
        String strDL=ResourcesHelper.getBaseContextString(m_context, R.string.download);

        if (dataFtpdlObject==null) {
            sendLogDownloadMessage("FTP "+strDL+" "+strObject+" is null");
        }
        String filename=dataFtpdlObject.m_relative_pathlist;
        if (!dataFtpdlObject.m_bEnable) {
            sendLogDownloadMessage(strNotEnable+" - " + filename);
        }
        else if (dataFtpdlObject.m_bSkip) {
            sendLogDownloadMessage(strSkip+" "+strDL+" - " + filename);
        }
        else if (dataFtpdlObject.m_bDownloadSuccess) {
            sendLogDownloadMessage(strSuccess+" "+strDL+" - " + filename);
        }
        else {
            sendLogDownloadMessage(strFail+" "+strDL+" - " + filename);
        }
    }
    private void sendLogDownloadMessage(String strMsg) {
        Message msg=m_handler.obtainMessage(MSG_HANDLER_SEND_LOGDOWNLOADMSG);
        msg.obj=strMsg;
        m_handler.sendMessage(msg);
    }

    private ArrayList<Data_ftpdlObject> generateTmsFileNameList(String strData) {
        final String strEmpty=ResourcesHelper.getBaseContextString(m_context, R.string.empty);
        final String strFilename=ResourcesHelper.getBaseContextString(m_context, R.string.filename);
        final String strList=ResourcesHelper.getBaseContextString(m_context, R.string.list);
        final String strFinish=ResourcesHelper.getBaseContextString(m_context, R.string.finish);

        ArrayList<Data_ftpdlObject> list=new ArrayList<>();
        String[] strArrayTmp=strData.split("\n");
        if (strArrayTmp==null) {
            ToastMessage(strEmpty+" "+strFilename+" "+strList);
            return list;
        }
        boolean bForceDownload=m_cb_ftp_forcedownload.isChecked();
        String relativePath=m_et_ftp_folder.getText().toString();
        relativePath=fixFolderSlash(relativePath);
        for (String s: strArrayTmp) {
            s=s.trim();
            if ( (s!=null)&&(!s.equals("")) ) {
                String path=relativePath+s;
                Callback<Object> cb_finishdl=new Callback<Object>() {
                    @Override
                    public Object call() throws Exception {
                        Object obj=this.getParameter();
                        Data_ftpdlObject dataFtpdlObject=(Data_ftpdlObject)obj;
                        sendBriefDownloadMessage(dataFtpdlObject);
                        sendLogDownloadMessage(dataFtpdlObject);

                        if (dataFtpdlObject.m_id==m_tmsFtpdlObjectList.size()-1) {
                            m_handler.sendEmptyMessage(MSG_HANDLER_UPDATE_LOG_UI_BTN_OK);
                            String strLastMessage=strFinish+"\n";
                            sendBriefDownloadMessage(strLastMessage);
                            sendLogDownloadMessage(strLastMessage);
                        }
                        else {
                            m_idxLogFileName++;
                            updateUI_logTitle();
                            updateUI_log_startMessage(m_idxLogFileName);
                        }
                        return null;
                    }
                };
                Data_ftpdlObject x=new Data_ftpdlObject(path, list.size(), bForceDownload, cb_finishdl);
                list.add(x);
            }
        }
        return list;
    }

    private void updateUI_log_startMessage(int idx) {
        Data_ftpdlObject dataFtpdlObject=m_tmsFtpdlObjectList.get(idx);
        sendLogDownloadMessage("START "+dataFtpdlObject.m_relative_pathlist+" . . .");
    }

    private void updateUI_tmsCountByPost() {
        m_tv_tmsfilecount.post(new Runnable() {
            @Override
            public void run() {
                int count = (m_tmsFtpdlObjectList == null) ? 0 : m_tmsFtpdlObjectList.size();
                m_tv_tmsfilecount.setText("" + count);
            }
        });
    }

    private void fetchTMSListFileParams() {
        m_tmsFileNameListString=getFilteredTmsListFileString_sp530();
        m_tmsFtpdlObjectList=new ArrayList<>();
        if (m_tmsFileNameListString==null) {
            Logger.i(m_className, "fetchTMSListFileParams, m_tmsFileNameListString is null");
        }
        else if (m_tmsFileNameListString.equals("")) {
            Logger.i(m_className, "fetchTMSListFileParams, m_tmsFileNameListString is empty");
        }
        else {
            //Logger.i(m_className, "TMS list file content: "+inputFilteredString);
            m_tmsFtpdlObjectList = generateTmsFileNameList(m_tmsFileNameListString);
        }
        updateUI_tmsCountByPost();
    }

    private String getFilteredTmsListFileString_sp530() {
        return getFilteredTmsListFileString("SP530");
    }
    private String getFilteredTmsListFileString(String name) {
        String strText=getTmsListFileString();
        if ( (strText==null)||(strText.equals("")) ) {
            return strText;
        }

        String strResult="";

        int countLineWithData=0;
        int countTms=0;

        Scanner scanner=new Scanner(strText);
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();

            // empty
            if (line==null) {
                continue;
            }
            if (line.length()==0) {
                continue;
            }

            String key=getTMSFileListKey(line);

            // detect first tms
            if (countLineWithData==0) {
                if (!key.equals(name)) {
                    return strResult;
                }
                countLineWithData++;
                continue;
            }

            // detect end of tms
            if (key.equals("END")) {
                countLineWithData++;
                break;
            }

            strResult+=line+"\n";
            countTms++;

            countLineWithData++;
        }

        Logger.i(m_className, "getFilteredTmsListFileString, name: " + name + ", countTms: " + countTms);
        return strResult;
    }

    private String getTMSFileListKey(String line) {
        if ( (line==null)||(line.length()<2) ) {
            return null;
        }
        String strResult=line.substring(1);
        strResult=strResult.substring(0, strResult.length()-1);
        return strResult;
    }

    private String getTmsListFileString() {
        String strText=null;
        boolean bTmsListFileExist=isTmsListFileExist();
        if (!bTmsListFileExist) {
            return strText;
        }
        String tmsFileListpath=getLocalFullPathTmsListFile();
        File f=new File(tmsFileListpath);
        try {
            FileInputStream fis = new FileInputStream(f);
            strText=getFileString(fis);
        }
        catch (FileNotFoundException fnf) {
            Logger.w(m_className, "getTmsListFileString, FileNotFoundException fnf: " + fnf.toString());
            strText=null;
        }
        return strText;
    }

    private String getFileString(InputStream is) {
        if (is==null) {
            Logger.w(m_className, "getFileString, InputStream is null");
            return null;
        }
        String strResult;
        byte[] bytes = new byte[1024];
        StringBuilder total = new StringBuilder();
        int numRead = 0;
        try {
            while ((numRead = is.read(bytes)) >= 0) {
                total.append(new String(bytes, 0, numRead));
            }
            strResult=total.toString();
        }
        catch (IOException ioex) {
            Logger.w(m_className, "ioex: " + ioex.toString());
            strResult="";
        }
        return strResult;
    }

    private boolean isTmsListFileExist() {
        boolean bRet=false;
        String localTmsFilepath=getLocalFullPathTmsListFile();
        File f=new File(localTmsFilepath);
        if (f.exists()) {
            bRet=true;
        }
        return bRet;
    }

    private String getLocalFullPathTmsListFileDirectory() {
        String fullfname=getLocalFullPathTmsListFile();
        File f=new File(fullfname);
        String dir="";
        if (f!=null) {
            dir=f.getParent();
        }
        dir=fixFolderSlash(dir);
        return dir;
    }

    private String getLocalFullPathTmsListFile() {
        String fpath=fixFolderSlash(DEFAULTROOTPATH);
        String relative_pathlist=getRelativePathTmsListFile();
        fpath+=relative_pathlist;
        return fpath;
    }

    private String getRelativePathTmsListFile() {
        String relative_pathlist=m_et_ftp_folder.getText().toString();
        relative_pathlist=fixFolderSlash(relative_pathlist);
        relative_pathlist+=m_et_ftp_filelist.getText().toString();
        return relative_pathlist;
    }

    private void downloadTMSFileListFile() {
        String relative_pathlist=getRelativePathTmsListFile();
        boolean bForceDownload=m_cb_ftp_forcedownload.isChecked();
        Data_ftpdlObject dataFtpdlObject=new Data_ftpdlObject(relative_pathlist, 0, bForceDownload, m_cb_finish_tmsListDownload);
        downloadTMSListFile(dataFtpdlObject);
    }
    private void downloadTMSListFile(Data_ftpdlObject dataFtpdlObject) {
        ArrayList<Data_ftpdlObject> dataFtpdlObjectList=new ArrayList<>();
        dataFtpdlObjectList.add(dataFtpdlObject);
        downloadTMSListFile(dataFtpdlObjectList);
    }
    private void downloadTMSListFile(ArrayList<Data_ftpdlObject> dataFtpdlObjectList) {
        Logger.i(m_className, "downloadTMSListFile");

        FtpClientHelper instFtpClientHelper= FtpClientHelper.getInstance();
        String ftp_host=m_et_ftp_address.getText().toString();
        int port;
        try {
            String strPort=m_et_ftp_port.getText().toString();
            int tmpPort=Integer.parseInt(strPort);
            port=tmpPort;
        }
        catch (NumberFormatException nfe) {
            Logger.w(m_className, "downloadTMSListFile, NumberFormatException nfe: " + nfe.toString());
            port= FtpClientHelper.DEFAULT_FTP_PORT;
        }
        Boolean bPassiveMode=m_cb_ftp_passivemode.isChecked();
        String username=m_et_ftp_username.getText().toString();
        String password=m_et_ftp_password.getText().toString();
        Data_ftpClient dataFtpClient=new Data_ftpClient(ftp_host, port, bPassiveMode, username, password);
        instFtpClientHelper.downloadFile(dataFtpClient, dataFtpdlObjectList, DEFAULTROOTPATH);
    }

    private String fixFolderSlash(String strFolder) {
        String resultFolder=strFolder;
        if ( (resultFolder==null)||(resultFolder.length()==0) ) {
            return resultFolder;
        }
        resultFolder=resultFolder.replace('\\','/');
        resultFolder=resultFolder.replace("//","/");
        char charLast=resultFolder.charAt(resultFolder.length()-1);
        if (charLast!='/')  {
            resultFolder+="/";
        }
        return resultFolder;
    }
}
