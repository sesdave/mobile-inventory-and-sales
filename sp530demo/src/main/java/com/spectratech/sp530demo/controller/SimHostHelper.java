package com.spectratech.sp530demo.controller;

import com.spectratech.lib.ByteHexHelper;
import com.spectratech.lib.sp530.constant.SimHostConstant;
import com.spectratech.lib.sp530.data.Data_AP_ZERO;

/**
 * SimHostHelper - sim host helper
 */
public class SimHostHelper {
    private static final String m_className="SimHostHelper";

    private static SimHostHelper m_inst;

    /**
     * Returns SimHostHelper instance
     * @return static SimHostHelper instance
     */
    public static SimHostHelper getInstance() {
        if (m_inst==null) {
            m_inst=new SimHostHelper();
        }
        return m_inst;
    }

    /**
     * Get packet of response of online authentication
     * @param bAccept flag to indicate the request is accept transaction or NOT accept transaction
     * @return packet of response of online authentication
     */
    public byte[] getpacketbuf_responseOnLineRequest(boolean bAccept) {
        Data_AP_ZERO dataAP=new Data_AP_ZERO();

        String strData="";
        if (bAccept) {
            strData= SimHostConstant.RESPONSE_STANDARD_ACCEPT;
        }
        else {
            strData= SimHostConstant.RESPONSE_STANDARD_DECLINE;
        }
        ByteHexHelper instByteHexHelper=ByteHexHelper.getInstance();
        dataAP.m_dataArray=instByteHexHelper.hexStringToByteArray(strData);
        byte[] buf=dataAP.getPacket();
        return buf;
    }
}
