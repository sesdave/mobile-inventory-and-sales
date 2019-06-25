package com.spectratech.sp530demo.data;

import com.spectratech.lib.Logger;

/**
 * Data class for tms header module
 */
public class Data_tms_header_module {

    private static final String m_className="Data_tms_header_module";

    public static final int LEN_CODPAGELOCATION=2;
    public static final int LEN_NAME=10;
    public static final int LEN_VERSION=3;
    public static final int LEN_SUBVERSION=2;
    public static final int LEN_STARTADDRESS=4;
    public static final int LEN_ENDADDRESS=4;
    public static final int LEN_CSUMMODULE=8;
    public static final int LEN_CSUMAPPDISP=8;

    public static final int LEN_FF=2;
    public static final int LEN_CSUMAPP=8;
    public static final int LEN_DATAATTRIBUTE=6;

    public static final int MINIMUM_LINE_LEN= (LEN_CODPAGELOCATION+LEN_NAME+LEN_VERSION+LEN_SUBVERSION+LEN_STARTADDRESS+LEN_ENDADDRESS+LEN_CSUMMODULE+LEN_CSUMAPP+7);

    public String m_moduleLine;

    public String m_codePageLocation;
    public String m_name;
    public String m_version;
    public String m_subVersion;
    public String m_startAddress;
    public String m_endAddress;
    public String m_csumModule;
    public String m_csumAppDis;

    public String m_FF;
    public String m_csumApp;
    public String m_dataAttribute;

    public boolean m_bSuccessLoad;

    public Data_tms_header_module() {
        init();
    }

    public Data_tms_header_module(String strModuleLine) {
        init();
        load(strModuleLine);
    }

    private void init() {
        String strVal="";
        m_moduleLine=strVal;

        m_codePageLocation=strVal;
        m_name=strVal;
        m_version=strVal;
        m_subVersion=strVal;
        m_startAddress=strVal;
        m_endAddress=strVal;
        m_csumModule=strVal;
        m_csumAppDis=strVal;

        m_FF=strVal;
        m_csumApp=strVal;
        m_dataAttribute=strVal;

        m_bSuccessLoad=false;
    }

    public void load(String strModuleLine) {
        m_bSuccessLoad=false;
        if (strModuleLine==null) {
            Logger.w(m_className, "load, strModuleLine is null");
            return;
        }
        if (strModuleLine.length()<MINIMUM_LINE_LEN) {
            Logger.w(m_className, "load, strModuleLine.length()<MINIMUM_LINE_LEN, val: "+MINIMUM_LINE_LEN);
            return;
        }

        m_moduleLine=strModuleLine;

        int count=0;
        String strTmp="";

        // code page location
        strTmp=m_moduleLine.substring(count, (count+LEN_CODPAGELOCATION+1));
        count+=(LEN_CODPAGELOCATION+1);
        m_codePageLocation=strTmp.substring(0, strTmp.length()-1);

        // name
        strTmp=m_moduleLine.substring(count, (count+LEN_NAME+1));
        count+=(LEN_NAME+1);
        m_name=strTmp.substring(0, strTmp.length()-1);

        // version
        strTmp=m_moduleLine.substring(count, (count+LEN_VERSION+1));
        count+=(LEN_VERSION+1);
        m_version=strTmp.substring(0, strTmp.length()-1);

        // sub-version
        strTmp=m_moduleLine.substring(count, (count+LEN_SUBVERSION+1));
        count+=(LEN_SUBVERSION+1);
        m_subVersion=strTmp.substring(0, strTmp.length()-1);

        // start address
        strTmp=m_moduleLine.substring(count, (count+LEN_STARTADDRESS+1));
        count+=(LEN_STARTADDRESS+1);
        m_startAddress=strTmp.substring(0, strTmp.length()-1);

        // end address
        strTmp=m_moduleLine.substring(count, (count+LEN_ENDADDRESS+1));
        count+=(LEN_ENDADDRESS+1);
        m_endAddress=strTmp.substring(0, strTmp.length()-1);

        // module checksum
        strTmp=m_moduleLine.substring(count, (count+LEN_CSUMMODULE+1));
        count+=(LEN_CSUMMODULE+1);
        m_csumModule=strTmp.substring(0, strTmp.length()-1);

        // app dis checksum
        strTmp=m_moduleLine.substring(count, (count+LEN_CSUMAPPDISP));
        count+=(LEN_CSUMAPPDISP);
        m_csumAppDis=strTmp.substring(0, strTmp.length());
        m_csumApp=m_csumAppDis;
        if (count<m_moduleLine.length()) {
            if (m_moduleLine.charAt(count) == ' ') {
                count++;
            }
        }

        // check for ff
        if (count+LEN_FF+1+LEN_CSUMAPP<=m_moduleLine.length()) {
            strTmp=m_moduleLine.substring(count, (count+LEN_FF+1));
            if (strTmp.charAt(LEN_FF)==' ') {
                count+=(LEN_FF+1);
                m_FF=strTmp.substring(0, strTmp.length()-1);

                strTmp=m_moduleLine.substring(count, (count+LEN_CSUMAPP));
                count+=(LEN_CSUMAPP);
                m_csumApp=strTmp.substring(0, strTmp.length());
                if (count<m_moduleLine.length()) {
                    if (m_moduleLine.charAt(count)==' ') {
                        count++;
                    }
                }
            }
        }

        // check for attribute
        if (count+LEN_DATAATTRIBUTE<=m_moduleLine.length()) {
            strTmp=m_moduleLine.substring(count, (count+LEN_DATAATTRIBUTE));
            count+=(LEN_DATAATTRIBUTE);
            m_dataAttribute=strTmp.substring(0, strTmp.length());
            if (count<m_moduleLine.length()) {
                if (m_moduleLine.charAt(count)==' ') {
                    count++;
                }
            }
        }

        m_bSuccessLoad=true;
    }

    public void printDebugInfo() {
        Logger.i(m_className, "m_codePageLocation: "+m_codePageLocation+", "+
                        "m_name: "+m_name+", "+
                        "m_version: "+m_version+", "+
                        "m_subVersion: "+m_subVersion+", "+
                        "m_startAddress: "+m_startAddress+", "+
                        "m_endAddress: "+m_endAddress+", "+
                        "m_csumModule: "+m_csumModule+", "+
                        "m_csumAppDis: "+m_csumAppDis+", "+
                        "m_FF: "+m_FF+", "+
                        "m_csumApp: "+m_csumApp+", "+
                        "m_dataAttribute: "+m_dataAttribute
        );
    }
}
