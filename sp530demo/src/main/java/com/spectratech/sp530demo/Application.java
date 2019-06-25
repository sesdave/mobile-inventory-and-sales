package com.spectratech.sp530demo;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import com.spectratech.lib.Logger;
import com.spectratech.lib.data.Data_dbTransSummaryDetail;
import com.spectratech.sp530demo.controller.ActivityHelper;
import com.spectratech.sp530demo.data.Data_logdatapacket;
import com.spectratech.sp530demo.view.S3LogOverlayFragment;
import com.spectratech.sp530demo.view.S3TemplateFragment;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Application - class helps to start an activity
 */
public class Application {
    private static final String m_className = "Application";

    private static Application instance;

    public static HashMap<String, String> hashUrlSchemeForDemoMain;

    /**
     * Variable to store rx msg for mutu auth
     */
    public static byte[] RxmsgForMutuAuth;
    /**
     * Variable to store key for mutu auth
     */
    public static byte[] SkeyForMutuAuth;

    /**
     * Variable to log receive packet
     */
    public static List<Data_logdatapacket> logPacketList;

    /**
     * Variable to store Data_dbTransSummaryDetail for showing
     */
    public static List<Data_dbTransSummaryDetail> dbTransSummaryDetailList;

    /**
     * Variable to store Data_dbTransSummaryDetail for showing in TransReceiptActivity
     */
    public static List<Data_dbTransSummaryDetail> dbTransSummaryDetailListForTransReceipt;

    public static boolean FLAG_UPDATE_DATA_SETTING_GENERAL_PARAMS      = false;
    public static boolean FLAG_UPDATE_DATA_SETTING_DEVICEST1000_PARAMS = false;
    public static boolean FLAG_UPDATE_DATA_SETTING_DEVICESSP530_PARAMS = false;
    public static boolean FLAG_UPDATE_DATA_SETTING_TRANSACTION_PARAMS  = false;
    public static boolean FLAG_UPDATE_DATA_SETTING_BTPRINTER_PARAMS    = false;
    public static boolean FLAG_UPDATE_DATA_SETTING_SSLCERT_PARAMS      = false;

    public static final int ACTIVITY_DEMO_MAIN                          = 101;
    public static final int ACTIVITY_BLUETOOTH_CONNECT                  = 102;
    public static final int ACTIVITY_SETTING                            = 103;
    public static final int ACTIVITY_WEBVIEW                            = 104;
    public static final int ACTIVITY_WEBVIEW_FULLSCN                    = 105;
    public static final int ACTIVITY_SETTINGTRANSACTION                 = 106;
    public static final int ACTIVITY_TRANSSUMMARY                       = 107;
    public static final int ACTIVITY_TRANSSUMMARYDETAIL                 = 108;
    public static final int ACTIVITY_VIEWLOG                            = 109;
    public static final int ACTIVITY_TRANSRECEIPT                       = 110;
    public static final int ACTIVITY_TRANSRECEIPTTOUCHIMAGESINGLE       = 111;
    public static final int ACTIVITY_SETTINGDEVICESP530                 = 112;
    public static final int ACTIVITY_TRANSRECEIPT_SIGNATURE             = 113;
    public static final int ACTIVITY_SETTINGGENERAL                     = 114;
    public static final int ACTIVITY_SETTINGBLUETOOTHPRINTER            = 115;
    public static final int ACTIVITY_BTPRINTER_BLUETOOTH_CONNECT        = 116;
    public static final int ACTIVITY_SETTINGTRANSACTIONMUTUAUTH         = 117;
    public static final int ACTIVITY_SETTINGBLUETOOTHPRINTER_GENIMAGE   = 118;
    public static final int ACTIVITY_DEVICESELECT                       = 119;
    public static final int ACTIVITY_SETTINSP530SSLCERT                 = 120;
    public static final int ACTIVITY_SETTINGLANGUAGE                    = 130;

    public static final int ACTIVITY_SETTTINGDEVICET1000                = 1000;
    public static final int ACTIVITY_T1000_SOCKET_CONNECT               = 1001;

    private Application() {
    }

    /**
     * Returns Application instance
     * @return static Application instance
     */
    public static Application getInstance() {
        if (instance == null) {
            instance = new Application();
        }
        return instance;
    }

    /**
     * Start an activity with id
     * @param ctx context of application
     * @param id activity id
     */
    public static void startActivity(Context ctx, int id) {
        startActivity(ctx, id, "");
    }

    /**
     * Start an activity with id
     * @param ctx context of application
     * @param id activity id
     * @param data data to be passed to activity
     */
    public static void startActivity(Context ctx, int id, Object data) {
        startActivity(ctx, id, data, null);
    }

    /**
     * Start an activity with id
     * @param ctx context of application
     * @param id activity id
     * @param data data to be passed to activity
     * @param reqCode request code send to activity
     */
    public static void startActivity(Context ctx, int id, Object data, Integer reqCode) {
        Logger.i(m_className, "Start activity with id=" + id);

        Intent intent = null;

        if (id==ACTIVITY_DEMO_MAIN) {
            intent = new Intent(ctx, DemoMainActivity.class);
        }
        else if (id==ACTIVITY_BLUETOOTH_CONNECT) {
            intent = new Intent(ctx, SP530DemoBluetoothConnectActivity.class);
        }
        else if (id==ACTIVITY_BTPRINTER_BLUETOOTH_CONNECT) {
            intent = new Intent(ctx, BTPrinterBluetoothConnectActivity.class);
        }
        else if (id==ACTIVITY_SETTING) {
            intent = new Intent(ctx, SettingActivity.class);
        }
        else if (id==ACTIVITY_WEBVIEW) {
            intent = new Intent(ctx, WebViewActivity.class);
            intent.putExtra("url", (String) data);
        }
        else if (id==ACTIVITY_WEBVIEW_FULLSCN) {
            intent = new Intent(ctx, WebViewFullScnActivity.class);
            intent.putExtra("url", (String) data);
        }
        else if (id == ACTIVITY_SETTINGTRANSACTION) {
            intent = new Intent(ctx, SettingTransactionActivity.class);
        }
        else if (id == ACTIVITY_SETTINGTRANSACTIONMUTUAUTH) {
            intent = new Intent(ctx, SettingTransactionMutuAuthActivity.class);
        }
        else if (id==ACTIVITY_TRANSSUMMARY) {
            intent = new Intent(ctx, TransSummaryActivity.class);
        }
        else if (id==ACTIVITY_TRANSSUMMARYDETAIL) {
            intent = new Intent(ctx, TransSummaryDetailActivity.class);
        }
        else if (id==ACTIVITY_VIEWLOG) {
            intent = new Intent(ctx, ViewLogActivity.class);
        }
        else if (id==ACTIVITY_TRANSRECEIPT) {
            intent = new Intent(ctx, TransReceiptActivity.class);
        }
        else if (id==ACTIVITY_TRANSRECEIPTTOUCHIMAGESINGLE) {
            intent = new Intent(ctx, TransReceiptTouchImageViewSingleActivity.class);
        }
        else if (id==ACTIVITY_SETTTINGDEVICET1000) {
            intent = new Intent(ctx, SettingDeviceT1000Activity.class);
        }
        else if (id==ACTIVITY_SETTINGDEVICESP530) {
            intent = new Intent(ctx, SettingDeviceSP530Activity.class);
        }
        else if (id==ACTIVITY_SETTINSP530SSLCERT) {
            intent =  new Intent(ctx, SettingSP530SSLCertActivity.class);
        }
        else if (id==ACTIVITY_TRANSRECEIPT_SIGNATURE) {
            intent = new Intent(ctx, TransReceiptSignatureActivity.class);
        }
        else if (id==ACTIVITY_SETTINGGENERAL) {
            intent = new Intent(ctx, SettingGeneralActivity.class);
        }
        else if (id==ACTIVITY_SETTINGBLUETOOTHPRINTER) {
            intent = new Intent(ctx, SettingDeviceBTPrinterActivity.class);
        }
        else if (id==ACTIVITY_SETTINGBLUETOOTHPRINTER_GENIMAGE) {
            intent = new Intent(ctx, BTPrinterGenerateImageActivity.class);
        }
        else if (id==ACTIVITY_T1000_SOCKET_CONNECT) {
            intent = new Intent(ctx, T1000DemoSocketConnectActivity.class);
        }
        else if (id==ACTIVITY_DEVICESELECT) {
            intent = new Intent(ctx, DeviceSelectActivity.class);
        }
        else if (id==ACTIVITY_SETTINGLANGUAGE) {
            intent = new Intent(ctx, SettingLanguageActivity.class);
        }

        if (data != null) {
            if (data instanceof HashMap) {
                HashMap<String, String> map = (HashMap<String, String>) data;
                for (Map.Entry<String, String> entry : map.entrySet()) {
                    String key = entry.getKey();
                    String value = entry.getValue();
                    intent.putExtra(key, value);
                }
            }
            else if (data instanceof String) {
                String argv = (String) data;
                if (!argv.equals(""))
                    intent.putExtra("argv", argv);
            }
            else if (data instanceof Bundle) {
                Bundle b=(Bundle)data;
                intent.putExtras(b);
            }
        }
        if (intent != null) {
            if (reqCode != null && ctx instanceof Activity) {
                Activity act = (Activity) ctx;
                act.startActivityForResult(intent, reqCode.intValue());
            }
            else
                ctx.startActivity(intent);
        }
    }

    public static void add2LogMemory(Data_logdatapacket logdataPacket) {
        if (logdataPacket == null) {
            Logger.w(m_className, "add2LogMemory, logdataPacket is NULL");
            return;
        }
        if (Application.logPacketList.size() > 1000) {
            for (int i = Application.logPacketList.size() - 1; i >= 1000 - 1; i--) {
                Application.logPacketList.remove(i);
            }
        }
        Application.logPacketList.add(0, logdataPacket);

        ActivityHelper instActivityHelper=ActivityHelper.getInstance();
        DemoMainActivity act=instActivityHelper.getDemoMainActivity();
        if (act!=null) {
            Fragment frag=null;

            frag=act.getS3TemplateFragment();
            if ( (frag!=null)&&(frag.getUserVisibleHint()) ) {
                S3TemplateFragment.log(logdataPacket);
            }

            frag=act.getS3CommandFragment();
            if ( (frag!=null)&&(frag.getUserVisibleHint()) ) {
                S3LogOverlayFragment.logData(logdataPacket);
            }
        }
    }

}

