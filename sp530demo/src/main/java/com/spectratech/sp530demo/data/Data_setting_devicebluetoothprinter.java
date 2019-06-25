package com.spectratech.sp530demo.data;

import android.content.Context;
import android.content.SharedPreferences;
import com.spectratech.lib.constant.BluetoothTransmissionEnum.PACKET_ENCAPSULATE_LEVEL;
import com.spectratech.sp530demo.conf.ConfigDevice;
import com.spectratech.sp530demo.constant.AppSectionsConstant;

/**
 * Data_setting_devicebluetoothprinter - data class for string Bluetooth printer parameters
 */
public class Data_setting_devicebluetoothprinter {
    private static final String m_className="Data_setting_devicebluetoothprinter";

    public static final boolean DEFAULT_USE_SECURE_RFCOMMSOCKET=false;

    public static final PACKET_ENCAPSULATE_LEVEL DEFAULT_PACKET_ENCAPSULATE_LEVEL=PACKET_ENCAPSULATE_LEVEL.SOH;

    private Context m_context;

    public boolean m_bUseSecureRfcommSocket;

    public PACKET_ENCAPSULATE_LEVEL m_packetEncapsulateLevel;

    public boolean m_bEnableBTPrinter;

    private final String KEY_USE_SECURE_RFCOMMSOCKET="USE_SECURE_RFCOMMSOCKET";
    private final String KEY_PACKET_ENCAPSULATE_LEVEL="PACKET_ENCAPSULATE_LEVEL";
    private static final String KEY_ENABLE_BTPRINTER="ENABLE_BTPRINTER";

    /**
     * Constructor for Data_setting_devicebluetoothprinter
     * @param context context of application
     */
    public Data_setting_devicebluetoothprinter(Context context) {
        m_context=context;
        refresh();
    }

    /**
     * Refresh parameters
     */
    public void refresh() {
        SharedPreferences preference=m_context.getSharedPreferences(AppSectionsConstant.Storage.PREFERENCE_BLUETOOTHPRINTER_CONF_SETTING, Context.MODE_PRIVATE);

        m_bUseSecureRfcommSocket=preference.getBoolean(KEY_USE_SECURE_RFCOMMSOCKET, DEFAULT_USE_SECURE_RFCOMMSOCKET);

        int idx=preference.getInt(KEY_PACKET_ENCAPSULATE_LEVEL, DEFAULT_PACKET_ENCAPSULATE_LEVEL.toInt());
        if (idx==PACKET_ENCAPSULATE_LEVEL.RAW.toInt()) {
            m_packetEncapsulateLevel=PACKET_ENCAPSULATE_LEVEL.RAW;
        }
        else if (idx==PACKET_ENCAPSULATE_LEVEL.SOH.toInt()) {
            m_packetEncapsulateLevel=PACKET_ENCAPSULATE_LEVEL.SOH;
        }
        else if (idx==PACKET_ENCAPSULATE_LEVEL.MCP.toInt()) {
            m_packetEncapsulateLevel=PACKET_ENCAPSULATE_LEVEL.MCP;
        }
        else {
            m_packetEncapsulateLevel=PACKET_ENCAPSULATE_LEVEL.MCP;
            setPacketEncapsulateLevel(m_packetEncapsulateLevel.toInt());
        }

        m_bEnableBTPrinter=preference.getBoolean(KEY_ENABLE_BTPRINTER, ConfigDevice.ENABLE_BTPRINTER_FUNCTIONS);
    }

    public void setUseSecureRfcommSocket(boolean flag) {
        SharedPreferences preference=m_context.getSharedPreferences(AppSectionsConstant.Storage.PREFERENCE_BLUETOOTHPRINTER_CONF_SETTING, Context.MODE_PRIVATE);
        preference.edit().putBoolean(KEY_USE_SECURE_RFCOMMSOCKET, flag).commit();
        refresh();
    }

    /**
     * Set packet encapsulate level of Bluetooth printer
     * @param val encapsulate level of Bluetooth printer
     */
    public void setPacketEncapsulateLevel(int val) {
        SharedPreferences preference=m_context.getSharedPreferences(AppSectionsConstant.Storage.PREFERENCE_BLUETOOTHPRINTER_CONF_SETTING, Context.MODE_PRIVATE);
        preference.edit().putInt(KEY_PACKET_ENCAPSULATE_LEVEL, val).commit();
        refresh();
    }


    /**
     * Check for enable of Bluetooth printer
     * @param context context of application
     * @return true if enable; false otherwise
     */
    public static boolean isEnableBTPrinter(Context context) {
        SharedPreferences preference=context.getSharedPreferences(AppSectionsConstant.Storage.PREFERENCE_BLUETOOTHPRINTER_CONF_SETTING, Context.MODE_PRIVATE);
        boolean bDebugMode=preference.getBoolean(KEY_ENABLE_BTPRINTER, ConfigDevice.ENABLE_BTPRINTER_FUNCTIONS);
        return bDebugMode;
    }

    /**
     * Set enable Bluetooth printer
     * @param flag true for enable; false otherwise
     */
    public void setEnableBTPrinter(boolean flag) {
        SharedPreferences preference=m_context.getSharedPreferences(AppSectionsConstant.Storage.PREFERENCE_BLUETOOTHPRINTER_CONF_SETTING, Context.MODE_PRIVATE);
        preference.edit().putBoolean(KEY_ENABLE_BTPRINTER, flag).commit();
        refresh();
    }

}
