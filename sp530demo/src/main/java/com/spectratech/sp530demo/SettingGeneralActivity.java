package com.spectratech.sp530demo;

import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.spectratech.lib.ResourcesHelper;
import com.spectratech.sp530demo.data.Data_setting_general;

/**
 * SettingGeneralActivity - setting general activity
 */
public class SettingGeneralActivity extends SP530DemoBaseActivity {

    private static final String m_className="SettingGeneralActivity";

    private LinearLayout m_mainll;

    private TextView m_tvDebugMode;
    private TextView m_tvTransUIShowMore;
    private TextView m_tvSendEchoWhenConnected;

    private Data_setting_general m_dataSettingGeneral;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        m_mainll=(LinearLayout)getLayoutInflater().inflate(R.layout.activity_settinggeneral, null);
        setContentView(m_mainll);

        m_dataSettingGeneral=new Data_setting_general(m_context);

        m_tvDebugMode=(TextView)m_mainll.findViewById(R.id.settings_debugmode);
        if (m_tvDebugMode!=null) {
            boolean bTransactionUIShowMore=m_dataSettingGeneral.m_bDebugMode;
            int checkbox = bTransactionUIShowMore ? R.drawable.settings_checked : R.drawable.settings_check;
            m_tvDebugMode.setCompoundDrawablesWithIntrinsicBounds(0, 0, checkbox, 0);
            if (bTransactionUIShowMore) {
                m_tvDebugMode.setTag(true);
            }
            else {
                m_tvDebugMode.setTag(false);
            }
        }

        m_tvTransUIShowMore=(TextView)m_mainll.findViewById(R.id.settings_transactionshowuimore);
        if (m_tvTransUIShowMore!=null) {
            boolean bTransactionUIShowMore=m_dataSettingGeneral.m_bTransactionUIShowMore;
            int checkbox = bTransactionUIShowMore ? R.drawable.settings_checked : R.drawable.settings_check;
            m_tvTransUIShowMore.setCompoundDrawablesWithIntrinsicBounds(0, 0, checkbox, 0);
            if (bTransactionUIShowMore) {
                m_tvTransUIShowMore.setTag(true);
            }
            else {
                m_tvTransUIShowMore.setTag(false);
            }
        }

        m_tvSendEchoWhenConnected=(TextView)m_mainll.findViewById(R.id.settings_sendecho_connected);
        if (m_tvSendEchoWhenConnected!=null) {
            boolean bSendEchoWhenConnected=m_dataSettingGeneral.m_bSendEchoWhenConnected;
            int checkbox = bSendEchoWhenConnected ? R.drawable.settings_checked : R.drawable.settings_check;
            m_tvSendEchoWhenConnected.setCompoundDrawablesWithIntrinsicBounds(0, 0, checkbox, 0);
            if (bSendEchoWhenConnected) {
                m_tvSendEchoWhenConnected.setTag(true);
            }
            else {
                m_tvSendEchoWhenConnected.setTag(false);
            }
        }

        boolean bLandscape=getResources().getBoolean(R.bool.is_landscape);
        if (bLandscape) {
            onOrientationLandscape();
        }
        else {
            onOrientationPortrait();
        }
    }

    @Override
    public void onBackPressed() {
        String strCancel=ResourcesHelper.getBaseContextString(m_context, R.string.cancel);
        Toast.makeText(m_context, strCancel, Toast.LENGTH_SHORT).show();
        super.onBackPressed();
    }

    /**
     * On click function for saving parameters and finish this activity
     * @param v corresponding view
     */
    public void onProcess(View v) {
        final String strParamSaved= ResourcesHelper.getBaseContextString(m_context, R.string.param_saved);

        boolean flag=false;

        flag=(boolean)m_tvDebugMode.getTag();
        m_dataSettingGeneral.setDebugMode(flag);

        flag=(boolean)m_tvTransUIShowMore.getTag();
        m_dataSettingGeneral.setTransactionUIShowMore(flag);

        flag=(boolean)m_tvSendEchoWhenConnected.getTag();
        m_dataSettingGeneral.setSendEchoWhenConnected(flag);

        Application.FLAG_UPDATE_DATA_SETTING_GENERAL_PARAMS=true;

        Toast.makeText(m_context, strParamSaved, Toast.LENGTH_SHORT).show();
        finish();
    }

    /**
     * On click function for enable/disable debug mode
     * @param v corresponding view
     */
    public void onClickDebugMode(View v) {
        if (m_tvDebugMode!=null) {
            boolean flag=(boolean)m_tvDebugMode.getTag();
            flag=!flag;
            m_tvDebugMode.setTag(flag);
            int checkbox = flag ? R.drawable.settings_checked : R.drawable.settings_check;
            m_tvDebugMode.setCompoundDrawablesWithIntrinsicBounds(0, 0, checkbox, 0);
        }
    }

    /**
     * On click function for enable/disable show more Ui on main screen
     * @param v corresponding view
     */
    public void onTransactionShowUIMore(View v){
		if (m_tvTransUIShowMore!=null) {
            boolean flag=(boolean)m_tvTransUIShowMore.getTag();
            flag=!flag;
            m_tvTransUIShowMore.setTag(flag);
			int checkbox = flag ? R.drawable.settings_checked : R.drawable.settings_check;
            m_tvTransUIShowMore.setCompoundDrawablesWithIntrinsicBounds(0, 0, checkbox, 0);
		}
    }

    public void onSendEchoWhenConnected(View v) {
        if (m_tvSendEchoWhenConnected!=null) {
            boolean flag=(boolean)m_tvSendEchoWhenConnected.getTag();
            flag=!flag;
            m_tvSendEchoWhenConnected.setTag(flag);
            int checkbox = flag ? R.drawable.settings_checked : R.drawable.settings_check;
            m_tvSendEchoWhenConnected.setCompoundDrawablesWithIntrinsicBounds(0, 0, checkbox, 0);
        }
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
