package com.spectratech.sp530demo.data;

import com.spectratech.lib.Logger;
import com.spectratech.sp530demo.TmsHelper;
import com.spectratech.spectratechlib_ftp.data.Data_ftpdlObject;

/**
 * Tms transfers to device object
 */
public class Data_tmsTrans2DeviceObject {
    private static final String m_className="Data_tmsTrans2DeviceObject";

    public boolean m_bFileNotExist;
    public boolean m_bSkip;
    public boolean m_bTransferSuccess;

    public Data_ftpdlObject m_dataFtpdlObject;
    public Data_tms_header m_dataTmsHeader;

    public Data_tmsTrans2DeviceObject() {
        init();
    }

    public Data_tmsTrans2DeviceObject(String relative_pathlist) {
        init();
    }

    public Data_tmsTrans2DeviceObject(Data_ftpdlObject dataFtpdlObject) {
        m_dataFtpdlObject=new Data_ftpdlObject(dataFtpdlObject);
    }

    private void init() {
        m_bFileNotExist=false;
        m_bSkip=false;
        m_bTransferSuccess=false;
        m_dataFtpdlObject=null;
        m_dataTmsHeader=null;
    }

    public String getHashKey() {
        if (m_dataTmsHeader==null) {
            Logger.w(m_className, "getHashKey, m_dataTmsHeader is null");
            return null;
        }
        if (m_dataTmsHeader.m_moduleList==null) {
            Logger.w(m_className, "getHashKey, m_dataTmsHeader.m_moduleList is null");
            return null;
        }
        if (m_dataTmsHeader.m_moduleList.size()==0) {
            Logger.w(m_className, "getHashKey, m_dataTmsHeader.m_moduleList.size()==0");
            return null;
        }

        Data_tms_header_module x=m_dataTmsHeader.m_moduleList.get(m_dataTmsHeader.m_moduleList.size()-1);
        TmsHelper instTmsHelper=TmsHelper.getInstance();
        return instTmsHelper.getHashKey(x);
    }
}
