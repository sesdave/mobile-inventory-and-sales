package com.spectratech.sp530demo;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.view.WindowManager;

import com.spectratech.lib.Logger;
import com.spectratech.lib.ResourcesHelper;
import com.spectratech.lib.bluetooth.BluetoothConnectActivity;
import com.spectratech.lib.bluetooth.BluetoothConnectConstant;
import com.spectratech.lib.bluetooth.BluetoothManagementInterface;
import com.spectratech.lib.bluetooth.BluetoothSocketClass;
import com.spectratech.lib.bluetooth.BluetoothStatusClass;
import com.spectratech.lib.bluetooth.BluetoothUserActionClass;
import com.spectratech.lib.printer.bp80.Lpt_funcCHelper;
import com.spectratech.sp530demo.classes.BTEventsForActivity;
import com.spectratech.sp530demo.controller.ActivityHelper;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * BTPrinterBluetoothConnectActivity - activity to connect Bluetooth printer
 */
public class BTPrinterBluetoothConnectActivity extends BluetoothConnectActivity implements BluetoothManagementInterface {

    private final String m_className="BTPrinterBluetoothConnectActivity";

    /**
     * Variable to store Bluetooth printer BTEventsForActivity
     */
    public static BTEventsForActivity m_BTEventsForActivity;

    private BroadcastReceiver m_broadcastReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        initBTEventsForActivityInstance(this);
        super.onCreate(savedInstanceState);
        m_activitycommmon=new SP530DemoBaseActivityCommonClass(m_context);
        m_activitycommmon.onCreate(savedInstanceState, this);

        String strPrinter=ResourcesHelper.getBaseContextString(m_context, R.string.printer);
        setTitle(strPrinter);

        registerBroadcastReceiver();

        m_uuid_string= BluetoothConnectConstant.DEFAULT_UUID_STRING;

        boolean bLandscape=getResources().getBoolean(R.bool.is_landscape);
        if (bLandscape) {
            onOrientationLandscape();
        }
        else {
            onOrientationPortrait();
        }

        // use InsecureRfcommSocketToServiceRecord
        m_bUseSecureRfcommSocket=m_BTEventsForActivity.m_bUseSecureRfcommSocket;
    }

    public static void setSecureRfcommSocket(boolean bUseSecureRfcommSocket) {
        if (m_BTEventsForActivity!=null) {
            m_BTEventsForActivity.m_bUseSecureRfcommSocket=bUseSecureRfcommSocket;
        }
    }

    /**
     * Initial m_BTEventsForActivity function
     * @param context context of application
     */
    public static void initBTEventsForActivityInstance(Context context) {
        boolean bForceNew=false;
        initBTEventsForActivityInstance(context, bForceNew);
    }
    /**
     * Initial m_BTEventsForActivity function
     * @param context context of application
     * @param bForceNew flag to force new instance
     */
    public static void initBTEventsForActivityInstance(Context context, boolean bForceNew) {
        boolean bUseSecureRfcommSocket=false;
        initBTEventsForActivityInstance(context, bUseSecureRfcommSocket, bForceNew);
    }
    public static void initBTEventsForActivityInstance(Context context, boolean bUseSecureRfcommSocket, boolean bForceNew) {
        if ( (m_BTEventsForActivity==null)||(bForceNew) ) {
            m_BTEventsForActivity = new BTEventsForActivity(context);
            m_BTEventsForActivity.m_bUseSecureRfcommSocket=bUseSecureRfcommSocket;
            m_BTEventsForActivity.setKeyForDeviceAddressStoreInSharedPreferences("BP80");
        }
    }

    @Override
    protected void onDestroy() {
        unregisterBroadcastReceiver();
        super.onDestroy();
    }

    @Override
    public void userActionBTConnectTrigger() {
        if (m_BTEventsForActivity!=null) {
            if (m_BTEventsForActivity.m_btUserAction.m_stausUserAction != BluetoothUserActionClass.STATUS_USERACTION.USERACTION_BT_CONNECT) {
                m_BTEventsForActivity.m_btUserAction.m_stausUserAction = BluetoothUserActionClass.STATUS_USERACTION.USERACTION_BT_CONNECT;
            }
        }
    }

    @Override
    public void userActionBTDisconnectTrigger() {
        if (m_BTEventsForActivity!=null) {
            if (m_BTEventsForActivity.m_btUserAction.m_stausUserAction != BluetoothUserActionClass.STATUS_USERACTION.USERACTION_BT_DISCONNECT) {
                m_BTEventsForActivity.m_btUserAction.m_stausUserAction = BluetoothUserActionClass.STATUS_USERACTION.USERACTION_BT_DISCONNECT;
            }
        }
    }


    @Override
    public boolean isActiveBluetoothDeviceConnected() {
        boolean flag=false;
        if (m_BTEventsForActivity!=null) {
           flag= m_BTEventsForActivity.isActiveBluetoothDeviceConnected();
        }
        return flag;
    }

    @Override
    public void setActiveBluetoothDevice(BluetoothDevice device) {
        if (m_BTEventsForActivity!=null) {
            m_BTEventsForActivity.setActiveBluetoothDevice(device);
        }
    }

    @Override
    public BluetoothDevice getActiveBluetoothDevice() {
        BluetoothDevice btDevice=null;
        if (m_BTEventsForActivity!=null) {
            btDevice = m_BTEventsForActivity.getActiveBluetoothDevice();
        }
        return btDevice;
    }

    @Override
    public void unsetActiveBluetoothDevice(BluetoothDevice device) {
        if (m_BTEventsForActivity!=null) {
            m_BTEventsForActivity.unsetActiveBluetoothDevice(device);
        }
    }

    @Override
    public void manageConnectedSocket(BluetoothSocket socket) {
        if (m_BTEventsForActivity!=null) {
            m_BTEventsForActivity.manageConnectedSocket(socket);
            m_BTEventsForActivity.m_btStatus.m_stausDeviceConnection = BluetoothStatusClass.STATUS_DEVICE_CONNECTION.CONNECTED;

            // set fetch soh rx seq
            Lpt_funcCHelper instLpt_funcCHelper=Lpt_funcCHelper.getInstance();
            instLpt_funcCHelper.setSOH_fetchRxSeq();
        }
    }

    @Override
    public void safeFreeBTSocket() {
        if (m_BTEventsForActivity!=null) {
            // fix Android OS 4.4 crashes on bt disconnect
            Lpt_funcCHelper instLpt_funcCHelper=Lpt_funcCHelper.getInstance();
            instLpt_funcCHelper.setIO(null, null);

            m_BTEventsForActivity.safeFreeBTSocket();
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
                        String strTmp = m_className;
                        int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
                        switch (state) {
                            case BluetoothAdapter.STATE_OFF:
                                break;
                            case BluetoothAdapter.STATE_TURNING_OFF:
                                break;
                            case BluetoothAdapter.STATE_ON:
                                break;
                            case BluetoothAdapter.STATE_TURNING_ON:
                                break;
                            default:
                                break;
                        }
                    }
                    else if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                    }
                    else if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)) {
                        BTEventsForActivity btEventsForActivity=m_BTEventsForActivity;
                        if (btEventsForActivity!=null) {
                            BluetoothDevice activeDevice = btEventsForActivity.getActiveBluetoothDevice();
                            if ((activeDevice != null) && (activeDevice.equals(device))) {
                                btEventsForActivity.m_btStatus.m_stausDeviceConnection = BluetoothStatusClass.STATUS_DEVICE_CONNECTION.CONNECTED;
                                Logger.i(m_className, "ACTION_ACL_CONNECTED, device connected: " + activeDevice.getAddress());
                                ActivityHelper instActivityHelper = ActivityHelper.getInstance();
                                DemoMainActivity act = instActivityHelper.getDemoMainActivity();
                                if (act!=null) {
                                    act.refreshBluetoothPrinterTextRunOnUiThread();
                                }
                                else {
                                    Logger.w(m_className, "registerBroadcastReceiver, DemoMainActivity is NULL");
                                }
                            }
                        }
                    }
                    else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                    }
                    else if (BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED.equals(action)) {
                    }
                    else if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)) {
                        BTEventsForActivity btEventsForActivity=m_BTEventsForActivity;
                        if (btEventsForActivity!=null) {
                            BluetoothDevice activeDevice = btEventsForActivity.getActiveBluetoothDevice();
                            if ((activeDevice != null) && (activeDevice.equals(device))) {
                                btEventsForActivity.m_btStatus.m_stausDeviceConnection = BluetoothStatusClass.STATUS_DEVICE_CONNECTION.DISCONNECTED;
                                Logger.i(m_className, "ACTION_ACL_DISCONNECTED, device disconnected: " + activeDevice.getAddress());
                                ActivityHelper instActivityHelper = ActivityHelper.getInstance();
                                DemoMainActivity act = instActivityHelper.getDemoMainActivity();
                                if (act!=null) {
                                    act.refreshBluetoothPrinterTextRunOnUiThread();
                                }
                                else {
                                    Logger.w(m_className, "registerBroadcastReceiver, DemoMainActivity is NULL");
                                }
                            }
                        }
                    }
                }
            };

            HandlerThread handlerThread = new HandlerThread("btprinterReceiverHandler");
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
     * Check for printer ready
     * @return true if ready; false otherwise
     */
    public static boolean isReadyPrinter() {
        String className="BTPrinterBluetoothConnectActivity";
        boolean flag = false;
        if (BTPrinterBluetoothConnectActivity.m_BTEventsForActivity==null) {
            Logger.w(className, "printImage, BTPrinterBluetoothConnectActivity.m_BTEventsForActivity is NULL");
            return flag;
        }
        BluetoothSocketClass btSocketClass=BTPrinterBluetoothConnectActivity.m_BTEventsForActivity.getBluetoothSocketClassInstance();
        if (btSocketClass==null) {
            Logger.w(className, "printImage, btSocketClass is NULL");
            return flag;
        }
        InputStream is=null;
        OutputStream os=null;
        is=btSocketClass.getInputStream();
        os=btSocketClass.getOutputStream();
        if (is==null) {
            Logger.w(className, "printImage, is is NULL");
            return flag;
        }
        if (os==null) {
            Logger.w(className, "printImage, os is NULL");
            return flag;
        }
        flag=true;
        return flag;
    }

    /**
     * Start Bluetooth printer connection activity with no animation
     * @param context context of application
     */
    public static void startBTPrinterBluetoothConnectActivityWithNoAnimation(Context context) {
        Application.startActivity(context, Application.ACTIVITY_BTPRINTER_BLUETOOTH_CONNECT);
        ((Activity) context).overridePendingTransition(0, 0);
    }

    /**
     * Start Bluetooth printer connection activity with no animation
     * @param context context of application
     * @param data data send to activity
     * @param reqCode request code send to activity
     */
    public static void startBTPrinterBluetoothConnectActivityWithNoAnimation(Context context, Object data, Integer reqCode) {
        Application.startActivity(context, Application.ACTIVITY_BTPRINTER_BLUETOOTH_CONNECT, data, reqCode);
        ((Activity) context).overridePendingTransition(0, 0);
    }
}
