package com.spectratech.sp530demo.data;

import android.content.Context;
import android.content.SharedPreferences;
import com.spectratech.lib.sp530.constant.SP530Constant;
import com.spectratech.sp530demo.constant.AppSectionsConstant;
import com.spectratech.sp530demo.constant.ExtraTlvConstant;

/**
 * Data_setting_s3trans - data class for storing setting transaction parameters
 */
public class Data_setting_s3trans {
    private static final String m_className="Data_setting_s3trans";

    public static final int DEFAULT_IDXTRANSRESULT=0;
    public static final int DEFAULT_MAX_BATCH=10;
    public static final int DEFAULT_WAITTIMENORMALINS=25;
    public static final String DEFAULT_VAL_TRANSACTIONEXTRATLV=ExtraTlvConstant.ENABLE_ALLCARD_INPUT;
    public static final boolean DEFAULT_VAL_TRANSACTIONSIMULATETCPHOST=false;
    public static final boolean DEFAULT_VAL_TRANSACTIONUISHOWMORE=false;

    private Context m_context;

    public int m_idxTransResult;
    public int m_maxBatch;
    public int m_waitTimeNormalInS;

    public String m_strTransactionExtraTlv;

    public boolean m_bTransactionSimulateTCPHost;

    private static final String KEY_IDXTRANSRESULT="idxTransResult";
    private static final String KEY_MAXBATCH="maxBatch";
    private static final String KEY_WAITTIMENORMALINS="waitTimeNormalInS";
    public static final String KEY_TRANSACTIONEXTRATLV="transactionExtraTlv";
    public static final String KEY_TRANSACTIONSIMULATETCPHOST="transactionSimulateTCPHost";


    public int m_waitReponseTimeInMs;

    /**
     * Constructor for Data_setting_s3trans
     * @param context context of application
     */
    public Data_setting_s3trans(Context context) {
        m_context=context;
        refresh();
    }

    /**
     * Refresh paramters
     */
    public void refresh() {
        SharedPreferences preference=m_context.getSharedPreferences(AppSectionsConstant.Storage.PREFERENCE_CONF_SETTING, Context.MODE_PRIVATE);
        m_idxTransResult=preference.getInt(KEY_IDXTRANSRESULT, DEFAULT_IDXTRANSRESULT);
        m_maxBatch=preference.getInt(KEY_MAXBATCH, DEFAULT_MAX_BATCH);
        m_waitTimeNormalInS=preference.getInt(KEY_WAITTIMENORMALINS, DEFAULT_WAITTIMENORMALINS);

        m_strTransactionExtraTlv=preference.getString(KEY_TRANSACTIONEXTRATLV, DEFAULT_VAL_TRANSACTIONEXTRATLV);

        m_bTransactionSimulateTCPHost=preference.getBoolean(KEY_TRANSACTIONSIMULATETCPHOST, DEFAULT_VAL_TRANSACTIONSIMULATETCPHOST);

        // Note:
        //  need to add sp530 present card wait time
        m_waitReponseTimeInMs= SP530Constant.TIME_WAIT_PRESENTCARD_INMS+(m_waitTimeNormalInS*1000);
    }

    /**
     * Set index for transaction result
     * @param val index value
     */
    public void setIdxTransResult(int val) {
        SharedPreferences preference=m_context.getSharedPreferences(AppSectionsConstant.Storage.PREFERENCE_CONF_SETTING, Context.MODE_PRIVATE);
        preference.edit().putInt(KEY_IDXTRANSRESULT, val).commit();
        refresh();
    }

    /**
     * Set maximum number for batch transaction
     * @param val maximum number for batch transacton
     */
    public void setMaxBatch(int val) {
        SharedPreferences preference=m_context.getSharedPreferences(AppSectionsConstant.Storage.PREFERENCE_CONF_SETTING, Context.MODE_PRIVATE);
        preference.edit().putInt(KEY_MAXBATCH, val).commit();
        refresh();
    }

    /**
     * Set waiting time for transaction
     * @param val waiting time for transaction in ms
     */
    public void setWaitTimeNormalInS(int val) {
        SharedPreferences preference=m_context.getSharedPreferences(AppSectionsConstant.Storage.PREFERENCE_CONF_SETTING, Context.MODE_PRIVATE);
        preference.edit().putInt(KEY_WAITTIMENORMALINS, val).commit();
        refresh();
    }

    /**
     * Set the transaction extra tlv
     * @param str extra tlv string
     */
    public void setTransactionExtraTlv(String str) {
        SharedPreferences preference=m_context.getSharedPreferences(AppSectionsConstant.Storage.PREFERENCE_CONF_SETTING, Context.MODE_PRIVATE);
        preference.edit().putString(KEY_TRANSACTIONEXTRATLV, str).commit();
        refresh();
    }

    /**
     * Get transaction extra tlv string
     * @param context context of application
     * @return extra tlv string
     */
    public static String getTransactionExtraTlv(Context context) {
        SharedPreferences preference=context.getSharedPreferences(AppSectionsConstant.Storage.PREFERENCE_CONF_SETTING, Context.MODE_PRIVATE);
        String str=preference.getString(KEY_TRANSACTIONEXTRATLV, DEFAULT_VAL_TRANSACTIONEXTRATLV);
        return str;
    }

    /**
     * Set the transaction result is simulated from Android device
     * @param flag true of simulated; false otherwise
     */
    public void setTransactionSimulateTCPHost(boolean flag) {
        SharedPreferences preference=m_context.getSharedPreferences(AppSectionsConstant.Storage.PREFERENCE_CONF_SETTING, Context.MODE_PRIVATE);
        preference.edit().putBoolean(KEY_TRANSACTIONSIMULATETCPHOST, flag).commit();
        refresh();
    }

    /**
     * Check for transaction result is from the simulated host of Android device
     * @param context context of application
     * @return true if result is from the simulated host; false otherwise
     */
    public static boolean isTransactionSimulateTCPHost(Context context) {
        SharedPreferences preference=context.getSharedPreferences(AppSectionsConstant.Storage.PREFERENCE_CONF_SETTING, Context.MODE_PRIVATE);
        boolean bTransactionSimulateTCPHost=preference.getBoolean(KEY_TRANSACTIONSIMULATETCPHOST, DEFAULT_VAL_TRANSACTIONSIMULATETCPHOST);
        return bTransactionSimulateTCPHost;
    }
}
