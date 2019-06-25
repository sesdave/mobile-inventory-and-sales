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
import android.widget.ProgressBar;
import android.widget.TextView;

import com.spectratech.lib.DisplayHelper;
import com.spectratech.lib.Logger;
import com.spectratech.lib.ResourcesHelper;
import com.spectratech.sp530demo.DemoMainActivity;
import com.spectratech.sp530demo.R;

/**
 * TMSDlProgressOverlayFragment - tms download progress overlay fragment
 */
public class TMSDlProgressOverlayFragment extends Fragment {

    private static final String m_className = "TMSDlProgressOverlayFragment";

    protected Activity m_act;
    protected Context m_context;

    private LinearLayout m_mainll;

    private ProgressBar m_pb_progress;
    private TextView m_tv_progress_title;
    private TextView m_tv_progress_text;

    private Button m_btn_logview;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        Logger.i(m_className, "onCreate, called");
        super.onCreate(savedInstanceState);
        m_act = getActivity();
        m_context = (Context) m_act;
        init();
    }

    private void init() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Logger.i(m_className, "onCreateView, called");
        m_mainll = (LinearLayout) inflater.inflate(R.layout.view_tmsdlprogressoverlay, null);
        m_mainll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });
        DisplayHelper instDisplayHelper = DisplayHelper.getInstance();
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
//        int margin = 30;
//        margin = instDisplayHelper.convertPixelsToDp(m_context, margin);
//        int marginTop = 40;
//        marginTop = instDisplayHelper.convertPixelsToDp(m_context, marginTop);
//        lp.setMargins(margin, marginTop, margin, margin);
        m_mainll.setLayoutParams(lp);

        return m_mainll;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        initUIs();
    }

    public void initUIs() {
        final String strOK= ResourcesHelper.getBaseContextString(m_context, R.string.ok);

        m_pb_progress=(ProgressBar)m_mainll.findViewById(R.id.pb_progress);

        m_tv_progress_title=(TextView)m_mainll.findViewById(R.id.tv_progress_title);

        m_tv_progress_text=(TextView)m_mainll.findViewById(R.id.tv_progress_text);

        m_btn_logview=(Button)m_mainll.findViewById(R.id.btn_logview);
        m_btn_logview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DemoMainActivity act = null;
                if (m_context instanceof DemoMainActivity) {
                    act = (DemoMainActivity) m_context;
                }
                if (act == null) {
                    return;
                }
                String strText = m_btn_logview.getText().toString();
                if (strText.equals(strOK)) {
                    act.enableAllUIInputs();
                    return;
                }
                act.showResetGeneralDialog();
            }
        });
    }

    public void showProgressBar() {
        boolean bShow=true;
        showProgressBar(bShow);
    }
    public void showProgressBar(boolean flag) {
        int vis=m_pb_progress.getVisibility();
        if (flag) {
            if (vis!=View.VISIBLE) {
                m_pb_progress.setVisibility(View.VISIBLE);
            }
        }
        else {
            if (vis!=View.GONE) {
                m_pb_progress.setVisibility(View.GONE);
            }
        }
    }

    public void setTitle(String strText) {
        m_tv_progress_title.setText(strText);
    }

    public void setText(String strText) {
        m_tv_progress_text.setText(strText);
    }

    public void setButtonText(String strText) {
        m_btn_logview.setText(strText);
    }

}
