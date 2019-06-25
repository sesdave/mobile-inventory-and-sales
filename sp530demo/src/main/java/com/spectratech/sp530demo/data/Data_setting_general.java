package com.spectratech.sp530demo.data;

import android.content.Context;
import android.content.SharedPreferences;
import com.spectratech.sp530demo.constant.AppSectionsConstant;

/**
 * Data_setting_general - data class for setting general parameters
 */
public class Data_setting_general {
    private static final String m_className="Data_setting_general";

    public static final boolean DEFAULT_VAL_DEBUGMODE=false;
    public static final boolean DEFAULT_VAL_TRANSACTIONUISHOWMORE=false;
    public static final boolean DEFAULT_VAL_SENDECHOWHENCONNECTED=true;

    private Context m_context;

    public boolean m_bDebugMode;
    public boolean m_bTransactionUIShowMore;
    public boolean m_bSendEchoWhenConnected;

    private static final String KEY_SETTING_GENERAL_DEBUGMODE="settinggeneraldebugmode";
    private static final String KEY_TRANSACTIONUISHOWMORE="transactionUIShowMore";
    private static final String KEY_SENDECHOWHENCONNECTED="sendechowhenconnected";

    /**
     * Constructor for Data_setting_general
     * @param context context of applicaton
     */
    public Data_setting_general(Context context) {
        m_context=context;
        refresh();
    }

    /**
     * Refresh parameters
     */
    public void refresh() {
        SharedPreferences preference=m_context.getSharedPreferences(AppSectionsConstant.Storage.PREFERENCE_CONF_SETTING, Context.MODE_PRIVATE);
        m_bDebugMode=preference.getBoolean(KEY_SETTING_GENERAL_DEBUGMODE, DEFAULT_VAL_DEBUGMODE);
        m_bTransactionUIShowMore=preference.getBoolean(KEY_TRANSACTIONUISHOWMORE, DEFAULT_VAL_TRANSACTIONUISHOWMORE);
        m_bSendEchoWhenConnected=preference.getBoolean(KEY_SENDECHOWHENCONNECTED, DEFAULT_VAL_SENDECHOWHENCONNECTED);
    }


    /**
     * Check for in debug mode
     * @param context context of application
     * @return true if in debug mode; false otherwise
     */
    public static boolean isDebugMode(Context context) {
        SharedPreferences preference=context.getSharedPreferences(AppSectionsConstant.Storage.PREFERENCE_CONF_SETTING, Context.MODE_PRIVATE);
        boolean bDebugMode=preference.getBoolean(KEY_SETTING_GENERAL_DEBUGMODE, DEFAULT_VAL_DEBUGMODE);
        return bDebugMode;
    }

    /**
     * Set debug mode
     * @param flag true for debug mode; false for not in debug mode
     */
    public void setDebugMode(boolean flag) {
        SharedPreferences preference=m_context.getSharedPreferences(AppSectionsConstant.Storage.PREFERENCE_CONF_SETTING, Context.MODE_PRIVATE);
        preference.edit().putBoolean(KEY_SETTING_GENERAL_DEBUGMODE, flag).commit();
        refresh();
    }


    /**
     * Check for main view shows more input in UI
     * @param context context of application
     * @return true for show more input in UI; false for no show more input in UI
     */
    public static boolean isTransactionUIShowMore(Context context) {
        SharedPreferences preference=context.getSharedPreferences(AppSectionsConstant.Storage.PREFERENCE_CONF_SETTING, Context.MODE_PRIVATE);
        boolean bTransactionUIShowMore=preference.getBoolean(KEY_TRANSACTIONUISHOWMORE, DEFAULT_VAL_TRANSACTIONUISHOWMORE);
        return bTransactionUIShowMore;
    }

    /**
     * Set main view shows more input in UI
     * @param flag true for set; false for not set
     */
    public void setTransactionUIShowMore(boolean flag) {
        SharedPreferences preference=m_context.getSharedPreferences(AppSectionsConstant.Storage.PREFERENCE_CONF_SETTING, Context.MODE_PRIVATE);
        preference.edit().putBoolean(KEY_TRANSACTIONUISHOWMORE, flag).commit();
        refresh();
    }

    public void setSendEchoWhenConnected(boolean flag) {
        SharedPreferences preference=m_context.getSharedPreferences(AppSectionsConstant.Storage.PREFERENCE_CONF_SETTING, Context.MODE_PRIVATE);
        preference.edit().putBoolean(KEY_SENDECHOWHENCONNECTED, flag).commit();
        refresh();
    }
}
