package com.spectratech.sp530demo.data;

import com.spectratech.lib.Logger;

/**
 * Data class for tms module obtained from SP530
 */
public class Data_SP530_tms_module {
    private static final String m_className="Data_SP530_tms_module";

    public static final int LEN_NAME=12;
    public static final int LEN_VERSION=3;
    public static final int LEN_SUBVERSION=2;
    public static final int LEN_CSUMAPPDISP=8;
    public static final int LEN_CSUMAPP=8;

    public static final int MINIMUM_LINE_LEN= (LEN_NAME+LEN_VERSION+LEN_SUBVERSION+LEN_CSUMAPP+LEN_CSUMAPPDISP+4);

    public String m_moduleLine;

    public String m_name_prefix;
    public String m_name;
    public String m_version;
    public String m_subVersion;
    public String m_csumAppDis;
    public String m_csumApp;

    public boolean m_bSuccessLoad;

    public Data_SP530_tms_module() {
        init();
    }

    public Data_SP530_tms_module(String strModuleLine) {
        init();
        load(strModuleLine);
    }

    private void init() {
        String strVal="";
        m_moduleLine=strVal;

        m_name_prefix=strVal;
        m_name=strVal;
        m_version=strVal;
        m_subVersion=strVal;
        m_csumAppDis=strVal;
        m_csumApp=strVal;

        m_bSuccessLoad=false;
    }

    public void load(String strModuleLine) {
        m_bSuccessLoad=false;
        if (strModuleLine==null) {
            Logger.w(m_className, "load, strModuleLine is null");
            return;
        }
        if (strModuleLine.length()<MINIMUM_LINE_LEN) {
            Logger.w(m_className, "load, strModuleLine.length()<MINIMUM_LINE_LEN, val: "+strModuleLine.length()+"<"+MINIMUM_LINE_LEN);
            return;
        }

        m_moduleLine=strModuleLine;

        int count=0;
        String strTmp="";

        // name
        strTmp=m_moduleLine.substring(count, (count+LEN_NAME+1));
        count+=(LEN_NAME+1);
        m_name=strTmp.substring(0, strTmp.length()-1);
        String strTrim=m_name.trim();

        // note
        // 20160229
        // no first two digits
//        if (strTrim.equals("SYSTEM")) {
//            m_name_prefix="";
//            m_name=m_name.substring(0, 10);
//        }
//        else {
//            m_name_prefix=m_name.substring(0, 2);
//            m_name=m_name.substring(2);
//        }
        m_name_prefix="";
        m_name=m_name.substring(0, 10);

        // version
        strTmp=m_moduleLine.substring(count, (count+LEN_VERSION+1));
        count+=(LEN_VERSION+1);
        m_version=strTmp.substring(0, strTmp.length()-1);

        // sub-version
        strTmp=m_moduleLine.substring(count, (count+LEN_SUBVERSION+1));
        count+=(LEN_SUBVERSION+1);
        m_subVersion=strTmp.substring(0, strTmp.length()-1);

        // app checksum
        strTmp=m_moduleLine.substring(count, (count+LEN_CSUMAPP+1));
        count+=(LEN_CSUMAPP+1);
        m_csumApp=strTmp.substring(0, strTmp.length()-1);

        // app dis checksum
        strTmp=m_moduleLine.substring(count, (count+LEN_CSUMAPPDISP));
        count+=(LEN_CSUMAPPDISP);
        m_csumAppDis=strTmp.substring(0, strTmp.length());
        if (count<m_moduleLine.length()) {
            if (m_moduleLine.charAt(count) == ' ') {
                count++;
            }
        }

        m_bSuccessLoad=true;
    }

    public void printDebugInfo() {
        Logger.i(m_className, "m_name_prefix: "+m_name_prefix+", "+
                        "m_name: "+m_name+", "+
                        "m_version: "+m_version+", "+
                        "m_subVersion: "+m_subVersion+", "+
                        "m_csumAppDis: "+m_csumAppDis+", "+
                        "m_csumApp: "+m_csumApp
        );
    }
}
