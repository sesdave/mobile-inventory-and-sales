package com.spectratech.sp530demo.data;

import android.content.Context;
import android.content.SharedPreferences;
import com.spectratech.lib.constant.BluetoothTransmissionEnum.PACKET_ENCAPSULATE_LEVEL;
import com.spectratech.sp530demo.constant.AppSectionsConstant;

/**
 * Data_setting_devicesp530 - data class for storing SP530 parameters
 */
public class Data_setting_devicesp530 {
    private static final String m_className="Data_setting_devicesp530";

    public static final PACKET_ENCAPSULATE_LEVEL DEFAULT_PACKET_ENCAPSULATE_LEVEL=PACKET_ENCAPSULATE_LEVEL.MCP;
    public static final boolean DEFAULT_ALWAYS_USE_CRC_CHECKSUM_VALUE=false;
    public static final boolean DEFAULT_SSL_CHANNEL_ONE_VALUE=true;
//    public static final boolean DEFAULT_SSL_CHANNEL_TWO_VALUE=true;

    private Context m_context;

    public PACKET_ENCAPSULATE_LEVEL m_packetEncapsulateLevel;

    public boolean m_bAlwaysUseCRC;

    public boolean m_bSSLChOne;
//    public boolean m_bSSLChTwo;

    private final String KEY_PACKET_ENCAPSULATE_LEVEL="PACKET_ENCAPSULATE_LEVEL";
    private static final String KEY_ALWAYS_USE_CRC="ALWAYS_USE_CRC";
    private static final String KEY_SSL_CHANNEL_ONE="SSL_CHANNEL_ONE";
//    private static final String KEY_SSL_CHANNEL_TWO="SSL_CHANNEL_TWO";

    /**
     * Constructor for Data_setting_devicesp530
     * @param context context of application
     */
    public Data_setting_devicesp530(Context context) {
        m_context=context;
        refresh();
    }

    /**
     * Refresh parameters
     */
    public void refresh() {
        SharedPreferences preference=m_context.getSharedPreferences(AppSectionsConstant.Storage.PREFERENCE_DEVICE_CONF_SETTING, Context.MODE_PRIVATE);
        int idx=preference.getInt(KEY_PACKET_ENCAPSULATE_LEVEL, DEFAULT_PACKET_ENCAPSULATE_LEVEL.toInt());
        if (idx==PACKET_ENCAPSULATE_LEVEL.RAW.toInt()) {
            m_packetEncapsulateLevel=PACKET_ENCAPSULATE_LEVEL.RAW;
        }
//        else if (idx==PACKET_ENCAPSULATE_LEVEL.SOH.toInt()) {
//            m_packetEncapsulateLevel=PACKET_ENCAPSULATE_LEVEL.SOH;
//        }
        else if (idx==PACKET_ENCAPSULATE_LEVEL.MCP.toInt()) {
            m_packetEncapsulateLevel=PACKET_ENCAPSULATE_LEVEL.MCP;
        }
        else {
            m_packetEncapsulateLevel=PACKET_ENCAPSULATE_LEVEL.MCP;
            setPacketEncapsulateLevel(m_packetEncapsulateLevel.toInt());
        }

        m_bAlwaysUseCRC=preference.getBoolean(KEY_ALWAYS_USE_CRC, DEFAULT_ALWAYS_USE_CRC_CHECKSUM_VALUE);

        m_bSSLChOne=preference.getBoolean(KEY_SSL_CHANNEL_ONE, DEFAULT_SSL_CHANNEL_ONE_VALUE);
//        m_bSSLChTwo=preference.getBoolean(KEY_SSL_CHANNEL_TWO, DEFAULT_SSL_CHANNEL_TWO_VALUE);
    }

    /**
     * Set packet encapsulate level of SP530
     * @param val packet encapsulate level of SP530
     */
    public void setPacketEncapsulateLevel(int val) {
        SharedPreferences preference=m_context.getSharedPreferences(AppSectionsConstant.Storage.PREFERENCE_DEVICE_CONF_SETTING, Context.MODE_PRIVATE);
        preference.edit().putInt(KEY_PACKET_ENCAPSULATE_LEVEL, val).commit();
        refresh();
    }

    /**
     * Check for always use CRC as checksum method
     * @param context context of application
     * @return true if always use crc checksum; false otherwise
     */
    public static boolean isAlwaysUseCRC(Context context) {
        SharedPreferences preference=context.getSharedPreferences(AppSectionsConstant.Storage.PREFERENCE_DEVICE_CONF_SETTING, Context.MODE_PRIVATE);
        boolean bAlwaysUseCRC=preference.getBoolean(KEY_ALWAYS_USE_CRC, DEFAULT_ALWAYS_USE_CRC_CHECKSUM_VALUE);
        return bAlwaysUseCRC;
    }

    /**
     * Set for always use CRC as checksum method
     * @param val true for always use crc checsum; false otherwise
     */
    public void setAlwaysUseCRC(boolean val) {
        SharedPreferences preference=m_context.getSharedPreferences(AppSectionsConstant.Storage.PREFERENCE_DEVICE_CONF_SETTING, Context.MODE_PRIVATE);
        preference.edit().putBoolean(KEY_ALWAYS_USE_CRC, val).commit();
        refresh();
    }

    /**
     * Check for the use of SSL for logical channel one
     * @param context context of application
     * @return true if use of SSL for logical channel one; false otherwise
     */
    public static boolean isSSLChannelOne(Context context) {
        SharedPreferences preference=context.getSharedPreferences(AppSectionsConstant.Storage.PREFERENCE_DEVICE_CONF_SETTING, Context.MODE_PRIVATE);
        boolean bSSLChOne=preference.getBoolean(KEY_SSL_CHANNEL_ONE, DEFAULT_SSL_CHANNEL_ONE_VALUE);
        return bSSLChOne;
    }

    /**
     * Set the use of SSL for logical channel one
     * @param val true for enable; false for disable
     */
    public void setSSLChannelOne(boolean val) {
        SharedPreferences preference=m_context.getSharedPreferences(AppSectionsConstant.Storage.PREFERENCE_DEVICE_CONF_SETTING, Context.MODE_PRIVATE);
        preference.edit().putBoolean(KEY_SSL_CHANNEL_ONE, val).commit();
        refresh();
    }


//    public static boolean isSSLChannelTwo(Context context) {
//        SharedPreferences preference=context.getSharedPreferences(AppSectionsConstant.Storage.PREFERENCE_DEVICE_CONF_SETTING, Context.MODE_PRIVATE);
//        boolean bSSLChTwo=preference.getBoolean(KEY_SSL_CHANNEL_TWO, DEFAULT_SSL_CHANNEL_TWO_VALUE);
//        return bSSLChTwo;
//    }
//    public void setSSLChannelTwo(boolean val) {
//        SharedPreferences preference=m_context.getSharedPreferences(AppSectionsConstant.Storage.PREFERENCE_DEVICE_CONF_SETTING, Context.MODE_PRIVATE);
//        preference.edit().putBoolean(KEY_SSL_CHANNEL_TWO, val).commit();
//        refresh();
//    }
}
