package com.spectratech.sp530demo.view;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.spectratech.sp530demo.DemoMainActivity;
import com.spectratech.sp530demo.R;

/**
 * S3AuthFragment - Fragment for mutu auth
 */
public class S3AuthFragment extends S3CommonFragment {

    private static final String m_className="S3AuthFragment";

    private boolean m_bPause;

    private ViewS3AuthLinearLayout m_viewS3Authll;

    @Override
    public void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Activity act=getActivity();
        m_context=(Context)act;
        m_bPause=false;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        m_viewS3Authll=(ViewS3AuthLinearLayout)inflater.inflate(R.layout.view_s3auth, container, false);
        View rootView = m_viewS3Authll;
        m_viewS3Authll.setDataRuntimeS3Auth(DemoMainActivity.m_dataRunTimeS3Auth);
        Bundle args = getArguments();
        m_viewS3Authll.initUIs();
        return rootView;
    }

    @Override
    public void onDestroyView() {
        if (m_viewS3Authll!=null) {
            m_viewS3Authll.updateRuntimeParameters();
        }
        super.onDestroyView();
    }


    @Override
    public void onPause() {
        super.onPause();
        m_bPause=true;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (m_bPause) {
            m_bPause=false;
        }
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);

        // Make sure that we are currently visible
        if (this.isVisible()) {
            if (isVisibleToUser) {
                m_viewS3Authll.refreshUIs();
            }
        }
    }
}
