package com.spectratech.sp530demo;

import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.spectratech.lib.ResourcesHelper;
import com.spectratech.sp530demo.view.ViewS3AuthLinearLayout;

/**
 * SettingTransactionMutuAuthActivity - setting mutu auth activity
 */
public class SettingTransactionMutuAuthActivity extends SP530DemoBaseActivity {

    private static final String m_className = "SettingTransactionMutuAuthActivity";

    private ViewS3AuthLinearLayout m_mainll;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        m_mainll = (ViewS3AuthLinearLayout) getLayoutInflater().inflate(R.layout.view_s3auth, null);
        setContentView(m_mainll);

        m_mainll.setDataRuntimeS3Auth(DemoMainActivity.m_dataRunTimeS3Auth);
        m_mainll.initUIs();

        m_mainll.hideMutuAuthControl();
        m_mainll.showSaveButton();

        boolean bLandscape=getResources().getBoolean(R.bool.is_landscape);
        if (bLandscape) {
            onOrientationLandscape();
        }
        else {
            onOrientationPortrait();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    public void onProcess(View v) {
        final String strParamSaved= ResourcesHelper.getBaseContextString(m_context, R.string.param_saved);

        if (m_mainll!=null) {
            m_mainll.updateRuntimeParameters();
        }

        Toast.makeText(m_context, strParamSaved, Toast.LENGTH_SHORT).show();
        finish();
    }

    private void onOrientationPortrait() {
        // not full screen mode
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    private void onOrientationLandscape() {
        // full screen mode
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        switch (newConfig.orientation) {
            case Configuration.ORIENTATION_PORTRAIT: {
                onOrientationPortrait();
            }
            break;
            case Configuration.ORIENTATION_LANDSCAPE: {
                onOrientationLandscape();
            }
            break;
        }
    }
}
