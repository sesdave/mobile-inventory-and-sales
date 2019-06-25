package com.spectratech.sp530demo.controller;

import android.content.Context;
import android.content.SharedPreferences;
import com.spectratech.lib.ByteHexHelper;

/**
 * SkeyForMutuAuthHelper - Key helper of mutu auth
 */
public class SkeyForMutuAuthHelper {

    private static final String SkeyForMutuAuthHelper="SimHostHelper";

    /**
     * Share preference name
     */
    public static final String PREFERENCE_SKEYFORMUTUAUTH = "SKEYFORMUTUAUTH";
    /**
     * Key of sequence number for application layer
     */
    public static final String KEY_SEQNO_FOR_APPLICATIONLAYOUTER = "seqno_for_applicationlayer";
    /**
     * Key for receive message
     */
    public static final String KEY_RXMSG="key_rxmsg";
    /**
     * Key for key of mutu auth
     */
    public static final String KEY_FOR_SKEY="key_for_skey";

    private static SkeyForMutuAuthHelper m_inst;


    /**
     * Returns SkeyForMutuAuthHelper instance
     * @return static SkeyForMutuAuthHelper instance
     */
    public static SkeyForMutuAuthHelper getInstance() {
        if (m_inst==null) {
            m_inst=new SkeyForMutuAuthHelper();
        }
        return m_inst;
    }

    private SkeyForMutuAuthHelper() {

    }

    /**
     * Get sequence number for application layer
     * @param context context of application
     * @return sequence number for application layer
     */
    public int getSeqnoForApplicationLayer(Context context) {
        SharedPreferences preference=context.getSharedPreferences(PREFERENCE_SKEYFORMUTUAUTH, Context.MODE_PRIVATE);
        int idx=preference.getInt(KEY_SEQNO_FOR_APPLICATIONLAYOUTER, 1);
        return idx;
    }

    /**
     * Set sequence number for application layer
     * @param context context of application
     * @param idx sequencye number
     * @return true if sucess; false otherwise
     */
    public boolean setSeqnoForApplicationLayer(Context context, int idx) {
        SharedPreferences preference=context.getSharedPreferences(PREFERENCE_SKEYFORMUTUAUTH, Context.MODE_PRIVATE);
        return preference.edit().putInt(KEY_SEQNO_FOR_APPLICATIONLAYOUTER, idx).commit();
    }


    /**
     * Get receive message from share preference
     * @param context context of application
     * @return receive message in byte array format
     */
    public byte[] getRxmsg(Context context) {
        SharedPreferences preference=context.getSharedPreferences(PREFERENCE_SKEYFORMUTUAUTH, Context.MODE_PRIVATE);
        String strHex=preference.getString(KEY_RXMSG, "");
        if (strHex.equals("")) {
            return null;
        }
        ByteHexHelper instByteHexHelper=ByteHexHelper.getInstance();
        byte[] skey=instByteHexHelper.hexStringToByteArray(strHex);
        return skey;
    }

    /**
     * Set receive message to share preference
     * @param context context of application
     * @param rxmsg receive message
     * @return true if set; false otherwise
     */
    public boolean setRxmsg(Context context, byte[] rxmsg) {
        ByteHexHelper instByteHexHelper=ByteHexHelper.getInstance();
        String strHex=instByteHexHelper.bytesArrayToHexString(rxmsg);
        SharedPreferences preference=context.getSharedPreferences(PREFERENCE_SKEYFORMUTUAUTH, Context.MODE_PRIVATE);
        return preference.edit().putString(KEY_RXMSG, strHex).commit();
    }

    /**
     * Get key for mutu auth
     * @param context context of application
     * @return key in byte array format
     */
    public byte[] getSkey(Context context) {
        SharedPreferences preference=context.getSharedPreferences(PREFERENCE_SKEYFORMUTUAUTH, Context.MODE_PRIVATE);
        String strHex=preference.getString(KEY_FOR_SKEY, "");
        if (strHex.equals("")) {
            return null;
        }
        ByteHexHelper instByteHexHelper=ByteHexHelper.getInstance();
        byte[] skey=instByteHexHelper.hexStringToByteArray(strHex);
        return skey;
    }

    /**
     * Set key for mutu auth to share preference
     * @param context context of application
     * @param skey key for mutu auth
     * @return ture if success; false otherwise
     */
    public boolean setSKey(Context context, byte[] skey) {
        ByteHexHelper instByteHexHelper=ByteHexHelper.getInstance();
        String strHex=instByteHexHelper.bytesArrayToHexString(skey);
        SharedPreferences preference=context.getSharedPreferences(PREFERENCE_SKEYFORMUTUAUTH, Context.MODE_PRIVATE);
        return preference.edit().putString(KEY_FOR_SKEY, strHex).commit();
    }
}
