package com.spectratech.sp530demo;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.spectratech.lib.ResourcesHelper;
import com.spectratech.lib.constant.BluetoothTransmissionEnum;
import com.spectratech.sp530demo.data.Data_setting_devicesp530;

import java.util.ArrayList;

/**
 * SettingDeviceSP530Activity - setting SP530 device activity
 */
public class SettingDeviceSP530Activity extends SP530DemoBaseActivity {

    private static final String m_className="SettingDeviceSP530Activity";

    private LinearLayout m_mainll;

    private Data_setting_devicesp530 m_dataSP530Config;

    private TextView m_tvAlwaysUseCRC;

    private TextView m_tvSSLChannelone;
    private TextView m_tvSSLChanneloneExplain;

//    private TextView m_tvSSLChanneltwo;

    private int m_uiIdx;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        m_mainll=(LinearLayout)getLayoutInflater().inflate(R.layout.activity_settingdevicesp530, null);
        setContentView(m_mainll);

        initListeners();

        m_dataSP530Config=new Data_setting_devicesp530(m_context);
        m_uiIdx=m_dataSP530Config.m_packetEncapsulateLevel.toInt();


        m_tvAlwaysUseCRC=(TextView)m_mainll.findViewById(R.id.settings_device_alwaysusecrc);
        if (m_tvAlwaysUseCRC!=null) {
            boolean bAlwaysUseCRC=m_dataSP530Config.m_bAlwaysUseCRC;
            int checkbox = bAlwaysUseCRC ? R.drawable.settings_checked : R.drawable.settings_check;
            m_tvAlwaysUseCRC.setCompoundDrawablesWithIntrinsicBounds(0, 0, checkbox, 0);
            if (bAlwaysUseCRC) {
                m_tvAlwaysUseCRC.setTag(true);
            }
            else {
                m_tvAlwaysUseCRC.setTag(false);
            }
        }

        m_tvSSLChannelone=(TextView)m_mainll.findViewById(R.id.settings_device_sslchone);
        if (m_tvSSLChannelone!=null) {
            boolean bSSLChOne=m_dataSP530Config.m_bSSLChOne;
            int checkbox = bSSLChOne ? R.drawable.settings_checked : R.drawable.settings_check;
            m_tvSSLChannelone.setCompoundDrawablesWithIntrinsicBounds(0, 0, checkbox, 0);
            if (bSSLChOne) {
                m_tvSSLChannelone.setTag(true);
            }
            else {
                m_tvSSLChannelone.setTag(false);
            }
        }

        int minSDK=Build.VERSION_CODES.LOLLIPOP;;
        int currentAndroidSDKVersion= Build.VERSION.SDK_INT;
        if (currentAndroidSDKVersion<minSDK) {
            boolean bSSLChOne=Data_setting_devicesp530.isSSLChannelOne(m_context);
            if (bSSLChOne) {
                m_tvSSLChanneloneExplain = (TextView) m_mainll.findViewById(R.id.settings_device_sslchone_explain);
                if (m_tvSSLChanneloneExplain != null) {
                    String strSSLChOneExplain = "(Min. SDK for enable: " + Build.VERSION_CODES.LOLLIPOP;
                    strSSLChOneExplain += ", current version: " + Build.VERSION.SDK_INT + ", please UNCHECK it!)";
                    if (m_tvSSLChannelone != null) {
                        m_tvSSLChannelone.setTextColor(Color.parseColor("#FFFF0000"));
                    }
                    m_tvSSLChanneloneExplain.setTextColor(Color.parseColor("#FFFF0000"));
                    m_tvSSLChanneloneExplain.setText(strSSLChOneExplain);
                    m_tvSSLChanneloneExplain.setVisibility(View.VISIBLE);
                }
            }
        }

//        m_tvSSLChanneltwo=(TextView)m_mainll.findViewById(R.id.settings_device_sslchtwo);
//        if (m_tvSSLChanneltwo!=null) {
//            boolean bSSLChTwo=m_dataSP530Config.m_bSSLChTwo;
//            int checkbox = bSSLChTwo ? R.drawable.settings_checked : R.drawable.settings_check;
//            m_tvSSLChanneltwo.setCompoundDrawablesWithIntrinsicBounds(0, 0, checkbox, 0);
//            if (bSSLChTwo) {
//                m_tvSSLChanneltwo.setTag(true);
//            }
//            else {
//                m_tvSSLChanneltwo.setTag(false);
//            }
//        }


        initDropDownUIs();

        boolean bLandscape=getResources().getBoolean(R.bool.is_landscape);
        if (bLandscape) {
            onOrientationLandscape();
        }
        else {
            onOrientationPortrait();
        }
    }

    private void initDropDownUIs() {
        final String strPacketEncap=ResourcesHelper.getBaseContextString(m_context, R.string.packet_encap_level);
        TextView tv = (TextView) m_mainll.findViewById(R.id.conf_trans_packet_encapsulate_level_tv);
        {
            final TextView ftv=tv;
            final String title=strPacketEncap;
            ArrayList listTitleArray=new ArrayList<String>();
            for (BluetoothTransmissionEnum.PACKET_ENCAPSULATE_LEVEL dir : BluetoothTransmissionEnum.PACKET_ENCAPSULATE_LEVEL.values()) {
                switch (dir) {
                    case RAW: {
                        listTitleArray.add("RAW");
                    }
                    break;
                    case MCP: {
                        listTitleArray.add("MCP");
                    }
                    break;
                }
            }
            final ArrayAdapter<String> adapter=new ArrayAdapter<String>(m_context, android.R.layout.simple_spinner_dropdown_item, listTitleArray);
            if ( (tv!=null)&&(adapter!=null) ) {
                setDropDownTextView(tv, adapter, m_uiIdx);
                tv.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        AlertDialog.Builder dialog=new AlertDialog.Builder(m_context);
                        dialog.setTitle(title);
                        DialogInterface.OnClickListener listener=new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                setDropDownTextView(ftv, adapter, which);
                                m_uiIdx=which;
                                dialog.dismiss();
                            }
                        };

                        if (m_uiIdx<0) {
                            dialog.setAdapter(adapter, listener).show();
                        }
                        else {
                            dialog.setSingleChoiceItems(adapter, m_uiIdx, listener).show();
                        }
                    }
                });
            }
        }
    }

    private void setDropDownTextView(TextView tv, ArrayAdapter<String> adapter, int which) {
        if (which>-1) {
            String val = adapter.getItem(which);
            tv.setText(val);
            int idx = which;
            tv.setTag("" + idx);
        }
    }

    private void initListeners() {

    }

    /**
     * On click function for saving parameters and finish this activity
     * @param v corresponding view
     */
    public void onProcess(View v) {
        final String strParamSaved=ResourcesHelper.getBaseContextString(m_context, R.string.param_saved);

        int idx=m_uiIdx;
        m_dataSP530Config.setPacketEncapsulateLevel(idx);

        boolean flag=(boolean)m_tvAlwaysUseCRC.getTag();
        m_dataSP530Config.setAlwaysUseCRC(flag);

        flag=(boolean)m_tvSSLChannelone.getTag();
        m_dataSP530Config.setSSLChannelOne(flag);

//        boolean flag=(boolean)m_tvSSLChanneltwo.getTag();
//        m_dataSP530Config.setSSLChannelTwo(flag);

        Application.FLAG_UPDATE_DATA_SETTING_DEVICESSP530_PARAMS=true;

        Toast.makeText(m_context, strParamSaved, Toast.LENGTH_SHORT).show();
        finish();
    }

    @Override
    public void onBackPressed() {
        String strCancel=ResourcesHelper.getBaseContextString(m_context, R.string.cancel);
        Toast.makeText(m_context, strCancel, Toast.LENGTH_SHORT).show();
        super.onBackPressed();
    }

    /**
     * On click function for enable/disable alway use CRC checksum
     * @param v corresponding view
     */
    public void onClickAlwaysUseCRCChecksum(View v) {
        if (m_tvAlwaysUseCRC!=null) {
            boolean flag=(boolean)m_tvAlwaysUseCRC.getTag();
            flag=!flag;
            m_tvAlwaysUseCRC.setTag(flag);
            int checkbox = flag ? R.drawable.settings_checked : R.drawable.settings_check;
            m_tvAlwaysUseCRC.setCompoundDrawablesWithIntrinsicBounds(0, 0, checkbox, 0);
        }
    }


    /**
     * On click function for enable/disable SSL connection for logical channel one
     * @param v corresponding view
     */
    public void onClickSSLChannelOne(View v) {
        if (m_tvSSLChannelone!=null) {
            boolean flag=(boolean)m_tvSSLChannelone.getTag();
            flag=!flag;
            m_tvSSLChannelone.setTag(flag);
            int checkbox = flag ? R.drawable.settings_checked : R.drawable.settings_check;
            m_tvSSLChannelone.setCompoundDrawablesWithIntrinsicBounds(0, 0, checkbox, 0);
        }
    }

//    public void onClickSSLChannelTwo(View v) {
//        if (m_tvSSLChanneltwo!=null) {
//            boolean flag=(boolean)m_tvSSLChanneltwo.getTag();
//            flag=!flag;
//            m_tvSSLChanneltwo.setTag(flag);
//            int checkbox = flag ? R.drawable.settings_checked : R.drawable.settings_check;
//            m_tvSSLChanneltwo.setCompoundDrawablesWithIntrinsicBounds(0, 0, checkbox, 0);
//        }
//    }

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
