package com.spectratech.sp530demo.classes;

import android.bluetooth.BluetoothDevice;
import com.spectratech.lib.BluetoothHelper;
import com.spectratech.lib.Callback;
import com.spectratech.lib.Logger;
import com.spectratech.lib.bluetooth.BluetoothConnectBaseThread;
import com.spectratech.lib.bluetooth.BluetoothConnectConstant;

/**
 * BTReconnectThread - Bluetooth auto reconnect thread
 */
public class BTReconnectThread extends BluetoothConnectBaseThread {
    private static final String m_className="BTReconnectThread";

    private BTEventsForActivity m_BTEventsForActivity;

    private String m_uuid_string;

    private Callback<Object> m_cb_connected;

    /**
     * Constructor for BTReconnectThread
     * @param BTEventsForActivity BTEventsForActivity object
     * @param uuid_string UUID string
     */
    public BTReconnectThread(BTEventsForActivity BTEventsForActivity, String uuid_string) {
        this(BTEventsForActivity, (Callback<Object>)null);
    }

    /**
     * Constructor for BTReconnectThread
     * @param BTEventsForActivity BTEventsForActivity object
     * @param uuid_string UUID string
     * @param cb_connected callback function for connected
     */
    public BTReconnectThread(BTEventsForActivity BTEventsForActivity, String uuid_string, Callback<Object> cb_connected) {
        this(BTEventsForActivity);
        m_uuid_string=uuid_string;
    }

    /**
     * Constructor for BTReconnectThread
     * @param BTEventsForActivity BTEventsForActivity object
     */
    public BTReconnectThread(BTEventsForActivity BTEventsForActivity) {
        this(BTEventsForActivity, (Callback<Object>)null);
    }

    /**
     * Constructor for BTReconnectThread
     * @param BTEventsForActivity BTEventsForActivity object
     * @param cb_connected callback function for connected
     */
    public BTReconnectThread(BTEventsForActivity BTEventsForActivity, Callback<Object> cb_connected) {
        super(BTEventsForActivity.getActiveBluetoothDevice());
        m_BTEventsForActivity=BTEventsForActivity;
        m_cb_connected=cb_connected;
        m_retyTimeInMS=(5*1000);
        m_uuid_string= BluetoothConnectConstant.DEFAULT_UUID_STRING;
    }

    @Override
    protected void initBluetoothSocket() {
        m_BTEventsForActivity.safeFreeBTSocket();
        if (m_btDevice_ConnectBaseThread==null) {
            Logger.w(m_className, "initBluetoothSocket, m_btDevice_ConnectBaseThread is NULL");
            return;
        }

        int iBondState=m_btDevice_ConnectBaseThread.getBondState();
        if (iBondState!= BluetoothDevice.BOND_BONDED) {
            Logger.i(m_className, "initBluetoothSocket, bond state is no BOND_BONDED(" + BluetoothDevice.BOND_BONDED + "), val: " + iBondState);
            return;
        }

        BluetoothHelper btHelperInst=BluetoothHelper.getInstance();
        mmSocket=btHelperInst.getBluetoothSocketFromBluetoothDevice(m_btDevice_ConnectBaseThread, m_uuid_string, m_BTEventsForActivity.m_bUseSecureRfcommSocket);
    }

    /**
     * Thread running function
     */
    public void run() {
        run_socketConnect();

        if (!m_bConnected) {
            Logger.i(m_className, "socket NOT connected and return");
            setFinishFlag();
            return;
        }

        Logger.i(m_className, "socket connected");

        m_BTEventsForActivity.manageConnectedSocket(mmSocket);

        if (m_cb_connected!=null) {
            try {
                m_cb_connected.setParameter(m_btDevice_ConnectBaseThread);
                m_cb_connected.call();
            } catch (Exception e) {
                Logger.w(m_className, "e: " + e.toString());
            }
        }

        Logger.i(m_className, "finish ReconnectThread run");

        setFinishFlag();
    }
}
