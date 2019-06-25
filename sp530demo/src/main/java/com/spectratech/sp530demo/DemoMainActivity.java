package com.spectratech.sp530demo;

import android.Manifest;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.common.primitives.Bytes;
import com.spectratech.lib.BluetoothHelper;
import com.spectratech.lib.ByteHexHelper;
import com.spectratech.lib.Callback;
import com.spectratech.lib.EncryptionHelper;
import com.spectratech.lib.FilesDownloadActivity;
import com.spectratech.lib.IOStreamHelper;
import com.spectratech.lib.Logger;
import com.spectratech.lib.NetworkHelper;
import com.spectratech.lib.ResourcesHelper;
import com.spectratech.lib.ShowStringActivity;
import com.spectratech.lib.StringHelper;
import com.spectratech.lib.bluetooth.BluetoothConnectActivity;
import com.spectratech.lib.bluetooth.BluetoothSocketClass;
import com.spectratech.lib.bluetooth.BluetoothStatusClass;
import com.spectratech.lib.bluetooth.BluetoothUserActionClass;
import com.spectratech.lib.conf.SpectratechPathClass;
import com.spectratech.lib.data.Data_dbTransSummaryDetail;
import com.spectratech.lib.data.Data_trans_summary;
import com.spectratech.lib.printer.bp80.Lpt_funcCHelper;
import com.spectratech.lib.sp530.ApplicationProtocolHelper;
import com.spectratech.lib.sp530.comm_protocol_c.BtauxCHelper;
import com.spectratech.lib.sp530.comm_protocol_c.McpCHelper;
import com.spectratech.lib.sp530.comm_protocol_c.SP530_AppMcpCHelper;
import com.spectratech.lib.sp530.constant.ApplicationProtocolConstant;
import com.spectratech.lib.sp530.db.DBTransSummaryDetail;
import com.spectratech.lib.tcpip.SSLServerHelper;
import com.spectratech.lib.tcpip.TCPHelper;
import com.spectratech.lib.tcpip.TCPIPSocketClass;
import com.spectratech.lib.tcpip.data.Data_SSLServerLocal;
import com.spectratech.lib.tcpip.data.Data_tcpip_v4;
import com.spectratech.sp530demo.adapters.AppSectionsPagerAdapter;
import com.spectratech.sp530demo.classes.BTEventsForActivity;
import com.spectratech.sp530demo.conf.PathClass;
import com.spectratech.sp530demo.constant.ActivityRequestCodeEnum;
import com.spectratech.sp530demo.constant.AppSectionsConstant;
import com.spectratech.sp530demo.constant.FullEmvEnum.TRANSACTIONSTATUS;
import com.spectratech.sp530demo.controller.ActivityHelper;
import com.spectratech.sp530demo.controller.SkeyForMutuAuthHelper;
import com.spectratech.sp530demo.controller.UrlSchemeHelper;
import com.spectratech.sp530demo.data.Data_S3INS_response;
import com.spectratech.sp530demo.data.Data_device_mcpchcfg;
import com.spectratech.sp530demo.data.Data_executeCommand;
import com.spectratech.sp530demo.data.Data_executeCommand_filedl;
import com.spectratech.sp530demo.data.Data_executeCommand_tmsdl;
import com.spectratech.sp530demo.data.Data_logdatapacket;
import com.spectratech.sp530demo.data.Data_runtime_s3Auth;
import com.spectratech.sp530demo.data.Data_setting_devicebluetoothprinter;
import com.spectratech.sp530demo.data.Data_setting_devicesp530;
import com.spectratech.sp530demo.data.Data_setting_devicet1000;
import com.spectratech.sp530demo.data.Data_setting_general;
import com.spectratech.sp530demo.data.Data_setting_s3trans;
import com.spectratech.sp530demo.sp530class.SP530_bt_S3INS;
import com.spectratech.sp530demo.sp530class.SP530_bt_S3INS_FILEDL;
import com.spectratech.sp530demo.sp530class.SP530_bt_S3INS_TMSDL;
import com.spectratech.sp530demo.t1000class.T1000_S3INS;
import com.spectratech.sp530demo.view.FilesDownloadFragment;
import com.spectratech.sp530demo.view.S3CommonFragment;
import com.spectratech.sp530demo.view.S3LogOverlayFragment;
import com.spectratech.sp530demo.view.S3TemplateFragment;
import com.spectratech.sp530demo.view.S3TransFragment;
import com.spectratech.sp530demo.view.S3TransOverlayFragment;
import com.spectratech.sp530demo.view.TMSDownloadFragment;
import com.spectratech.spectratechlib_ftp.data.Data_ftpdlObject;

import org.apache.commons.lang3.ArrayUtils;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;


/**
 * DemoMainActivity - main activity for SP530 demo
 */
public class DemoMainActivity extends SP530DemoBaseFragmentActivity implements ActionBar.TabListener {

    private static final String m_className="DemoMainActivity";

    private static final int QR_CODE_REQUEST = 0x8002;

    private enum TRANSACTIONMODE {
        TRANSACTIONMODE_UNKNOWN(-1),
        TRANSACTIONMODE_AMOUNT(1),
        TRANSACTIONMODE_AMOUNT_BATCH(2);
        private final int intValue;
        private TRANSACTIONMODE(int value) {
            intValue = value;
        }
        public int toInt() {
            return intValue;
        }
    }

    private TRANSACTIONMODE m_transactionMode=TRANSACTIONMODE.TRANSACTIONMODE_UNKNOWN;

    /**
     * Variable to store run time generated values for Mutu Auth
     */
    public static Data_runtime_s3Auth m_dataRunTimeS3Auth;

    /**
     * Variable to store Bluetooth event parameters
     */
    public static BTEventsForActivity m_BTEventsForActivity;

    private final int MSG_HANDLER_REFRESHUI_BLUETOOTHROW=1100;
    private final int MSG_HANDLER_LOOPBATCHTRANSACTIONS=1101;
    private final int MSG_HANDLER_REFRESHUI_BLUETOOTHPRINTERROW=1102;

    private Handler m_handler;

    private ActionBar m_actionBar;
    private LinearLayout m_mainll;

    private LinearLayout m_overlay_container;
    private S3TransOverlayFragment m_frag_s3transOverlay;
    private S3LogOverlayFragment m_frag_s3logOverlay;

    private ViewPager m_viewPager;

    private LinearLayout m_container_title_rowll;

    private ImageView m_t1000ConnectIV;
    private LinearLayout m_t1000ConnectOutlinell;
    private View.OnClickListener m_t1000ConnectOnClickListener;

    private ImageView m_btrowIV;
    private LinearLayout m_btrowOutlinell;

    /**
     * On click listener for Bluetooth connection button
     */
    public View.OnClickListener m_btrowOnClickListener;
    private LinearLayout m_btrowPrintercontainer;
    private ImageView m_btrowPrinterIV;
    /**
     * On click listener for Bluetooth printer connection button
     */
    public View.OnClickListener m_btrowPrinterOnClickListener;
    private TextView m_btrowTV;
    private int m_btrowTVCallCount;

    private AppSectionsPagerAdapter m_AppSectionsPagerAdapter;

    private BroadcastReceiver m_broadcastReceiver;

    /**
     * Variable to store SP530 application interface
     */
    public SP530_bt_S3INS m_sp530Class;

    /**
     * Variable to store T1000 application interface
     */
    public T1000_S3INS m_t1000Class;

    private boolean m_bPostponeRefreshBluetoothText;

    private Data_executeCommand m_currentExecuteCommand;

    /**
     * General callback function for transaction
     */
    public Callback<Object> m_cb_general;
    private Callback<Object> m_cb_transfinish_singlemode;
    private Callback<Object> m_cb_transfinish_batchmode;

    private Callback<Object> m_cb_transprogressText;

    private boolean m_bCancelLoopBatchMode;

    private String m_responseMessage;

    private boolean m_bDebugMode;
    private boolean m_bTransactionUIShowMore;

    private int m_currentExecuteSeqno;

    private final int m_idxDefaultVPPage=AppSectionsConstant.IDX_SECTION_NAME.S3_TRANS.toInt();

    private boolean m_bLandscape;

    private Callback<Object> m_cbSuccess_enableBT;

    private int m_idx_deviceselected;

    private byte[] m_dataBufPtrForOnActivityResult;

    private Thread m_threadForUrlScheme;

    private Toast m_toast;

    private boolean m_bLockActionBarVisibilityStatus;

    private Data_setting_general m_dataSettingGeneral;
    private Data_setting_devicet1000 m_dataSettingDeviceT1000;
    private Data_setting_devicesp530 m_dataSettingDeviceSP530;
    private Data_setting_s3trans m_dataSettingS3Trans;
    private Data_setting_devicebluetoothprinter m_dataSettingBTPrinter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final ActivityHelper instActivityHelper=ActivityHelper.getInstance();
        instActivityHelper.setDemoMainActivity(this);

        // keep the screen turned on
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        final String strPleaseEnableWifi=ResourcesHelper.getBaseContextString(m_context, R.string.please_enable_wifi);
        final String strPleaseConnectWifiAP=ResourcesHelper.getBaseContextString(m_context, R.string.please_connect_wifi_ap);
        final String strDeviceNotSupportBT=ResourcesHelper.getBaseContextString(m_context, R.string.this_device_not_support_bt);

        m_bPostponeRefreshBluetoothText=false;

        resetInstances();

        m_dataRunTimeS3Auth=new Data_runtime_s3Auth();

        SkeyForMutuAuthHelper instSkeyForMutuAuthHelper=SkeyForMutuAuthHelper.getInstance();
        int seqno=instSkeyForMutuAuthHelper.getSeqnoForApplicationLayer(m_context);

        m_currentExecuteSeqno=seqno;

        ApplicationProtocolHelper instApplicationProtocolHelper=ApplicationProtocolHelper.getInstance();
        instApplicationProtocolHelper.setCurrentSequenceNumber(seqno);
        Application.RxmsgForMutuAuth=instSkeyForMutuAuthHelper.getRxmsg(m_context);
        Application.SkeyForMutuAuth=instSkeyForMutuAuthHelper.getSkey(m_context);
        if (Application.SkeyForMutuAuth!=null) {
            ByteHexHelper instByteHexHelper=ByteHexHelper.getInstance();
            String strHex=instByteHexHelper.bytesArrayToHexString(Application.SkeyForMutuAuth);
            Logger.i(m_className, "SKey for mutu auth: "+strHex);
        }

        if (Application.logPacketList==null) {
            Application.logPacketList = new ArrayList<Data_logdatapacket>();
        }
        else {
            Application.logPacketList.clear();
        }

        m_dataSettingGeneral=new Data_setting_general(m_context);
        m_dataSettingDeviceT1000=new Data_setting_devicet1000(m_context);
        m_dataSettingDeviceSP530=new Data_setting_devicesp530(m_context);
        m_dataSettingS3Trans=new Data_setting_s3trans(m_context);
        m_dataSettingBTPrinter=new Data_setting_devicebluetoothprinter(m_context);

        // bluetooth
        Data_setting_devicebluetoothprinter dataBTPrinterConf=new Data_setting_devicebluetoothprinter(m_context);
        initBTEventsForActivityInstance(this, dataBTPrinterConf.m_bUseSecureRfcommSocket);
        BTPrinterBluetoothConnectActivity.initBTEventsForActivityInstance(this, true);
        final BluetoothHelper instBluetoothHelper=BluetoothHelper.getInstance();
        m_cbSuccess_enableBT=null;

        initHandler();

        m_mainll=(LinearLayout)LinearLayout.inflate(m_context, R.layout.activity_demomain, null);
        setContentView(m_mainll);

        m_overlay_container=(LinearLayout)m_mainll.findViewById(R.id.overlay_container);
        m_overlay_container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });

        m_frag_s3transOverlay=new S3TransOverlayFragment();
        m_frag_s3logOverlay=new S3LogOverlayFragment();
        setS3TransOverlayFragment();

        m_viewPager=(ViewPager)m_mainll.findViewById(R.id.pager);

        m_container_title_rowll=(LinearLayout)m_mainll.findViewById(R.id.container_title_row);

        m_t1000ConnectIV=(ImageView)m_mainll.findViewById(R.id.row_t1000_connect_iv);
        m_t1000ConnectOnClickListener=new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isOnPause()) {
                    NetworkHelper instNetworkHelper=NetworkHelper.getInstance();
                    if (!instNetworkHelper.isWifiEnable(m_context)) {
                        Toast.makeText(m_context, strPleaseEnableWifi, Toast.LENGTH_SHORT).show();
                        instNetworkHelper.startActivity_wifiSetting(m_context);
                        return;
                    }
                    if (!instNetworkHelper.isWifiConnected(m_context)) {
                        Toast.makeText(m_context, strPleaseConnectWifiAP, Toast.LENGTH_SHORT).show();
                        instNetworkHelper.startActivity_wifiSetting(m_context);
                        return;
                    }
                    dismissProgressDialog();
                    startT1000ConnectActivityWithNoAnimation();
                }
            }
        };
        m_t1000ConnectIV.setOnClickListener(m_t1000ConnectOnClickListener);
        m_t1000ConnectIV.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                int idx_target=1;
                int idx=DeviceSelectActivity.getSelectedIndex(m_context);
                if (idx!=idx_target) {
                    DeviceSelectActivity.setSelectedIndex(m_context, idx_target);
                    m_idx_deviceselected = DeviceSelectActivity.getSelectedIndex(m_context);
                    setSelectedIconBorder(m_idx_deviceselected);
                    Toast.makeText(m_context, "select T1000 device", Toast.LENGTH_SHORT).show();
                }
                return true;
            }
        });
        m_t1000ConnectOutlinell=(LinearLayout)m_mainll.findViewById(R.id.row_t1000_connect_outline);


        m_btrowIV=(ImageView)m_mainll.findViewById(R.id.row_bluetooth_iv);
        m_btrowOnClickListener=new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!instBluetoothHelper.isSupportedBluetooth()) {
                    String strTmp = strDeviceNotSupportBT;
                    Logger.i(m_className, strTmp);
                    Toast.makeText(m_context, strTmp, Toast.LENGTH_SHORT).show();
                    return;
                }

                // ask for enable bluetooth
                Callback<Object> cb_success=new Callback<Object>() {
                    @Override
                    public Object call() throws Exception {
                        dismissProgressDialog();
                        startBluetoothConnectActivityWithNoAnimation();
                        return null;
                    }
                };
                boolean bRequestBTEnable=checkRequestEnableBluetooth(cb_success);
                if (bRequestBTEnable) {
                    return;
                }

                if (!isOnPause()) {
                    dismissProgressDialog();
                    startBluetoothConnectActivityWithNoAnimation();
                }
            }
        };
        m_btrowIV.setOnClickListener(m_btrowOnClickListener);
        m_btrowIV.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                int idx_target=0;
                int idx=DeviceSelectActivity.getSelectedIndex(m_context);
                if (idx!=idx_target) {
                    DeviceSelectActivity.setSelectedIndex(m_context, idx_target);
                    m_idx_deviceselected = DeviceSelectActivity.getSelectedIndex(m_context);
                    setSelectedIconBorder(m_idx_deviceselected);
                    Toast.makeText(m_context, "select SP530 device", Toast.LENGTH_SHORT).show();
                }
                return true;
            }
        });
        m_btrowOutlinell=(LinearLayout)m_mainll.findViewById(R.id.row_bluetooth_outline);

        m_btrowPrintercontainer=(LinearLayout)m_mainll.findViewById(R.id.row_printer_bluetooth_icon_container);

        m_btrowPrinterIV=(ImageView)m_mainll.findViewById(R.id.row_printer_bluetooth_iv);
        m_btrowPrinterOnClickListener=new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!instBluetoothHelper.isSupportedBluetooth()) {
                    String strTmp = strDeviceNotSupportBT;
                    Logger.i(m_className, strTmp);
                    Toast.makeText(m_context, strTmp, Toast.LENGTH_SHORT).show();
                    return;
                }

                // ask for enable bluetooth
                Callback<Object> cb_success=new Callback<Object>() {
                    @Override
                    public Object call() throws Exception {
                        dismissProgressDialog();
                        BTPrinterBluetoothConnectActivity.startBTPrinterBluetoothConnectActivityWithNoAnimation(m_context);
                        return null;
                    }
                };
                boolean bRequestBTEnable=checkRequestEnableBluetooth(cb_success);
                if (bRequestBTEnable) {
                    return;
                }

                if (!isOnPause()) {
                    dismissProgressDialog();
                    BTPrinterBluetoothConnectActivity.startBTPrinterBluetoothConnectActivityWithNoAnimation(m_context);
                }
            }
        };
        m_btrowPrinterIV.setOnClickListener(m_btrowPrinterOnClickListener);

        m_btrowTV=(TextView)m_mainll.findViewById(R.id.row_bluetooth_tv);
        m_btrowTVCallCount=0;

        m_bLockActionBarVisibilityStatus=false;
        m_actionBar= getActionBar();

        setActionBarDefaultStyle(m_actionBar);

        m_AppSectionsPagerAdapter=new AppSectionsPagerAdapter(getFragmentManager(), m_context);
        m_viewPager.setAdapter(m_AppSectionsPagerAdapter);
        m_viewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                // When swiping between different app sections, select the corresponding tab.
                // We can also use ActionBar.Tab#select() to do this if we have a reference to the
                // Tab.
                if (m_actionBar!=null) {
                    m_actionBar.setSelectedNavigationItem(position);
                }

                hideSoftKeyboard();
            }
        });

        refreshActionBarTab();

        initUI();

        registerBroadcastReceiver();

        initCallbacks();

        m_responseMessage="";

        m_viewPager.setCurrentItem(m_idxDefaultVPPage);

        String signatureFilePath= PathClass.getSignatureDir(m_context);
        Data_trans_summary.setSignatureFilePath(signatureFilePath);

        m_bDebugMode = m_dataSettingGeneral.m_bDebugMode;
        m_bTransactionUIShowMore=m_dataSettingGeneral.m_bTransactionUIShowMore;

        if (m_bDebugMode) {
            if (m_actionBar!=null) {
                m_actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
            }
        }
        else {
            if (m_actionBar!=null) {
                m_actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
            }
        }

        boolean bLandscape=getResources().getBoolean(R.bool.is_landscape);
        if (bLandscape) {
            onOrientationLandscape();
        }
        else {
            onOrientationPortrait();
        }

        m_idx_deviceselected=DeviceSelectActivity.getSelectedIndex(m_context);
        m_dataBufPtrForOnActivityResult=null;

        m_bCancelLoopBatchMode=false;

        m_threadForUrlScheme=null;

        createFtpRootFolder();
        String strRootPath=getExternalFtpDirectoryString()+"/";
        TMSDownloadFragment.setDefaultRootPath(strRootPath);
        FilesDownloadFragment.setDefaultRootPath(strRootPath);
    }

    public void setEmptyOverlayContainer() {
        Logger.i(m_className, "setEmptyOverlayContainer");
        LinearLayout ll=(LinearLayout)m_mainll.findViewById(R.id.overlay_container);
        if (ll!=null) {
            ll.removeAllViews();
        }
    }

    /**
     * set overlay fragment to container
     * @param frag - fragment to be add to container
     */
    public void setOverlayFragment(Fragment frag) {
        if (frag==null) {
            Logger.w(m_className, "setOverlayFragment, frag is null");
        }
        int id=frag.getId();
        View v=m_mainll.findViewById(id);
        if (v==null) {
            Logger.i(m_className, "setOverlayFragment, transaction.replace");
            FragmentManager fragman=getFragmentManager();
            FragmentTransaction transaction = fragman.beginTransaction();
            transaction.replace(R.id.overlay_container, frag);
            transaction.commit();
        }
        else {
            String strName="";
            if (frag instanceof S3TransOverlayFragment) {
                strName="S3TransOverlayFragment";
            }
            else if (frag instanceof  S3LogOverlayFragment) {
                strName="S3LogOverlayFragment";
            }
            Logger.i(m_className, "setOverlayFragment, exist frag: " + strName);
        }
    }
    public void setS3TransOverlayFragment() {
        setOverlayFragment(m_frag_s3transOverlay);
    }
    public void setLogOverlayFragment() {
        setOverlayFragment(m_frag_s3logOverlay);
    }
    public S3LogOverlayFragment getLogOverlayFragment() {
        return m_frag_s3logOverlay;
    }

    public void showResetGeneralDialog() {
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE: {
                        sendResetTransactionCommand();

                        m_handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                enableAllUIInputsRunOnUiThread();
                            }
                        }, 100);
                    }
                    break;
                    case DialogInterface.BUTTON_NEGATIVE: {
                        //No button clicked
                    }
                    break;
                }
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(m_context);
        builder.setMessage("Cancel?").setPositiveButton("Yes", dialogClickListener)
                .setNegativeButton("No", dialogClickListener).show();

    }

    public void showCancelGeneralDialog() {
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE: {
                        cancelGeneral();
                    }
                    break;
                    case DialogInterface.BUTTON_NEGATIVE: {
                        //No button clicked
                    }
                    break;
                }
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(m_context);
        builder.setMessage("Cancel?").setPositiveButton("Yes", dialogClickListener)
                .setNegativeButton("No", dialogClickListener).show();

    }

    public void showCancelTransactionDialog() {
        String strCancel=ResourcesHelper.getBaseContextString(m_context, R.string.cancel);
        String strCurrent=ResourcesHelper.getBaseContextString(m_context, R.string.current);
        String strTransaction=ResourcesHelper.getBaseContextString(m_context, R.string. transaction);
        String strYes=ResourcesHelper.getBaseContextString(m_context, R.string.yes);
        String strNo=ResourcesHelper.getBaseContextString(m_context, R.string.no);
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE: {
                        cancelTransaction();
                    }
                    break;
                    case DialogInterface.BUTTON_NEGATIVE: {
                        //No button clicked
                    }
                    break;
                }
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(m_context);
        builder.setMessage(strCancel+" "+strCurrent+" "+strTransaction+"?").setPositiveButton(strYes, dialogClickListener)
                .setNegativeButton(strNo, dialogClickListener).show();
    }

    public void cancelTransaction() {
        //Yes button clicked
        m_bCancelLoopBatchMode = true;
        m_handler.removeMessages(MSG_HANDLER_LOOPBATCHTRANSACTIONS);
        cancelGeneral();
    }
    public void cancelGeneral() {
        switch (m_idx_deviceselected) {
            case 0: {
                if (m_sp530Class!=null) {
                    m_sp530Class.safeCancelThread();
                }
            }
            break;
            case 1: {
                if (m_t1000Class!=null) {
                    m_t1000Class.safeCancelThread();
                }
            }
        }

        m_handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                enableAllUIInputsRunOnUiThread();
            }
        }, 100);
    }

    private boolean processRuntimeCheckPermission() {
        return processRuntimeCheckPermission(-1);
    }
    private boolean processRuntimeCheckPermission(int idx) {
        if (Build.VERSION.SDK_INT < 23) {
            Logger.i(m_className, "processRuntimeCheckPermission, not support runtime permission");
            return false;
        }

        boolean bFullCheck = (idx < 0) ? true : false;

        boolean bCheck=bFullCheck;

        String strPermission = "";

        if ((bCheck) || (idx == 0)) {
            strPermission = Manifest.permission.ACCESS_COARSE_LOCATION;
            boolean bRequest=checkNeedRequestRuntimePermission(strPermission, ActivityRequestCodeEnum.RequestPermissionCode.REQUEST_PERMISSION_ACCESS_COARSE_LOCATION.toInt());
            if (bRequest) {
                return true;
            }
            bCheck=true;
        }

        if ((bCheck) || (idx == 1)) {
            strPermission = Manifest.permission.WRITE_EXTERNAL_STORAGE;
            boolean bRequest=checkNeedRequestRuntimePermission(strPermission, ActivityRequestCodeEnum.RequestPermissionCode.REQUEST_PERMISSION_WRITE_EXTERNAL_STORAGE.toInt());
            if (bRequest) {
                return true;
            }
            bCheck=true;
        }

        if ((bCheck) || (idx == 2)) {
            strPermission = Manifest.permission.CAMERA;
            boolean bRequest=checkNeedRequestRuntimePermission(strPermission, ActivityRequestCodeEnum.RequestPermissionCode.REQUEST_PERMISSION_CAMERA.toInt());
            if (bRequest) {
                return true;
            }
        }

        return false;
    }
    private boolean checkNeedRequestRuntimePermission(String strPermission, int reqCode) {
        boolean bRequest=false;
        int permission = ContextCompat.checkSelfPermission(m_context, strPermission);
        if (permission != PackageManager.PERMISSION_GRANTED) {
//            // Should we show an explanation?
//            if (ActivityCompat.shouldShowRequestPermissionRationale((Activity) m_context, strPermission)) {
//                Logger.i(m_className, "shouldShowRequestPermissionRationale, strPermission: "+strPermission);
//                // Show an expanation to the user *asynchronously* -- don't block
//                // this thread waiting for the user's response! After the user
//                // sees the explanation, try again to request the permission.
//                bRequest=false;
//            } else {
//                // No explanation needed, we can request the permission.
//                ActivityCompat.requestPermissions((Activity)m_context, new String[]{strPermission}, reqCode);
//                bRequest=true;
//            }
            ActivityCompat.requestPermissions((Activity)m_context, new String[]{strPermission}, reqCode);
            bRequest=true;
        }
        else {
            bRequest=false;
        }
        return bRequest;
    }

    private void refreshActionBarTab() {
        if (m_actionBar==null) {
            return;
        }
        int countTab=m_actionBar.getTabCount();
        if (countTab>0) {
            m_actionBar.removeAllTabs();
        }
        // For each of the sections in the app, add a tab to the action bar.
        for (int i = 0; i < m_AppSectionsPagerAdapter.getCount(); i++) {
            // Create a tab with text corresponding to the page title defined by the adapter.
            // Also specify this Activity object, which implements the TabListener interface, as the
            // listener for when this tab is selected.
            ActionBar.Tab tab=m_actionBar.newTab();
            tab.setTabListener(this);

            LinearLayout tabll=(LinearLayout)getLayoutInflater().inflate(R.layout.actionbar_textview, null);
            TextView tabTV=(TextView)tabll.findViewById(R.id.texttv);
            tabTV.setText(m_AppSectionsPagerAdapter.getPageTitle(i));
            // set the height to a really high value to fill the parent
            tabTV.setHeight(200);
            tab.setCustomView(tabll);

            m_actionBar.addTab(tab);
        }
    }

    public void sendResetTransactionCommand() {
        Logger.i(m_className, "sendResetTransactionCommand called");
        setCurrentExecuteCommand(ApplicationProtocolConstant.S3INS_RESET, null, m_cb_general, false);
        startCommand();
    }

    private void resetInstances() {
        boolean bForce=true;

        SP530_AppMcpCHelper.getInstance(bForce);
        McpCHelper.getInstance(bForce);
        BtauxCHelper.getInstance(bForce);
        IOStreamHelper.getInstance(bForce);

        ApplicationProtocolHelper.getInstance(bForce);

        Lpt_funcCHelper.freeInstance();

        T1000DemoSocketConnectActivity.safeFreeAll();
    }

    public void startActivityForResultFileDownload(ArrayList<String> UrlListFile) {
        if (!isOnPause()) {
            String strEmptyFileList=ResourcesHelper.getBaseContextString(m_context, R.string.empty_file_list);

            if ( (UrlListFile==null)||(UrlListFile.size()==0) ) {
                Logger.w(m_className, "startActivityForResultFileDownload, UrlListFile is NULL");
                ToastMessage(strEmptyFileList);
                return;
            }
            Intent intent = new Intent(m_context, SP530DemoFilesDownloadActivity.class);

            Bundle b=new Bundle();
            b.putStringArrayList(SP530DemoFilesDownloadActivity.KEY_URL_FILE_LIST, UrlListFile);
            String downloadDir=getDataS3TemplateDirectoryString();
            b.putString(SP530DemoFilesDownloadActivity.KEY_DOWNLOAD_DIRECTORY, downloadDir);
            intent.putExtras(b);

            int requestCode=ActivityRequestCodeEnum.RequestCode.REQUEST_FILES_DOWNLOAD.toInt();
            ((Activity)m_context).startActivityForResult(intent, requestCode);
        }
    }

    public static String getExternalDataS3TemplateDirectoryString() {
        String strDir=getDataS3TemplateDirectoryString();
        String strExternalDir= Environment.getExternalStorageDirectory().getPath()+"/"+strDir;
        return strExternalDir;
    }
    public static String getDataS3TemplateDirectoryString() {
        return SpectratechPathClass.ROOTDIR + "/" + SpectratechPathClass.DATADIR + "/wxEmvclPos_L2/S3Template";
    }

    public static String getExternalFtpDirectoryString() {
        String strDir=getFtpDirectoryString();
        String strExternalDir= Environment.getExternalStorageDirectory().getPath()+"/"+strDir;
        return strExternalDir;
    }
    public static String getFtpDirectoryString() {
        return SpectratechPathClass.ROOTDIR + "/" + SpectratechPathClass.DATADIR + "/ftp";
    }

    public static void createFtpRootFolder() {
        String dir = getExternalFtpDirectoryString();
        File newDirectory = new File(dir);
        if (!newDirectory.exists()) {
            boolean bMakeDir = newDirectory.mkdirs();
            if (bMakeDir) {
                Logger.i(m_className, "createRootFolder success: " + dir);
            } else {
                Logger.i(m_className, "createRootFolder FAILED: " + dir);
            }
        }
        else {
            Logger.i(m_className, "createRootFolder does not need, exist: " + dir);
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == BluetoothConnectActivity.REQUEST_ENABLE_BT) {
            if (resultCode == Activity.RESULT_OK) {
                String strEnableBluetT="Bluetooth enabled";
                Toast.makeText(m_context, strEnableBluetT, Toast.LENGTH_SHORT).show();
                refreshBluetoothIcon();

                if (m_cbSuccess_enableBT!=null) {
                    try {
                        m_cbSuccess_enableBT.call();
                    } catch (Exception e) {
                        Logger.w(m_className, "e: " + e.toString());
                    }
                    m_cbSuccess_enableBT=null;
                }
            }
        }
        else if (requestCode == ActivityRequestCodeEnum.RequestCode.REQUEST_STARTTRANSACTION_AMOUNT.toInt()) {
            if (resultCode == RESULT_OK) {

                boolean bSSLChOne=m_dataSettingDeviceSP530.m_bSSLChOne;
                if (bSSLChOne) {
                    int currentAndroidSDKVersion=Build.VERSION.SDK_INT;
                    if (currentAndroidSDKVersion<Build.VERSION_CODES.LOLLIPOP) {
                        Toast.makeText(m_context, "currentOSVersion: " + currentAndroidSDKVersion+" < "+Build.VERSION_CODES.LOLLIPOP, Toast.LENGTH_LONG).show();
                        int id_act=Application.ACTIVITY_SETTINGDEVICESP530;
                        Application.startActivity(m_context, id_act);
                        return;
                    }
                }

                preStartCommand();
                startCommand();

                m_bPostponeRefreshBluetoothText=true;
            }
            else {

                if (m_AppSectionsPagerAdapter != null) {
                    S3TemplateFragment frag = (S3TemplateFragment) m_AppSectionsPagerAdapter.getS3TemplateFragment(m_viewPager);
                    if (frag != null) {
                        frag.cancelSend();
                    }
                }
            }
        }
        else if (requestCode == ActivityRequestCodeEnum.RequestCode.REQUEST_SHOWTRANSACTION_RECEIPT.toInt()) {
            if (m_cb_showTransactionReceipt!=null) {
                try {
                    m_cb_showTransactionReceipt.call();
                }
                catch (Exception ex) {
                    Logger.e(m_className, "onActivityResult, m_cb_showTransactionReceipt, ex: "+ex.toString());
                }
            }
        }
        else if (requestCode==ActivityRequestCodeEnum.RequestCode.REQUEST_DEVICE_SELECT_AND_STARTTRANSACTION.toInt()) {
            if (resultCode == Activity.RESULT_OK) {
                m_idx_deviceselected=DeviceSelectActivity.getSelectedIndex(m_context);
                startTransaction_s3trans(m_dataBufPtrForOnActivityResult);
            }
            m_dataBufPtrForOnActivityResult=null;
        }
        else if (requestCode==QR_CODE_REQUEST) {
            if (resultCode == RESULT_OK) {
                String barcode = data.getStringExtra(com.google.zxing.client.android.Intents.Scan.RESULT);
                Logger.i("QRCode", "got barcode: " + barcode);
                UrlSchemeHelper instUrlSchemeHelper=UrlSchemeHelper.getInstance();
                instUrlSchemeHelper.process(m_context, barcode);
            }
        }
        else if (requestCode==ActivityRequestCodeEnum.RequestCode.REQUEST_FILES_DOWNLOAD.toInt()) {
            Logger.i(m_className, "onActivityResult, REQUEST_FILES_DOWNLOAD");

            if (data==null) {
                return;
            }
            boolean bHaveFileDownloaded = data.getBooleanExtra(FilesDownloadActivity.KEY_HAVE_FILE_DOWNLOADED, false);
            if (bHaveFileDownloaded) {
                if (m_AppSectionsPagerAdapter != null) {
                    S3TemplateFragment frag = (S3TemplateFragment) m_AppSectionsPagerAdapter.getS3TemplateFragment(m_viewPager);
                    if (frag != null) {
                        frag.reloadDropDownUis();
                    }
                }
            }
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        Logger.i(m_className, "onRequestPermissionsResult, requestCode: " + requestCode);

        final String strPermission=ResourcesHelper.getBaseContextString(m_context, R.string.permission);
        final String strGranted=ResourcesHelper.getBaseContextString(m_context, R.string.granted);
        final String strDenied=ResourcesHelper.getBaseContextString(m_context, R.string.denied);

        if (requestCode == ActivityRequestCodeEnum.RequestPermissionCode.REQUEST_PERMISSION_ACCESS_COARSE_LOCATION.toInt()) {
            String strReponse;
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // permission was granted, yay! Do the
                // contacts-related task you need to do.
                strReponse=strGranted;
            }
            else {
                // permission denied, boo! Disable the
                // functionality that depends on this permission.
                strReponse=strDenied;
            }
            Toast.makeText(m_context, strPermission+" "+strReponse+": ACCESS_COARSE_LOCATION", Toast.LENGTH_SHORT).show();

            processRuntimeCheckPermission(1);
        }
        else if (requestCode == ActivityRequestCodeEnum.RequestPermissionCode.REQUEST_PERMISSION_WRITE_EXTERNAL_STORAGE.toInt()) {
            String strReponse;
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // permission was granted, yay! Do the
                // contacts-related task you need to do.
                strReponse=strGranted;

                OpeningActivity.createRootFolder(this);
                createFtpRootFolder();
            }
            else {
                // permission denied, boo! Disable the
                // functionality that depends on this permission.
                strReponse=strDenied;
            }
            Toast.makeText(m_context, strPermission+" " + strReponse + ": WRITE_EXTERNAL_STORAGE", Toast.LENGTH_SHORT).show();

            processRuntimeCheckPermission(2);
        }
        else if (requestCode == ActivityRequestCodeEnum.RequestPermissionCode.REQUEST_PERMISSION_CAMERA.toInt()) {
            String strReponse;
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // permission was granted, yay! Do the
                // contacts-related task you need to do.
                strReponse=strGranted;
            }
            else {
                // permission denied, boo! Disable the
                // functionality that depends on this permission.
                strReponse=strDenied;
            }
            Toast.makeText(m_context, strPermission+" " + strReponse + ": CAMERA", Toast.LENGTH_SHORT).show();

            // ask for enable bluetooth
            boolean bRequestBTEnable = checkRequestEnableBluetooth();
        }
    }

    /**
     * Start T1000DemoConnectActivity without animation
     */
    public void startT1000ConnectActivityWithNoAnimation() {
        Application.startActivity(m_context, Application.ACTIVITY_T1000_SOCKET_CONNECT);
        ((Activity) m_context).overridePendingTransition(0, 0);
    }

    /**
     * Start BluetoothConnectActivity without animation
     */
    public void startBluetoothConnectActivityWithNoAnimation() {
        Application.startActivity(m_context, Application.ACTIVITY_BLUETOOTH_CONNECT);
        ((Activity) m_context).overridePendingTransition(0, 0);
    }
    /**
     * Start BluetoothConnectActivity for result without animation
     */
    public void startBluetoothConnectActivityForResultWithNoAnimation() {
        int reqCode= ActivityRequestCodeEnum.RequestCode.REQUEST_STARTTRANSACTION_AMOUNT.toInt();
        HashMap<String, String> data=new HashMap<String, String>();
        data.put(BluetoothConnectActivity.RESPONSE_ENDACTITYWITHRESULT_WHENCONNECTED, "1");
        Application.startActivity(m_context, Application.ACTIVITY_BLUETOOTH_CONNECT, data, reqCode);
        ((Activity) m_context).overridePendingTransition(0, 0);
    }

    private boolean need2StartBTConnectionActivity() {
        if (m_BTEventsForActivity.getActiveBluetoothDevice()==null) {
            return true;
        }
        if (m_BTEventsForActivity.m_btUserAction.m_stausUserAction!= BluetoothUserActionClass.STATUS_USERACTION.USERACTION_BT_CONNECT) {
            return true;
        }
        return false;
    }
    private void popupNeed2StartBTConnectionActivity(boolean bStartActivityForResult) {
        boolean bNeed=need2StartBTConnectionActivity();
        if (bNeed) {
//            DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
//                @Override
//                public void onClick(DialogInterface dialog, int which) {
//                    switch (which){
//                        case DialogInterface.BUTTON_POSITIVE:
//                            //Yes button clicked
//                            startBluetoothConnectActivityForResultWithNoAnimation();
//                            break;
//
//                        case DialogInterface.BUTTON_NEGATIVE:
//                            //No button clicked
//                            break;
//                    }
//                }
//            };
//
//            AlertDialog.Builder builder = new AlertDialog.Builder(m_context);
//            builder.setMessage("Make a bluetooth connection?").setPositiveButton("Yes", dialogClickListener)
//                    .setNegativeButton("No", dialogClickListener).show();

            dismissProgressDialog();
            if (bStartActivityForResult) {
                startBluetoothConnectActivityForResultWithNoAnimation();
            }
            else {
                startBluetoothConnectActivityWithNoAnimation();
            }
        }
    }

    private void initHandler() {
        m_handler=new Handler() {
            public void handleMessage(Message msg) {
                //Logger.i(m_className, "Handler message: "+msg.what);
                switch (msg.what) {
                    case MSG_HANDLER_REFRESHUI_BLUETOOTHROW: {
                        refreshBluetoothRow();
                    }
                    break;
                    case MSG_HANDLER_LOOPBATCHTRANSACTIONS: {
                        loopBatchTransactions();
                    }
                    break;
                    case MSG_HANDLER_REFRESHUI_BLUETOOTHPRINTERROW: {
                        refreshBluetoothPrinterText();
                    }
                    break;
                }
            }
        };
    }

    @Override
    protected void onDestroy() {
        unregisterBroadcastReceiver();
        ActivityHelper instActivityHelper=ActivityHelper.getInstance();
        instActivityHelper.setDemoMainActivity(null);

        // app stop please
        SP530_AppMcpCHelper.freeInstance();

        Thread thread = new Thread() {
            @Override
            public void run() {
                if (m_sp530Class!=null) {
                    m_sp530Class.onDestroy();
                    m_sp530Class=null;
                }
                if (m_t1000Class!=null) {
                    m_t1000Class.onDestroy();
                    m_t1000Class=null;
                }

                // stop ssl server local please if exist
                SSLServerHelper instSSLServerHelper=SSLServerHelper.getInstance();
                instSSLServerHelper.ssl_disconnectAll();

                if (m_BTEventsForActivity!=null) {
                    Logger.i(m_className, "onDestroy, m_BTEventsForActivity.onDestroy called");
                    m_BTEventsForActivity.onDestroy();
                    m_BTEventsForActivity=null;
                }

                if (BTPrinterBluetoothConnectActivity.m_BTEventsForActivity!=null) {
                    Logger.i(m_className, "onDestroy, BTPrinterBluetoothConnectActivity.m_BTEventsForActivity.onDestroy called");
                    BTPrinterBluetoothConnectActivity.m_BTEventsForActivity.onDestroy();
                }

                Lpt_funcCHelper.freeInstance();
            }
        };
        thread.start();

        T1000DemoSocketConnectActivity.safeFreeAll();

        super.onDestroy();
    }

    private static void initBTEventsForActivityInstance(Context context) {
        boolean bForceNew=false;
        initBTEventsForActivityInstance(context, bForceNew);
    }
    private static void initBTEventsForActivityInstance(Context context, boolean bForceNew) {
        boolean bUseSecureRfcommSocket=true;
        initBTEventsForActivityInstance(context, bUseSecureRfcommSocket, bForceNew);
    }
    private static void initBTEventsForActivityInstance(Context context, boolean bUseSecureRfcommSocket, boolean bForceNew) {
        if ( (m_BTEventsForActivity==null)||(bForceNew) ) {
            m_BTEventsForActivity = new BTEventsForActivity(context);
            m_BTEventsForActivity.m_bUseSecureRfcommSocket=bUseSecureRfcommSocket;
            m_BTEventsForActivity.setKeyForDeviceAddressStoreInSharedPreferences("SP530");
        }
    }

    private void setSelectedIconBorder(int idx_deviceselected) {
        switch (idx_deviceselected) {
            case 0: {
                m_btrowOutlinell.setVisibility(View.VISIBLE);
                m_t1000ConnectOutlinell.setVisibility(View.GONE);
            }
            break;
            case 1: {
                m_btrowOutlinell.setVisibility(View.GONE);
                m_t1000ConnectOutlinell.setVisibility(View.VISIBLE);
            }
            break;
        }
    }

    private boolean updateUserSpecificParams() {
        boolean bUpdate=false;
        if (Application.FLAG_UPDATE_DATA_SETTING_GENERAL_PARAMS) {
            bUpdate|=Application.FLAG_UPDATE_DATA_SETTING_GENERAL_PARAMS;
            m_dataSettingGeneral.refresh();
            Application.FLAG_UPDATE_DATA_SETTING_GENERAL_PARAMS=false;
        }
        if (Application.FLAG_UPDATE_DATA_SETTING_DEVICEST1000_PARAMS) {
            bUpdate|=Application.FLAG_UPDATE_DATA_SETTING_DEVICEST1000_PARAMS;
            m_dataSettingDeviceT1000.refresh();
            Application.FLAG_UPDATE_DATA_SETTING_DEVICEST1000_PARAMS=false;
        }
        if (Application.FLAG_UPDATE_DATA_SETTING_DEVICESSP530_PARAMS) {
            bUpdate|=Application.FLAG_UPDATE_DATA_SETTING_DEVICESSP530_PARAMS;
            m_dataSettingDeviceSP530.refresh();
            Application.FLAG_UPDATE_DATA_SETTING_DEVICESSP530_PARAMS=false;
        }
        if (Application.FLAG_UPDATE_DATA_SETTING_TRANSACTION_PARAMS) {
            bUpdate|=Application.FLAG_UPDATE_DATA_SETTING_TRANSACTION_PARAMS;
            m_dataSettingS3Trans.refresh();
            Application.FLAG_UPDATE_DATA_SETTING_TRANSACTION_PARAMS=false;
        }
        if (Application.FLAG_UPDATE_DATA_SETTING_BTPRINTER_PARAMS) {
            bUpdate|=Application.FLAG_UPDATE_DATA_SETTING_BTPRINTER_PARAMS;
            m_dataSettingBTPrinter.refresh();
            Application.FLAG_UPDATE_DATA_SETTING_BTPRINTER_PARAMS=false;
        }
        if (Application.FLAG_UPDATE_DATA_SETTING_SSLCERT_PARAMS) {
            bUpdate|=Application.FLAG_UPDATE_DATA_SETTING_SSLCERT_PARAMS;
            Application.FLAG_UPDATE_DATA_SETTING_SSLCERT_PARAMS=false;
        }
        return bUpdate;
    }

    @Override
    protected void onResume() {
        boolean bCreate=m_activitycommmon.m_bOnCreate;
        super.onResume();

        boolean bUpdateUserParams=updateUserSpecificParams();
        if (bUpdateUserParams) {
            switch (m_dataSettingDeviceSP530.m_packetEncapsulateLevel) {
                case RAW: {
                    SP530_AppMcpCHelper.freeInstance();
                }
                break;
                default: {
                    SP530_AppMcpCHelper instAppMcpCHelper = SP530_AppMcpCHelper.getInstance();
                    if (!instAppMcpCHelper.isAppStarted()) {
                        boolean bForceNew = false;
                        start_communicationProtocol(bForceNew);
                    } else {
                        // SSL?
                        boolean bLocalChannelSSL = m_dataSettingDeviceSP530.m_bSSLChOne;
                        if (bLocalChannelSSL) {
                            if (!instAppMcpCHelper.isLocalChannelSSLEnable()) {
                                sslDisconnectTriggered(bLocalChannelSSL);
                            }
                        } else {
                            sslDisconnectTriggered(bLocalChannelSSL);
                        }

                    }
                }
                break;
            }
        }

        int idx_deviceselected=DeviceSelectActivity.getSelectedIndex(m_context);
        setSelectedIconBorder(idx_deviceselected);

        // make sure m_BTEventsForActivity is here
        if (m_BTEventsForActivity==null) {
            Logger.w(m_className, "onResume, m_BTEventsForActivity is not here, try to initialize it again");
            initBTEventsForActivityInstance(this);
        }

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        boolean bChangeEnableBTPrinter=false;
        if (m_btrowPrintercontainer!=null) {
            if (m_dataSettingBTPrinter.m_bEnableBTPrinter) {
                if (m_btrowPrintercontainer.getVisibility()!=View.VISIBLE) {
                    m_btrowPrintercontainer.setVisibility(View.VISIBLE);
                    bChangeEnableBTPrinter=true;
                }
            }
            else {
                if (m_btrowPrintercontainer.getVisibility()!=View.GONE) {
                    m_btrowPrintercontainer.setVisibility(View.GONE);
                    bChangeEnableBTPrinter=true;
                }
            }
        }

        if (bCreate) {
            setMessage(ResourcesHelper.getBaseContextString(m_context, R.string.welcome));
            refreshBluetoothIcon();

            boolean bProcessRuntimePermission=processRuntimeCheckPermission();
            if (!bProcessRuntimePermission) {
                // ask for enable bluetooth
                boolean bRequestBTEnable = checkRequestEnableBluetooth();
            }

            m_BTEventsForActivity.loadSavedActiveBluetoothDevice("DemoMainActivity");
            if (BTPrinterBluetoothConnectActivity.m_BTEventsForActivity!=null) {
                BTPrinterBluetoothConnectActivity.m_BTEventsForActivity.loadSavedActiveBluetoothDevice("BTPrinterBluetoothConnectActivity");
            }
        }
        else {
            refreshBluetoothRow();
            refreshBluetoothPrinterText();

            boolean bRefreshPageViewer=false;

            boolean bDebugMode=m_dataSettingGeneral.m_bDebugMode;
            if (m_bDebugMode!=bDebugMode) {
                m_bDebugMode=bDebugMode;
                if (m_AppSectionsPagerAdapter!=null) {
                    m_AppSectionsPagerAdapter.notifyDataSetChanged();
                }
                refreshActionBarTab();
                if (m_bDebugMode) {
                    if (m_actionBar!=null) {
                        m_actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
                    }
                }
                else {
                    if (m_actionBar!=null) {
                        m_actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
                    }
                }
                bRefreshPageViewer=true;
            }

            // handle change of enable bluetooth printer UI
            if (!bRefreshPageViewer) {
                if (bChangeEnableBTPrinter) {
                    if (m_AppSectionsPagerAdapter!=null) {
                        m_AppSectionsPagerAdapter.notifyDataSetChanged();
                    }
                    refreshActionBarTab();
                    bRefreshPageViewer=true;
                }
            }

            if (bRefreshPageViewer) {
                safeInvalidatePageViewer();
            }

            boolean bTransactionUIShowMore=m_dataSettingGeneral.m_bTransactionUIShowMore;
            if (m_bTransactionUIShowMore!=bTransactionUIShowMore) {
                m_bTransactionUIShowMore=bTransactionUIShowMore;
                invalidateOptionsMenu();
            }
        }

        processInputIntent();

        validateBTConnections();
    }

    private void processInputIntent() {
        Logger.i(m_className, "processInputIntent");

        final String strPreparing=ResourcesHelper.getBaseContextString(m_context, R.string.preparing);
        final String strTransaction=ResourcesHelper.getBaseContextString(m_context, R.string.transaction);
        final String strPleaseWaitCurrentTransFinish=ResourcesHelper.getBaseContextString(m_context, R.string.please_wait_currenttransfinish);

        if (Application.hashUrlSchemeForDemoMain!=null) {
            if (isTransactionFinish()) {
                String strCmd = "";
                String strData= "";
                strCmd=Application.hashUrlSchemeForDemoMain.get("cmd");
                if ((strCmd!=null)&&(!strCmd.equals(""))) {
                    strCmd=strCmd.toLowerCase();
                    if (strCmd.equals("s3ins_full_emv")) {
                        strData=Application.hashUrlSchemeForDemoMain.get("data");
                        if ( (strData!=null)&&(!strData.equals("")) ) {
                            //Toast.makeText(m_context, "inputIntent, strData: "+strData, Toast.LENGTH_SHORT).show();
                            float fData=-1.0f;
                            try {
                                fData=Float.parseFloat(strData);
                                fData=fData*100;
                            }
                            catch (Exception ex) {
                                Logger.w(m_className, "ex: " + ex.toString());
                                fData=-1;
                            }
                            if (fData>0) {
                                String strAmount=""+(int)fData;
                                final String fstrAmount=strAmount;
                                if (m_AppSectionsPagerAdapter != null) {
                                    final Runnable r = new Runnable() {
                                        public void run() {
                                            int MAX_TRY=3;
                                            int n_try=0;
                                            while (n_try<MAX_TRY) {
                                                n_try++;
                                                Logger.i(m_className, "processInputIntent, setInputAmountAndStartTransaction, n_try: "+n_try);
                                                S3TransFragment frag = (S3TransFragment) m_AppSectionsPagerAdapter.getS3TransFragment(m_viewPager);
                                                if (frag != null) {
                                                    final S3TransFragment ffrag=frag;
                                                    ((Activity)m_context).runOnUiThread(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            ffrag.setInputAmountAndStartTransaction(fstrAmount);
                                                            Toast.makeText(m_context, strPreparing+" "+strTransaction+" . . . ", Toast.LENGTH_SHORT).show();
                                                        }
                                                    });
                                                    break;
                                                }
                                                try {
                                                    Thread.sleep(1000);
                                                }
                                                catch (InterruptedException ie) {
                                                    Logger.w(m_className, "ie: " + ie.toString());
                                                    break;
                                                }
                                            }
                                            m_threadForUrlScheme=null;
                                        }
                                    };
                                    if (m_threadForUrlScheme!=null) {
                                        m_threadForUrlScheme.interrupt();
                                        m_threadForUrlScheme=null;
                                    }
                                    m_threadForUrlScheme=new Thread(r);
                                    m_threadForUrlScheme.start();
                                }
                            }
                        }
                    }
                }
            }
            else {
                Toast.makeText(m_context, strPleaseWaitCurrentTransFinish, Toast.LENGTH_SHORT).show();
            }
            Application.hashUrlSchemeForDemoMain=null;
        }
    }

    private boolean checkRequestEnableBluetooth() {
        Callback<Object> cb_success=null;
        return checkRequestEnableBluetooth(cb_success);
    }
    private boolean checkRequestEnableBluetooth(Callback<Object> cb_success) {
        boolean bRequest=false;
        BluetoothHelper btHelperInst=BluetoothHelper.getInstance();
        if (btHelperInst.isSupportedBluetooth()) {
            if (!btHelperInst.isEnableBluetooth()) {
                Activity act=this;
                m_cbSuccess_enableBT=cb_success;
                btHelperInst.requestEnableBluetooth(act, BluetoothConnectActivity.REQUEST_ENABLE_BT);
                bRequest=true;
            }
        }
        return bRequest;
    }

    private void safeInvalidatePageViewer() {
        if (m_viewPager!=null) {
            if (m_AppSectionsPagerAdapter!=null) {
                if (m_viewPager.getAdapter() != null) {
                    m_viewPager.setAdapter(null);
                }
                m_viewPager.setAdapter(m_AppSectionsPagerAdapter);
            }
            m_viewPager.setCurrentItem(m_idxDefaultVPPage);
        }
    }

    public void setLockActionBarVisibilityStatus(boolean flag) {
        m_bLockActionBarVisibilityStatus=flag;
    }

    public void setActionBarShow() {
        if (m_bLockActionBarVisibilityStatus) {
            return;
        }
        if (m_actionBar!=null) {
            m_actionBar.show();
        }
    }

    public void setActionBarHide() {
        if (m_bLockActionBarVisibilityStatus) {
            return;
        }
        if (m_actionBar!=null) {
            m_actionBar.hide();
        }
    }

    private void onOrientationPortrait() {
        m_bLandscape=false;

        if (m_AppSectionsPagerAdapter!=null) {
            m_AppSectionsPagerAdapter.setLandscape(m_bLandscape);
        }

        // not full screen mode
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        if (m_overlay_container.getVisibility()!=View.VISIBLE) {
            setActionBarShow();
        }

        refreshActionBarTab();

        if (m_container_title_rowll!=null) {
            m_container_title_rowll.setVisibility(View.VISIBLE);
        }

        safeInvalidatePageViewer();
    }

    private void onOrientationLandscape() {
        m_bLandscape=true;

        if (m_AppSectionsPagerAdapter!=null) {
            m_AppSectionsPagerAdapter.setLandscape(m_bLandscape);
        }

        // full screen mode
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setActionBarHide();

        if (m_container_title_rowll!=null) {
            m_container_title_rowll.setVisibility(View.GONE);
        }

        safeInvalidatePageViewer();
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
     * Check for connection of active Bluetooth device
     * @return true if valid; false otherwise
     */
    public boolean isActiveBluetoothDeviceConnected() {
        boolean flag=false;
        if (m_BTEventsForActivity!=null) {
            flag = m_BTEventsForActivity.isActiveBluetoothDeviceConnected();
        }
        return flag;
    }

    // ssl

    public Data_SSLServerLocal getSSLServerDataObject(Context context) {
        Data_SSLServerLocal dataSSLServerLocal= SettingSP530SSLCertActivity.generateDataSSLServerLocal(context);
        if (dataSSLServerLocal==null) {
            Logger.i(m_className, "getSSLServerDataObject, use DEFAULT SSL cert");
            String keystorepass = "4321";
            String keypassword = "1234";
            InputStream is_ks = context.getResources().openRawResource(R.raw.testserver_sha256);
            InputStream is_ca = context.getResources().openRawResource(R.raw.spectra_ca_sha256_cert);
            dataSSLServerLocal = new Data_SSLServerLocal(context, is_ks, is_ca, keystorepass, keypassword);
            dataSSLServerLocal.m_port = 0;    // any available port
        }
        return dataSSLServerLocal;
    }

    public void sslDisconnectTriggered(boolean bLocalChannelSSL) {
        Logger.i(m_className, "sslDisconnectTriggered");
        SP530_AppMcpCHelper instSP530_AppMcpCHelper=SP530_AppMcpCHelper.getInstance();
        Data_SSLServerLocal dataSSLServerLocal=getSSLServerDataObject(m_context);
        instSP530_AppMcpCHelper.localChannelDisconnectTriggered(bLocalChannelSSL, dataSSLServerLocal);
    }

    private synchronized void start_communicationProtocol(boolean bForceNew) {
        Logger.v(m_className, "start_communicationProtocol");
        if (bForceNew) {
            SP530_AppMcpCHelper.freeInstance();
        }

        // debug
//        SP530_AppMcpCHelper.m_debugStatistic.reset();
//        updateDebugTextView();

        BluetoothSocketClass btSocketClassInst=m_BTEventsForActivity.getBluetoothSocketClassInstance();
        if (btSocketClassInst==null) {
            Logger.v(m_className, "start_communicationProtocol, btSocketClassInst is null at this moment");
            return;
        }
        final InputStream is=btSocketClassInst.getInputStream();
        final OutputStream os=btSocketClassInst.getOutputStream();
        if (is==null) {
            Logger.w(m_className, "start_communicationProtocol, InputStream is null");
            return;
        }
        if (os==null) {
            Logger.w(m_className, "start_communicationProtocol, OutputStream is null");
            return;
        }

        switch (m_dataSettingDeviceSP530.m_packetEncapsulateLevel) {
            case RAW: {
                sendEcho();
            }
            break;
            default: {
                final SP530_AppMcpCHelper instAppMcpCHelper = SP530_AppMcpCHelper.getInstance();

                // enable soh
                final int iEnableSOH=1;

                // SSL?
                boolean bSSLChOne=m_dataSettingDeviceSP530.m_bSSLChOne;

                if (bSSLChOne) {
                    Callback<Object> cb_success_listening = new Callback<Object>() {
                        @Override
                        public Object call() throws Exception {
                            instAppMcpCHelper.app_start(m_context, is, os, iEnableSOH);

                            dismissProgressDialogRunOnUiThread();

                            sendEcho();

                            return null;
                        }
                    };
                    Callback<Object> cb_fail_listening = new Callback<Object>() {
                        @Override
                        public Object call() throws Exception {
                            dismissProgressDialogRunOnUiThread();
                            ToastMessageRunOnUiThread("Fail to create SSL Server");
                            return null;
                        }
                    };

                    Data_SSLServerLocal dataSSLServerLocal = getSSLServerDataObject(m_context);
                    instAppMcpCHelper.setLocalChannelSSL(bSSLChOne, dataSSLServerLocal, cb_success_listening, cb_fail_listening);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            showProgressDialogAndDismissInTimeSlot(10 * 1000);
                        }
                    });
                }
                else {
                    instAppMcpCHelper.setLocalChannelSSL(false, null, null, null);
                    instAppMcpCHelper.app_start(m_context, is, os, iEnableSOH);

                    sendEcho();
                }
            }
        }
    }

    public void device_rebootreq() {
        final byte S3INS_REBOOTREQ=(byte)0x36;
        boolean bIncludeUIExtraTlv=false;
        safeStartCommand(S3INS_REBOOTREQ, null, null, bIncludeUIExtraTlv);
    }

    public void device_updatemcpchcfg(Data_device_mcpchcfg dataMcpChCfg, Callback<Object> cb_finish) {
        final byte S3INS_UPDMCPCHCFG=(byte)0x35;
        byte[] data=dataMcpChCfg.getByteData();

        ByteHexHelper instByteHexHelper=ByteHexHelper.getInstance();
        String strHex=instByteHexHelper.bytesArrayToHexString(data);
        Logger.i(m_className, "device_updatemcpchcfg, data length: "+data.length+", strHex: "+strHex);

        boolean bIncludeUIExtraTlv=false;
        safeStartCommand(S3INS_UPDMCPCHCFG, data, cb_finish, bIncludeUIExtraTlv);
    }

    private void sendEcho() {
        if (m_dataSettingGeneral.m_bSendEchoWhenConnected) {
            Logger.i(m_className, "sendEcho");
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    showProgressDialogAndDismissInTimeSlot(5*1000);
                }
            });
            Callback<Object> cb_finish=new Callback<Object>() {
                @Override
                public Object call() {
                    dismissProgressDialogRunOnUiThread();
                    return null;
                }
            };
            boolean bIncludeUIExtraTlv=false;
            safeStartCommand(ApplicationProtocolConstant.S3INS_ECHO, null, cb_finish, bIncludeUIExtraTlv);
        }
    }

    private void validateBTConnections() {
        Logger.i(m_className, "validateBTConnections called");

        switch (m_idx_deviceselected) {
            case 0: {
                // sp530
                if (m_BTEventsForActivity!=null) {
                    Callback<Object> cb_connected=new Callback<Object>() {
                        @Override
                        public Object call() throws Exception {
                            m_BTEventsForActivity.m_btStatus.m_stausDeviceConnection = BluetoothStatusClass.STATUS_DEVICE_CONNECTION.CONNECTED;
                            m_handler.sendEmptyMessage(MSG_HANDLER_REFRESHUI_BLUETOOTHROW);
                            Object obj=getParameter();
                            if (obj instanceof BluetoothDevice) {
                                final BluetoothDevice btdevice=(BluetoothDevice)obj;
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        String strConnect=ResourcesHelper.getBaseContextString(m_context, R.string.connect);
                                        String strHex=strConnect+" "+btdevice.getName()+" ("+btdevice.getAddress()+")";
                                        Toast.makeText(m_context, strHex, Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }

                            boolean bForceNew=true;
                            start_communicationProtocol(bForceNew);

                            return null;
                        }
                    };
                    m_BTEventsForActivity.validateBTConnection(cb_connected);
                }
            }
            break;
        }

        // bluetooth printer
        if (m_dataSettingBTPrinter.m_bEnableBTPrinter) {
            if (BTPrinterBluetoothConnectActivity.m_BTEventsForActivity != null) {
                Callback<Object> cb_printer_connected = new Callback<Object>() {
                    @Override
                    public Object call() throws Exception {
                        BTPrinterBluetoothConnectActivity.m_BTEventsForActivity.m_btStatus.m_stausDeviceConnection = BluetoothStatusClass.STATUS_DEVICE_CONNECTION.CONNECTED;
                        m_handler.sendEmptyMessage(MSG_HANDLER_REFRESHUI_BLUETOOTHPRINTERROW);
                        Object obj = getParameter();
                        if (obj instanceof BluetoothDevice) {
                            final BluetoothDevice btdevice = (BluetoothDevice) obj;
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    String strHex = "Connect " + btdevice.getName() + " (" + btdevice.getAddress() + ")";
                                    Toast.makeText(m_context, strHex, Toast.LENGTH_SHORT).show();
                                }
                            });

                            // set fetch soh rx seq
                            Lpt_funcCHelper instLpt_funcCHelper=Lpt_funcCHelper.getInstance();
                            instLpt_funcCHelper.setSOH_fetchRxSeq();
                        }
                        return null;
                    }
                };

                // load for secureRfcommSocket
                Data_setting_devicebluetoothprinter dataBTPrinterConf=new Data_setting_devicebluetoothprinter(m_context);
                BTPrinterBluetoothConnectActivity.m_BTEventsForActivity.m_bUseSecureRfcommSocket=dataBTPrinterConf.m_bUseSecureRfcommSocket;

                BTPrinterBluetoothConnectActivity.m_BTEventsForActivity.validateBTConnection(cb_printer_connected);
            }
        }
    }

    private void registerBroadcastReceiver() {
        if (m_broadcastReceiver == null) {
            m_broadcastReceiver = new BroadcastReceiver() {
                public void onReceive(Context context, Intent intent) {

                    // Get the BluetoothDevice object from the Intent
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                    String action = intent.getAction();

                    if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
//                        Logger.i(m_className, "registerBroadcastReceiver, ACTION_STATE_CHANGED");
                        String strTmp = m_className;
                        int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
                        switch (state) {
                            case BluetoothAdapter.STATE_OFF: {
                                Logger.i(m_className, "registerBroadcastReceiver, STATE_OFF");

                                BTEventsForActivity btEventsForActivity=m_BTEventsForActivity;
                                if (btEventsForActivity!=null) {
                                    btEventsForActivity.m_btStatus.m_stausDeviceConnection = BluetoothStatusClass.STATUS_DEVICE_CONNECTION.DISCONNECTED;
                                }
                                btEventsForActivity=BTPrinterBluetoothConnectActivity.m_BTEventsForActivity;
                                if (btEventsForActivity!=null) {
                                    btEventsForActivity.m_btStatus.m_stausDeviceConnection = BluetoothStatusClass.STATUS_DEVICE_CONNECTION.DISCONNECTED;
                                }

                                refreshBluetoothRowRunOnUiThread();
                            }
                            break;
                            case BluetoothAdapter.STATE_TURNING_OFF: {
                                Logger.i(m_className, "registerBroadcastReceiver, STATE_TURNING_OFF");
                            }
                            break;
                            case BluetoothAdapter.STATE_ON: {
                                Logger.i(m_className, "registerBroadcastReceiver, STATE_ON");
                                refreshBluetoothRowRunOnUiThread();
                            }
                            break;
                            case BluetoothAdapter.STATE_TURNING_ON: {
                                Logger.i(m_className, "registerBroadcastReceiver, STATE_TURNING_ON");
                            }
                            break;
                            default: {
                                Logger.i(m_className, "registerBroadcastReceiver, STATE_DEFAULT");
                            }
                            break;
                        }
                    }
                    else if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                        //Device found
//                        Logger.i(m_className, "registerBroadcastReceiver, ACTION_FOUND");
                    }
                    else if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)) {
                        //Device is now connected
                        Logger.i(m_className, "registerBroadcastReceiver, ACTION_ACL_CONNECTED");
                        BTEventsForActivity btEventsForActivity=m_BTEventsForActivity;
                        if (btEventsForActivity!=null) {
                            BluetoothDevice activeDevice = btEventsForActivity.getActiveBluetoothDevice();
                            if ((activeDevice != null) && (activeDevice.equals(device))) {
                                btEventsForActivity.m_btStatus.m_stausDeviceConnection = BluetoothStatusClass.STATUS_DEVICE_CONNECTION.CONNECTED;
                                refreshBluetoothRowRunOnUiThread();
                            }
                        }

                        btEventsForActivity=BTPrinterBluetoothConnectActivity.m_BTEventsForActivity;
                        if (btEventsForActivity!=null) {
                            BluetoothDevice activeDevice = btEventsForActivity.getActiveBluetoothDevice();
                            if ((activeDevice != null) && (activeDevice.equals(device))) {
                                btEventsForActivity.m_btStatus.m_stausDeviceConnection = BluetoothStatusClass.STATUS_DEVICE_CONNECTION.CONNECTED;
                                refreshBluetoothPrinterTextRunOnUiThread();
                            }
                        }

                    }
                    else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                        //Done searching
                        Logger.i(m_className, "registerBroadcastReceiver, ACTION_DISCOVERY_FINISHED");
                    }
                    else if (BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED.equals(action)) {
                        //Device is about to disconnect
                        Logger.i(m_className, "registerBroadcastReceiver, ACTION_ACL_DISCONNECT_REQUESTED");
                    }
                    else if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)) {
                        //Device has disconnected
                        Logger.i(m_className, "registerBroadcastReceiver, ACTION_ACL_DISCONNECTED");
                        BTEventsForActivity btEventsForActivity=m_BTEventsForActivity;
                        if (btEventsForActivity!=null) {
                            BluetoothDevice activeDevice = btEventsForActivity.getActiveBluetoothDevice();
                            if ((activeDevice != null) && (activeDevice.equals(device))) {
                                btEventsForActivity.m_btStatus.m_stausDeviceConnection = BluetoothStatusClass.STATUS_DEVICE_CONNECTION.DISCONNECTED;
                                refreshBluetoothRowRunOnUiThread();
                                if (!isOnPause()) {
                                    validateBTConnections();
                                }

                                SP530_AppMcpCHelper.freeInstance();
                            }
                        }

                        btEventsForActivity=BTPrinterBluetoothConnectActivity.m_BTEventsForActivity;
                        if (btEventsForActivity!=null) {
                            BluetoothDevice activeDevice = btEventsForActivity.getActiveBluetoothDevice();
                            if ((activeDevice != null) && (activeDevice.equals(device))) {
                                btEventsForActivity.m_btStatus.m_stausDeviceConnection = BluetoothStatusClass.STATUS_DEVICE_CONNECTION.DISCONNECTED;
                                refreshBluetoothPrinterTextRunOnUiThread();
                            }
                        }

                    }
                }
            };

            HandlerThread handlerThread = new HandlerThread("demomain");
            handlerThread.start();
            Looper looper = handlerThread.getLooper();
            Handler handler = new Handler(looper);

            // Register the BroadcastReceiver
            // Don't forget to unregister during onDestroy
            Activity act = (Activity) m_context;
            IntentFilter filter = new IntentFilter();
            // ACTION_FOUND
            filter.addAction(BluetoothDevice.ACTION_FOUND);
            // ACTION_STATE_CHANGED
            filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
            // ACTION_ACL_CONNECTED
            filter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
            // ACTION_ACL_DISCONNECT_REQUESTED
            filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED);
            // ACTION_ACL_DISCONNECTED
            filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);

            act.registerReceiver(m_broadcastReceiver, filter, null, handler);
        }
    }
    private void unregisterBroadcastReceiver() {
        if (m_broadcastReceiver!=null) {
            Activity act = (Activity) m_context;
            act.unregisterReceiver(m_broadcastReceiver);
            m_broadcastReceiver=null;
        }
    }


    /**
     * Unset active BluetoothDevice
     * @param device device going to set inactive
     */
    public static void unsetActiveBluetoothDevice(BluetoothDevice device) {
        if (m_BTEventsForActivity!=null) {
            m_BTEventsForActivity.unsetActiveBluetoothDevice(device);
        }
    }

    /**
     * Set active Bluetooth device
     * @param device device going to set active
     */
    public static void setActiveBluetoothDevice(BluetoothDevice device) {
        if (m_BTEventsForActivity!=null) {
            m_BTEventsForActivity.setActiveBluetoothDevice(device);
        }
    }

    /**
     * Get active Bluetooth device
     * @return BluetoothDevice if exist active Bluetooth device; false otherwise
     */
    public static BluetoothDevice getActiveBluetoothDevice() {
        BluetoothDevice btDevice=null;
        if (m_BTEventsForActivity!=null) {
            btDevice=m_BTEventsForActivity.getActiveBluetoothDevice();
        }
        return btDevice;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.clear();
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.mainmenu, menu);

        if (m_bTransactionUIShowMore) {
            menu.removeItem(R.id.menu_transaciton_history);
        }

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.menu_setting:
                menuOnClick_setting(item);
                return true;
            case R.id.menu_qrscan:
                menuOnClick_qrscan(item);
                return true;
            case R.id.menu_transaciton_history:
                startViewTransactionHistory();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void menuOnClick_qrscan(MenuItem item) {
        //final Intent intent = new Intent(this, com.google.zxing.client.android.CaptureActivity.class);
        final Intent intent = new Intent(this, MainActivity.class);
        intent.setAction(com.google.zxing.client.android.Intents.Scan.ACTION);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        startActivityForResult(intent, QR_CODE_REQUEST);
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {
        int pos=tab.getPosition();
        m_viewPager.setCurrentItem(pos);
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction ft) {

    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction ft) {

    }

    private void initUI() {

    }

    @Override
    public void onAttachFragment(Fragment fragment) {
        super.onAttachFragment(fragment);
    }

    /**
     * On click function for start setting activity
     * @param v corresponding view
     */
    public void onClick_setting(View v) {
        menuOnClick_setting(null);
    }

    /**
     * On click function for start view log activity
     * @param v corresponding view
     */
    public void onClick_viewHistory(View v) {
        startViewTransactionHistory();
    }

    /**
     * Get Bluetooth status string
     * @return Bluetooth status string
     */
    public String getBluetoothStatusString() {
        BTEventsForActivity btEventsForActivity=m_BTEventsForActivity;
        return getBluetoothStatusString(btEventsForActivity);
    }
    /**
     * Get Bluetooth status string
     * @param btEventsForActivity BTEventsForActivity object
     * @return Bluetooth status string
     */
    public String getBluetoothStatusString(BTEventsForActivity btEventsForActivity) {
        String strOut="";
        if (btEventsForActivity==null) {
            return strOut;
        }
        BluetoothDevice device=btEventsForActivity.getActiveBluetoothDevice();
        if (device!=null) {
            String name = device.getName();
            String address = device.getAddress();
            strOut = name + " (" + address + ")";
            if (btEventsForActivity.m_btStatus.m_stausDeviceConnection == BluetoothStatusClass.STATUS_DEVICE_CONNECTION.CONNECTED) {
                String strConnected=ResourcesHelper.getBaseContextString(m_context, R.string.connected);
                strOut = strConnected+": " + strOut;
            } else {
                String strDisconnected=ResourcesHelper.getBaseContextString(m_context, R.string.disconnected);
                strOut = strDisconnected+": " + strOut;
            }
        }
        return strOut;
    }

    /**
     * Refresh Bluetooth printer text and run in Ui thread
     */
    public void refreshBluetoothPrinterTextRunOnUiThread() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                refreshBluetoothPrinterText();
            }
        });
    }

    /**
     * Refresh Bluetooth printer text
     */
    public void refreshBluetoothPrinterText() {
        BTEventsForActivity btEventsForActivity=BTPrinterBluetoothConnectActivity.m_BTEventsForActivity;
        String strOut=getBluetoothStatusString(btEventsForActivity);
        setMessagePrinter2CommonFragments(strOut);
    }

    /**
     * Refresh Bluetooth text and run in Ui thread
     */
    private void refreshBluetoothRowRunOnUiThread() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                refreshBluetoothRow();
            }
        });
    }

    /**
     * Refresh Bluetooth text
     */
    private void refreshBluetoothRow() {
        refreshBluetoothText();
        refreshBluetoothIcon();
    }
    private void refreshBluetoothText() {
        if (m_bPostponeRefreshBluetoothText) {
            m_bPostponeRefreshBluetoothText=false;
            return;
        }
        if (m_btrowTV==null) {
            return;
        }
        if ( (m_BTEventsForActivity==null)||(m_BTEventsForActivity.getActiveBluetoothDevice()==null) ) {
            return;
        }
        String strOut=getBluetoothStatusString();
        setMessage(strOut);

        setMessageDevice2CommonFragments(strOut);
    }
    private void refreshBluetoothIcon() {
        if (m_btrowIV==null) {
            return;
        }
        BluetoothHelper instBlutetoothHelper=BluetoothHelper.getInstance();
        boolean bEnableBT=true;
        bEnableBT&=instBlutetoothHelper.isSupportedBluetooth();
        if (!bEnableBT) {
            m_btrowIV.setImageDrawable(getResources().getDrawable(R.drawable.bluetooth_disabled_128px));
            return;
        }
        bEnableBT&=instBlutetoothHelper.isEnableBluetooth();
        if (!bEnableBT) {
            m_btrowIV.setImageDrawable(getResources().getDrawable(R.drawable.bluetooth_disabled_128px));
            return;
        }
        if (m_BTEventsForActivity==null) {
            Logger.w(m_className, "refreshBluetoothIcon, m_BTEventsForActivity is NULL");
            return;
        }
        int id=R.drawable.bluetooth_enabled_128px;
        if (m_BTEventsForActivity.m_btStatus.m_stausDeviceConnection== BluetoothStatusClass.STATUS_DEVICE_CONNECTION.CONNECTED) {
            id=R.drawable.bluetooth_connected_128px;
        }
        m_btrowIV.setImageDrawable(getResources().getDrawable(id));
    }

    public void append2PopupTransMessageRunOnUiThread(String strAppend) {
        m_frag_s3transOverlay.append2PopupTransMessageByPost(strAppend);
    }

    /**
     * Set response message and run in Ui thread
     * @param strMsg message going to set
     */
    public void setResponseMessageRunOnUiThread(final String strMsg) {
        m_responseMessage=strMsg;
        setMessageRunOnUiThread(strMsg);
    }
    private void setMessageRunOnUiThread(final String strMsg) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                setMessage(strMsg);
            }
        });
    }

    /**
     * Set message
     * @param strMsg message going to set
     */
    public void setMessage(String strMsg) {
        int iColor=getResources().getColor(R.color.color_44);
        m_btrowTV.setTextColor(iColor);
        m_btrowTV.setText(strMsg);
        setMessage2CommonFragments(strMsg);
        incrementMBtRowTVCount();
    }

    /**
     * Set message with alert color
     * @param strMsg message going to set
     */
    public void setMessageWithAlertColor(String strMsg) {
        int iColor=Color.parseColor("#FF0000");
        m_btrowTV.setTextColor(iColor);
        m_btrowTV.setText(strMsg);
        setMessage2CommonFragments(strMsg);
        incrementMBtRowTVCount();
    }

    private String getFragmentTagName(int viewId, int index) {
        return "android:switcher:" + viewId + ":" + index;
    }
    private void setMessage2CommonFragments(String strMsg) {
        FragmentManager fm = getFragmentManager();
        for (int i=0; i<m_AppSectionsPagerAdapter.getCount(); i++) {
            String tag_frag=getFragmentTagName(m_viewPager.getId(), i);
            Fragment frag=fm.findFragmentByTag(tag_frag);
            if (frag!=null) {
                if (frag instanceof S3CommonFragment) {
                    ((S3CommonFragment)frag).setMessageLocal(strMsg);
                }
            }
        }
    }
    private void setMessage2CommonFragmentsRunOnUiThread(final String strMsg) {
        FragmentManager fm = getFragmentManager();
        for (int i=0; i<m_AppSectionsPagerAdapter.getCount(); i++) {
            String tag_frag=getFragmentTagName(m_viewPager.getId(), i);
            final Fragment frag=fm.findFragmentByTag(tag_frag);
            if (frag!=null) {
                if (frag instanceof S3CommonFragment) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ((S3CommonFragment) frag).setMessageLocal(strMsg);
                        }
                    });
                }
            }
        }
    }
    private void setMessageDevice2CommonFragments(String strMsg) {
        FragmentManager fm = getFragmentManager();
        for (int i=0; i<m_AppSectionsPagerAdapter.getCount(); i++) {
            String tag_frag=getFragmentTagName(m_viewPager.getId(), i);
            Fragment frag=fm.findFragmentByTag(tag_frag);
            if (frag!=null) {
                if (frag instanceof S3CommonFragment) {
                    ((S3CommonFragment) frag).setMessageDeviceLocal(strMsg);
                }
            }
        }
    }
    private void setMessagePrinter2CommonFragments(String strMsg) {
        FragmentManager fm = getFragmentManager();
        for (int i=0; i<m_AppSectionsPagerAdapter.getCount(); i++) {
            String tag_frag=getFragmentTagName(m_viewPager.getId(), i);
            Fragment frag=fm.findFragmentByTag(tag_frag);
            if (frag!=null) {
                if (frag instanceof S3CommonFragment) {
                    ((S3CommonFragment) frag).setMessagePrinterLocal(strMsg);
                }
            }
        }
    }

    private void incrementMBtRowTVCount() {
        m_btrowTVCallCount++;
        m_btrowTVCallCount%=Integer.MAX_VALUE;
    }

    /**
     * Get number of message set count
     * @return number of message set count
     */
    public int getMessageCallCount() {
        return m_btrowTVCallCount;
    }

    private void reloadSP530Class(byte commandCode) {

        ByteHexHelper instByteHexHelper=ByteHexHelper.getInstance();
        String commandCodeString=instByteHexHelper.byteToHexString(commandCode);
        String commandDescription=ApplicationProtocolConstant.S3RC_COMMAND_MAP.get(commandCode);
        if (commandDescription==null) {
            commandDescription="No defined in S3RC_COMMAND_MAP";
        }
        Logger.i(m_className, "reloadSP530Class, "+commandDescription+" ["+commandCodeString+"]");

        if (m_sp530Class!=null) {
            m_sp530Class.onDestroy();
            m_sp530Class=null;
        }
        if (m_t1000Class!=null) {
            m_t1000Class.onDestroy();
            m_t1000Class=null;
        }

        switch (m_idx_deviceselected) {
            case 0: {
                boolean bTransactionSimulateTCPHost=m_dataSettingS3Trans.m_bTransactionSimulateTCPHost;
                boolean bAlwaysUseCRC=m_dataSettingDeviceSP530.m_bAlwaysUseCRC;
                boolean bSSLChOne=m_dataSettingDeviceSP530.m_bSSLChOne;
                BluetoothSocketClass btSocketClassInst=m_BTEventsForActivity.getBluetoothSocketClassInstance();


                if (m_currentExecuteCommand instanceof Data_executeCommand_tmsdl) {
                    SP530_bt_S3INS_TMSDL sp530class= new SP530_bt_S3INS_TMSDL(m_context, btSocketClassInst);
                    Data_executeCommand_tmsdl dataObj=(Data_executeCommand_tmsdl)m_currentExecuteCommand;
                    sp530class.setTmsDlData(dataObj.m_listFtpdlObj, dataObj.m_filePath);
                    sp530class.setCallback_progress(dataObj.m_cb_progress);
                    m_sp530Class=sp530class;
                }
                else if (m_currentExecuteCommand instanceof Data_executeCommand_filedl) {
                    SP530_bt_S3INS_FILEDL sp530class= new SP530_bt_S3INS_FILEDL(m_context, btSocketClassInst);
                    Data_executeCommand_filedl dataObj=(Data_executeCommand_filedl)m_currentExecuteCommand;
                    sp530class.setFileDlData(dataObj.m_uri_file);
                    sp530class.setCallback_progress(dataObj.m_cb_progress);
                    m_sp530Class=sp530class;
                }
                else {
                    m_sp530Class = new SP530_bt_S3INS(m_context, btSocketClassInst);
                }

                if (m_sp530Class!=null) {
                    m_sp530Class.setPacketEncapsulateLevel(m_dataSettingDeviceSP530.m_packetEncapsulateLevel);

                    // device simulate TCP host?
                    if (bTransactionSimulateTCPHost) {
                        m_sp530Class.setSimulateTCPHost();
                    }

                    // always use crc checksum?
                    m_sp530Class.setAlwaysUseCRCChecksum(bAlwaysUseCRC);

                    // SSL channel one?
                    m_sp530Class.setLogicalChannelOneUseSSL(bSSLChOne);
                }
            }
            break;
            case 1: {
                Data_tcpip_v4 dataTcpIpV4=T1000DemoSocketConnectActivity.m_dataTcpIpV4;
                if (dataTcpIpV4==null) {
                    Logger.w(m_className, "reloadSP530Class, dataTcpIpV4 is NULL");
                    return;
                }
                TCPIPSocketClass tcpipSocketClass=dataTcpIpV4.m_tcpipSocketClass;
                if (tcpipSocketClass==null) {
                    Logger.w(m_className, "reloadSP530Class, tcpipSocketClass is NULL");
                    return;
                }
                m_t1000Class=new T1000_S3INS(m_context, tcpipSocketClass);
            }
            break;
        }
    }

    /**
     * Manage connected socket function
     * @param socket BluetoothSocket object
     */
    public void manageConnectedSocket(BluetoothSocket socket) {
        if (m_BTEventsForActivity!=null) {
            m_BTEventsForActivity.manageConnectedSocket(socket);
            m_BTEventsForActivity.m_btStatus.m_stausDeviceConnection = BluetoothStatusClass.STATUS_DEVICE_CONNECTION.CONNECTED;

            boolean bForceNew=true;
            start_communicationProtocol(bForceNew);
        }
    }

    /**
     * Free resource for BTEventsForActivity object
     */
    public void safeFreeBTSocket() {
        if (m_BTEventsForActivity!=null) {
            // fix Android OS 4.4 crashes on bt disconnect
            BtauxCHelper instBtauxCHelper=BtauxCHelper.getInstance();
            instBtauxCHelper.setIO(null, null);

            m_BTEventsForActivity.safeFreeBTSocket();
        }
    }

    /**
     * Get active fragment
     * @return Fragment object; null for no fragment object
     */
    public Fragment getActiveFragment() {
        Fragment frag=m_AppSectionsPagerAdapter.getActiveFragment(m_viewPager, 0);
        if (frag==null) {
            Logger.e(m_className, "getActiveFragment, fragment is NULL");
        }
        return frag;
    }
    public Fragment getS3TemplateFragment() {
        Fragment frag=m_AppSectionsPagerAdapter.getS3TemplateFragment(m_viewPager);
        return frag;
    }
    public Fragment getS3CommandFragment() {
        Fragment frag=m_AppSectionsPagerAdapter.getS3CommandFragment(m_viewPager);
        return frag;
    }


    /**
     * Get EditText view for input amount
     * @return EditText object; null for no EditText view for input amount
     */
    public EditText getViewInputAmountEditText() {
        EditText ed=null;
        Fragment frag=getActiveFragment();
        if (frag instanceof S3TransFragment) {
            S3TransFragment inst=(S3TransFragment)frag;
            ed=inst.getViewInputAmountEditText();
        }
        return ed;
    }

    /**
     * Disable all onClick event triggering of main screen
     */
    public void disableAllUIInputs() {
        if (m_overlay_container.getVisibility()!=View.VISIBLE) {
            m_overlay_container.setVisibility(View.VISIBLE);

            //getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
            EditText ed = getViewInputAmountEditText();
            hideSoftKeyboard(ed);

            setLockActionBarVisibilityStatus(false);
            setActionBarHide();
            setLockActionBarVisibilityStatus(true);
        }
    }

    /**
     * Enable all onClick event triggering of main screen and run in Ui thread with delay time
     * @param tInMs dealy time in ms for running
     */
    public void enableAllUIInputsRunOnUiThreadWithDelay(int tInMs) {
        m_handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                enableAllUIInputs();
            }
        },tInMs);
    }

    /**
     * Enable all onClick event triggering of main screen and run in Ui thread
     */
    public void enableAllUIInputsRunOnUiThread() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                enableAllUIInputs();
            }
        });
    }

    /**
     * Enable all onClick event triggering of main screen
     */
    public void enableAllUIInputs() {
        if (m_overlay_container.getVisibility()!=View.GONE) {
            m_overlay_container.setVisibility(View.GONE);

            setLockActionBarVisibilityStatus(false);
            setActionBarShow();
        }
    }

    /**
     * Hide soft keyboard
     */
    public void hideSoftKeyboard() {
         hideSoftKeyboard(m_mainll);
    }
    private void hideSoftKeyboard(View v) {
        if (v==null) {
            return;
        }
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm!=null) {
            imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
        }
    }


    private static final int TIME_INTERVAL = 2000; // # milliseconds, desired time passed between two back presses.
    private long mBackPressed;
    @Override
    public void onBackPressed() {
        if (mBackPressed + TIME_INTERVAL > System.currentTimeMillis()) {
            hideSoftKeyboard();
            super.onBackPressed();
            return;
        }
        else {
            String strText=ResourcesHelper.getBaseContextString(m_context, R.string.press_backagn_exit);
            Toast.makeText(m_context, strText, Toast.LENGTH_SHORT).show();
        }

        mBackPressed = System.currentTimeMillis();
    }




    // click - from S3TransFragment

    private boolean bEnableTransactionClick=true;

    /**
     * Start batch transaction
     * @param dataBuf input data array
     */
    public void startTransaction_s3trans_batch(byte[] dataBuf) {
        startTransaction_s3trans(dataBuf, TRANSACTIONMODE.TRANSACTIONMODE_AMOUNT_BATCH);
    }

    public void startTransaction_s3transBySelectDevice(byte[] dataBuf) {
        if (!isOnPause()) {
            int id_act = Application.ACTIVITY_DEVICESELECT;
            Application.startActivity(m_context, id_act, null, ActivityRequestCodeEnum.RequestCode.REQUEST_DEVICE_SELECT_AND_STARTTRANSACTION.toInt());
            m_dataBufPtrForOnActivityResult=dataBuf;
        }
    }

    /**
     * Start transaction
     * @param dataBuf input data array
     */
    public void startTransaction_s3trans(byte[] dataBuf) {
        startTransaction_s3trans(dataBuf, TRANSACTIONMODE.TRANSACTIONMODE_AMOUNT);
    }
    private void startTransaction_s3trans(byte[] dataBuf, TRANSACTIONMODE transactionMode) {
        if (!bEnableTransactionClick) {
            return;
        }
        bEnableTransactionClick=false;
        m_handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                bEnableTransactionClick=true;
            }
        },1000);

        m_transactionMode=transactionMode;

        Callback<Object> cb_finsih=null;
        if (m_transactionMode==TRANSACTIONMODE.TRANSACTIONMODE_AMOUNT) {
            cb_finsih=m_cb_transfinish_singlemode;
        }
        else if (m_transactionMode==TRANSACTIONMODE.TRANSACTIONMODE_AMOUNT_BATCH) {
            m_bCancelLoopBatchMode=false;
            cb_finsih=m_cb_transfinish_batchmode;
        }
        safeStartCommand(ApplicationProtocolConstant.S3INS_FULL_EMV, dataBuf, cb_finsih);
    }

    /**
     * Get callback function for finish batch mode transaction
     * @return Callback object; null for no Callback object
     */
    public Callback<Object> getCBTransFinishBatchMode() {
        return m_cb_transfinish_batchmode;
    }

    private int m_countBatchTrans=0;

    private void loopBatchTransactions() {
        boolean bStartCommand=true;
        loopBatchTransactions(bStartCommand);
    }
    private void loopBatchTransactions(boolean bStartCommand) {
        String strProcessing=ResourcesHelper.getBaseContextString(m_context, R.string.processing);
        String strBatch=ResourcesHelper.getBaseContextString(m_context, R.string.batch);
        m_countBatchTrans++;
        byte[] dataBuf=m_currentExecuteCommand.m_dataBuf;
        String strText=strProcessing+" "+strBatch+" ("+m_countBatchTrans+"/"+m_dataSettingS3Trans.m_maxBatch+") "+getTransactionAmountStringRaw(dataBuf);

        m_frag_s3transOverlay.setPopupTransMessageByPost(strText);

        if (bStartCommand) {
            startCommand();
        }
    }

    private Toast m_toastWaitTransactionFinish;

    public boolean isShowingTransReceipt() {
        boolean bShow=false;

        switch (m_idx_deviceselected) {
            case 0: {
                if (m_sp530Class!=null) {
                    bShow=m_sp530Class.isShowingTransReceipt();
                }
            }
            break;
            case 1: {
                if (m_t1000Class!=null) {
                    bShow=m_t1000Class.isShowingTransReceipt();
                }
            }
            break;
        }
        return bShow;
    }

    private boolean isTransactionFinish() {
        boolean bFinish=true;

        switch (m_idx_deviceselected) {
            case 0: {
                if (m_sp530Class!=null) {
                    bFinish=m_sp530Class.isFinish();
                }
            }
            break;
            case 1: {
                if (m_t1000Class!=null) {
                    bFinish=m_t1000Class.isFinish();
                }
            }
            break;
        }
        return bFinish;
    }

    public void showS3INSResponseData() {
        Data_S3INS_response dataResponse=getS3INSResponseObject();
        if (dataResponse==null) {
            ToastMessageRunOnUiThread("Data_S3INS_response is empty");
            return;
        }
        byte[] dataBuf=dataResponse.m_data;
        if (dataBuf==null) {
            ToastMessageRunOnUiThread("Data_S3INS_response, data is empty");
            return;
        }
        ByteHexHelper instByteHexHelper=ByteHexHelper.getInstance();
        String resultString=instByteHexHelper.bytesArrayToHexString(dataBuf);
        if (resultString!=null) {
            StringHelper instStringHelper=StringHelper.getInstance();
            resultString=instStringHelper.addPaddingStartingFromHead(" ", resultString, 16);
        }
        String strTitle="S3INS response";
        Intent intent=new Intent(m_context, ShowStringActivity.class);
        intent.putExtra(ShowStringActivity.KEY_INPUTTITLE, strTitle);
        intent.putExtra(ShowStringActivity.KEY_INPUTSTRING, resultString);
        m_context.startActivity(intent);
    }

    public Data_S3INS_response getS3INSResponseObject() {
        Data_S3INS_response dataResponse=null;
        switch (m_idx_deviceselected) {
            case 0: {
                if (m_sp530Class!=null) {
                    dataResponse=m_sp530Class.getS3INSResponseObject();
                }
            }
            break;
            case 1: {
                if (m_t1000Class!=null) {
                    dataResponse=m_t1000Class.getS3INSResponseObject();
                }
            }
            break;
        }
        return dataResponse;
    }

    public void getTransferTmsResult(Callback<Object> cb_finish, Callback<Object> cb_progress) {
        Logger.i(m_className, "getTransferTmsResult call");

        // dummy data
        ArrayList<Data_ftpdlObject> dataFtpdlObjectList=new ArrayList<>();
        String filePath="";

        byte[] commandCodeArray=new byte[] { SP530_bt_S3INS_TMSDL.S3INS_TMSDLRESULT };

        // put data here
        setCurrentExecuteCommand_tmsdl(commandCodeArray, dataFtpdlObjectList, filePath, cb_finish, cb_progress);

        m_idx_deviceselected=DeviceSelectActivity.getSelectedIndex(m_context);

        boolean bCommandValid2Start=isCommandValid2Start(m_idx_deviceselected, false);
        if (!bCommandValid2Start) {
            return;
        }

        preStartCommand();
        startCommand();
    }

    public void startTransferFile(Uri uri_file, Callback<Object> cb_finish, Callback<Object> cb_progress) {
        Logger.i(m_className, "startTransferFile call");

        if (uri_file==null) {
            Logger.w(m_className, "startTransferFile, uri_file is null");
            return;
        }

        byte[] commandCodeArray=new byte[] {SP530_bt_S3INS_FILEDL.S3INS_DATADL };

        // put data here
        setCurrentExecuteCommand_filedl(commandCodeArray, uri_file, cb_finish, cb_progress);

        m_idx_deviceselected=DeviceSelectActivity.getSelectedIndex(m_context);

        boolean bCommandValid2Start=isCommandValid2Start(m_idx_deviceselected, false);
        if (!bCommandValid2Start) {
            return;
        }

        preStartCommand();
        startCommand();
    }

    public void setCurrentExecuteCommand_filedl(byte[] commandCodeArray, Uri uri_file, Callback<Object> cb_finish, Callback<Object> cb_progress) {
        Logger.i(m_className, "setCurrentExecuteCommand_tmsdl call");
        Data_executeCommand_filedl dataExecuteCommandFiledl=new Data_executeCommand_filedl();
        dataExecuteCommandFiledl.m_commandCode=commandCodeArray;
        dataExecuteCommandFiledl.m_dataBuf=null;
        dataExecuteCommandFiledl.m_cb_finish=cb_finish;

        dataExecuteCommandFiledl.m_uri_file=uri_file;
        dataExecuteCommandFiledl.m_cb_progress=cb_progress;

        dataExecuteCommandFiledl.m_bIncludeUIExtraTlv=false;

        m_currentExecuteCommand=dataExecuteCommandFiledl;
    }

    public void startTransferTms(ArrayList<Data_ftpdlObject> dataFtpdlObjectList, String filePath, Callback<Object> cb_finish, Callback<Object> cb_progress) {
        Logger.i(m_className, "startTransferTms call");

        if (dataFtpdlObjectList==null) {
            Logger.w(m_className, "startTransferTms, dataFtpdlObjectList is null");
            return;
        }

        byte[] commandCodeArray=new byte[] { SP530_bt_S3INS_TMSDL.S3INS_NUM_TMSDL };

        // put data here
        setCurrentExecuteCommand_tmsdl(commandCodeArray, dataFtpdlObjectList, filePath, cb_finish, cb_progress);

        m_idx_deviceselected=DeviceSelectActivity.getSelectedIndex(m_context);

        boolean bCommandValid2Start=isCommandValid2Start(m_idx_deviceselected, false);
        if (!bCommandValid2Start) {
            return;
        }

        preStartCommand();
        startCommand();
    }

    public void setCurrentExecuteCommand_tmsdl(byte[] commandCodeArray, ArrayList<Data_ftpdlObject> dataFtpdlObjectList, String filePath, Callback<Object> cb_finish, Callback<Object> cb_progress) {
        Logger.i(m_className, "setCurrentExecuteCommand_tmsdl call");
        Data_executeCommand_tmsdl dataExecuteCommandTmsdl=new Data_executeCommand_tmsdl();
        dataExecuteCommandTmsdl.m_commandCode=commandCodeArray;
        dataExecuteCommandTmsdl.m_dataBuf=null;
        dataExecuteCommandTmsdl.m_cb_finish=cb_finish;

        dataExecuteCommandTmsdl.m_listFtpdlObj=dataFtpdlObjectList;
        dataExecuteCommandTmsdl.m_filePath=filePath;
        dataExecuteCommandTmsdl.m_cb_progress=cb_progress;

        dataExecuteCommandTmsdl.m_bIncludeUIExtraTlv=false;

        m_currentExecuteCommand=dataExecuteCommandTmsdl;
    }

    /**
     * Set current executing command
     * @param commandCode command code byte
     * @param dataBuf input data array
     * @param cb_finish callback function for finish
     */
    public void setCurrentExecuteCommand(byte commandCode, byte[] dataBuf, Callback<Object> cb_finish, boolean bIncludeUIExtraTlv) {
        byte[] commandCodeArray=new byte [1];
        commandCodeArray[0]=commandCode;
        setCurrentExecuteCommand(commandCodeArray, dataBuf, cb_finish, bIncludeUIExtraTlv);
    }
    /**
     * Set current executing command
     * @param commandCodeArray command code byte array
     * @param dataBuf input data array
     * @param cb_finish callback function for finish
     */
    public void setCurrentExecuteCommand(byte[] commandCodeArray, byte[] dataBuf, Callback<Object> cb_finish, boolean bIncludeUIExtraTlv) {
        m_currentExecuteCommand=new Data_executeCommand();
        m_currentExecuteCommand.m_commandCode=new byte[commandCodeArray.length];
        System.arraycopy(commandCodeArray, 0, m_currentExecuteCommand.m_commandCode, 0, commandCodeArray.length);
        m_currentExecuteCommand.m_dataBuf=dataBuf;
        m_currentExecuteCommand.m_cb_finish=cb_finish;
        m_currentExecuteCommand.m_bIncludeUIExtraTlv=bIncludeUIExtraTlv;
    }


    // for sp530 command

    public boolean isCommandValid2Start(boolean bStartActivityForResult) {
        m_idx_deviceselected=DeviceSelectActivity.getSelectedIndex(m_context);
        return isCommandValid2Start(m_idx_deviceselected, bStartActivityForResult);
    }

    private boolean isCommandValid2Start(int idx_deviceselected, boolean bStartActivityForResult) {
        final String strPleaseEnableWifi=ResourcesHelper.getBaseContextString(m_context, R.string.please_enable_wifi);
        final String strPleaseConnectWifiAP=ResourcesHelper.getBaseContextString(m_context, R.string.please_connect_wifi_ap);
        final String strPleaseMakeTCPConnection=ResourcesHelper.getBaseContextString(m_context, R.string.please_make_tcp_connection);
        switch (idx_deviceselected) {
            case 0: {
                if (need2StartBTConnectionActivity()) {
                    popupNeed2StartBTConnectionActivity(bStartActivityForResult);
                    return false;
                }
                BluetoothSocketClass btSocketClassInst=m_BTEventsForActivity.getBluetoothSocketClassInstance();
                if ( (btSocketClassInst==null)||(btSocketClassInst.getInputStream()==null)||(btSocketClassInst.getOutputStream()==null) ) {
                    Logger.i(m_className, "safeStartCommand, btSocketClassInst is not good");
                    dismissProgressDialog();
                    if (bStartActivityForResult) {
                        startBluetoothConnectActivityForResultWithNoAnimation();
                    }
                    else {
                        startBluetoothConnectActivityWithNoAnimation();
                    }
                    return false;
                }

                boolean bSSLChOne=m_dataSettingDeviceSP530.m_bSSLChOne;
                if (bSSLChOne) {
                    int currentAndroidSDKVersion=Build.VERSION.SDK_INT;
                    if (currentAndroidSDKVersion<Build.VERSION_CODES.LOLLIPOP) {
                        Toast.makeText(m_context, "currentOSVersion: " + currentAndroidSDKVersion+" < "+Build.VERSION_CODES.LOLLIPOP, Toast.LENGTH_LONG).show();
                        int id_act=Application.ACTIVITY_SETTINGDEVICESP530;
                        Application.startActivity(m_context, id_act);
                        return false;
                    }
                }
            }
            break;
            case 1: {
                NetworkHelper instNetworkHelper=NetworkHelper.getInstance();
                if (!instNetworkHelper.isWifiEnable(m_context)) {
                    Toast.makeText(m_context, strPleaseEnableWifi, Toast.LENGTH_SHORT).show();
                    instNetworkHelper.startActivity_wifiSetting(m_context);
                    return false;
                }
                if (!instNetworkHelper.isWifiConnected(m_context)) {
                    Toast.makeText(m_context, strPleaseConnectWifiAP, Toast.LENGTH_SHORT).show();
                    instNetworkHelper.startActivity_wifiSetting(m_context);
                    return false;
                }
                if (!T1000DemoSocketConnectActivity.isSocketConnected()) {
                    Toast.makeText(m_context, strPleaseMakeTCPConnection, Toast.LENGTH_SHORT).show();
                    dismissProgressDialog();
                    startT1000ConnectActivityWithNoAnimation();
                    return false;
                }
            }
            break;
        }
        return true;
    }

    /**
     * Start command in a safe way
     * @param commandCode command code byte
     * @param dataBuf input data byte array
     * @param cb_finish callback function for finish
     */
    public void safeStartCommand(byte commandCode, byte[] dataBuf, Callback<Object> cb_finish) {
        boolean bIncludeUIExtraTlv=true;
        safeStartCommand(commandCode, dataBuf, cb_finish, bIncludeUIExtraTlv);
    }
    public void safeStartCommand(byte commandCode, byte[] dataBuf, Callback<Object> cb_finish, boolean bIncludeUIExtraTlv) {
        Logger.i(m_className, "safeStartCommand called");

        // put data here
        setCurrentExecuteCommand(commandCode, dataBuf, cb_finish, bIncludeUIExtraTlv);

        m_idx_deviceselected=DeviceSelectActivity.getSelectedIndex(m_context);

        boolean bCommandValid2Start=isCommandValid2Start(m_idx_deviceselected, true);
        if (!bCommandValid2Start) {
            return;
        }

        preStartCommand();
        startCommand();
    }
    private void startCommand() {
        byte commandCode=m_currentExecuteCommand.m_commandCode[0];

        List<Byte> dataList=new ArrayList<Byte>();
        if ( (m_currentExecuteCommand.m_dataBuf!=null)&&(m_currentExecuteCommand.m_dataBuf.length>0) ) {
            dataList.addAll(Arrays.asList(ArrayUtils.toObject(m_currentExecuteCommand.m_dataBuf)));
        }

        // add transaction extra tlv
        if (m_currentExecuteCommand.m_bIncludeUIExtraTlv) {
            String strExtraTlv = m_dataSettingS3Trans.m_strTransactionExtraTlv;
            if ((strExtraTlv != null) && (!strExtraTlv.equals(""))) {
                byte[] byteExtraTlv = null;
                try {
                    ByteHexHelper instByteHexHelper = ByteHexHelper.getInstance();
                    byteExtraTlv = instByteHexHelper.hexStringToByteArray(strExtraTlv);
                } catch (Exception ex) {
                    Logger.w(m_className, "ex: " + ex.toString());
                    byteExtraTlv = null;
                }
                if ((byteExtraTlv != null) && (byteExtraTlv.length > 0)) {
                    dataList.addAll(Arrays.asList(ArrayUtils.toObject(byteExtraTlv)));
                }
            }
        }

        byte[] dataBuf= Bytes.toArray(dataList);

        Callback<Object> cb_finish=m_currentExecuteCommand.m_cb_finish;

        ApplicationProtocolHelper instApplicationProtocolHelper=ApplicationProtocolHelper.getInstance();

        m_currentExecuteSeqno=instApplicationProtocolHelper.getCurrentSequnceNumber();

        m_responseMessage="";

        reloadSP530Class(commandCode);

        switch (m_idx_deviceselected) {
            case 0: {
                m_sp530Class.set_callback_progressText(m_cb_transprogressText);
                m_sp530Class.start(commandCode, dataBuf, cb_finish);
            }
            break;
            case 1: {
                m_t1000Class.set_callback_progressText(m_cb_transprogressText);
                m_t1000Class.start(commandCode, dataBuf, cb_finish);
            }
            break;
        }
    }
    private void preStartCommand() {
        String strStart=ResourcesHelper.getBaseContextString(m_context, R.string.start);
        String strCancel=ResourcesHelper.getBaseContextString(m_context, R.string.cancel);
        switch (m_currentExecuteCommand.m_commandCode[0]) {
            case ApplicationProtocolConstant.S3INS_TRANS:
            case ApplicationProtocolConstant.S3INS_FULL_EMV:
            {
                byte[] dataBuf=m_currentExecuteCommand.m_dataBuf;
                if (m_currentExecuteCommand.m_cb_finish.equals(m_cb_transfinish_singlemode)) {
                    setS3TransOverlayFragment();
                    m_frag_s3transOverlay.setTransButtonText(strCancel);
                    disableAllUIInputs();
                    String strText=strStart+" "+getTransactionAmountStringRaw(dataBuf);

                    m_frag_s3transOverlay.setPopupTransMessageByPost(strText);

                    boolean bFinish=isTransactionFinish();
                    if (!bFinish) {
                        if (m_toastWaitTransactionFinish==null) {
                            String strWaitTransFinish=ResourcesHelper.getBaseContextString(m_context, R.string.please_wait_currenttransfinish);
                            m_toastWaitTransactionFinish = Toast.makeText(m_context, strWaitTransFinish, Toast.LENGTH_SHORT);
                        }
                        m_toastWaitTransactionFinish.show();
                        return;
                    }
                    if (m_toastWaitTransactionFinish!=null) {
                        m_toastWaitTransactionFinish.cancel();
                    }
                    strText=strStart+" "+getTransactionAmountStringRaw(dataBuf);
                    setMessage(strText);
                }
                else if (m_currentExecuteCommand.m_cb_finish.equals(m_cb_transfinish_batchmode)) {
                    setS3TransOverlayFragment();
                    m_frag_s3transOverlay.setTransButtonText(strCancel);
                    disableAllUIInputs();
                    m_countBatchTrans=0;
                    boolean bStartCommand=false;
                    loopBatchTransactions(bStartCommand);
                }
                break;
            }
            default:
                break;
        }
    }

    private void cb_finish_common() {
        m_responseMessage="";
    }
    private void showTransResponseMessage(String strPrefix, String strStatus) {
        final String strOK=ResourcesHelper.getBaseContextString(m_context, R.string.ok);
        String strXMLStatus=ResourcesHelper.getBaseContextString(m_context, R.string.status);
        String strText="";
        if (strPrefix!=null) {
            strText=strPrefix;
        }
        strText+=", "+strXMLStatus+": ";
        strText+=strStatus;
        if ((m_responseMessage!=null)&&(!m_responseMessage.equals(""))) {
            strText+=", "+m_responseMessage;
        }
        m_frag_s3transOverlay.setPopupTransMessageByPost(strText);

        setMessage2CommonFragmentsRunOnUiThread(strText);
    }
    private void initCallbacks() {
        final String strOK=ResourcesHelper.getBaseContextString(m_context, R.string.ok);
        final String strError=ResourcesHelper.getBaseContextString(m_context, R.string.error);
        final String strFinish=ResourcesHelper.getBaseContextString(m_context, R.string.finish);
        final String strSendFail=ResourcesHelper.getBaseContextString(m_context, R.string.send_fail);
        final String strResponse=ResourcesHelper.getBaseContextString(m_context, R.string.response);
        final String strRequestCanceled=ResourcesHelper.getBaseContextString(m_context, R.string.request_canceled);
        final String strBatch=ResourcesHelper.getBaseContextString(m_context, R.string.batch);

        m_cb_general=new Callback<Object>() {
            @Override
            public Void call() throws Exception {
                cb_finish_common();
                return null;
            }
        };
        m_cb_transfinish_singlemode=new Callback<Object>() {
            @Override
            public Void call() throws Exception {
                String strStatus=strError;
                Object param = this.getParameter();
                if (param instanceof Data_S3INS_response) {
                    Data_S3INS_response dataS3INSResponse=(Data_S3INS_response)param;
                    TRANSACTIONSTATUS status_trans=dataS3INSResponse.m_statusTrans;
                    switch (status_trans) {
                        case STATUS_FINISH: {
                            strStatus=strFinish;
                        }
                        break;
                        case STATUS_REQUEST_SEND_FAIL: {
                            strStatus=strError+" ("+strSendFail+")";
                            switch (m_idx_deviceselected) {
                                case 0: {
                                }
                                break;
                                case 1: {
                                    String key=m_dataSettingDeviceT1000.getName();
                                    TCPHelper instTCPHelper=TCPHelper.getInstance();
                                    instTCPHelper.tcp_disconnect(key);
                                }
                                break;
                            }
                        }
                        break;
                        case STATUS_RESPONSE_GET_FAIL: {
                            strStatus=strError+" ("+strResponse+")";
                            switch (m_idx_deviceselected) {
                                case 0: {
                                }
                                break;
                                case 1: {
                                    String key=m_dataSettingDeviceT1000.getName();
                                    TCPHelper instTCPHelper=TCPHelper.getInstance();
                                    instTCPHelper.tcp_disconnect(key);
                                }
                                break;
                            }
                        }
                        break;
                        case STATUS_GET_LOCALCHANNEL_INDEX_FAIL: {
                            strStatus=strError+" (Get local channel index)";
                        }
                        break;
                        case STATUS_WAIT_COMM_READY_FAIL: {
                            strStatus=strError+" (Communication)";
                        }
                        break;
                        case STATUS_WAIT_SSLSERVER_READY_FAIL: {
                            strStatus=strError+" (SSL SERVER LOCAL)";
                        }
                        break;
                        case STATUS_WAIT_SSL_HANDSHAKE_COMPLETE_FAIL: {
                            strStatus=strError+" (SSL HANDSHAKE)";
                        }
                        break;
//                        case STATUS_WAIT_TCP_READY_FAIL: {
//                            strStatus=strError+" (TCP)";
//                        }
//                        break;
                        case STATUS_REQUEST_CANCEL: {
                            strStatus=strRequestCanceled;
                            setCurrentExecuteCommand(ApplicationProtocolConstant.S3INS_RESET, null, m_cb_general, false);
                            startCommand();
                        }
                        break;
                        case STATUS_LOCALCHANNEL_DISCONNECT_EVENT_TRIGGERED: {
                            strStatus=strError+" (LOCALCHANNEL_DISCONNECT)";
                        }
                        break;
                    }
                }

                byte[] dataBuf=m_currentExecuteCommand.m_dataBuf;
                String strText=getTransactionAmountStringRaw(dataBuf);
                showTransResponseMessage(strText, strStatus);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        m_frag_s3transOverlay.setTransButtonText(strOK);
                    }
                });

                cb_finish_common();

                return null;
            }
        };
        m_cb_transfinish_batchmode=new Callback<Object>() {
            @Override
            public Void call() throws Exception {
                if (m_bCancelLoopBatchMode) {
                    Logger.i(m_className, "m_cb_transfinish_batchmode, m_bCancelLoopBatchMode is TRUE, cancel batch looping");
                    return null;
                }
                Object param = this.getParameter();
                if (param instanceof Data_S3INS_response) {
                    Data_S3INS_response dataS3INSResponse=(Data_S3INS_response)param;
                    String strStatus=strError;
                    TRANSACTIONSTATUS status_trans = dataS3INSResponse.m_statusTrans;
                    if (status_trans==TRANSACTIONSTATUS.STATUS_FINISH) {
                        strStatus=strFinish;
                    }
                    byte[] dataBuf = m_currentExecuteCommand.m_dataBuf;
                    if (status_trans != TRANSACTIONSTATUS.STATUS_REQUEST_CANCEL) {
                        if (m_countBatchTrans < m_dataSettingS3Trans.m_maxBatch) {
                            String strText = strBatch+" (" + m_countBatchTrans + "/" + m_dataSettingS3Trans.m_maxBatch + ") " + getTransactionAmountStringRaw(dataBuf);
                            showTransResponseMessage(strText, strStatus);
                            Message msg = new Message();
                            msg.what = MSG_HANDLER_LOOPBATCHTRANSACTIONS;
                            m_handler.sendMessageDelayed(msg, 2000);
                            cb_finish_common();
                            return null;
                        }
                    }
                    String strText = strBatch+" (" + m_countBatchTrans + "/" + m_dataSettingS3Trans.m_maxBatch + ") " + getTransactionAmountStringRaw(dataBuf);
                    showTransResponseMessage(strText, strStatus);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            m_frag_s3transOverlay.setTransButtonText(strOK);
                        }
                    });
                    cb_finish_common();
                }
                return null;
            }
        };

        m_cb_transprogressText=new Callback<Object>() {
            @Override
            public Void call() throws Exception {
                Object param = this.getParameter();
                if (param instanceof String) {
                    String strObj=(String)param;
                    m_frag_s3transOverlay.setSubPopupTransMessageByPost(strObj);
                }
                return null;
            }
        };
    }

    /**
     * Get raw amount
     * @param buf input byte array
     * @return amount in double format
     */
    public double getAmountRaw(byte[] buf) {
        if (buf == null) {
            return -1.0f;
        }
        ByteHexHelper instByteHexHelper = ByteHexHelper.getInstance();
        String strHex = instByteHexHelper.bytesArrayToHexString(buf);
        long lAmount=Long.parseLong(strHex);
        double amount = lAmount/100.0;
        return amount;
    }

    /**
     * Get raw amount in string format
     * @param buf input byte array
     * @return amount in string format
     */
    public String getTransactionAmountStringRaw(byte[] buf) {
        String strTransaction=ResourcesHelper.getBaseContextString(m_context, R.string.transaction);
        String strSeqNoShort=ResourcesHelper.getBaseContextString(m_context, R.string.sequence_number_short);
        String strAmount=ResourcesHelper.getBaseContextString(m_context, R.string.amount);
        double amount=getAmountRaw(buf);
        int seqNumber=m_currentExecuteSeqno;
        String strText=strTransaction+" "+strSeqNoShort+" "+seqNumber+", "+strAmount+": "+String.format("%.2f", amount);
        return strText;
    }

    /**
     * Start view transaction history activity
     */
    public void startViewTransactionHistory() {
        String strNoRecordsAreFound=ResourcesHelper.getBaseContextString(m_context, R.string.no_records_are_found);
        if (!isOnPause()) {
            int n_record=DBTransSummaryDetail.getCount(m_context);
            if (n_record>0) {
                int id = Application.ACTIVITY_TRANSSUMMARY;
                Application.startActivity(m_context, id);
            }
            else {
                Toast.makeText(m_context, strNoRecordsAreFound, Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Start transaction receipt activity for demo
     */
    public void startDemoTransactionReceipt() {
        if (!isOnPause()) {
            HashMap<String, String> hash = new HashMap<String, String>();
            hash.put("demo", "true");
            int id = Application.ACTIVITY_TRANSRECEIPT;
            Application.startActivity(m_context, id, hash);
        }
    }

    /**
     * Start view log activity
     */
    public void startViewLogHistory() {
        String strNoLogsAreFound=ResourcesHelper.getBaseContextString(m_context, R.string.no_logs_are_found);
        if (!isOnPause()) {
            int n_record=Application.logPacketList.size();
            if (n_record>0) {
                int id = Application.ACTIVITY_VIEWLOG;
                Application.startActivity(m_context, id);
            }
            else {
                Toast.makeText(m_context, strNoLogsAreFound, Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Variable to store callback of finish showing transaction receipt
     */
    public Callback<Object> m_cb_showTransactionReceipt=null;
    public void showTransactionReceipt(byte[] dataBuf, Callback<Object> cb) {
        List<Data_dbTransSummaryDetail> dbTransSummaryDetailListForTransReceipt=new ArrayList<Data_dbTransSummaryDetail>();
        Data_dbTransSummaryDetail dataDbTransSummaryDetail=new Data_dbTransSummaryDetail();
        dataDbTransSummaryDetail.m_id=0;
        dataDbTransSummaryDetail.m_importDate=0;
        dataDbTransSummaryDetail.m_dataBuf=dataBuf;
        dbTransSummaryDetailListForTransReceipt.add(dataDbTransSummaryDetail);
        Application.dbTransSummaryDetailListForTransReceipt=dbTransSummaryDetailListForTransReceipt;

        m_cb_showTransactionReceipt=cb;
        int id = Application.ACTIVITY_TRANSRECEIPT;
        HashMap<String, String> hash=new HashMap<String, String>();
        hash.put(TransReceiptActivity.KEY_SIGNATURE_VALIDATION, "true");
        hash.put(TransReceiptActivity.KEY_PRINTRECEIPT, "true");

        Application.startActivity(m_context, id, hash, ActivityRequestCodeEnum.RequestCode.REQUEST_SHOWTRANSACTION_RECEIPT.toInt());
    }
    public void showTransactionReceiptOnly(byte[] dataBuf, Callback<Object> cb) {
        if (!isOnPause()) {
            List<Data_dbTransSummaryDetail> dbTransSummaryDetailListForTransReceipt=new ArrayList<Data_dbTransSummaryDetail>();
            Data_dbTransSummaryDetail dataDbTransSummaryDetail=new Data_dbTransSummaryDetail();
            dataDbTransSummaryDetail.m_id=0;
            dataDbTransSummaryDetail.m_importDate=0;
            dataDbTransSummaryDetail.m_dataBuf=dataBuf;
            dbTransSummaryDetailListForTransReceipt.add(dataDbTransSummaryDetail);
            Application.dbTransSummaryDetailListForTransReceipt=dbTransSummaryDetailListForTransReceipt;

            m_cb_showTransactionReceipt=cb;
            int id = Application.ACTIVITY_TRANSRECEIPT;
            HashMap<String, String> hash=new HashMap<String, String>();
            hash.put(TransReceiptActivity.KEY_SIGNATURE_VALIDATION, "false");
            hash.put(TransReceiptActivity.KEY_PRINTRECEIPT, "false");

            Application.startActivity(m_context, id, hash, ActivityRequestCodeEnum.RequestCode.REQUEST_SHOWTRANSACTION_RECEIPT.toInt());
        }
    }


    /**
     * Get initial auth data in byte array format
     * @return initial auth data in byte array format
     */
    public static byte[] getInitAuthDataBytes() {
        byte KType=m_dataRunTimeS3Auth.KType;
        byte KIdx=m_dataRunTimeS3Auth.KIdx;
        byte[] dataBytes=m_dataRunTimeS3Auth.TRnd;
        byte[] buf=new byte [dataBytes.length+2];
        System.arraycopy(dataBytes, 0, buf, 2, dataBytes.length);
        buf[0]=KType;
        buf[1]=KIdx;
        return buf;
    }

    /**
     * Get mutu auth data in byte array format
     * @return mutu auth data in byte array format
     */
    public static byte[] getMutuAuthDataBytes() {
        byte KType=m_dataRunTimeS3Auth.KType;
        byte KIdx=m_dataRunTimeS3Auth.KIdx;

        // generate tdes please
        EncryptionHelper instEncryptionHelper=EncryptionHelper.getInstance();
        byte[] txMsg=m_dataRunTimeS3Auth.TRnd;
        byte[] rxMsg=Application.RxmsgForMutuAuth;
        byte[] SkeyForMutuAuth=Application.SkeyForMutuAuth;

        byte[] tDes=null;
        try {
            tDes=instEncryptionHelper.encrypt_tdes_16bytes_interpolate4bytes(SkeyForMutuAuth, txMsg, rxMsg);
        }
        catch (Exception ex) {
            Logger.i(m_className, "getMutuAuthDataBytes, exception ex1: "+ex.toString());
        }

        byte[] buf=new byte [tDes.length+3];
        System.arraycopy(tDes, 0, buf, 2, tDes.length);
        buf[0]=KType;
        buf[1]=KIdx;

        byte modeMutuAuth=m_dataRunTimeS3Auth.ModeMutuAuth;
        buf[buf.length-1]=modeMutuAuth;

        return buf;
    }

    public void ToastMessage(String msg) {
        if (m_toast!=null) {
            m_toast.cancel();
            m_toast=null;
        }
        try {
            m_toast = Toast.makeText(m_context, msg, Toast.LENGTH_SHORT);
            m_toast.show();
        }
        catch (Exception ex) {
        }
    }

    public void ToastMessageRunOnUiThread(final String msg) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ToastMessage(msg);
            }
        });
    }

    public void showMessageDialog(Context context, String msg) {
        if (context==null) {
            Toast.makeText(m_context, msg, Toast.LENGTH_SHORT).show();
            return;
        }
        String strMessage=ResourcesHelper.getBaseContextString(m_context, R.string.message);
        new AlertDialog.Builder(context)
                .setTitle(strMessage)
                .setMessage(msg)
                .setCancelable(false)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .show();
    }
}
