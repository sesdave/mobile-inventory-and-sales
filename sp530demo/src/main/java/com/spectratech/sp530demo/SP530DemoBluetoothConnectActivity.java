package com.spectratech.sp530demo;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.WindowManager;

import com.spectratech.lib.BluetoothHelper;
import com.spectratech.lib.Logger;
import com.spectratech.lib.bluetooth.BluetoothConnectActivity;
import com.spectratech.lib.bluetooth.BluetoothManagementInterface;
import com.spectratech.lib.bluetooth.BluetoothUserActionClass;
import com.spectratech.lib.sp530.comm_protocol_c.SP530_AppMcpCHelper;
import com.spectratech.sp530demo.controller.ActivityHelper;

import java.io.IOException;

/**
 * SP530DemoBluetoothConnectActivity - SP530 BLuetooth connection activity
 */
public class SP530DemoBluetoothConnectActivity extends BluetoothConnectActivity implements BluetoothManagementInterface {

    private final String m_className="SP530DemoBluetoothConnectActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        m_activitycommmon=new SP530DemoBaseActivityCommonClass(m_context);
        m_activitycommmon.onCreate(savedInstanceState, this);

        setTitle("SP530");

        boolean bLandscape=getResources().getBoolean(R.bool.is_landscape);
        if (bLandscape) {
            onOrientationLandscape();
        }
        else {
            onOrientationPortrait();
        }

        m_bUseSecureRfcommSocket=true;
        ActivityHelper instActivityHelper=ActivityHelper.getInstance();
        DemoMainActivity act=instActivityHelper.getDemoMainActivity();
        if (act==null) {
            Logger.e(m_className, "onCreate, DemoMainActivity is NULL");
        }
        else {
            m_bUseSecureRfcommSocket=act.m_BTEventsForActivity.m_bUseSecureRfcommSocket;
        }
    }

    @Override
    public void userActionBTConnectTrigger() {
        ActivityHelper instActivityHelper=ActivityHelper.getInstance();
        DemoMainActivity act=instActivityHelper.getDemoMainActivity();
        if (act==null) {
            Logger.e(m_className, "reloadConnectThreadInstance, DemoMainActivity is NULL");
        }
        else {
            if (act.m_BTEventsForActivity.m_btUserAction.m_stausUserAction!=BluetoothUserActionClass.STATUS_USERACTION.USERACTION_BT_CONNECT) {
                act.m_BTEventsForActivity.m_btUserAction.m_stausUserAction=BluetoothUserActionClass.STATUS_USERACTION.USERACTION_BT_CONNECT;
            }
        }
    }
    @Override
    public void userActionBTDisconnectTrigger() {
        SP530_AppMcpCHelper.freeInstance();

        ActivityHelper instActivityHelper=ActivityHelper.getInstance();
        DemoMainActivity act=instActivityHelper.getDemoMainActivity();
        if (act==null) {
            Logger.e(m_className, "userActionBTDisconnectTrigger, DemoMainActivity is NULL");
        }
        else {
            if (act.m_BTEventsForActivity.m_btUserAction.m_stausUserAction!=BluetoothUserActionClass.STATUS_USERACTION.USERACTION_BT_DISCONNECT) {
                act.m_BTEventsForActivity.m_btUserAction.m_stausUserAction=BluetoothUserActionClass.STATUS_USERACTION.USERACTION_BT_DISCONNECT;
                act.m_BTEventsForActivity.setBTAddress2SharedPreferences("");
            }
        }
    }


    @Override
    public boolean isActiveBluetoothDeviceConnected() {
        boolean flag=false;
        ActivityHelper instActivityHelper=ActivityHelper.getInstance();
        DemoMainActivity act=instActivityHelper.getDemoMainActivity();
        if (act!=null) {
            flag=act.isActiveBluetoothDeviceConnected();
        }
        return flag;
    }

    @Override
    public void setActiveBluetoothDevice(BluetoothDevice device) {
        DemoMainActivity.setActiveBluetoothDevice(device);
    }

    @Override
    public BluetoothDevice getActiveBluetoothDevice() {
        BluetoothDevice btDevice=DemoMainActivity.getActiveBluetoothDevice();
        return btDevice;
    }

    @Override
    public void unsetActiveBluetoothDevice(BluetoothDevice device) {
        DemoMainActivity.unsetActiveBluetoothDevice(device);
    }

    @Override
    public void manageConnectedSocket(BluetoothSocket socket) {
        ActivityHelper instActivityHelper=ActivityHelper.getInstance();
        DemoMainActivity act=instActivityHelper.getDemoMainActivity();
        if (act==null) {
            Logger.e(m_className, "manageConnectedSocket, DemoMainActivity is NULL");
            try {
                socket.close();
                BluetoothHelper btHelperInst=BluetoothHelper.getInstance();
                //btHelperInst.socketClose_threadSleep();
            } catch (IOException e) {
            }
            return;
        }
        act.manageConnectedSocket(socket);
    }

    @Override
    public void safeFreeBTSocket() {
        ActivityHelper instActivityHelper=ActivityHelper.getInstance();
        DemoMainActivity act=instActivityHelper.getDemoMainActivity();
        if (act==null) {
            Logger.e(m_className, "manageConnectedSocket, DemoMainActivity is NULL");
            return;
        }
        act.safeFreeBTSocket();
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
