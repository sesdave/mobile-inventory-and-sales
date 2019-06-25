package com.spectratech.sp530demo;

import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.spectratech.lib.ResourcesHelper;
import com.spectratech.sp530demo.data.Data_setting_devicet1000;

/**
 * SettingDeviceT1000Activity - setting T1000 device activity
 */
public class SettingDeviceT1000Activity extends SP530DemoBaseActivity {

    private static final String m_className="SettingDeviceSP530Activity";

    private LinearLayout m_mainll;
    private EditText m_ipone_et;
    private EditText m_iptwo_et;
    private EditText m_ipthree_et;
    private EditText m_ipfour_et;
    private EditText m_port_et;

    private SeekBar m_connectiontimeout_seekbar;
    private TextView m_connectiontimeout_tv;
    private SeekBar.OnSeekBarChangeListener m_listenConnectionTimeoutSeek;

    private int m_uiValConnectionTimeout;

    private Data_setting_devicet1000 m_dataT1000Config;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        m_mainll=(LinearLayout)getLayoutInflater().inflate(R.layout.activity_settingdevicet1000, null);
        setContentView(m_mainll);

        // focus me please
        m_mainll.setFocusable(true);
        m_mainll.setFocusableInTouchMode(true);
        m_mainll.requestFocus();

        initListeners();

        m_ipone_et=(EditText)m_mainll.findViewById(R.id.val_ipone);
        m_iptwo_et=(EditText)m_mainll.findViewById(R.id.val_iptwo);
        m_ipthree_et=(EditText)m_mainll.findViewById(R.id.val_ipthree);
        m_ipfour_et=(EditText)m_mainll.findViewById(R.id.val_ipfour);
        m_port_et=(EditText)m_mainll.findViewById(R.id.val_port);

        m_connectiontimeout_seekbar=(SeekBar) m_mainll.findViewById(R.id.conf_connectiontimeout);
        m_connectiontimeout_seekbar.setOnSeekBarChangeListener(m_listenConnectionTimeoutSeek);
        m_connectiontimeout_tv=(TextView)m_mainll.findViewById(R.id.conf_connectiontimeout_tv);

        m_dataT1000Config=new Data_setting_devicet1000(m_context);
        setUIParams();

        boolean bLandscape=getResources().getBoolean(R.bool.is_landscape);
        if (bLandscape) {
            onOrientationLandscape();
        }
        else {
            onOrientationPortrait();
        }
    }

    private void setUIParams() {
        m_ipone_et.setText(""+m_dataT1000Config.m_ipArray[0]);
        m_iptwo_et.setText(""+m_dataT1000Config.m_ipArray[1]);
        m_ipthree_et.setText(""+m_dataT1000Config.m_ipArray[2]);
        m_ipfour_et.setText(""+m_dataT1000Config.m_ipArray[3]);

        m_port_et.setText(""+m_dataT1000Config.m_port);

        m_uiValConnectionTimeout= map2seekbar_connectiontimeout_value(m_dataT1000Config.m_connectionTimeoutInS);
        setMaxIterSeekBar(m_uiValConnectionTimeout);
    }

    private void setMaxIterSeekBar(int val) {
        m_connectiontimeout_seekbar.setProgress(val);
        int valReal=map2rea_connectiontimeout_value(val);
        m_connectiontimeout_tv.setText("" + valReal);
    }

    private int map2seekbar_connectiontimeout_value(int val) {
        int result= val- Data_setting_devicet1000.DEFAULT_MIN_CONNECTION_TIMEOUT_INS;
        return result;
    }
    private int map2rea_connectiontimeout_value(int val) {
        int result= val+Data_setting_devicet1000.DEFAULT_MIN_CONNECTION_TIMEOUT_INS;
        return result;
    }


    private void initListeners() {
        m_listenConnectionTimeoutSeek=new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                m_uiValConnectionTimeout=progress;
                int valReal=map2rea_connectiontimeout_value(m_uiValConnectionTimeout);
                m_connectiontimeout_tv.setText(""+valReal);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }

        };

    }

    private boolean validInputParams() {
        boolean bRet=true;
        int val=-1;
        EditText[] edArray=new EditText[] { m_ipone_et, m_iptwo_et, m_ipthree_et, m_ipfour_et };
        String tmp="";
        for (int i=0; i<4; i++) {
            tmp = edArray[i].getText().toString();
            try {
                val = Integer.parseInt(tmp);
            } catch (Exception ex) {
                val = -1;
            }
            if ((val < 0) || (val > 255)) {
                Toast.makeText(m_context, "IP field "+(i+1)+" is invalid", Toast.LENGTH_SHORT).show();
                bRet=false;
                return bRet;
            }
        }

        tmp=m_port_et.getText().toString();
        try {
            val = Integer.parseInt(tmp);
        } catch (Exception ex) {
            val = -1;
        }
        if ((val < 1025) || (val > 65535)) {
            Toast.makeText(m_context, "Port field is invalid", Toast.LENGTH_SHORT).show();
            bRet=false;
            return bRet;
        }

        return bRet;
    }

    private void saveParams() {
        int[] ipArray=new int [4];
        int val=-1;
        EditText[] edArray=new EditText[] { m_ipone_et, m_iptwo_et, m_ipthree_et, m_ipfour_et };
        String tmp="";
        for (int i=0; i<4; i++) {
            tmp = edArray[i].getText().toString();
            try {
                val = Integer.parseInt(tmp);
            } catch (Exception ex) {
                val = -1;
            }
            ipArray[i]=val;
        }
        m_dataT1000Config.setIPArray(ipArray);

        tmp=m_port_et.getText().toString();
        try {
            val = Integer.parseInt(tmp);
        } catch (Exception ex) {
            val = -1;
        }
        m_dataT1000Config.setPort(val);

        val=map2rea_connectiontimeout_value(m_uiValConnectionTimeout);
        m_dataT1000Config.setConnectionTimeout(val);
    }

    /**
     * On click function for saving parameters and finish this activity
     * @param v corresponding view
     */
    public void onProcess(View v) {
        final String strParamSaved= ResourcesHelper.getBaseContextString(m_context, R.string.param_saved);

        boolean bValid=validInputParams();
        if (!bValid) {
            return;
        }
        saveParams();

        Application.FLAG_UPDATE_DATA_SETTING_DEVICEST1000_PARAMS=true;

        Toast.makeText(m_context, strParamSaved, Toast.LENGTH_SHORT).show();
        finish();
    }

    @Override
    public void onBackPressed() {
        String strCancel=ResourcesHelper.getBaseContextString(m_context, R.string.cancel);
        Toast.makeText(m_context, strCancel, Toast.LENGTH_SHORT).show();
        super.onBackPressed();
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
