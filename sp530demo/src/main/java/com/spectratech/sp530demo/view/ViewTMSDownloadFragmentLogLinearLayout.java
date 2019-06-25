package com.spectratech.sp530demo.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import com.spectratech.sp530demo.R;
import com.spectratech.lib.Logger;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * ViewTMSDownloadFragmentLogLinearLayout for TMS download loggind
 */
public class ViewTMSDownloadFragmentLogLinearLayout extends LinearLayout {

    private static final String m_className = "ViewTMSDownloadFragmentLogLinearLayout";

    private static final DateFormat timeStampFmt = new SimpleDateFormat("HH:mm:ss.SSS  ", Locale.US);

    protected Context m_context;

    protected LinearLayout m_mainll;

    private Button m_btn_logview;

    private TextView m_tv_activity_log_title;

    private ScrollView m_sv_activity_log;
    private TextView m_tv_activity_log;
    private StringBuilder logCache;

    private int COUNT_LOG_STRING=0;

    public ViewTMSDownloadFragmentLogLinearLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        m_context = context;
        init();
    }

    public ViewTMSDownloadFragmentLogLinearLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        m_context = context;
        init();
    }

    private void init() {
        m_mainll = this;
        logCache = new StringBuilder();
    }

    public void initUIs() {
        m_btn_logview=(Button)m_mainll.findViewById(R.id.btn_logview);
        m_tv_activity_log_title=(TextView)m_mainll.findViewById(R.id.tv_activity_log_title);
        m_sv_activity_log=(ScrollView)m_mainll.findViewById(R.id.sv_activity_log);
        m_tv_activity_log=(TextView)m_mainll.findViewById(R.id.tv_activity_log);
    }

    public void setButtonText(String strText) {
        m_btn_logview.setText(strText);
    }

    public void setButtonOnClickListener(OnClickListener listener) {
        m_btn_logview.setOnClickListener(listener);
    }

    public void reset() {
        m_tv_activity_log_title.setText("");
        m_tv_activity_log.setText("");
    }

    public void setTitleByPost(final String strText) {
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
        if (logCache!=null) {
            logCache.setLength(0);
        }
        COUNT_LOG_STRING=0;
        if (m_tv_activity_log != null) {
            m_tv_activity_log.setText("");
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
