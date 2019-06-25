package com.spectratech.sp530demo.data;

import java.util.ArrayList;

/**
 * Data class for tms header
 */
public class Data_tms_header {
    public String m_modelName;
    public ArrayList<Data_tms_header_module> m_moduleList;
    public boolean m_bBinary;

    public Data_tms_header() {
        init();
    }

    private void init() {
        m_modelName="";
        m_moduleList=null;
        m_bBinary=true;
    }

    public void printDebugInfo() {
        if (m_moduleList==null) {
            return;
        }
        for (int i=0; i<m_moduleList.size(); i++) {
            Data_tms_header_module dataTmsHeaderModule=m_moduleList.get(i);
            dataTmsHeaderModule.printDebugInfo();
        }
    }
}
