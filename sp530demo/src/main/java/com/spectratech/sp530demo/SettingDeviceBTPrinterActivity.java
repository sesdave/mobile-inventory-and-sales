package com.spectratech.sp530demo;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.spectratech.lib.ResourcesHelper;
import com.spectratech.lib.constant.BluetoothTransmissionEnum;
import com.spectratech.sp530demo.data.Data_setting_devicebluetoothprinter;

import java.util.ArrayList;

/**
 * SettingDeviceBTPrinterActivity - setting Bluetooth printer activity
 */
public class SettingDeviceBTPrinterActivity extends SP530DemoBaseActivity {

    private static final String m_className="SettingDeviceBTPrinterActivity";

    private LinearLayout m_mainll;

    private Data_setting_devicebluetoothprinter m_dataBTPrinterConfig;

    private TextView m_tvSecureRfcommSocket;

    private TextView m_tvEnableBTPrinter;

    private int m_uiIdx;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        m_mainll=(LinearLayout)getLayoutInflater().inflate(R.layout.activity_settingdevicebluetoothprinter, null);
        setContentView(m_mainll);

        initListeners();

        m_dataBTPrinterConfig=new Data_setting_devicebluetoothprinter(m_context);

        m_tvSecureRfcommSocket=(TextView)m_mainll.findViewById(R.id.settings_btsecurerfcomm_enable);
        if (m_tvSecureRfcommSocket!=null) {
            boolean bUseSecureRfcommSocket=m_dataBTPrinterConfig.m_bUseSecureRfcommSocket;
            int checkbox = bUseSecureRfcommSocket ? R.drawable.settings_checked : R.drawable.settings_check;
            m_tvSecureRfcommSocket.setCompoundDrawablesWithIntrinsicBounds(0, 0, checkbox, 0);
            if (bUseSecureRfcommSocket) {
                m_tvSecureRfcommSocket.setTag(true);
            }
            else {
                m_tvSecureRfcommSocket.setTag(false);
            }
        }

        m_uiIdx=m_dataBTPrinterConfig.m_packetEncapsulateLevel.toInt();

        // for skipping mcp please
        if (m_uiIdx>1) {
            m_uiIdx--;
        }

        initDropDownUIs();


        m_tvEnableBTPrinter=(TextView)m_mainll.findViewById(R.id.settings_btprinter_enable);
        if (m_tvEnableBTPrinter!=null) {
            boolean bEnableBTPrinter=m_dataBTPrinterConfig.m_bEnableBTPrinter;
            int checkbox = bEnableBTPrinter ? R.drawable.settings_checked : R.drawable.settings_check;
            m_tvEnableBTPrinter.setCompoundDrawablesWithIntrinsicBounds(0, 0, checkbox, 0);
            if (bEnableBTPrinter) {
                m_tvEnableBTPrinter.setTag(true);
            }
            else {
                m_tvEnableBTPrinter.setTag(false);
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

    private void initDropDownUIs() {
        final String strSetting=ResourcesHelper.getBaseContextString(m_context, R.string.setting);
        TextView tv = (TextView) m_mainll.findViewById(R.id.conf_trans_packet_encapsulate_level_tv);
        {
            final TextView ftv=tv;
            final String title=strSetting;
            ArrayList listTitleArray=new ArrayList<String>();
            for (BluetoothTransmissionEnum.PACKET_ENCAPSULATE_LEVEL dir : BluetoothTransmissionEnum.PACKET_ENCAPSULATE_LEVEL.values()) {
                switch (dir) {
                    case RAW: {
                        listTitleArray.add("RAW");
                    }
                    break;
//                    case MCP: {
//                        listTitleArray.add("MCP");
//                    }
//                    break;
                    case SOH: {
                        listTitleArray.add("SOH");
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

    public void onClickEnableBTSecureRfcommSocket(View v) {
        if (m_tvSecureRfcommSocket!=null) {
            boolean bUseSecureRfcommSocket=(boolean)m_tvSecureRfcommSocket.getTag();
            bUseSecureRfcommSocket=!bUseSecureRfcommSocket;
            m_tvSecureRfcommSocket.setTag(bUseSecureRfcommSocket);
            int checkbox = bUseSecureRfcommSocket ? R.drawable.settings_checked : R.drawable.settings_check;
            m_tvSecureRfcommSocket.setCompoundDrawablesWithIntrinsicBounds(0, 0, checkbox, 0);
        }
    }

    /**
     * On click function for saving parameters and finish this activity
     * @param v corresponding view
     */
    public void onProcess(View v) {
        final String strParamSaved=ResourcesHelper.getBaseContextString(m_context, R.string.param_saved);

        boolean bUseSecureRfcommSocket=(boolean)m_tvSecureRfcommSocket.getTag();
        m_dataBTPrinterConfig.setUseSecureRfcommSocket(bUseSecureRfcommSocket);

        // set secure rfcomm socket
        BTPrinterBluetoothConnectActivity.setSecureRfcommSocket(bUseSecureRfcommSocket);

        int idx=m_uiIdx;

        // for skipping mcp please
        if (idx>0) {
            idx++;
        }

        m_dataBTPrinterConfig.setPacketEncapsulateLevel(idx);

        boolean flag=(boolean)m_tvEnableBTPrinter.getTag();
        m_dataBTPrinterConfig.setEnableBTPrinter(flag);

        Application.FLAG_UPDATE_DATA_SETTING_BTPRINTER_PARAMS=true;

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

    /**
     * On click function for enable/disable Bluetooth printer
     * @param v corresponding view
     */
    public void onClickEnableBTPrinter(View v) {
        if (m_tvEnableBTPrinter!=null) {
            boolean flag=(boolean)m_tvEnableBTPrinter.getTag();
            flag=!flag;
            m_tvEnableBTPrinter.setTag(flag);
            int checkbox = flag ? R.drawable.settings_checked : R.drawable.settings_check;
            m_tvEnableBTPrinter.setCompoundDrawablesWithIntrinsicBounds(0, 0, checkbox, 0);
        }
    }
}
