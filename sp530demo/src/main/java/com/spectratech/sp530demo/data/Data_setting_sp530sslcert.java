package com.spectratech.sp530demo.data;

import android.content.Context;
import android.content.SharedPreferences;
import com.spectratech.lib.Logger;
import com.spectratech.sp530demo.constant.AppSectionsConstant;

/**
 * Data_setting_sp530sslcert - data class for storing setting of SP530 SSL certificate parameters
 */
public class Data_setting_sp530sslcert {
    private static final String m_className="Data_setting_sp530sslcert";

    public static final String DEFAULT_FILENAME_STRING="";
    public static final String DEFAULT_P12PASSWORD_STRING="1234";
    public static final boolean DEFAULT_NEEDCLIENTAUTH_VALUE=false;
    public static final String DEFAULT_CAFILENAME_STRING="";

    private static final String KEY_FILENAME_STRING="FILENAME_STRING";
    private static final String KEY_P12PASSWORD="P12PASSWORD";
    private static final String KEY_NEEDCLIENTAUTH_STRING="NEEDCLIENTAUTH_STRING";
    private static final String KEY_CAFILENAME_STRING="CAFILENAME_STRING";

    public String m_strFilename;
    public String m_strP12Password;
    public boolean m_bNeedClientAuth;
    public String m_strCAFilename;

    public Data_setting_sp530sslcert(Context context) {
        refresh(context);
    }

    /**
     * Refresh parameters
     */
    public void refresh(Context context) {
        if (context==null) {
            Logger.w(m_className, "refresh, context is NULL");
        }
        SharedPreferences preference=context.getSharedPreferences(AppSectionsConstant.Storage.PREFERENCE_SP530SSLCERT_CONF_SETTING, Context.MODE_PRIVATE);
        m_strFilename=preference.getString(KEY_FILENAME_STRING, DEFAULT_FILENAME_STRING);
        m_strP12Password=preference.getString(KEY_P12PASSWORD, DEFAULT_P12PASSWORD_STRING);
        m_bNeedClientAuth=preference.getBoolean(KEY_NEEDCLIENTAUTH_STRING, DEFAULT_NEEDCLIENTAUTH_VALUE);
        m_strCAFilename=preference.getString(KEY_CAFILENAME_STRING, DEFAULT_CAFILENAME_STRING);
    }

    public boolean setFilenameString(Context context, String strFilename) {
        if (context==null) {
            Logger.w(m_className, "setFilenameString, context is NULL");
            return false;
        }
        if (strFilename==null) {
            return resetFilenameString(context);
        }
        SharedPreferences preference=context.getSharedPreferences(AppSectionsConstant.Storage.PREFERENCE_SP530SSLCERT_CONF_SETTING, Context.MODE_PRIVATE);
        boolean bPut=preference.edit().putString(KEY_FILENAME_STRING, strFilename).commit();
        if (bPut) {
            m_strFilename=strFilename;
        }
        return bPut;
    }
    public boolean resetFilenameString(Context context) {
        if (context==null) {
            Logger.w(m_className, "resetFilenameString, context is NULL");
            return false;
        }
        SharedPreferences preference=context.getSharedPreferences(AppSectionsConstant.Storage.PREFERENCE_SP530SSLCERT_CONF_SETTING, Context.MODE_PRIVATE);
        boolean bRemove=preference.edit().remove(KEY_FILENAME_STRING).commit();
        if (bRemove) {
            m_strFilename=DEFAULT_FILENAME_STRING;
        }
        return bRemove;
    }

    public boolean setP12Password(Context context, String strP12Password) {
        if (context==null) {
            Logger.w(m_className, "setP12Password, context is NULL");
            return false;
        }
        if (strP12Password==null) {
            return false;
        }
        SharedPreferences preference=context.getSharedPreferences(AppSectionsConstant.Storage.PREFERENCE_SP530SSLCERT_CONF_SETTING, Context.MODE_PRIVATE);
        boolean bPut=preference.edit().putString(KEY_P12PASSWORD, strP12Password).commit();
        if (bPut) {
            m_strP12Password=strP12Password;
        }
        return bPut;
    }
    public boolean resetP12Password(Context context) {
        if (context==null) {
            Logger.w(m_className, "resetP12Password, context is NULL");
            return false;
        }
        SharedPreferences preference=context.getSharedPreferences(AppSectionsConstant.Storage.PREFERENCE_SP530SSLCERT_CONF_SETTING, Context.MODE_PRIVATE);
        boolean bRemove=preference.edit().remove(KEY_P12PASSWORD).commit();
        if (bRemove) {
            m_strP12Password=DEFAULT_P12PASSWORD_STRING;
        }
        return bRemove;
    }

    public boolean setCAFilenameString(Context context, String strFilename) {
        if (context==null) {
            Logger.w(m_className, "setCAFilenameString, context is NULL");
            return false;
        }
        if (strFilename==null) {
            return resetCAFilenameString(context);
        }
        SharedPreferences preference=context.getSharedPreferences(AppSectionsConstant.Storage.PREFERENCE_SP530SSLCERT_CONF_SETTING, Context.MODE_PRIVATE);
        boolean bPut=preference.edit().putString(KEY_CAFILENAME_STRING, strFilename).commit();
        if (bPut) {
            m_strCAFilename=strFilename;
        }
        return bPut;
    }
    public boolean resetCAFilenameString(Context context) {
        if (context==null) {
            Logger.w(m_className, "resetCAFilenameString, context is NULL");
            return false;
        }
        SharedPreferences preference=context.getSharedPreferences(AppSectionsConstant.Storage.PREFERENCE_SP530SSLCERT_CONF_SETTING, Context.MODE_PRIVATE);
        boolean bRemove=preference.edit().remove(KEY_CAFILENAME_STRING).commit();
        if (bRemove) {
            m_strCAFilename=DEFAULT_FILENAME_STRING;
        }
        return bRemove;
    }

    public boolean setNeedClientAuth(Context context, boolean val) {
        if (context==null) {
            Logger.w(m_className, "setNeedClientAuth, context is NULL");
            return false;
        }
        SharedPreferences preference=context.getSharedPreferences(AppSectionsConstant.Storage.PREFERENCE_SP530SSLCERT_CONF_SETTING, Context.MODE_PRIVATE);
        boolean bPut=preference.edit().putBoolean(KEY_NEEDCLIENTAUTH_STRING, val).commit();
        if (bPut) {
            m_bNeedClientAuth=val;
        }
        return bPut;
    }

}
