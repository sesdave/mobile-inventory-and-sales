package com.spectratech.sp530demo.data;

import android.content.Context;
import android.content.SharedPreferences;
import com.spectratech.lib.Logger;
import com.spectratech.lib.tcpip.data.Data_tcpip_v4;

/**
 * Data_setting_devicesp1000 - data class for storing t1000 parameters
 */
public class Data_setting_devicet1000 {
    private static final String m_className="Data_setting_devicet1000";

    private static String PREFERENCE_DEVICET1000_CONF_SETTING="DEVICET1000_CONF_SETTING";

    public static final int[] DEFAULT_IP=new int[] { 192, 168, 2, 200 };
    public static final int DEFAULT_PORT=50005;
    public static final int DEFAULT_MIN_CONNECTION_TIMEOUT_INS=30;

    private Context m_context;

    private final String KEY_IPARRAY_PREFIX="KEY_IPARRAY_PREFIX";
    private final String KEY_PORT="KEY_PORT";
    private final String KEY_CONNECTINOTIMEOUT="KEY_CONNECTINOTIMEOUT";

    public int[] m_ipArray;
    public int m_port;
    public int m_connectionTimeoutInS;

    /**
     * Constructor for Data_setting_devicesp1000
     * @param context context of application
     */
    public Data_setting_devicet1000(Context context) {
        m_context=context;
        m_ipArray=null;
        m_port=-1;
        m_connectionTimeoutInS=-1;
        refresh();
    }

    /**
     * Refresh parameters
     */
    public void refresh() {
        SharedPreferences preference=m_context.getSharedPreferences(PREFERENCE_DEVICET1000_CONF_SETTING, Context.MODE_PRIVATE);
        loadIPArray();
        loadPort();
        loadConnectionTimeout();
    }

    /**
     * Set connection timeout in second
     * @param val connection timeout in second
     */
    public void setConnectionTimeout(int val) {
        SharedPreferences preference=m_context.getSharedPreferences(PREFERENCE_DEVICET1000_CONF_SETTING, Context.MODE_PRIVATE);
        preference.edit().putInt(KEY_CONNECTINOTIMEOUT, val).commit();
        refresh();
    }

    public void loadConnectionTimeout() {
        SharedPreferences preference = m_context.getSharedPreferences(PREFERENCE_DEVICET1000_CONF_SETTING, Context.MODE_PRIVATE);
        m_connectionTimeoutInS = preference.getInt(KEY_CONNECTINOTIMEOUT, DEFAULT_MIN_CONNECTION_TIMEOUT_INS);
    }


    /**
     * Set IP of T1000
     * @param valArray IP address array of T1000
     */
    public void setIPArray(int[] valArray) {
        if ( (valArray==null)&&(valArray.length!=4) ) {
            if (valArray==null) {
                Logger.w(m_className, "setIPArray, valArray is NULL");
            }
            else {
                Logger.w(m_className, "setIPArray, valArray length is not equl to 4");
            }
            return;
        }
        SharedPreferences preference=m_context.getSharedPreferences(PREFERENCE_DEVICET1000_CONF_SETTING, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor=preference.edit();
        for (int i=0; i<4; i++) {
            String key=KEY_IPARRAY_PREFIX+i;
            int val=valArray[i];
            editor.putInt(key, val).commit();
        }
        refresh();
    }

    public void loadIPArray() {
        m_ipArray = new int[4];
        SharedPreferences preference = m_context.getSharedPreferences(PREFERENCE_DEVICET1000_CONF_SETTING, Context.MODE_PRIVATE);
        boolean bValid = true;
        for (int i=0; i<4; i++) {
            String key=KEY_IPARRAY_PREFIX+i;
            int val=preference.getInt(key, -1);
            if (val<0) {
                bValid=false;
                break;
            }
            else {
                m_ipArray[i]=val;
            }
        }

        if (!bValid) {
            for (int i=0; i<4; i++) {
                m_ipArray[i]=DEFAULT_IP[i];
            }
        }
    }

    public void setPort(int val) {
        SharedPreferences preference=m_context.getSharedPreferences(PREFERENCE_DEVICET1000_CONF_SETTING, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor=preference.edit();
        editor.putInt(KEY_PORT, val).commit();
        refresh();
    }

    public void loadPort() {
        SharedPreferences preference = m_context.getSharedPreferences(PREFERENCE_DEVICET1000_CONF_SETTING, Context.MODE_PRIVATE);
        m_port = preference.getInt(KEY_PORT, DEFAULT_PORT);
    }

    public Data_tcpip_v4 convert2DataTcpIpV4() {
        Data_tcpip_v4 data=new Data_tcpip_v4();
        for (int i=0; i<4; i++) {
            data.m_ipByteList[i] = (byte) (m_ipArray[i] & 0xFF);
        }
        data.m_port=m_port;
        data.m_connectTimeoutInS=m_connectionTimeoutInS;
        return data;
    }

    public String getName() {
        String ipString=getIPString();
        String result=ipString+":"+m_port;
        return result;
    }

    public String getIPString() {
        String strResult="";
        for (int i=0; i<4; i++) {
            if (i>0) {
                strResult+=".";
            }
            strResult+=m_ipArray[i];
        }
        return strResult;
    }

}
