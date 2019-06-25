package com.spectratech.sp530demo.view;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.spectratech.lib.ByteHexHelper;
import com.spectratech.lib.DisplayHelper;
import com.spectratech.lib.Logger;
import com.spectratech.lib.ResourcesHelper;
import com.spectratech.lib.StringHelper;
import com.spectratech.sp530demo.DemoMainActivity;
import com.spectratech.sp530demo.R;
import com.spectratech.sp530demo.data.Data_logdatapacket;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * S3LogOverlayFragment - overlay view - fragment for SP530 log
 */
public class S3LogOverlayFragment extends Fragment {

    private static final String m_className="S3LogOverlayFragment";

    private static final DateFormat timeStampFmt = new SimpleDateFormat("HH:mm:ss.SSS  ", Locale.US);

    private static S3LogOverlayFragment m_current_viewFragLogllPtr;

    protected Activity m_act;
    protected Context m_context;

    private LinearLayout m_mainll;

    private Button m_btn_logview;

    private TextView m_tv_activity_log_title;

    private ScrollView m_sv_activity_log;
    private TextView m_tv_activity_log;
    private StringBuilder logCache;

    private int COUNT_LOG_STRING=0;

    @Override
    public void onCreate (Bundle savedInstanceState) {
        Logger.i(m_className, "onCreate, called");
        super.onCreate(savedInstanceState);
        m_act=getActivity();
        m_context=(Context)m_act;
        init();
    }

    private void init() {
        logCache = new StringBuilder();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Logger.i(m_className, "onCreateView, called");
        m_mainll=(LinearLayout)inflater.inflate(R.layout.view_s3logoverlay, null);
        m_mainll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });
        DisplayHelper instDisplayHelper= DisplayHelper.getInstance();
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        int margin=30;
        margin=instDisplayHelper.convertPixelsToDp(m_context, margin);
        int marginTop=40;
        marginTop=instDisplayHelper.convertPixelsToDp(m_context, marginTop);
        lp.setMargins(margin, marginTop, margin, margin);
        m_mainll.setLayoutParams(lp);
        initUIs();

        m_current_viewFragLogllPtr=this;

        return m_mainll;
    }

    @Override
    public void onDestroyView() {
        m_current_viewFragLogllPtr=null;
        super.onDestroyView();
    }

    public static void logData(Data_logdatapacket logdataPacket) {
        if (m_current_viewFragLogllPtr!=null) {
            m_current_viewFragLogllPtr.log(logdataPacket);
        }
    }

    public void initUIs() {
        final String strOK=ResourcesHelper.getBaseContextString(m_context, R.string.ok);
        m_btn_logview=(Button)m_mainll.findViewById(R.id.btn_logview);
        m_btn_logview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DemoMainActivity act=null;
                if (m_context instanceof DemoMainActivity) {
                    act=(DemoMainActivity)m_context;
                }
                if (act==null) {
                    return;
                }
                String strText=m_btn_logview.getText().toString();
                if (strText.equals(strOK)) {
                    act.enableAllUIInputs();
                    return;
                }
                act.showCancelTransactionDialog();
            }
        });
        m_tv_activity_log_title=(TextView)m_mainll.findViewById(R.id.tv_activity_log_title);
        m_sv_activity_log=(ScrollView)m_mainll.findViewById(R.id.sv_activity_log);
        m_tv_activity_log=(TextView)m_mainll.findViewById(R.id.tv_activity_log);
    }

    public void setButtonTextByPost(final String strText) {
        if (m_btn_logview==null) {
            Logger.w(m_className, "setButtonTextByPost, m_btn_logview is null");
            return;
        }
        m_btn_logview.post(new Runnable() {
            @Override
            public void run() {
                setButtonText(strText);
            }
        });
    }
    public void setButtonText(String strText) {
        m_btn_logview.setText(strText);
    }

    public void setButtonOnClickListener(View.OnClickListener listener) {
        m_btn_logview.setOnClickListener(listener);
    }

    public void reset() {
        m_tv_activity_log_title.setText("");
        m_tv_activity_log.setText("");
    }

    public void setTitleByPost(final String strText) {
        if (m_tv_activity_log_title==null) {
            Logger.w(m_className, "setTitleByPost, m_tv_activity_log_title is null");
            return;
        }
        m_tv_activity_log_title.post(new Runnable() {
            public void run() {
                setTitle(strText);
            }
        });
    }

    public void setTitle(String strText) {
        m_tv_activity_log_title.setText(strText);
    }

    public void clearLog() {
        if (logCache==null) {
            if (m_tv_activity_log != null) {
                m_tv_activity_log.setText("");
            }
            return;
        }
        if (logCache!=null) {
            logCache.setLength(0);
        }
        COUNT_LOG_STRING=0;
        if (m_tv_activity_log != null) {
            m_tv_activity_log.setText("");
        }
    }

    public void log(Data_logdatapacket logdataPacket) {
        try {
            if (logdataPacket == null) {
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
            Logger.w(m_className, "log, ex: " + ex.toString());
        }
    }
    public void log(String tag, Object... messageFragments) {
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
                    synchronized (logCache) {
                        if (m_tv_activity_log != null) {
                            if (COUNT_LOG_STRING>2*1024*1024) {
                                Logger.i(m_className, "log, COUNT_LOG_STRING>2*1024*1024");
                                COUNT_LOG_STRING=logCache.length();
                                m_tv_activity_log.setText(logCache);
                            }
                            else {
                                COUNT_LOG_STRING+=logCache.length();
                                m_tv_activity_log.append(logCache);
                            }
                            if (logCache!=null) {
                                logCache.setLength(0);
                            }
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

    public void log_noRunnable(String tag, Object... messageFragments) {
        StringBuilder message = new StringBuilder();
        for (Object fragment : messageFragments) {
            message.append(fragment.toString());
        }
        String text = message.toString();

        logCache.append(timeStampFmt.format(new Date())).append(tag).append(" ").append(text).append('\n');

        if (m_tv_activity_log != null) {
            synchronized (logCache) {
                if (m_tv_activity_log != null) {
                    if (COUNT_LOG_STRING>2*1024*1024) {
                        Logger.i(m_className, "log, COUNT_LOG_STRING>2*1024*1024");
                        COUNT_LOG_STRING=logCache.length();
                        m_tv_activity_log.setText(logCache);
                    }
                    else {
                        COUNT_LOG_STRING+=logCache.length();
                        Logger.i(m_className, logCache.toString());
                        m_tv_activity_log.append(logCache);
                    }
                    if (logCache!=null) {
                        logCache.setLength(0);
                    }
                }
            }
            if (m_sv_activity_log != null) {
                m_sv_activity_log.scrollTo(0,m_tv_activity_log.getHeight());
            }
        }
    }

}
