package com.spectratech.sp530demo.data;

import com.spectratech.sp530demo.constant.FullEmvEnum.TRANSACTIONSTATUS;

/**
 * Data_S3INS_response - response data for S3INS
 */
public class Data_S3INS_response {
    private static final String m_clssName="Data_S3INS_response";

    public TRANSACTIONSTATUS m_statusTrans;
    public byte[] m_data;

    public Data_S3INS_response() {
        init();
    }

    public Data_S3INS_response(TRANSACTIONSTATUS status, byte[] responseData) {
        init();
        m_statusTrans=status;
        if ( (responseData!=null)&&(responseData.length>0) ) {
            m_data=new byte[responseData.length];
            System.arraycopy(responseData, 0, m_data, 0, m_data.length);
        }
    }

    private void init() {
        m_statusTrans=TRANSACTIONSTATUS.STATUS_UNKNOWN;
        m_data=null;
    }
}
