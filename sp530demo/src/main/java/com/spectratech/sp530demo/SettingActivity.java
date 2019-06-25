package com.spectratech.sp530demo;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.spectratech.lib.AndroidHelper;
import com.spectratech.lib.ResourcesHelper;
import com.spectratech.lib.constant.SpectratechConstant;
import com.spectratech.sp530demo.controller.HouseKeepHelper;
import com.spectratech.sp530demo.data.Data_setting_devicebluetoothprinter;

/**
 * SettingActivity - setting activity for SP530 demo
 */
public class SettingActivity extends SP530DemoBaseActivity {
    private static final String m_className="SettingActivity";

    private LinearLayout m_mainll;
    private ActionBar m_actionBar;

    private LinearLayout m_btnGoWeb;

    private LinearLayout m_setting_bluetooth_printer_container;
    private LinearLayout m_setting_bluetooth_printerimage_container;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final String strVersion=ResourcesHelper.getBaseContextString(m_context, R.string.version_wcolon);
        final String strBuild=ResourcesHelper.getBaseContextString(m_context, R.string.build_wcolon);

        m_mainll=(LinearLayout)getLayoutInflater().inflate(R.layout.activity_setting, null);
        setContentView(m_mainll);

        m_setting_bluetooth_printer_container=(LinearLayout)m_mainll.findViewById(R.id.setting_bluetooth_printer_container);
        m_setting_bluetooth_printerimage_container=(LinearLayout)m_mainll.findViewById(R.id.setting_bluetooth_printerimage_container);

        m_actionBar= getActionBar();
        setActionBarDefaultStyle_noTab(m_actionBar);

        AndroidHelper instAndroidHelper= AndroidHelper.getInstance();
        TextView tv=(TextView)m_mainll.findViewById(R.id.setting_versionname);
        if (tv!=null) {
            String tmp=strVersion;
            String strVersionName=instAndroidHelper.getVersionName(m_context);
            tmp+=strVersionName;
            tv.setText(tmp);
        }
        TextView tvVersionCode=(TextView)m_mainll.findViewById(R.id.setting_versioncode);
        if (tvVersionCode!=null){
            String strBuildVersion=""+instAndroidHelper.getVersion(m_context);
            if ( (strBuildVersion!=null)&&(!strBuildVersion.equals("")) ) {
                String tmp=strBuild;
                tmp+=strBuildVersion;
                tvVersionCode.setText(tmp);
            }
        }

        /*m_btnGoWeb=(LinearLayout)m_mainll.findViewById(R.id.btn_spectratech_web);
        if (m_btnGoWeb!=null) {
            m_btnGoWeb.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String url = SpectratechConstant.URL_SPECTRATECH;
                    Uri uriUrl = Uri.parse(url);
                    Intent launchBrowser = new Intent(Intent.ACTION_VIEW, uriUrl);
                    startActivity(launchBrowser);
                }
            });
        }*/

        boolean bLandscape=getResources().getBoolean(R.bool.is_landscape);
        if (bLandscape) {
            onOrientationLandscape();
        }
        else {
            onOrientationPortrait();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (m_setting_bluetooth_printerimage_container!=null) {
            if (Data_setting_devicebluetoothprinter.isEnableBTPrinter(m_context)) {
                if (m_setting_bluetooth_printerimage_container.getVisibility()!=View.VISIBLE) {
                    m_setting_bluetooth_printerimage_container.setVisibility(View.VISIBLE);
                }
            }
            else {
                if (m_setting_bluetooth_printerimage_container.getVisibility()!=View.GONE) {
                    m_setting_bluetooth_printerimage_container.setVisibility(View.GONE);
                }
            }
        }
    }

    @Override
    public void onBackPressed() {
        endActivity();
    }

    private void endActivity() {
        this.finish();
        overridePendingTransition(0, 0);
    }

    /**
     * On click function for about us
     * @param v corresponding view
     */
    public void aboutUS(View v) {
        if (!isOnPause()) {
            String url=SpectratechConstant.URL_SPECTRATECH;
            Application.startActivity(m_context, Application.ACTIVITY_WEBVIEW, url);
        }
    }

    /**
     * On click function for setting general
     * @param v corresponding view
     */
    public void onclick_settinggeneral(View v) {
        if (!isOnPause()) {
            int id=Application.ACTIVITY_SETTINGGENERAL;
            Application.startActivity(m_context, id);
        }
    }

    public void onclick_settinglanguage(View v) {
        if (!isOnPause()) {
            int id=Application.ACTIVITY_SETTINGLANGUAGE;
            Application.startActivity(m_context, id);
        }
    }

    /**
     * On click function for setting device t1000
     * @param v corresponding view
     */
    public void onclick_settingdevicespt1000(View v) {
        if (!isOnPause()) {
            int id=Application.ACTIVITY_SETTTINGDEVICET1000;
            Application.startActivity(m_context, id);
        }
    }

    /**
     * On click function for setting device sp530
     * @param v corresponding view
     */
    public void onclick_settingdevicesp530(View v) {
        if (!isOnPause()) {
            int id=Application.ACTIVITY_SETTINGDEVICESP530;
            Application.startActivity(m_context, id);
        }
    }

    /**
     * On click function for setting device sp530
     * @param v corresponding view
     */
    public void onclick_settingsp530sslcert(View v) {
        if (!isOnPause()) {
            int id=Application.ACTIVITY_SETTINSP530SSLCERT;
            Application.startActivity(m_context, id);
        }
    }

    /**
     * On click function for setting transaction
     * @param v corresponding view
     */
    public void onclick_settingtransaction(View v) {
        if (!isOnPause()) {
            int id=Application.ACTIVITY_SETTINGTRANSACTION;
            Application.startActivity(m_context, id);
        }
    }

    /**
     * On click function for setting mutu auth
     * @param v corresponding view
     */
    public void onclick_settingtransactionmutualauth(View v) {
        if (!isOnPause()) {
            int id=Application.ACTIVITY_SETTINGTRANSACTIONMUTUAUTH;
            Application.startActivity(m_context, id);
        }
    }

    /**
     * On click function for setting Bluetooth printer
     * @param v corresponding view
     */
    public void onclick_settingdevicebtprinter(View v) {
        if (!isOnPause()) {
            int id=Application.ACTIVITY_SETTINGBLUETOOTHPRINTER;
            Application.startActivity(m_context, id);
        }
    }

    /**
     * On click function for setting Bluetooth printer image
     * @param v corresponding view
     */
    public void onclick_settingdevicebtprinterimage(View v) {
        if (!isOnPause()) {
            int id=Application.ACTIVITY_SETTINGBLUETOOTHPRINTER_GENIMAGE;
            Application.startActivity(m_context, id);
        }
    }

    public void onclick_deviceselect(View v) {
        if (!isOnPause()) {
            int id_act = Application.ACTIVITY_DEVICESELECT;
            Application.startActivity(m_context, id_act);
        }
    }

    /**
     * Function for clear records before seven days ago in db
     * @param v corresponding view
     */
    public void clearCacheBeforeSevenDays(View v) {
        final String strAreUSure=ResourcesHelper.getBaseContextString(m_context, R.string.are_u_sure);
        final String strYes=ResourcesHelper.getBaseContextString(m_context, R.string.yes);
        final String strNo=ResourcesHelper.getBaseContextString(m_context, R.string.no);

        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case DialogInterface.BUTTON_POSITIVE:
                        //Yes button clicked

                        long ageInSec=(7 * 24 * 60*60);	// 7days
                        HouseKeepHelper instHouseKeepHelper=HouseKeepHelper.getInstance();
                        instHouseKeepHelper.clearTransHistoryOlderThan(m_context, ageInSec);

                        Toast.makeText(m_context, R.string.clear_cache_before7days_finish, Toast.LENGTH_SHORT).show();

                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        //No button clicked
                        break;
                }
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(m_context);
        builder.setMessage(strAreUSure+"?").setPositiveButton(strYes, dialogClickListener)
                .setNegativeButton(strNo, dialogClickListener).show();
    }

    /**
     * Function for clear all records in db
     * @param v corresponding view
     */
    public void clearCache(View v) {
        final String strAreUSure=ResourcesHelper.getBaseContextString(m_context, R.string.are_u_sure);
        final String strYes=ResourcesHelper.getBaseContextString(m_context, R.string.yes);
        final String strNo=ResourcesHelper.getBaseContextString(m_context, R.string.no);

        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case DialogInterface.BUTTON_POSITIVE:
                        //Yes button clicked

                        HouseKeepHelper instHouseKeepHelper=HouseKeepHelper.getInstance();
                        instHouseKeepHelper.clearTransHistory(m_context);

                        Toast.makeText(m_context, R.string.clear_cache_finish, Toast.LENGTH_SHORT).show();

                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        //No button clicked
                        break;
                }
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(m_context);
        builder.setMessage(strAreUSure+"?").setPositiveButton(strYes, dialogClickListener)
                .setNegativeButton(strNo, dialogClickListener).show();
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
