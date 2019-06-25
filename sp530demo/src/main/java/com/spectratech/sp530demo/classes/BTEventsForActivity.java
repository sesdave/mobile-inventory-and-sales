package com.spectratech.sp530demo.classes;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.SharedPreferences;
import com.spectratech.lib.BluetoothHelper;
import com.spectratech.lib.Callback;
import com.spectratech.lib.Logger;
import com.spectratech.lib.bluetooth.BluetoothSocketClass;
import com.spectratech.lib.bluetooth.BluetoothStatusClass;
import com.spectratech.lib.bluetooth.BluetoothUserActionClass;
import com.spectratech.sp530demo.constant.AppSectionsConstant;

/**
 * BTEventsForActivity - Bluetooth event for activity object
 */
public class BTEventsForActivity {

    private static final String m_className="BTEventsForActivity";

    private Context m_context;

    /**
     * FLag to use secure/insecure RfcommSocket
     */
    public boolean m_bUseSecureRfcommSocket;

    /**
     * Variable to store Bluetooth status
     */
    public BluetoothStatusClass m_btStatus;
    /**
     * Variable to store user action on Bluetooth connection
     */
    public BluetoothUserActionClass m_btUserAction;

    private BluetoothDevice m_activeBTDevice;
    private BluetoothSocketClass m_btSocketClassInst;

    BTReconnectThread m_reconnectThreadInst;

    private String m_keyDeviceAddressStoreInSP;

    /**
     * Constructor for BTEventsForActivity
     * @param context context of application
     */
    public BTEventsForActivity(Context context) {
        m_context=context;
        init();
    }

    private void init() {
        reset();
    }

    private void reset() {
        m_bUseSecureRfcommSocket=true;
        m_activeBTDevice=null;
        m_btSocketClassInst=null;
        m_btStatus=new BluetoothStatusClass();
        m_btUserAction=new BluetoothUserActionClass();
        m_keyDeviceAddressStoreInSP="";
    }

    /**
     * Function for managing connected socket
     * @param socket BluetoothSocket object
     */
    public void manageConnectedSocket(BluetoothSocket socket) {
        if (m_btSocketClassInst!=null) {
            m_btSocketClassInst.safeFreeBTSocket();
            m_btSocketClassInst=null;
        }
        m_btSocketClassInst=new BluetoothSocketClass(socket);
    }

    /**
     * Get Bluetooth socket class instance
     * @return BluetoothSocketClass object; null for no BluetoothSocketClass object
     */
    public BluetoothSocketClass getBluetoothSocketClassInstance() {
        return m_btSocketClassInst;
    }

    /**
     * Check for active Bluetooth device connected
     * @return true if connected; false otherwise
     */
    public boolean isActiveBluetoothDeviceConnected() {
        boolean flag=false;
        if (m_activeBTDevice!=null) {
            if (m_btStatus.m_stausDeviceConnection== BluetoothStatusClass.STATUS_DEVICE_CONNECTION.CONNECTED) {
                flag=true;
            }
        }
        return flag;
    }

    /**
     * Set key using Bluetooth device address for storing Bluetooth device information
     * @param key key for storing Bluetooth device address
     */
    public void setKeyForDeviceAddressStoreInSharedPreferences(String key) {
        m_keyDeviceAddressStoreInSP=key;
    }

    /**
     * Load saved active Bluetooth device from shared preferences
     * @param tag tag for logging
     */
    public void loadSavedActiveBluetoothDevice(String tag) {
        if ( (tag==null)||(tag.equals("")) ) {
            Logger.i(m_className, "loadSavedActiveBluetoothDevice called");
        }
        else {
            Logger.i(m_className, tag+", loadSavedActiveBluetoothDevice called");
        }
        String btAddress=getBTAddressFromSharedPreferences();
        if ( (btAddress==null)||(btAddress.equals("")) ) {
            if ( (tag==null)||(tag.equals("")) ) {
                Logger.i(m_className, "loadSavedActiveBluetoothDevice, btAddress is NULL");
            }
            else {
                Logger.i(m_className, tag+", loadSavedActiveBluetoothDevice, btAddress is NULL");

            }
            return;
        }
        BluetoothHelper instBlutetoothHelper=BluetoothHelper.getInstance();
        m_activeBTDevice=instBlutetoothHelper.getRemoteDevice(btAddress);
        if (m_activeBTDevice!=null) {
            int iBondState=m_activeBTDevice.getBondState();
            if (iBondState==BluetoothDevice.BOND_NONE) {
                Logger.i(m_className, "loadSavedActiveBluetoothDevice, bonding is NONE");
                setBTAddress2SharedPreferences("");
                m_btStatus.m_stausDeviceConnection = BluetoothStatusClass.STATUS_DEVICE_CONNECTION.DISCONNECTED;
                m_btUserAction.m_stausUserAction = BluetoothUserActionClass.STATUS_USERACTION.UNKNOWN;
                return;
            }

            m_btStatus.m_stausDeviceConnection = BluetoothStatusClass.STATUS_DEVICE_CONNECTION.DISCONNECTED;
            m_btUserAction.m_stausUserAction = BluetoothUserActionClass.STATUS_USERACTION.USERACTION_BT_CONNECT;
        }
    }

    private String getBTAddressFromSharedPreferences() {
        String btAddress="";
        String key=m_keyDeviceAddressStoreInSP;
        if ((key==null)||(key.equals(""))) {
            Logger.w(m_className, "getBTAddressFromSharedPreferences, btAddress is NULL");
            return btAddress;
        }
        SharedPreferences preference=m_context.getSharedPreferences(AppSectionsConstant.Storage.PREFERENCE_ACTIVEBLUETOOTHDEVICE_ADDRESS, Context.MODE_PRIVATE);
        btAddress=preference.getString(key, "");
        Logger.i(m_className, "getBTAddressFromSharedPreferences, btAddress: "+btAddress);
        return btAddress;
    }

    /**
     * Store Bluetooth address to share preferences
     * @param btAddress Bluetooth address in string format
     */
    public void setBTAddress2SharedPreferences(String btAddress) {
        String key=m_keyDeviceAddressStoreInSP;
        if ((key==null)||(key.equals(""))) {
            Logger.w(m_className, "setBTAddress2SharedPreferences, btAddress is NULL");
            return;
        }
        SharedPreferences preference=m_context.getSharedPreferences(AppSectionsConstant.Storage.PREFERENCE_ACTIVEBLUETOOTHDEVICE_ADDRESS, Context.MODE_PRIVATE);
        preference.edit().putString(key, btAddress).commit();
        Logger.i(m_className, "setBTAddress2SharedPreferences, btAddress: " + btAddress);
    }

    /**
     * Set active Bluetooth device
     * @param device BluetoothDevice object
     */
    public void setActiveBluetoothDevice(BluetoothDevice device) {
        m_activeBTDevice=device;
        if (m_activeBTDevice!=null) {
            String btAddress=m_activeBTDevice.getAddress();
            setBTAddress2SharedPreferences(btAddress);
        }
    }

    /**
     * Get active Bluetooth device
     * @return BluetoothDevice object; null for no BluetoothDevice object
     */
    public BluetoothDevice getActiveBluetoothDevice() {
        return m_activeBTDevice;
    }

    /**
     * Unset active Bluetooth device
     * @param device BluetoothDevice going to unset
     */
    public void unsetActiveBluetoothDevice(BluetoothDevice device) {
        if ( (m_activeBTDevice!=null)&&(device!=null) ) {
            String add1=m_activeBTDevice.getAddress();
            String add2=device.getAddress();
            if (add1.equals(add2)) {
                m_activeBTDevice=null;
                m_btStatus.m_stausDeviceConnection = BluetoothStatusClass.STATUS_DEVICE_CONNECTION.DISCONNECTED;
                setBTAddress2SharedPreferences("");
            }
        }
    }

    /**
     * Free resource of Bluetooth socket in BluetoothSocketClass
     */
    public synchronized void safeFreeBTSocket() {
        if (m_btSocketClassInst!=null) {
            Logger.i(m_className, "safeFreeBTSocket is Called");
            m_btSocketClassInst.safeFreeBTSocket();
            m_btSocketClassInst=null;
        }
    }

    /**
     * Validate Bluetooth connection
     * @param cb_connected callback for connected
     */
    public void validateBTConnection(Callback<Object> cb_connected) {
        if ( (m_btStatus.m_stausDeviceConnection==BluetoothStatusClass.STATUS_DEVICE_CONNECTION.DISCONNECTED) &&
                (m_btUserAction.m_stausUserAction==BluetoothUserActionClass.STATUS_USERACTION.USERACTION_BT_CONNECT))
        {
            // try to keep connect
            reloadReconnectThreadInstance(this, cb_connected);
        }
    }

    public void reloadReconnectThreadInstance(BTEventsForActivity BTEventsForActivity, Callback<Object> cb_connected) {
        Logger.i(m_className, "reloadReconnectThreadInstance start");
        if (m_reconnectThreadInst!=null) {
            if (!m_reconnectThreadInst.isFinished()) {
                Logger.i(m_className, "reloadReconnectThreadInstance, m_reconnectThreadInst, current procces is NOT FINISH");
                return;
            }
        }
        m_reconnectThreadInst=new BTReconnectThread(BTEventsForActivity, cb_connected);
        m_reconnectThreadInst.start();
    }


    public void onDestroy() {
        if (m_btSocketClassInst!=null) {
            m_btSocketClassInst.onDestroy();
            m_btSocketClassInst=null;
        }
        if (m_reconnectThreadInst!=null) {
            m_reconnectThreadInst.cancel();
            m_reconnectThreadInst=null;
        }
        reset();
    }
}
