package com.spectratech.sp530demo.data;

import com.spectratech.lib.ByteHexHelper;
import com.spectratech.sp530demo.constant.S3_AuthConstant;

/**
 * Data_runtime_s3Auth - data class for storing run time mutu auth data
 */
public class Data_runtime_s3Auth {
    public boolean m_bSelectMRMK;
    public byte KType;
    public byte KIdx;
    public byte[] Key;
    public byte[] TRnd;
    public byte[] RRnd;
    public byte ModeMutuAuth;

    /**
     * Constructor for Data_runtime_s3Auth
     */
    public Data_runtime_s3Auth() {
        init();
        loadDefaultValues();
    }

    private void loadDefaultValues() {
        String strVal="";
        byte[] tmpBuf=null;

        ByteHexHelper instByteHexHelper=ByteHexHelper.getInstance();

        m_bSelectMRMK=S3_AuthConstant.FLAG_SELECTMRMK;

        KType=(byte)0x00;

        if (m_bSelectMRMK) {
            strVal = S3_AuthConstant.IDX_MRMK;
        }
        else {
            strVal = S3_AuthConstant.IDX_RMK;
        }
        tmpBuf=instByteHexHelper.hexStringToByteArray(strVal);
        KIdx=tmpBuf[0];

        if (m_bSelectMRMK) {
            strVal = S3_AuthConstant.MRMK;
        }
        else {
            strVal = S3_AuthConstant.RMK;
        }
        Key=instByteHexHelper.hexStringToByteArray(strVal);

        strVal=S3_AuthConstant.TRAND;
        TRnd=instByteHexHelper.hexStringToByteArray(strVal);

        strVal=S3_AuthConstant.RRAND;
        RRnd=instByteHexHelper.hexStringToByteArray(strVal);

        strVal=S3_AuthConstant.MODE_MUTU_AUTH;
        tmpBuf=instByteHexHelper.hexStringToByteArray(strVal);
        ModeMutuAuth=tmpBuf[0];
    }

    private void init() {
        m_bSelectMRMK=true;
        KType=(byte)0x00;
        KIdx=(byte)0x00;
        Key=null;
        TRnd=null;
        RRnd=null;
        ModeMutuAuth=(byte)0x00;
    }
}
