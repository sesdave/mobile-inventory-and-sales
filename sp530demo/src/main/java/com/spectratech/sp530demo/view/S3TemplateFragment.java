package com.spectratech.sp530demo.view;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.ViewAnimator;

import com.spectratech.lib.ByteHexHelper;
import com.spectratech.lib.Callback;
import com.spectratech.lib.FileHelper;
import com.spectratech.lib.HTTPDownload;
import com.spectratech.lib.Logger;
import com.spectratech.lib.ResourcesHelper;
import com.spectratech.lib.StringHelper;
import com.spectratech.lib.data.Data_ptl;
import com.spectratech.lib.data.Data_ptl_item;
import com.spectratech.sp530demo.DemoMainActivity;
import com.spectratech.sp530demo.R;
import com.spectratech.sp530demo.constant.S3_TemplateConstant;
import com.spectratech.sp530demo.controller.ActivityHelper;
import com.spectratech.sp530demo.data.Data_logdatapacket;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * S3TemplateFragment - Fragment for handling template
 */
public class S3TemplateFragment extends S3CommonFragment {

    private static final String m_className = "S3TemplateFragment";

    private static final int MSG_HANDLER_INS=1100;

    private boolean m_bPause;

    private Handler m_handler;

    private DownloadFileListJsonThread m_downladThread;

    private ArrayList<String> m_url_ptlFileList;

    private LinearLayout m_mainll;

    private Button m_btn_controller_conf_files_toggle;
    private LinearLayout m_controller_conf_filesll;
    private EditText m_ed_filelist;
    private Button m_btn_clearFileListUrl;
    private Button m_btn_defaultFileListUrl;
    private Button m_btn_getFileList;
    private Button m_btn_downloadFiles;

    private ListView m_lv_filelist;

    private TextView m_tv_ptl_file_path_val;

    private Button m_btn_refresh;
    private Button m_btn_send;

    private ViewAnimator m_twoviews_viewanimator;

    private Button m_btn_logview;

    private static ScrollView m_sv_activity_log;
    private static TextView m_tv_activity_log;
    private static final StringBuilder logCache = new StringBuilder();
    private static final DateFormat timeStampFmt = new SimpleDateFormat("HH:mm:ss.SSS  ", Locale.US);

    private TextView m_tv_ptl_file_count;
    private TextView m_tv_conf_inputfile;
    private TextView m_tv_conf_inputins;
    private TextView m_tv_conf_inputitem;
    private TextView m_tv_conf_inputgoto;
    private TextView m_tv_conf_inputdata;

    private TextView m_tv_rawdata;

    private int m_uiIdxInputFile;
    private int m_uiIdxInputItem;

    private Data_ptl m_dataPtl;

    private ArrayAdapter<String> m_adapterForConfInputFile;
    private ArrayAdapter<Data_ptl_item> m_adapterForConfInputItem;

    private runPtlItemCommandInstance m_runPtlItemCmdInst;

    private int m_stateClickBtnLogviewOk;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Activity act = getActivity();
        m_context = (Context) act;
        m_bPause = false;
        m_downladThread = null;

        m_uiIdxInputFile = -1;
        m_uiIdxInputItem = -1;
        m_dataPtl = null;

        m_adapterForConfInputFile=null;
        m_adapterForConfInputItem=null;

        m_runPtlItemCmdInst=null;

        m_stateClickBtnLogviewOk=0;

        initHandler();
    }

    private void initHandler() {
        m_handler=new Handler() {
            public void handleMessage(Message msg) {
                //Logger.i(m_className, "Handler message: "+msg.what);
                switch (msg.what) {
                    case MSG_HANDLER_INS: {
                        runPtlItemCommandInstance inst=(runPtlItemCommandInstance)msg.obj;
                        inst.runPtlItemCommand();
                    }
                    break;
                }
            }
        };
    }

    @Override
    public void onDestroy() {
        if (m_downladThread != null) {
            m_downladThread.cancel();
            m_downladThread = null;
        }
        super.onDestroy();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final String strPleaseSelectType= ResourcesHelper.getBaseContextString(m_context, R.string.please_select_atype);
        final String strPleaseSelectItem=ResourcesHelper.getBaseContextString(m_context, R.string.please_select_anitem);
        final String strError=ResourcesHelper.getBaseContextString(m_context, R.string.error);
        final String strItem=ResourcesHelper.getBaseContextString(m_context, R.string.item);
        final String strPleaseWaitCurrentProcessFinish=ResourcesHelper.getBaseContextString(m_context, R.string.please_wait_current_dlprocess_finish);

        m_mainll = (LinearLayout) inflater.inflate(R.layout.view_s3template, container, false);
        View rootView = m_mainll;

        m_twoviews_viewanimator=(ViewAnimator)m_mainll.findViewById(R.id.twoviews_viewanimator);

        m_btn_controller_conf_files_toggle = (Button) m_mainll.findViewById(R.id.btn_controller_conf_files_toggle);
        m_controller_conf_filesll = (LinearLayout) m_mainll.findViewById(R.id.controller_conf_files);
        m_ed_filelist = (EditText) m_mainll.findViewById(R.id.s3template_input_filelisturl_val);
        m_btn_clearFileListUrl = (Button) m_mainll.findViewById(R.id.btn_filelisturl_clear);
        m_btn_defaultFileListUrl = (Button) m_mainll.findViewById(R.id.btn_filelisturl_default);
        m_btn_getFileList = (Button) m_mainll.findViewById(R.id.btn_filelist_get);
        m_btn_downloadFiles = (Button) m_mainll.findViewById(R.id.btn_files_download);

        m_lv_filelist = (ListView) m_mainll.findViewById(R.id.list_filelist);

        m_tv_ptl_file_path_val = (TextView) m_mainll.findViewById(R.id.tv_ptl_file_path_val);

        m_btn_refresh = (Button) m_mainll.findViewById(R.id.btn_refresh);
        m_btn_send = (Button) m_mainll.findViewById(R.id.btn_send);
        m_btn_send.setTag(false);

        m_tv_ptl_file_count = (TextView) m_mainll.findViewById(R.id.tv_ptl_file_count);
        m_tv_conf_inputfile = (TextView) m_mainll.findViewById(R.id.tv_conf_inputfile);
        m_tv_conf_inputins = (TextView) m_mainll.findViewById(R.id.tv_conf_inputins);
        m_tv_conf_inputitem = (TextView) m_mainll.findViewById(R.id.tv_conf_inputitem);
        m_tv_conf_inputgoto = (TextView) m_mainll.findViewById(R.id.tv_conf_inputgoto);
        m_tv_conf_inputdata = (TextView) m_mainll.findViewById(R.id.tv_conf_inputdata);
        m_tv_rawdata = (TextView) m_mainll.findViewById(R.id.tv_rawdata);

        m_btn_logview=(Button)m_mainll.findViewById(R.id.btn_logview);
        m_sv_activity_log=(ScrollView)m_mainll.findViewById(R.id.sv_activity_log);
        m_tv_activity_log=(TextView)m_mainll.findViewById(R.id.tv_activity_log);

        {
            View.OnClickListener listener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (m_stateClickBtnLogviewOk==0) {
                        cancelSend();
                    }
                    else {
                        m_twoviews_viewanimator.setDisplayedChild(0);
                    }
                    m_stateClickBtnLogviewOk++;
                    setUI_btnLogview();
                }
            };
            m_btn_logview.setOnClickListener(listener);
        }

        {
            View.OnClickListener listener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int vis = m_controller_conf_filesll.getVisibility();
                    if (vis == View.VISIBLE) {
                        m_controller_conf_filesll.setVisibility(View.GONE);
                    } else {
                        m_controller_conf_filesll.setVisibility(View.VISIBLE);
                    }
                }
            };
            m_btn_controller_conf_files_toggle.setOnClickListener(listener);
        }

        m_ed_filelist.setText(S3_TemplateConstant.DEFAULT_FILELIST_URL);

        {
            View.OnClickListener listener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    m_ed_filelist.setText("");
                }
            };
            m_btn_clearFileListUrl.setOnClickListener(listener);
        }

        {
            View.OnClickListener listener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    m_ed_filelist.setText(S3_TemplateConstant.DEFAULT_FILELIST_URL);
                }
            };
            m_btn_defaultFileListUrl.setOnClickListener(listener);
        }

        {
            View.OnClickListener listener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (m_downladThread != null) {
                        boolean bFinish = m_downladThread.isFinish();
                        if (!bFinish) {

                            ToastMainMessage(strPleaseWaitCurrentProcessFinish);
                            return;
                        }
                    }
                    String url = m_ed_filelist.getText().toString();
                    m_downladThread = new DownloadFileListJsonThread(url);
                    m_downladThread.start();
                }
            };
            m_btn_getFileList.setOnClickListener(listener);
        }

        {
            View.OnClickListener listener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ActivityHelper instActivityHelper = ActivityHelper.getInstance();
                    DemoMainActivity act = instActivityHelper.getDemoMainActivity();
                    if (act == null) {
                        Logger.w(m_className, "m_btn_downloadFiles onclick, DemoMainActivity is NULL");
                        return;
                    }
                    act.startActivityForResultFileDownload(m_url_ptlFileList);
                }
            };
            m_btn_downloadFiles.setOnClickListener(listener);
        }

        {
            String strDiectory = DemoMainActivity.getExternalDataS3TemplateDirectoryString();
            m_tv_ptl_file_path_val.setText(strDiectory);
        }


        {
            View.OnClickListener listener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    reloadDropDownUis();
                }
            };
            m_btn_refresh.setOnClickListener(listener);
        }

        {
            View.OnClickListener listener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (m_uiIdxInputFile<0) {
                        Logger.i(m_className, "onClick, m_btn_send, m_uiIdxInputFile<0, m_uiIdxInputFile: "+m_uiIdxInputFile);
                        ActivityHelper instActivityHelper=ActivityHelper.getInstance();
                        DemoMainActivity act=instActivityHelper.getDemoMainActivity();
                        if (act!=null) {
                            act.ToastMessage(strPleaseSelectType);
                        }
                        return;
                    }
                    if (m_uiIdxInputItem<0) {
                        Logger.i(m_className, "onClick, m_btn_send, m_uiIdxInputItem<0, m_uiIdxInputItem: "+m_uiIdxInputItem);
                        ActivityHelper instActivityHelper=ActivityHelper.getInstance();
                        DemoMainActivity act=instActivityHelper.getDemoMainActivity();
                        if (act!=null) {
                            act.ToastMessage(strPleaseSelectItem);
                        }
                        return;
                    }
                    if (m_dataPtl==null) {
                        Logger.w(m_className, "onClick, m_btn_send, m_dataPtl is NULL");
                        ActivityHelper instActivityHelper=ActivityHelper.getInstance();
                        DemoMainActivity act=instActivityHelper.getDemoMainActivity();
                        if (act!=null) {
                            act.ToastMessage(strError+": "+strItem);
                        }
                        return;
                    }

                    boolean bSending=(Boolean)m_btn_send.getTag();
                    bSending=!bSending;

                    // set UIs
                    setRunPtlItemCmdUIs(bSending);
                    if (bSending) {
                        m_tv_activity_log.setText("");
                        m_stateClickBtnLogviewOk=0;
                        setUI_btnLogview();
                        m_twoviews_viewanimator.setDisplayedChild(1);
                    }
                    else {
                        m_stateClickBtnLogviewOk++;
                        setUI_btnLogview();
                    }

                    if (m_runPtlItemCmdInst!=null) {
                        m_runPtlItemCmdInst.Stop();
                        m_runPtlItemCmdInst=null;
                    }

                    if (bSending) {
                        m_runPtlItemCmdInst = new runPtlItemCommandInstance(m_uiIdxInputItem);
                        m_runPtlItemCmdInst.Start();
                    }
                }
            };
            m_btn_send.setOnClickListener(listener);
        }


        initDropDownUIs();

        return rootView;
    }

    public static void log(Data_logdatapacket logdataPacket) {
        try {
            if (logdataPacket == null) {
                return;
            }
            if (m_tv_activity_log == null) {
                return;
            }
            ByteHexHelper instByteHexHelper = ByteHexHelper.getInstance();
            String tag = (logdataPacket.m_bFromSend) ? "SEND" : "RECEIVE";
            String strContent = instByteHexHelper.bytesArrayToHexString(logdataPacket.m_dataBuf);
            StringHelper instStringHelper=StringHelper.getInstance();
            strContent=instStringHelper.addPaddingStartingFromHead(" ", strContent, 16);
            log(tag, strContent);
        }
        catch (Exception ex) {
            Logger.w(m_className, "log, ex: "+ex.toString());
        }
    }
    public static void log(String tag, Object... messageFragments) {
        StringBuilder message = new StringBuilder();
        for (Object fragment : messageFragments) {
            message.append(fragment.toString());
        }
        String text = message.toString();

        logCache.append(timeStampFmt.format(new Date())).append(tag).append(" ").append(text).append('\n').append('\n');

        // any logging before activityLog is initialized does not get
        // cleared and will be printed to the screen later
        if (m_tv_activity_log != null) {
            m_tv_activity_log.post(new Runnable() {
                public void run() {
                    if (logCache!=null) {
                        synchronized (logCache) {
                            m_tv_activity_log.append(logCache);
                            logCache.setLength(0);
                        }
                    }
                    if (m_sv_activity_log != null) {
                        m_sv_activity_log.post(new Runnable() {
                            public void run() {
                                m_sv_activity_log.scrollTo(0,m_tv_activity_log.getHeight());
                            }
                        });
                    }
                }
            });

        }
    }

    private void setUI_btnLogview() {
        String strCancel=ResourcesHelper.getBaseContextString(m_context, R.string.cancel);
        String strOK=ResourcesHelper.getBaseContextString(m_context, R.string.ok);
        String strText=strCancel;
        if (m_stateClickBtnLogviewOk>0) {
            strText=strOK;
        }
        m_btn_logview.setText(strText);

    }

    private void setRunPtlItemCmdUIs(boolean bSending) {
        String strStart=ResourcesHelper.getBaseContextString(m_context, R.string.start);
        String strStop=ResourcesHelper.getBaseContextString(m_context, R.string.stop);
        m_btn_send.setTag(bSending);
        if (bSending) {
            m_btn_send.setText(strStop);
            m_btn_refresh.setEnabled(false);
            m_btn_controller_conf_files_toggle.setEnabled(false);
            m_controller_conf_filesll.setVisibility(View.GONE);
        }
        else {
            m_btn_send.setText(strStart);
            m_btn_refresh.setEnabled(true);
            m_btn_controller_conf_files_toggle.setEnabled(true);
        }
    }

    private class runPtlItemCommandInstance {
        private static final String m_instName = "runPtlItemCommandInstance";

        public boolean m_bStop;

        private int m_idxItemStart;

        private Data_ptl_item m_dataPtlItem;

        public runPtlItemCommandInstance(int idxItem) {
            init();
            m_idxItemStart=idxItem;
        }

        private void init() {
            m_bStop=false;
            m_idxItemStart=-1;
            m_dataPtlItem=null;
        }

        public void Start() {
            Logger.i(m_className, m_instName+", Start");
            m_dataPtlItem=m_dataPtl.m_itemList.get(m_idxItemStart);
            if (m_dataPtlItem==null) {
                Logger.w(m_className, m_instName+", m_run_dataPtlItem is NULL");
                return;
            }
            runPtlItemCommand();
        }

        public void Stop() {
            Logger.i(m_className, m_instName+", Stop");
            m_bStop=true;
        }

        public void runPtlItemCommand() {
            ActivityHelper instActivityHelper=ActivityHelper.getInstance();
            DemoMainActivity act=instActivityHelper.getDemoMainActivity();
            if (act!=null) {
                ByteHexHelper instByteHexHelper=ByteHexHelper.getInstance();
                String strCode=m_dataPtlItem.m_ins;
                byte[] tmpArray=instByteHexHelper.hexStringToByteArray(strCode);
                if ( (tmpArray==null) && (tmpArray.length < 1)) {
                    Logger.w(m_className, "onClick, m_btn_send_stop, tmpArray is NULL");
                    act.ToastMessage("Error: INS");

                    // set UIs
                    boolean bSending=false;
                    setRunPtlItemCmdUIs(bSending);
                    m_stateClickBtnLogviewOk++;
                    setUI_btnLogview();

                    return;
                }
                byte commandCode=tmpArray[0];
                String strData=m_dataPtlItem.m_data;
                if (strData==null) {
                    Logger.w(m_className, "onClick, m_btn_send_stop, strData is NULL");
                    act.ToastMessage("Error: Data");

                    // set UIs
                    boolean bSending=false;
                    setRunPtlItemCmdUIs(bSending);
                    m_stateClickBtnLogviewOk++;
                    setUI_btnLogview();

                    return;
                }

                Logger.i(m_className, "runPtlItemCommand, current Ptl item: "+m_dataPtlItem.m_item);
                log("Template: "+m_dataPtlItem.m_item);

                byte[] dataBuf=instByteHexHelper.hexStringToByteArray(strData);
                Callback<Object> cb_finish=new Callback<Object>() {
                    @Override
                    public Object call() {
                        if (m_bStop) {
                            String strMsg="Stopped! Last executed command: <"+m_dataPtlItem.m_item+">";
                            ToastMainMessage(strMsg);
                            Logger.i(m_className, "runPtlItemCommand, "+strMsg);

                            // set UIs
                            boolean bSending=false;
                            setRunPtlItemCmdUIs(bSending);
                            m_stateClickBtnLogviewOk++;
                            setUI_btnLogview();

                            return null;
                        }
                        if  ( (m_dataPtlItem.m_goto==null)||(m_dataPtlItem.m_goto.equals("")) ) {
                            String strMsg="Finish! Last executed command: <"+m_dataPtlItem.m_item+">";
                            ToastMainMessage(strMsg);

                            // set UIs
                            boolean bSending=false;
                            setRunPtlItemCmdUIs(bSending);
                            m_stateClickBtnLogviewOk++;
                            setUI_btnLogview();

                            return null;
                        }
                        else {
                            // goto next item
                            String nextItemCapital=m_dataPtlItem.m_goto.toUpperCase();
                            m_dataPtlItem=m_dataPtl.m_itemHash.get(nextItemCapital);
                            if (m_dataPtlItem==null) {
                                String strMsg="No command info <"+nextItemCapital+"> is FOUND";
                                ToastMainMessage(strMsg);

                                // set UIs
                                boolean bSending=false;
                                setRunPtlItemCmdUIs(bSending);
                                m_stateClickBtnLogviewOk++;
                                setUI_btnLogview();

                                return null;
                            }

                            Message msg = new Message();
                            msg.what = MSG_HANDLER_INS;
                            msg.obj=runPtlItemCommandInstance.this;
                            m_handler.sendMessage(msg);
                        }
                        return null;
                    }
                };
                if (commandCode==(byte)0xFF) {
                    Logger.i(m_className, "runPtlItemCommand, commandCode is "+strCode+", skip run "+m_dataPtlItem.m_item);
                    if (cb_finish!=null) {
                        try {
                            cb_finish.call();
                        }
                        catch (Exception ex) {
                            Logger.w(m_className, "runPtlItemCommand, callback, ex: "+ex.toString());
                        }
                    }
                }
                else {
                    act.safeStartCommand(commandCode, dataBuf, cb_finish);
                }
            }
        }

    }

    @Override
    public void onDestroyView() {
        m_sv_activity_log=null;
        m_tv_activity_log=null;

        if (m_runPtlItemCmdInst!=null) {
            m_runPtlItemCmdInst.Stop();
            m_runPtlItemCmdInst=null;
        }

        super.onDestroyView();
    }


    @Override
    public void onPause() {
        super.onPause();
        m_bPause = true;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (m_bPause) {
            m_bPause = false;
        }
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);

        // Make sure that we are currently visible
        if (this.isVisible()) {
            if (isVisibleToUser) {
            }
        }
    }

    private void load_dataPtl() {
        String currentFname = m_tv_conf_inputfile.getText().toString();
        if ((currentFname == null) || (currentFname.equals(""))) {
            Logger.w(m_className, "load_dataPtl, currentFname is NULL");
            return;
        }
        String dir = DemoMainActivity.getExternalDataS3TemplateDirectoryString();
        File f = new File(dir, currentFname);
        m_dataPtl = new Data_ptl(f);
    }

    public void cancelSend() {
        Logger.i(m_className, "cancelSend called");
        if (m_runPtlItemCmdInst!=null) {
            m_runPtlItemCmdInst.Stop();
            m_runPtlItemCmdInst=null;
        }
        boolean bSending=false;
        setRunPtlItemCmdUIs(bSending);
    }

    public void reloadDropDownUis() {
        Logger.i(m_className, "reloadDropDownUis call");
        resetUI_Data_ptl_item();
        resetDropDownUi_confInputFile();
        resetDropDownUi_confItemFile();
        initDropDownUIs();
    }
    private void resetDropDownUi_confInputFile() {
        m_uiIdxInputFile = -1;
        if (m_adapterForConfInputFile!=null) {
            m_adapterForConfInputFile.clear();
            m_adapterForConfInputFile=null;
        }
    }
    private void resetDropDownUi_confItemFile() {
        m_uiIdxInputItem = -1;
        if (m_adapterForConfInputItem!=null) {
            m_adapterForConfInputItem.clear();
            m_adapterForConfInputItem=null;
        }
    }

    private void initDropDownUIs() {
        Logger.i(m_className, "initDropDownUIs called");
        initDropDownUI_confInputFile();
        initDropDownUI_confInputItem();
    }

    private void initDropDownUI_confInputFile() {
        Logger.i(m_className, "initDropDownUI_confInputFile called");
        final String strCount=ResourcesHelper.getBaseContextString(m_context, R.string.count);
        final String strType=ResourcesHelper.getBaseContextString(m_context, R.string.type);
        if (m_adapterForConfInputFile==null) {
            Logger.i(m_className, "initDropDownUI_confInputFile, load m_adapterForConfInputFile");
            String strDiectory = DemoMainActivity.getExternalDataS3TemplateDirectoryString();
            FileHelper instFileHelper = FileHelper.getInstance();
            List<String> list = instFileHelper.getFilenameList(strDiectory, ".ptl");
            m_tv_ptl_file_count.setText(strCount+": " + ((list == null) ? 0 : list.size()));
            if (list == null) {
                Logger.i(m_className, "initDropDownUI_confInputFile, list is NULL");
                return;
            }
            m_adapterForConfInputFile = new ArrayAdapter<String>(m_context, android.R.layout.simple_spinner_dropdown_item, list);
        }
        TextView tv = m_tv_conf_inputfile;
        final TextView ftv = tv;
        final String title = strType;
        if ((tv != null) && (m_adapterForConfInputFile != null)) {
            setDropDownTextView_confInputFile(tv, m_adapterForConfInputFile, m_uiIdxInputFile);
            tv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog.Builder dialog = new AlertDialog.Builder(m_context);
                    dialog.setTitle(title);
                    DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            String strFile = m_adapterForConfInputFile.getItem(which);
                            Logger.i(m_className, "select confInputFile: " + strFile);
                            setDropDownTextView_confInputFile(ftv, m_adapterForConfInputFile, which);
                            if (m_uiIdxInputFile != which) {
                                resetDropDownUi_confItemFile();
                                initDropDownUI_confInputItem();
                            }
                            m_uiIdxInputFile = which;
                            dialog.dismiss();
                        }
                    };

                    if (m_uiIdxInputFile < 0) {
                        dialog.setAdapter(m_adapterForConfInputFile, listener).show();
                    } else {
                        dialog.setSingleChoiceItems(m_adapterForConfInputFile, m_uiIdxInputFile, listener).show();
                    }
                }
            });
        }
    }

    private void setDropDownTextView_confInputFile(TextView tv, ArrayAdapter<String> adapter, int which) {
        if (which > -1) {
            String val = adapter.getItem(which);
            tv.setText(val);
            int idx = which;
            tv.setTag("" + idx);
        } else {
            tv.setText("");
            tv.setTag(null);
        }
    }

    private void initDropDownUI_confInputItem() {
        Logger.i(m_className, "initDropDownUI_confInputItem called");
        final String strItem=ResourcesHelper.getBaseContextString(m_context, R.string.item);
        if (m_adapterForConfInputItem==null) {
            Logger.i(m_className, "initDropDownUI_confInputItem, load m_adapterForConfInputItem");
            load_dataPtl();
            if (m_dataPtl == null) {
                Logger.i(m_className, "initDropDownUI_confInputItem, dataPtl is NULL");
                return;
            }
            List<Data_ptl_item> list = m_dataPtl.m_itemList;
            m_adapterForConfInputItem = new ArrayAdapter<Data_ptl_item>(m_context, android.R.layout.simple_spinner_dropdown_item, list);
        }
        TextView tv = m_tv_conf_inputitem;
        final TextView ftv = tv;
        final String title = strItem;
        if ((tv != null) && (m_adapterForConfInputItem != null)) {
            setDropDownTextView_confInputItem(tv, m_adapterForConfInputItem, m_uiIdxInputItem);
            if (m_uiIdxInputItem > -1) {
                Data_ptl_item dataPtlItem = m_adapterForConfInputItem.getItem(m_uiIdxInputItem);
                setUI_Data_ptl_item(dataPtlItem);
            }
            tv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog.Builder dialog = new AlertDialog.Builder(m_context);
                    dialog.setTitle(title);
                    DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Data_ptl_item item = m_adapterForConfInputItem.getItem(which);
                            String strItem = item.m_item;
                            Logger.i(m_className, "select confInputItem: " + strItem);
                            Logger.i(m_className, "data: " + item.m_data);
                            setDropDownTextView_confInputItem(ftv, m_adapterForConfInputItem, which);
                            setUI_Data_ptl_item(item);
                            m_uiIdxInputItem = which;
                            dialog.dismiss();
                        }
                    };

                    if (m_uiIdxInputItem < 0) {
                        dialog.setAdapter(m_adapterForConfInputItem, listener).show();
                    } else {
                        dialog.setSingleChoiceItems(m_adapterForConfInputItem, m_uiIdxInputItem, listener).show();
                    }
                }
            });
        }
    }

    private void setDropDownTextView_confInputItem(TextView tv, ArrayAdapter<Data_ptl_item> adapter, int which) {
        if (which > -1) {
            Data_ptl_item item = adapter.getItem(which);
            tv.setText(item.m_item);
            int idx = which;
            tv.setTag("" + idx);
        } else {
            tv.setText("");
            tv.setTag(null);
        }
    }

    private void resetUI_Data_ptl_item() {
        Data_ptl_item dataPtlItem = new Data_ptl_item();
        setUI_Data_ptl_item(dataPtlItem);
    }

    private void setUI_Data_ptl_item(Data_ptl_item dataPtlItem) {
        setTextView_ins(dataPtlItem);
        setTextView_goto(dataPtlItem);
        setTextView_data(dataPtlItem);
        setTextView_rawData(dataPtlItem);
    }

    private void setTextView_rawData(Data_ptl_item dataPtlItem) {
        if (dataPtlItem==null) {
            Logger.w(m_className, "setTextView_data, dataPtlItem is NULL");
            return;
        }
        m_tv_rawdata.setText(dataPtlItem.m_strRawData);
    }

    private void setTextView_data(Data_ptl_item dataPtlItem) {
        if (dataPtlItem==null) {
            Logger.w(m_className, "setTextView_data, dataPtlItem is NULL");
            return;
        }
        StringHelper instStringHelper=StringHelper.getInstance();
        String strData=instStringHelper.addPaddingStartingFromHead(" ", dataPtlItem.m_data, 16);
        m_tv_conf_inputdata.setText(strData);
    }
    private void setTextView_goto(Data_ptl_item dataPtlItem) {
        if (dataPtlItem==null) {
            Logger.w(m_className, "setTextView_goto, dataPtlItem is NULL");
            return;
        }
        m_tv_conf_inputgoto.setText(dataPtlItem.m_goto);
    }
    private void setTextView_ins(Data_ptl_item dataPtlItem) {
        if (dataPtlItem==null) {
            Logger.w(m_className, "setTextView_ins, dataPtlItem is NULL");
            return;
        }
        m_tv_conf_inputins.setText(dataPtlItem.m_ins);
    }

    private void ToastMainMessage(String strMsg) {
        ActivityHelper instActivityHelper = ActivityHelper.getInstance();
        DemoMainActivity act = instActivityHelper.getDemoMainActivity();
        if (act != null) {
            act.ToastMessageRunOnUiThread(strMsg);
        }
    }

    private synchronized void fetch_url_ptlFileList(byte[] jsonData) {
        String strJsonData=new String(jsonData);
        JSONObject tmp = null;
        try {
            tmp = new JSONObject(strJsonData);
        }
        catch (JSONException jsonex) {
            Logger.w(m_className, "fetch_url_ptlFileList, jsonex: "+jsonex.toString());
            return;
        }

        JSONArray jsonFileListArray=tmp.optJSONArray("file_list");
        if (jsonFileListArray==null) {
            Logger.w(m_className, "fetch_url_ptlFileList, jsonFileListArray is NULL");
            return;
        }

        if (m_url_ptlFileList==null) {
            m_url_ptlFileList=new ArrayList<String>();
        }
        else {
            m_url_ptlFileList.clear();
        }

        for (int i=0; i<jsonFileListArray.length(); i++) {
            String url=(String)jsonFileListArray.opt(i);
            if ( (url!=null)&&(!url.equals("")) ) {
                m_url_ptlFileList.add(url);
                Logger.i(m_className, "fetch_url_ptlFileList, index "+i+", url: "+url);
            }
        }

        if (m_url_ptlFileList.size()>0) {
            String[] strArray = new String[m_url_ptlFileList.size()];
            strArray = m_url_ptlFileList.toArray(strArray);

            final ArrayAdapter<String> adapter = new ArrayAdapter<String>(m_context, android.R.layout.simple_list_item_1, android.R.id.text1, strArray);

            ((Activity)m_context).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    m_lv_filelist.setAdapter(adapter);
                    setListViewHeightBasedOnChildren(m_lv_filelist);
                }
            });
        }
    }

    /**** Method for Setting the Height of the ListView dynamically.
     **** Hack to fix the issue of not showing all the items of the ListView
     **** when placed inside a ScrollView  ****/
    public void setListViewHeightBasedOnChildren(ListView listView) {
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null)
            return;

        int desiredWidth = View.MeasureSpec.makeMeasureSpec(listView.getWidth(), MeasureSpec.UNSPECIFIED);
        int totalHeight = 0;
        View view = null;
        for (int i = 0; i < listAdapter.getCount(); i++) {
            view = listAdapter.getView(i, view, listView);
            if (i == 0)
                view.setLayoutParams(new ViewGroup.LayoutParams(desiredWidth, LinearLayout.LayoutParams.WRAP_CONTENT));

            view.measure(desiredWidth, MeasureSpec.UNSPECIFIED);
            totalHeight += view.getMeasuredHeight();
        }
        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        listView.setLayoutParams(params);
    }

    private class DownloadFileListJsonThread extends Thread {
        private static final String m_theadName="DownloadFileListJsonThread";

        private boolean m_bCancel;
        private boolean m_bFinish;
        private String m_url;
        private byte[] m_data;

        public DownloadFileListJsonThread(String url) {
            init();
            m_url=url;
        }

        private void init() {
            m_bCancel=false;
            m_bFinish=false;
            m_url=null;
            m_data=null;
        }

        @Override
        public void run() {
            final String strNetworkNotAvail=ResourcesHelper.getBaseContextString(m_context, R.string.network_not_avail);
            final String strError=ResourcesHelper.getBaseContextString(m_context, R.string.error);
            final String strFinish=ResourcesHelper.getBaseContextString(m_context, R.string.finish);
            final String strDownload=ResourcesHelper.getBaseContextString(m_context, R.string.download);
            final String strEmpty=ResourcesHelper.getBaseContextString(m_context, R.string.empty);
            final String strCancel=ResourcesHelper.getBaseContextString(m_context, R.string.cancel);
            final String strData=ResourcesHelper.getBaseContextString(m_context, R.string.data);
            final String strFileList=ResourcesHelper.getBaseContextString(m_context, R.string.file_list);
            final String strFileListUrl=ResourcesHelper.getBaseContextString(m_context, R.string.file_list_url);

            m_bFinish=false;
            if ((m_url == null) || (m_url.equals(""))) {
                String strMsg = ".ptl "+strFileListUrl+" "+strEmpty+"!";
                ToastMainMessage(strMsg);
                m_bFinish=true;
                return;
            }
            if (!HTTPDownload.isNetworkAvailable(m_context)) {
                String strMsg = strNetworkNotAvail+"!";
                ToastMainMessage(strMsg);
                m_bFinish=true;
                return;
            }

            if (m_bCancel) {
                Logger.i(m_className, m_theadName + ", "+strCancel);
                m_bFinish=true;
                return;
            }

            try {
                m_data = HTTPDownload.OpenHttpByteConnection(m_context, m_url);
                if (m_data != null && m_data.length > 0) {
                } else {
                    m_data = null;
                    ToastMainMessage(strError+": .ptl "+strFileList+" "+strDownload+", "+strData+" "+strEmpty);
                    m_bFinish=true;
                    return;
                }
            }
            catch (IOException ioex) {
                Logger.w(m_className, m_theadName+", ioex: "+ioex.toString());
                ToastMainMessage(strError+": .ptl "+strFileList+" "+strDownload);
                m_bFinish=true;
                return;
            }

            fetch_url_ptlFileList(m_data);

            m_bFinish=true;
            ToastMainMessage(strFinish+" .ptl "+strFileList+" "+strDownload);
        }

        public boolean isFinish() {
            return m_bFinish;
        }

        public void cancel() {
            interrupt();
            m_bCancel=true;
        }
    }
}
