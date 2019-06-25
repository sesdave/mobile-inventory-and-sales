package com.spectratech.sp530demo.view;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.spectratech.lib.DisplayHelper;
import com.spectratech.lib.ResourcesHelper;
import com.spectratech.sp530demo.DemoMainActivity;
import com.spectratech.sp530demo.R;

/**
 * S3TransOverlayFragment - overlay view - fragment for SP530 transaction
 */
public class S3TransOverlayFragment extends Fragment {

    private static final String m_className="S3TransOverlayFragment";

    protected Activity m_act;
    protected Context m_context;

    private LinearLayout m_mainll;

    private TextView m_popup_transtv;
    private TextView m_popup_transtv_sub;
    private Button m_popup_transbtn;

    @Override
    public void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        m_act=getActivity();
        m_context=(Context)m_act;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        m_mainll=(LinearLayout)inflater.inflate(R.layout.view_s3transoverlay, null);
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        m_mainll.setLayoutParams(lp);
        initOverlayView(inflater);
        return m_mainll;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {

    }

    private void initOverlayView(LayoutInflater inflater) {
        final String strOK=ResourcesHelper.getBaseContextString(m_context, R.string.ok);

        m_mainll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });

        LinearLayout ll=(LinearLayout)inflater.inflate(R.layout.view_overlay_text, null);
        m_popup_transtv=(TextView)ll.findViewById(R.id.text_popup_trans);
        m_popup_transtv_sub=(TextView)ll.findViewById(R.id.text_popup_trans_sub);
        m_popup_transbtn=(Button)ll.findViewById(R.id.btn_popup_trans);
        m_popup_transbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DemoMainActivity act=null;
                if (m_context instanceof DemoMainActivity) {
                    act=(DemoMainActivity)m_context;
                }
                if (act==null) {
                    return;
                }

                String strText = m_popup_transbtn.getText().toString();
                if (strText.equals(strOK)) {
                    act.enableAllUIInputsRunOnUiThread();
                    return;
                }

                act.showCancelTransactionDialog();
            }
        });
        DisplayHelper instDisplayHelper= DisplayHelper.getInstance();
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        int margin=30;
        margin=instDisplayHelper.convertPixelsToDp(m_context, margin);
        lp.setMargins(margin, margin, margin, margin);
        m_mainll.setGravity(Gravity.CENTER);
        m_mainll.addView(ll, lp);
    }

    /**
     * Set sub message to message in pop up dialog by post
     * @param strMsg message going to set
     */
    public void setSubPopupTransMessageByPost(final String strMsg) {
        m_popup_transtv_sub.post(new Runnable() {
            @Override
            public void run() {
                setSubPopupTransMessage(strMsg);
            }
        });
    }

    /**
     * Set sub message to message in pop up dialog
     * @param strMsg message going to set
     */
    public void setSubPopupTransMessage(String strMsg) {
        int iVis=m_popup_transtv_sub.getVisibility();
        if (iVis!= View.VISIBLE) {
            m_popup_transtv_sub.setVisibility(View.VISIBLE);
        }
        m_popup_transtv_sub.setText(strMsg);
    }

    /**
     * Append message to message in pop up dialog by post
     * @param strAppend message going to append
     */
    public void append2PopupTransMessageByPost(String strAppend) {
        String strTmp=m_popup_transtv.getText().toString();
        strTmp+=", "+strAppend;
        setPopupTransMessageByPost(strTmp);
    }
    /**
     * Set message to message in pop up dialog by post
     * @param strMsg message going to set
     */
    public void setPopupTransMessageByPost(final String strMsg) {
        m_popup_transtv.post(new Runnable() {
            @Override
            public void run() {
                m_popup_transtv.setText(strMsg);
            }
        });
    }
    /**
     * Set message to message in pop up dialog
     * @param strMsg message going to set
     */
    public void setPopupTransMessage(String strMsg) {
        m_popup_transtv.setText(strMsg);
    }

    public void setTransButtonText(String strText) {
        m_popup_transbtn.setText(strText);
    }
}
